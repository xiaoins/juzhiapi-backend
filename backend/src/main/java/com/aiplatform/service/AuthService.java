package com.aiplatform.service;

import com.aiplatform.dto.LoginRequest;
import com.aiplatform.dto.RegisterRequest;
import com.aiplatform.vo.LoginVO;

/**
 * 认证服务
 */
public interface AuthService {

    /**
     * 用户注册
     */
    void register(RegisterRequest request);

    /**
     * 用户登录
     *
     * @return 登录信息(含JWT Token)
     */
    LoginVO login(LoginRequest request);

    /**
     * 获取当前用户信息
     */
    Object getCurrentUser(Long userId);
}
