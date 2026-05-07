package com.aiplatform.service.impl;

import com.aiplatform.exception.BizException;
import com.aiplatform.common.ErrorCode;
import com.aiplatform.service.AiGatewayService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AI 网关服务实现 - 负责转发请求到上游 AI 网关 (New API / One API)
 */
@Slf4j
@Service
public class AiGatewayServiceImpl implements AiGatewayService {

    @Value("${ai-gateway.url}")
    private String gatewayUrl;

    @Value("${ai-gateway.api-key}")
    private String gatewayApiKey;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)  // 流式读取长超时
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    /**
     * 转发聊天请求到 AI 网关 (流式)
     */
    @Override
    public List<String> forwardChatStream(String modelName, List<Map<String, String>> messages, boolean stream) {
        List<String> results = new ArrayList<>();

        try {
            // 构建请求体 (OpenAI 格式)
            String jsonBody = buildRequestBody(modelName, messages, true);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"), jsonBody);

            Request request = new Request.Builder()
                    .url(gatewayUrl + "/v1/chat/completions")
                    .post(body)
                    .header("Authorization", "Bearer " + gatewayApiKey)
                    .header("Content-Type", "application/json")
                    .build();

            // 执行流式请求
            Response response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                log.error("AI网关调用失败: status={}, body={}", response.code(), errorBody);
                throw new BizException(ErrorCode.AI_GATEWAY_ERROR);
            }

            // 解析 SSE 流
            if (response.body() != null) {
                parseSseStream(response.body(), results);
            }

        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI网关转发异常", e);
            throw new BizException(ErrorCode.AI_GATEWAY_ERROR);
        }

        return results;
    }

    @Override
    public List<String> getAvailableModels() {
        List<String> models = new ArrayList<>();
        try {
            Request request = new Request.Builder()
                    .url(gatewayUrl + "/v1/models")
                    .get()
                    .header("Authorization", "Bearer " + gatewayApiKey)
                    .build();

            Response response = httpClient.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                // 简单解析模型列表
                String body = response.body().string();
                log.info("AI网关模型列表: {}", body.substring(0, Math.min(body.length(), 500)));
            }
        } catch (Exception e) {
            log.warn("获取AI网关模型列表失败", e);
        }
        return models;
    }

    /**
     * 构建 OpenAI 格式的请求体
     */
    private String buildRequestBody(String modelName, List<Map<String, String>> messages, boolean stream) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"model\":\"").append(modelName).append("\",");
        sb.append("\"stream\":").append(stream).append(",");
        sb.append("\"messages\":[");
        for (int i = 0; i < messages.size(); i++) {
            Map<String, String> msg = messages.get(i);
            sb.append("{\"role\":\"").append(msg.get("role")).append("\",");
            sb.append("\"content\":\"").append(escapeJson(msg.get("content"))).append("\"}");
            if (i < messages.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]}");
        return sb.toString();
    }

    /**
     * 解析 SSE 流
     */
    private void parseSseStream(ResponseBody responseBody, List<String> results) throws IOException {
        String line;
        var reader = new java.io.BufferedReader(new java.io.InputStreamReader(responseBody.byteStream()));

        StringBuilder contentBuffer = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("data: ")) {
                String data = line.substring(6);

                // 结束标记
                if ("[DONE]".equals(data)) break;

                // 解析 JSON 提取内容
                try {
                    contentBuffer.append(extractContent(data));
                } catch (Exception ignored) {}
            }
        }

        if (contentBuffer.length() > 0) {
            results.add(contentBuffer.toString());
        } else {
            results.add("");
        }
    }

    /**
     * 从 SSE data 中提取内容
     */
    private String extractContent(String data) throws Exception {
        // 简单 JSON 解析，提取 choices[0].delta.content
        if (!data.contains("choices")) return "";

        int contentStart = data.indexOf("\"content\"");
        if (contentStart == -1) return "";

        // 查找冒号后的值
        int colonPos = data.indexOf(':', contentStart + 9);
        if (colonPos == -1) return "";

        char quoteChar = data.charAt(colonPos + 1); // 应该是 "
        if (quoteChar != '"') return "";

        int valueStart = colonPos + 2;
        int valueEnd = findJsonStringEnd(data, valueStart);
        if (valueEnd == -1) return "";

        return unescapeJson(data.substring(valueStart, valueEnd));
    }

    private int findJsonStringEnd(String s, int start) {
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\') { i++; continue; }
            if (c == '"') return i;
        }
        return -1;
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String unescapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }
}
