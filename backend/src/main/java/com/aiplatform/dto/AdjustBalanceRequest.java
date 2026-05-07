package com.aiplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 调整余额请求(管理员)
 */
@Data
public class AdjustBalanceRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 变动金额(正数增加，负数减少) */
    @NotNull(message = "变动金额不能为空")
    private Long amount;

    @NotBlank(message = "备注不能为空")
    private String remark;
}
