package com.aiplatform.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 调用日志信息
 */
@Data
public class UsageLogVO {

    private Long id;
    private Long userId;
    private String username;
    private Long apiKeyId;
    private String keyPrefix;
    private Long sessionId;
    private String modelName;
    private String provider;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private Long cost;
    private String status;
    private String errorMessage;
    private String requestIp;
    private Integer latencyMs;
    private LocalDateTime createdAt;
}
