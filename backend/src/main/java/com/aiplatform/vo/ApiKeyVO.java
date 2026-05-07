package com.aiplatform.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * API Key 信息(列表展示用，不含完整Key)
 */
@Data
public class ApiKeyVO {

    private Long id;
    private String name;
    /** Key前缀 */
    private String keyPrefix;
    /** 完整API Key (仅创建时返回一次) */
    private String apiKey;
    private Integer status;
    private Long totalCalls;
    private Long totalCost;
    private LocalDateTime lastUsedAt;
    private LocalDateTime createdAt;
}

/**
 * API Key 创建响应(含完整Key)
 */
@Data
class ApiKeyCreateVO extends ApiKeyVO {

    /** 完整API Key (仅创建时返回一次) */
    private String apiKey;
}
