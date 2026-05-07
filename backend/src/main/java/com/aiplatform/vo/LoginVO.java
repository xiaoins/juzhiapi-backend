package com.aiplatform.vo;

import lombok.Data;

/**
 * 登录响应
 */
@Data
public class LoginVO {

    /** JWT Token */
    private String token;

    /** 过期时间(秒) */
    private Long expiresIn;

    /** 用户信息 */
    private UserInfoVO user;
}
