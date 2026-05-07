package com.aiplatform.controller.admin;

import com.aiplatform.common.ErrorCode;
import com.aiplatform.common.R;
import com.aiplatform.dto.OrderProcessRequest;
import com.aiplatform.entity.RechargeOrder;
import com.aiplatform.exception.BizException;
import com.aiplatform.mapper.RechargeOrderMapper;
import com.aiplatform.service.WalletService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理后台控制器 - 订单管理
 */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final RechargeOrderMapper orderMapper;
    private final WalletService walletService;

    /**
     * 获取订单列表(分页)
     */
    @GetMapping
    public R<Page<RechargeOrder>> getOrderList(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status) {
        var page = new Page<RechargeOrder>(current, size);
        LambdaQueryWrapper<RechargeOrder> wrapper = new LambdaQueryWrapper<>();

        if (userId != null) wrapper.eq(RechargeOrder::getUserId, userId);
        if (status != null && !status.isBlank()) wrapper.eq(RechargeOrder::getStatus, status);

        return R.ok(orderMapper.selectPage(page, wrapper.orderByDesc(RechargeOrder::getCreatedAt)));
    }

    /**
     * 处理订单(确认支付/取消)
     */
    @PutMapping("/{id}/process")
    public R<Void> processOrder(@PathVariable("id") Long id, @RequestBody OrderProcessRequest request) {
        RechargeOrder order = orderMapper.selectById(id);
        if (order == null) throw new BizException(ErrorCode.ORDER_NOT_FOUND);
        if (!"PENDING".equals(order.getStatus())) throw new BizException(ErrorCode.ORDER_STATUS_ERROR);

        String newStatus = request.getStatus();

        // 确认支付 -> 充值
        if ("PAID".equals(newStatus)) {
            order.setStatus("PAID");
            order.setPaidAt(java.time.LocalDateTime.now());
            orderMapper.updateById(order);

            // 充值到钱包
            walletService.addBalance(order.getUserId(), order.getCredits(), "RECHARGE",
                    "充值: 订单号" + order.getOrderNo());
        }
        // 取消
        else if ("CANCELLED".equals(newStatus)) {
            order.setStatus("CANCELLED");
            orderMapper.updateById(order);
        } else {
            throw new BizException(ErrorCode.PARAM_ERROR.getCode(), "无效的订单状态");
        }

        return R.ok();
    }
}
