package com.aiplatform.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话信息
 */
@Data
public class SessionVO {

    private Long id;
    private String title;
    private String modelName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
