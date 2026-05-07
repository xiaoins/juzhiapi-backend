package com.aiplatform.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单信息
 */
@Data
public class OrderVO {

    private Long id;
    private Long userId;
    private String username;
    private String orderNo;
    private BigDecimal amount;
    private Long credits;
    private String status;
    private String payType;
    private LocalDateTime paidAt;
    private String remark;
    private LocalDateTime createdAt;
}
