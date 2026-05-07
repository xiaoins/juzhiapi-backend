package com.aiplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 聊天发送请求
 */
@Data
public class ChatSendRequest {

    /** 会话ID (新建会话时可为null) */
    private Long sessionId;

    /** 模型名称 */
    @NotBlank(message = "模型名称不能为空")
    private String modelName;

    /** 消息内容 */
    @NotBlank(message = "消息内容不能为空")
    private String message;
}
