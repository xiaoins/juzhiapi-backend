package com.aiplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 钱包流水实体
 */
@Data
@TableName("wallet_log")
public class WalletLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 类型: RECHARGE/CONSUME/ADMIN_ADD/ADMIN_DEDUCT/REFUND */
    private String type;

    /** 变动金额(正增负减) */
    private Long amount;

    /** 变动前余额 */
    private Long beforeBalance;

    /** 变动后余额 */
    private Long afterBalance;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
