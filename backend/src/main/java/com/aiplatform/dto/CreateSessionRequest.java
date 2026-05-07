package com.aiplatform.dto;

import lombok.Data;

/**
 * 创建会话请求
 */
@Data
public class CreateSessionRequest {

    /** 模型名称 */
    private String modelName;

    /** 会话标题 */
    private String title;
}
