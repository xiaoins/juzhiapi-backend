package com.aiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI 模型实体
 */
@Data
@TableName("ai_model")
public class AiModel {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 显示名称 */
    private String displayName;

    /** 真实模型名 */
    private String modelName;

    /** 供应商 */
    private String provider;

    /** 输入价格(credits/1K tokens) */
    private Long inputPrice = 0L;

    /** 输出价格(credits/1K tokens) */
    private Long outputPrice = 0L;

    /** 排序权重 */
    private Integer sort = 0;

    /** 是否启用: 1是 0否 */
    private Integer enabled = 1;

    /** 是否推荐: 1是 0否 */
    private Integer recommended = 0;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
