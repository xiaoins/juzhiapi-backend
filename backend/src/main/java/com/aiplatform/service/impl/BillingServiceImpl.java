package com.aiplatform.service.impl;

import com.aiplatform.entity.AiModel;
import com.aiplatform.entity.ApiUsageLog;
import com.aiplatform.mapper.ApiUsageLogMapper;
import com.aiplatform.service.ApiKeyService;
import com.aiplatform.service.BillingService;
import com.aiplatform.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final WalletService walletService;
    private final ApiUsageLogMapper usageLogMapper;
    private final ApiKeyService apiKeyService;

    @Override
    public long calculateCost(AiModel model, int promptTokens, int completionTokens) {
        if (model == null || model.getInputPrice() == 0 && model.getOutputPrice() == 0) return 0;

        long inputCost = (long) Math.ceil((double) promptTokens / 1000 * model.getInputPrice());
        long outputCost = (long) Math.ceil((double) completionTokens / 1000 * model.getOutputPrice());

        return inputCost + outputCost;
    }

    @Override
    public long recordAndDeduct(Long userId, Long sessionId, AiModel model,
                                int promptTokens, int completionTokens,
                                String status, String errorMessage) {
        long cost = calculateCost(model, promptTokens, completionTokens);

        if ("SUCCESS".equals(status) && cost > 0) {
            try {
                walletService.deductBalance(userId, cost, "CONSUME",
                        "AI调用: " + (model != null ? model.getDisplayName() : "未知模型"));
            } catch (Exception e) {
                log.error("扣费失败，userId={}, cost={}", userId, cost, e);
                status = "FAILED";
                errorMessage = "扣费失败: " + e.getMessage();
            }
        }

        saveUsageLog(userId, null, sessionId, model,
                promptTokens, completionTokens, cost, status, errorMessage, null, null);

        return cost;
    }

    @Override
    public long recordAndDeductWithKey(Long userId, Long apiKeyId, AiModel model,
                                        int promptTokens, int completionTokens,
                                        String requestIp, String status, String errorMessage) {
        long cost = calculateCost(model, promptTokens, completionTokens);

        if ("SUCCESS".equals(status) && cost > 0) {
            try {
                walletService.deductBalance(userId, cost, "CONSUME",
                        "API Key调用: " + (model != null ? model.getModelName() : "未知模型"));
            } catch (Exception e) {
                log.error("扣费失败(API Key), userId={}, cost={}", userId, cost, e);
                status = "FAILED";
                errorMessage = "扣费失败: " + e.getMessage();
            }
        }

        if ("SUCCESS".equals(status) && apiKeyId != null) {
            try {
                apiKeyService.updateKeyStats(apiKeyId, cost);
            } catch (Exception ignored) {}
        }

        saveUsageLog(userId, apiKeyId, null, model,
                promptTokens, completionTokens, cost, status, errorMessage, requestIp, null);

        return cost;
    }

    private void saveUsageLog(Long userId, Long apiKeyId, Long sessionId,
                               AiModel model, int promptTokens, int completionTokens,
                               long cost, String status, String errorMessage,
                               String requestIp, Integer latencyMs) {
        ApiUsageLog logEntry = new ApiUsageLog();
        logEntry.setUserId(userId);
        logEntry.setApiKeyId(apiKeyId);
        logEntry.setSessionId(sessionId);
        logEntry.setModelName(model != null ? model.getModelName() : null);
        logEntry.setProvider(model != null ? model.getProvider() : null);
        logEntry.setPromptTokens(promptTokens);
        logEntry.setCompletionTokens(completionTokens);
        logEntry.setTotalTokens(promptTokens + completionTokens);
        logEntry.setCost(cost > 0 ? cost : 0L);
        logEntry.setStatus(status);
        logEntry.setErrorMessage(errorMessage);
        logEntry.setRequestIp(requestIp);
        logEntry.setLatencyMs(latencyMs);
        usageLogMapper.insert(logEntry);
    }
}
