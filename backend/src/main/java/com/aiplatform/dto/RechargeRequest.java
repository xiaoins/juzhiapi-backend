package com.aiplatform.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 充值请求(用户端)
 */
@Data
public class RechargeRequest {

    /** 充值金额(元) */
    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "1.00", message = "最低充值 1 元")
    @DecimalMax(value = "100000.00", message = "单次最多充值 100,000 元")
    private BigDecimal amount;

    /** 支付方式: ALIPAY/WECHAT/BANK_TRANSFER/MANUAL */
    @NotBlank(message = "支付方式不能为空")
    private String payType;
}
