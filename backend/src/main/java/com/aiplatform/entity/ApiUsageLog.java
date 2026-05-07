package com.aiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * API调用日志实体
 */
@Data
@TableName("api_usage_log")
public class ApiUsageLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** API Key ID(可为NULL) */
    private Long apiKeyId;

    /** 会话ID(网页聊天时有值) */
    private Long sessionId;

    /** 模型名 */
    private String modelName;

    /** 供应商 */
    private String provider;

    /** 输入token数 */
    private Integer promptTokens = 0;

    /** 输出token数 */
    private Integer completionTokens = 0;

    /** 总token数 */
    private Integer totalTokens = 0;

    /** 消耗金额(credits) */
    private Long cost = 0L;

    /** 状态: SUCCESS/FAILED */
    private String status = "SUCCESS";

    /** 错误信息 */
    private String errorMessage;

    /** 请求IP */
    private String requestIp;

    /** 响应耗时(ms) */
    private Integer latencyMs;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
