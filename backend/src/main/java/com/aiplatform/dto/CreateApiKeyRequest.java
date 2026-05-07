package com.aiplatform.dto;

import lombok.Data;

/**
 * 创建API Key请求
 */
@Data
public class CreateApiKeyRequest {

    /** Key名称 */
    private String name;
}
