package com.aiplatform.controller;

import com.aiplatform.common.R;
import com.aiplatform.dto.ChangePasswordRequest;
import com.aiplatform.service.UserService;
import com.aiplatform.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public R<?> getUserInfo() {
        Long userId = SecurityUtils.getRequiredUserId();
        return R.ok(userService.getUserInfo(userId));
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public R<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = SecurityUtils.getRequiredUserId();
        userService.changePassword(userId, request);
        return R.ok();
    }
}
