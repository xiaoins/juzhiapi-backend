package com.aiplatform.controller.admin;

import com.aiplatform.common.ErrorCode;
import com.aiplatform.common.R;
import com.aiplatform.dto.AdjustBalanceRequest;
import com.aiplatform.dto.ModelCreateRequest;
import com.aiplatform.entity.User;
import com.aiplatform.exception.BizException;
import com.aiplatform.service.WalletService;
import com.aiplatform.service.UserService;
import com.aiplatform.service.ModelService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理后台控制器 - 用户管理
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final WalletService walletService;

    /**
     * 获取用户列表(分页)
     */
    @GetMapping
    public R<?> getUserList(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(userService.getUserList(current, size));
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public R<?> getUserDetail(@PathVariable("id") Long id) {
        return R.ok(userService.getUserInfo(id));
    }

    /**
     * 更新用户状态 (启用/禁用)
     */
    @PutMapping("/{id}/status")
    public R<Void> updateUserStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status) {
        userService.updateUserStatus(id, status);
        return R.ok();
    }

    /**
     * 调整用户余额
     */
    @PutMapping("/balance")
    public R<Void> adjustBalance(@RequestBody AdjustBalanceRequest request) {
        if (request.getAmount() > 0) {
            walletService.addBalance(request.getUserId(), request.getAmount(), "ADMIN_ADD", request.getRemark());
        } else {
            walletService.deductBalance(request.getUserId(), Math.abs(request.getAmount()), "ADMIN_DEDUCT", request.getRemark());
        }
        return R.ok();
    }
}
