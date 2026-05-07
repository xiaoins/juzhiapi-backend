package com.aiplatform.vo;

import lombok.Data;

/**
 * 钱包信息
 */
@Data
public class WalletVO {

    private Long userId;
    private Long balance;
    private Long totalRecharge;
    private Long totalUsed;
}
