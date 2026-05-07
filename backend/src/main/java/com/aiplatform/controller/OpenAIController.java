package com.aiplatform.controller;

import com.aiplatform.common.ErrorCode;
import com.aiplatform.common.R;
import com.aiplatform.entity.AiModel;
import com.aiplatform.exception.BizException;
import com.aiplatform.service.AiGatewayService;
import com.aiplatform.service.BillingService;
import com.aiplatform.service.ModelService;
import com.aiplatform.vo.ModelVO;
import com.aiplatform.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * OpenAI 兼容 API 控制器
 * 提供 /v1/chat/completions 和 /v1/models 接口
 * 供外部工具 (Cherry Studio, Continue, Claude Code 等) 调用
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class OpenAIController {

    private final ModelService modelService;
    private final AiGatewayService aiGatewayService;
    private final BillingService billingService;

    /**
     * OpenAI 兼容 - 获取模型列表
     */
    @GetMapping("/v1/models")
    public R<Object> listModels() {
        List<ModelVO> models = modelService.getEnabledModels();

        // 构建OpenAI格式响应
        List<Map<String, Object>> data = new java.util.ArrayList<>();
        for (ModelVO m : models) {
            Map<String, Object> item = new java.util.HashMap<>();
            item.put("id", m.getModelName());
            item.put("object", "model");
            item.put("created", System.currentTimeMillis() / 1000);
            item.put("owned_by", m.getProvider().toLowerCase());
            data.add(item);
        }

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("object", "list");
        response.put("data", data);

        return R.ok(response);
    }

    /**
     * OpenAI 兼容 - 聊天补全 (支持流式)
     */
    @PostMapping(value = "/v1/chat/completions",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_EVENT_STREAM_VALUE})
    public SseEmitter chatCompletions(@RequestBody Map<String, Object> body) {
        // 解析请求参数
        String modelName = (String) body.get("model");
        Boolean isStream = (Boolean) body.getOrDefault("stream", false);
        List<Map<String, String>> messages = parseMessages(body);

        if (modelName == null || modelName.isBlank()) {
            throw new BizException(ErrorCode.PARAM_ERROR.getCode(), "模型名称不能为空");
        }

        // 获取当前用户(通过API Key认证)
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }

        // 检查模型是否可用
        AiModel model = modelService.getModelByName(modelName);
        if (model == null) throw new BizException(ErrorCode.MODEL_NOT_FOUND);

        // 创建 SSE Emitter
        SseEmitter emitter = new SseEmitter(120000L);

        // 异步处理
        processOpenAiRequest(emitter, userId, null, model, messages, isStream);

        return emitter;
    }

    /**
     * 处理 OpenAI 格式的请求
     */
    private void processOpenAiRequest(SseEmitter emitter, Long userId, Long apiKeyId,
                                       AiModel model, List<Map<String, String>> messages,
                                       boolean isStream) {
        try {
            List<String> streamEvents = aiGatewayService.forwardChatStream(
                    model.getModelName(), messages, true);

            StringBuilder fullContent = new StringBuilder();

            for (String event : streamEvents) {
                if (event == null || event.isEmpty()) continue;

                fullContent.append(event);

                // 构建 OpenAI SSE 格式
                String sseData = buildOpenAiSseChunk(model.getModelName(), event, false);
                emitter.send(org.springframework.http.codec.ServerSentEvent.builder(sseData).build());
            }

            // 发送结束 chunk
            int inputTokens = estimateTokens(messages.toString(), true);
            int outputTokens = estimateTokens(fullContent.toString(), false);
            long cost = billingService.recordAndDeductWithKey(
                    userId, apiKeyId, model, inputTokens, outputTokens,
                    getClientIp(), "SUCCESS", null);

            // 发送最终结束标记
            String endChunk = buildOpenAiSseFinalChunk(model.getModelName(),
                    inputTokens + outputTokens, inputTokens, outputTokens);
            emitter.send(org.springframework.http.codec.ServerSentEvent.builder(endChunk).build());

            emitter.send(org.springframework.http.codec.ServerSentEvent.builder("[DONE]").build());
            emitter.complete();

        } catch (BizException e) {
            log.error("OpenAI API 业务异常: {}", e.getMessage());
            sendError(emitter, e);
        } catch (Exception e) {
            log.error("OpenAI API 处理异常", e);
            billingService.recordAndDeductWithKey(userId, apiKeyId, model, 0, 0,
                    getClientIp(), "FAILED", e.getMessage());
            sendError(emitter, new BizException(ErrorCode.AI_GATEWAY_ERROR));
        }
    }

    /**
     * 构建 OpenAI SSE 格式的 chunk
     */
    private String buildOpenAiSseChunk(String modelName, String content, boolean isLast) {
        return String.format("""
                {"id":"chatcmpl-%s","object":"chat.completion.chunk","created":%d,"model":"%s","choices":[{"index":0,"delta":{"content":"%s"},"finish_reason":%s}]}""",
                generateId(), System.currentTimeMillis() / 1000, modelName,
                escapeJson(content), isLast ? "\"stop\"" : "null");
    }

    /**
     * 构建最终的 usage chunk
     */
    private String buildOpenAiSseFinalChunk(String modelName, int totalTokens, int promptTokens, int completionTokens) {
        return String.format("""
                {"id":"chatcmpl-%s","object":"chat.completion.chunk","created":%d,"model":"%s","choices":[{"index":0,"delta":{},"finish_reason":"stop"}],"usage":{"prompt_tokens":%d,"completion_tokens":%d,"total_tokens":%d}}""",
                generateId(), System.currentTimeMillis() / 1000, modelName,
                promptTokens, completionTokens, totalTokens);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> parseMessages(Map<String, Object> body) {
        Object msgsObj = body.get("messages");
        if (!(msgsObj instanceof List)) {
            throw new BizException(ErrorCode.PARAM_ERROR.getCode(), "消息格式不正确");
        }
        List<?> rawMessages = (List<?>) msgsObj;
        return rawMessages.stream()
                .filter(m -> m instanceof Map)
                .map(m -> (Map<String, Object>) m)
                .map(m -> {
                    Map<String, String> msg = new java.util.HashMap<>();
                    msg.put("role", (String) m.getOrDefault("role", "user"));
                    msg.put("content", (String) m.getOrDefault("content", ""));
                    return msg;
                })
                .toList();
    }

    private void sendError(SseEmitter emitter, BizException e) {
        try {
            emitter.send(org.springframework.http.codec.ServerSentEvent.builder(
                    String.format("{\"error\":{\"code\":%d,\"message\":\"%s\"}}",
                            e.getCode(), escapeJson(e.getMsg()))).build());
            emitter.completeWithError(e);
        } catch (Exception ignored) {}
    }

    private String generateId() {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 29);
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

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String getClientIp() {
        return ""; // TODO: 从请求上下文获取IP
    }
}
