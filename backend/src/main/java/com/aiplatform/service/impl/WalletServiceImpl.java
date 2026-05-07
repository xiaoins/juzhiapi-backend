package com.aiplatform.service.impl;

import com.aiplatform.common.ErrorCode;
import com.aiplatform.entity.Wallet;
import com.aiplatform.entity.WalletLog;
import com.aiplatform.exception.BizException;
import com.aiplatform.mapper.WalletLogMapper;
import com.aiplatform.mapper.WalletMapper;
import com.aiplatform.service.WalletService;
import com.aiplatform.vo.WalletLogVO;
import com.aiplatform.vo.WalletVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletMapper walletMapper;
    private final WalletLogMapper walletLogMapper;

    @Override
    public WalletVO getWalletInfo(Long userId) {
        Wallet wallet = getOrCreateWallet(userId);
        return convertToVO(wallet);
    }

    @Override
    public Page<WalletLogVO> getWalletLogs(Long userId, int current, int size) {
        Page<WalletLog> page = new Page<>(current, size);
        Page<WalletLog> logPage = walletLogMapper.selectPage(page,
                new LambdaQueryWrapper<WalletLog>()
                        .eq(WalletLog::getUserId, userId)
                        .orderByDesc(WalletLog::getCreatedAt));

        Page<WalletLogVO> result = new Page<>();
        result.setRecords(logPage.getRecords().stream().map(this::convertLogToVO).toList());
        result.setTotal(logPage.getTotal());
        result.setCurrent(logPage.getCurrent());
        result.setSize(logPage.getSize());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBalance(Long userId, Long amount, String type, String remark) {
        Wallet wallet = getOrCreateWallet(userId);
        long beforeBalance = wallet.getBalance();
        long afterBalance = beforeBalance + amount;

        wallet.setBalance(afterBalance);
        wallet.setTotalRecharge(wallet.getTotalRecharge() + amount);
        walletMapper.updateById(wallet);

        saveLog(userId, type, amount, beforeBalance, afterBalance, remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductBalance(Long userId, Long amount, String type, String remark) {
        Wallet wallet = getOrCreateWallet(userId);
        if (wallet.getBalance() < amount) {
            throw new BizException(ErrorCode.WALLET_BALANCE_INSUFFICIENT);
        }

        long beforeBalance = wallet.getBalance();
        long afterBalance = beforeBalance - amount;

        wallet.setBalance(afterBalance);
        wallet.setTotalUsed(wallet.getTotalUsed() + amount);
        walletMapper.updateById(wallet);

        saveLog(userId, type, -amount, beforeBalance, afterBalance, remark);
    }

    @Override
    public boolean hasEnoughBalance(Long userId, Long cost) {
        Wallet wallet = walletMapper.selectOne(
                new LambdaQueryWrapper<Wallet>().eq(Wallet::getUserId, userId));
        return wallet != null && wallet.getBalance() >= cost;
    }

    @Override
    public void createWallet(Long userId) {
        if (!walletMapper.exists(
                new LambdaQueryWrapper<Wallet>().eq(Wallet::getUserId, userId))) {
            Wallet wallet = new Wallet();
            wallet.setUserId(userId);
            wallet.setBalance(0L);
            wallet.setTotalRecharge(0L);
            wallet.setTotalUsed(0L);
            walletMapper.insert(wallet);
        }
    }

    private Wallet getOrCreateWallet(Long userId) {
        Wallet wallet = walletMapper.selectOne(
                new LambdaQueryWrapper<Wallet>().eq(Wallet::getUserId, userId));
        if (wallet == null) {
            createWallet(userId);
            return walletMapper.selectOne(
                    new LambdaQueryWrapper<Wallet>().eq(Wallet::getUserId, userId));
        }
        return wallet;
    }

    private void saveLog(Long userId, String type, long amount, long beforeBalance, long afterBalance, String remark) {
        WalletLog logEntry = new WalletLog();
        logEntry.setUserId(userId);
        logEntry.setType(type);
        logEntry.setAmount(amount);
        logEntry.setBeforeBalance(beforeBalance);
        logEntry.setAfterBalance(afterBalance);
        logEntry.setRemark(remark);
        walletLogMapper.insert(logEntry);
    }

    private WalletVO convertToVO(Wallet wallet) {
        WalletVO vo = new WalletVO();
        vo.setUserId(wallet.getUserId());
        vo.setBalance(wallet.getBalance());
        vo.setTotalRecharge(wallet.getTotalRecharge());
        vo.setTotalUsed(wallet.getTotalUsed());
        return vo;
    }

    private WalletLogVO convertLogToVO(WalletLog logEntry) {
        WalletLogVO vo = new WalletLogVO();
        vo.setId(logEntry.getId());
        vo.setType(logEntry.getType());
        vo.setAmount(logEntry.getAmount());
        vo.setBeforeBalance(logEntry.getBeforeBalance());
        vo.setAfterBalance(logEntry.getAfterBalance());
        vo.setRemark(logEntry.getRemark());
        vo.setCreatedAt(logEntry.getCreatedAt());
        return vo;
    }
}
