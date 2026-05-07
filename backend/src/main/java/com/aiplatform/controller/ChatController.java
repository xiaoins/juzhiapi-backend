package com.aiplatform.controller;

import com.aiplatform.common.R;
import com.aiplatform.dto.ChatSendRequest;
import com.aiplatform.dto.CreateSessionRequest;
import com.aiplatform.service.ChatService;
import com.aiplatform.utils.SecurityUtils;
import com.aiplatform.vo.MessageVO;
import com.aiplatform.vo.SessionVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 聊天控制器
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 创建会话
     */
    @PostMapping("/session")
    public R<SessionVO> createSession(@RequestBody(required = false) CreateSessionRequest request) {
        Long userId = SecurityUtils.getRequiredUserId();
        String modelName = request != null ? request.getModelName() : null;
        String title = request != null ? request.getTitle() : null;
        SessionVO session = chatService.createSession(userId, modelName, title);
        return R.ok(session);
    }

    /**
     * 获取会话列表
     */
    @GetMapping("/sessions")
    public R<com.baomidou.mybatisplus.extension.plugins.pagination.Page<SessionVO>> getSessionList(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtils.getRequiredUserId();
        return R.ok(chatService.getSessionList(userId, current, size));
    }

    /**
     * 发送消息 (SSE 流式)
     */
    @PostMapping(value = "/send", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendMessage(@Valid @RequestBody ChatSendRequest request) {
        Long userId = SecurityUtils.getRequiredUserId();
        return chatService.sendMessage(userId, request);
    }

    /**
     * 获取会话消息
     */
    @GetMapping("/messages/{sessionId}")
    public R<com.baomidou.mybatisplus.extension.plugins.pagination.Page<MessageVO>> getMessages(
            @PathVariable("sessionId") Long sessionId,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "50") int size) {
        Long userId = SecurityUtils.getRequiredUserId();
        return R.ok(chatService.getMessages(userId, sessionId, current, size));
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/session/{sessionId}")
    public R<Void> deleteSession(@PathVariable("sessionId") Long sessionId) {
        Long userId = SecurityUtils.getRequiredUserId();
        chatService.deleteSession(userId, sessionId);
        return R.ok();
    }
}
