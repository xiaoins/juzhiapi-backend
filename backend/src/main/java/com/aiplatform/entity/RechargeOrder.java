package com.aiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 充值订单实体
 */
@Data
@TableName("recharge_order")
public class RechargeOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 订单号 */
    private String orderNo;

    /** 充值金额(元) */
    private BigDecimal amount;

    /** 充值credits */
    private Long credits;

    /** 状态: PENDING/PAID/CANCELLED */
    private String status = "PENDING";

    /** 支付方式 */
    private String payType;

    /** 支付时间 */
    private LocalDateTime paidAt;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
