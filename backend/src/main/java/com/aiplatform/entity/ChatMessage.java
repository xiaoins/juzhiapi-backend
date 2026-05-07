package com.aiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 对话消息实体
 */
@Data
@TableName("chat_message")
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话ID */
    private Long sessionId;

    /** 用户ID */
    private Long userId;

    /** 角色: user/assistant/system */
    private String role;

    /** 消息内容 */
    private String content;

    /** 回复模型(assistant消息时记录) */
    private String modelName;

    /** token数 */
    private Integer tokenCount = 0;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
