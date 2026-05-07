package com.aiplatform.service;

import java.util.List;
import java.util.Map;

/**
 * AI 网关服务 - 负责转发请求到上游AI网关
 */
public interface AiGatewayService {

    /**
     * 获取可用模型列表
     *
     * @return 模型名称列表
     */
    List<String> getAvailableModels();

    /**
     * 转发聊天请求 (流式)
     *
     * @param modelName  模型名称
     * @param messages   消息列表 (OpenAI 格式)
     * @param stream     是否流式
     * @return 流式响应字符串(每行一个 SSE event)
     */
    List<String> forwardChatStream(String modelName, List<Map<String, String>> messages, boolean stream);
}
