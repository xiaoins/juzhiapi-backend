package com.aiplatform.service.impl;

import com.aiplatform.common.ErrorCode;
import com.aiplatform.dto.ChatSendRequest;
import com.aiplatform.entity.AiModel;
import com.aiplatform.entity.ChatMessage;
import com.aiplatform.entity.ChatSession;
import com.aiplatform.exception.BizException;
import com.aiplatform.mapper.ChatMessageMapper;
import com.aiplatform.mapper.ChatSessionMapper;
import com.aiplatform.service.AiGatewayService;
import com.aiplatform.service.BillingService;
import com.aiplatform.service.ChatService;
import com.aiplatform.service.ModelService;
import com.aiplatform.vo.MessageVO;
import com.aiplatform.vo.SessionVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final ModelService modelService;
    private final AiGatewayService aiGatewayService;
    private final BillingService billingService;

    @Override
    public SessionVO createSession(Long userId, String modelName, String title) {
        if (modelName != null && !modelName.isBlank()) {
            AiModel model = modelService.getModelByName(modelName);
            if (model == null) throw new BizException(ErrorCode.MODEL_NOT_FOUND);
        }

        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setModelName(modelName);
        session.setTitle(title != null ? title : "新会话");
        sessionMapper.insert(session);

        return convertToVO(session);
    }

    @Override
    public Page<SessionVO> getSessionList(Long userId, int current, int size) {
        Page<ChatSession> page = new Page<>(current, size);
        Page<ChatSession> sessionPage = sessionMapper.selectPage(page,
                new LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getUserId, userId)
                        .orderByDesc(ChatSession::getUpdatedAt));

        Page<SessionVO> result = new Page<>();
        result.setRecords(sessionPage.getRecords().stream().map(this::convertToVO).toList());
        result.setTotal(sessionPage.getTotal());
        result.setCurrent(sessionPage.getCurrent());
        result.setSize(sessionPage.getSize());
        return result;
    }

    @Override
    public SessionVO getSessionDetail(Long userId, Long sessionId) {
        ChatSession session = getAndCheckOwner(userId, sessionId);
        return convertToVO(session);
    }

    @Override
    public Page<MessageVO> getMessages(Long userId, Long sessionId, int current, int size) {
        getAndCheckOwner(userId, sessionId);

        Page<ChatMessage> page = new Page<>(current, size);
        Page<ChatMessage> msgPage = messageMapper.selectPage(page,
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .orderByAsc(ChatMessage::getCreatedAt));

        Page<MessageVO> result = new Page<>();
        result.setRecords(msgPage.getRecords().stream().map(this::convertMsgToVO).toList());
        result.setTotal(msgPage.getTotal());
        result.setCurrent(msgPage.getCurrent());
        result.setSize(msgPage.getSize());
        return result;
    }

    @Override
    public SseEmitter sendMessage(Long userId, ChatSendRequest request) {
        Long sessionId = request.getSessionId();
        ChatSession session;
        if (sessionId == null || sessionId == 0) {
            session = new ChatSession();
            session.setUserId(userId);
            session.setModelName(request.getModelName());
            session.setTitle(request.getMessage().length() > 20 ? request.getMessage().substring(0, 20) + "..." : request.getMessage());
            sessionMapper.insert(session);
            sessionId = session.getId();
        } else {
            session = getAndCheckOwner(userId, sessionId);
        }

        AiModel model = modelService.getModelByName(request.getModelName());
        if (model == null) throw new BizException(ErrorCode.MODEL_NOT_FOUND);

        ChatMessage userMessage = new ChatMessage();
        userMessage.setSessionId(sessionId);
        userMessage.setUserId(userId);
        userMessage.setRole("user");
        userMessage.setContent(request.getMessage());
        messageMapper.insert(userMessage);

        List<Map<String, String>> messages = buildMessages(sessionId, request.getMessage());

        SseEmitter emitter = new SseEmitter(120000L);
        processStreamResponse(emitter, userId, sessionId, model, messages);

        return emitter;
    }

    @Override
    public void deleteSession(Long userId, Long sessionId) {
        ChatSession session = getAndCheckOwner(userId, sessionId);

        messageMapper.delete(
                new LambdaQueryWrapper<ChatMessage>().eq(ChatMessage::getSessionId, sessionId));

        sessionMapper.deleteById(sessionId);
    }

    private List<Map<String, String>> buildMessages(Long sessionId, String currentMessage) {
        List<Map<String, String>> messages = new ArrayList<>();

        List<ChatMessage> historyMessages = messageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .orderByAsc(ChatMessage::getCreatedAt)
                        .last("LIMIT 50"));

        for (ChatMessage msg : historyMessages) {
            Map<String, String> msgMap = new HashMap<>();
            msgMap.put("role", msg.getRole());
            msgMap.put("content", msg.getContent());
            messages.add(msgMap);
        }

        Map<String, String> current = new HashMap<>();
        current.put("role", "user");
        current.put("content", currentMessage);
        messages.add(current);

        return messages;
    }

    private void processStreamResponse(SseEmitter emitter, Long userId, Long sessionId,
                                       AiModel model, List<Map<String, String>> messages) {
        try {
            emitter.send(SseEmitter.event()
                    .name("start")
                    .data("{\"type\":\"start\"}"));

            List<String> streamEvents = aiGatewayService.forwardChatStream(
                    model.getModelName(), messages, true);

            StringBuilder fullContent = new StringBuilder();

            for (String event : streamEvents) {
                emitter.send(SseEmitter.event()
                        .name("content")
                        .data(event));
                fullContent.append(event);
            }

            ChatMessage assistantMessage = new ChatMessage();
            assistantMessage.setSessionId(sessionId);
            assistantMessage.setUserId(userId);
            assistantMessage.setRole("assistant");
            assistantMessage.setContent(fullContent.toString());
            assistantMessage.setModelName(model.getModelName());
            messageMapper.insert(assistantMessage);

            int inputTokens = estimateTokens(messages.toString(), true);
            int outputTokens = estimateTokens(fullContent.toString(), false);
            long cost = billingService.recordAndDeduct(
                    userId, sessionId, model, inputTokens, outputTokens, "SUCCESS", null);

            String endData = String.format("{\"type\":\"end\",\"totalTokens\":%d,\"cost\":%d}",
                    inputTokens + outputTokens, cost);
            emitter.send(SseEmitter.event()
                    .name("end")
                    .data(endData));

            emitter.complete();
        } catch (BizException e) {
            log.error("聊天处理业务异常: {}", e.getMessage());
            sendError(emitter, e.getCode(), e.getMsg());
        } catch (Exception e) {
            log.error("聊天处理异常", e);
            billingService.recordAndDeduct(userId, sessionId, model, 0, 0, "FAILED", e.getMessage());
            sendError(emitter, ErrorCode.AI_GATEWAY_ERROR.getCode(), "AI 服务调用失败: " + e.getMessage());
        }
    }

    private int estimateTokens(String text, boolean isInput) {
        if (text == null || text.isEmpty()) return 0;
        int chineseCount = countChinese(text);
        int otherCount = text.length() - chineseCount;
        return (int) Math.ceil(chineseCount / 1.5 + otherCount / 4.0);
    }

    private int countChinese(String text) {
        int count = 0;
        for (char c : text.toCharArray()) {
            if (c >= '\u4e00' && c <= '\u9fff') count++;
        }
        return count;
    }

    private void sendError(SseEmitter emitter, int code, String msg) {
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data("{\"code\":" + code + ",\"message\":\"" + escapeJson(msg) + "\"}"));
            emitter.completeWithError(new RuntimeException(msg));
        } catch (IOException ignored) {}
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private ChatSession getAndCheckOwner(Long userId, Long sessionId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) throw new BizException(ErrorCode.SESSION_NOT_FOUND);
        if (!session.getUserId().equals(userId)) throw new BizException(ErrorCode.SESSION_NOT_BELONG_TO_USER);
        return session;
    }

    private SessionVO convertToVO(ChatSession session) {
        SessionVO vo = new SessionVO();
        vo.setId(session.getId());
        vo.setTitle(session.getTitle());
        vo.setModelName(session.getModelName());
        vo.setCreatedAt(session.getCreatedAt());
        vo.setUpdatedAt(session.getUpdatedAt());
        return vo;
    }

    private MessageVO convertMsgToVO(ChatMessage msg) {
        MessageVO vo = new MessageVO();
        vo.setId(msg.getId());
        vo.setSessionId(msg.getSessionId());
        vo.setRole(msg.getRole());
        vo.setContent(msg.getContent());
        vo.setModelName(msg.getModelName());
        vo.setTokenCount(msg.getTokenCount());
        vo.setCreatedAt(msg.getCreatedAt());
        return vo;
    }
}
