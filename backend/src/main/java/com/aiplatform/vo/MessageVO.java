package com.aiplatform.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息信息
 */
@Data
public class MessageVO {

    private Long id;
    private Long sessionId;
    private String role;
    private String content;
    private String modelName;
    private Integer tokenCount;
    private LocalDateTime createdAt;
}
