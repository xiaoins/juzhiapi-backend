package com.aiplatform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aiplatform.vo.WalletVO;
import com.aiplatform.vo.WalletLogVO;

/**
 * 钱包服务
 */
public interface WalletService {

    /**
     * 获取钱包信息
     */
    WalletVO getWalletInfo(Long userId);

    /**
     * 获取钱包流水(分页)
     */
    Page<WalletLogVO> getWalletLogs(Long userId, int current, int size);

    /**
     * 增加余额
     *
     * @param userId 用户ID
     * @param amount 增加金额(正数)
     * @param type   类型: RECHARGE/ADMIN_ADD/REFUND
     * @param remark 备注
     */
    void addBalance(Long userId, Long amount, String type, String remark);

    /**
     * 扣除余额
     *
     * @param userId 用户ID
     * @param amount 扣除金额(正数)
     * @param type   类型: CONSUME/ADMIN_DEDUCT
     * @param remark 备注
     */
    void deductBalance(Long userId, Long amount, String type, String remark);

    /**
     * 检查余额是否足够
     *
     * @return true: 余额足够; false: 余额不足
     */
    boolean hasEnoughBalance(Long userId, Long cost);

    /**
     * 注册时自动创建钱包
     */
    void createWallet(Long userId);
}
