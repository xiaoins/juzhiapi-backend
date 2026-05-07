package com.aiplatform.service;

import com.aiplatform.entity.AiModel;

/**
 * 计费服务
 */
public interface BillingService {

    /**
     * 计算调用费用
     *
     * @param model            模型
     * @param promptTokens     输入 token 数
     * @param completionTokens 输出 token 数
     * @return 费用(credits)
     */
    long calculateCost(AiModel model, int promptTokens, int completionTokens);

    /**
     * 记录并扣费 (网页聊天)
     *
     * @param userId           用户ID
     * @param sessionId        会话ID(可为null)
     * @param model            模型
     * @param promptTokens     输入token数
     * @param completionTokens 输出token数
     * @param status           状态: SUCCESS/FAILED
     * @param errorMessage     错误信息
     * @return 消耗金额
     */
    long recordAndDeduct(Long userId, Long sessionId, AiModel model,
                         int promptTokens, int completionTokens,
                         String status, String errorMessage);

    /**
     * 记录并扣费 (API Key 调用)
     *
     * @param userId           用户ID
     * @param apiKeyId         API Key ID
     * @param model            模型
     * @param promptTokens     输入token数
     * @param completionTokens 输出token数
     * @param requestIp        请求IP
     * @param status           状态
     * @param errorMessage     错误信息
     * @return 消耗金额
     */
    long recordAndDeductWithKey(Long userId, Long apiKeyId, AiModel model,
                                int promptTokens, int completionTokens,
                                String requestIp, String status, String errorMessage);
}
