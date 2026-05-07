package com.aiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * API Key 实体
 */
@Data
@TableName("api_key")
public class ApiKey {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** Key名称 */
    private String name;

    /** API Key(SHA-256哈希) */
    private String apiKey;

    /** Key前缀(用于展示) */
    private String keyPrefix;

    /** 状态: 1正常 0禁用 */
    private Integer status = 1;

    /** 累计调用次数 */
    private Long totalCalls = 0L;

    /** 累计消耗(credits) */
    private Long totalCost = 0L;

    /** 最后使用时间 */
    private LocalDateTime lastUsedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
