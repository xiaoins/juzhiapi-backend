package com.aiplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 订单处理请求(管理员)
 */
@Data
public class OrderProcessRequest {

    /** 订单状态: PAID/CANCELLED */
    @NotBlank(message = "状态不能为空")
    private String status;
}
