package com.aiplatform.controller;

import com.aiplatform.common.R;
import com.aiplatform.service.WalletService;
import com.aiplatform.utils.SecurityUtils;
import com.aiplatform.vo.WalletLogVO;
import com.aiplatform.vo.WalletVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 钱包控制器
 */
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    /**
     * 获取钱包信息
     */
    @GetMapping
    public R<WalletVO> getWalletInfo() {
        Long userId = SecurityUtils.getRequiredUserId();
        return R.ok(walletService.getWalletInfo(userId));
    }

    /**
     * 获取钱包流水
     */
    @GetMapping("/logs")
    public R<com.baomidou.mybatisplus.extension.plugins.pagination.Page<WalletLogVO>> getWalletLogs(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtils.getRequiredUserId();
        return R.ok(walletService.getWalletLogs(userId, current, size));
    }
}
