package com.aiplatform.controller;

import com.aiplatform.common.R;
import com.aiplatform.dto.LoginRequest;
import com.aiplatform.dto.RegisterRequest;
import com.aiplatform.service.AuthService;
import com.aiplatform.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public R<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return R.ok();
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        LoginVO result = authService.login(request);
        return R.ok(result);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public R<?> getCurrentUser() {
        Long userId = com.aiplatform.utils.SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return R.error(com.aiplatform.common.ErrorCode.UNAUTHORIZED);
        }
        Object user = authService.getCurrentUser(userId);
        return R.ok(user);
    }

    /**
     * 退出登录 (前端清除Token即可)
     */
    @PostMapping("/logout")
    public R<Void> logout() {
        return R.ok();
    }
}
