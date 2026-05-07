package com.aiplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 模型创建/编辑请求(管理员)
 */
@Data
public class ModelCreateRequest {

    @NotBlank(message = "显示名称不能为空")
    private String displayName;

    @NotBlank(message = "模型名称不能为空")
    private String modelName;

    @NotBlank(message = "供应商不能为空")
    private String provider;

    /** 输入价格(credits/1K tokens) */
    private Long inputPrice = 0L;

    /** 输出价格(credits/1K tokens) */
    private Long outputPrice = 0L;

    /** 排序权重 */
    private Integer sort = 0;

    /** 是否启用 */
    private Integer enabled = 1;

    /** 是否推荐 */
    private Integer recommended = 0;
}
