package com.aiplatform.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 钱包流水信息
 */
@Data
public class WalletLogVO {

    private Long id;
    private String type;
    private Long amount;
    private Long beforeBalance;
    private Long afterBalance;
    private String remark;
    private LocalDateTime createdAt;
}
