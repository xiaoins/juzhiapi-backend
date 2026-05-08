package com.aiplatform.controller;

import com.aiplatform.common.R;
import com.aiplatform.dto.RechargeRequest;
import com.aiplatform.entity.RechargeOrder;
import com.aiplatform.mapper.RechargeOrderMapper;
import com.aiplatform.service.WalletService;
import com.aiplatform.utils.SecurityUtils;
import com.aiplatform.vo.WalletLogVO;
import com.aiplatform.vo.WalletVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 钱包控制器
 */
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final RechargeOrderMapper orderMapper;

    /** 充值比例: 1元 = 1000 credits */
    private static final long CREDITS_PER_YUAN = 1000L;

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

    /**
     * 创建充值订单 (用户端)
     *
     * 流程: 用户提交 → 生成PENDING订单 → 管理员确认后到账
     */
    @PostMapping("/recharge")
    public R<RechargeOrder> createRecharge(@Valid @RequestBody RechargeRequest request) {
        Long userId = SecurityUtils.getRequiredUserId();
        BigDecimal amount = request.getAmount();

        // 计算credits: 1元=1000 credits
        long credits = amount.multiply(BigDecimal.valueOf(CREDITS_PER_YUAN)).longValue();

        // 生成订单号
        String orderNo = "RCD" + DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").format(LocalDateTime.now())
                + String.format("%04d", userId % 10000);

        RechargeOrder order = new RechargeOrder();
        order.setUserId(userId);
        order.setOrderNo(orderNo);
        order.setAmount(amount);
        order.setCredits(credits);
        order.setPayType(request.getPayType());
        order.setStatus("PENDING");
        order.setRemark("用户自助充值");

        orderMapper.insert(order);

        return R.ok(order);
    }

    /**
     * 我的充值订单列表 (用户端)
     */
    @GetMapping("/orders")
    public R<com.baomidou.mybatisplus.extension.plugins.pagination.Page<RechargeOrder>> getMyOrders(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        Long userId = SecurityUtils.getRequiredUserId();
        var page = new Page<RechargeOrder>(current, size);
        LambdaQueryWrapper<RechargeOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RechargeOrder::getUserId, userId);
        if (status != null && !status.isBlank()) {
            wrapper.eq(RechargeOrder::getStatus, status);
        }
        return R.ok(orderMapper.selectPage(page, wrapper.orderByDesc(RechargeOrder::getCreatedAt)));
    }
}
