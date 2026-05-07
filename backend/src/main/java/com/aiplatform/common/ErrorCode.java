package com.aiplatform.common;

import lombok.Getter;
import lombok.AllArgsConstructor;

/**
 * 统一错误码
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ========== 通用 ==========
    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    BALANCE_NOT_ENOUGH(402, "余额不足，请先充值"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),

    // ========== 用户 (1001-1999) ==========
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_DISABLED(1002, "用户已被禁用"),
    PASSWORD_ERROR(1003, "密码错误"),
    USERNAME_EXISTS(1004, "用户名已存在"),
    EMAIL_EXISTS(1005, "邮箱已被注册"),
    OLD_PASSWORD_ERROR(1006, "原密码错误"),

    // ========== 钱包 (2001-2999) ==========
    WALLET_NOT_FOUND(2001, "钱包不存在"),
    WALLET_BALANCE_INSUFFICIENT(2002, "余额不足"),

    // ========== API Key (3001-3999) ==========
    API_KEY_INVALID(3001, "API Key无效"),
    API_KEY_DISABLED(3002, "API Key已被禁用"),
    API_KEY_LIMIT_EXCEEDED(3003, "API Key调用次数已达上限"),

    // ========== 模型 (4001-4999) ==========
    MODEL_NOT_FOUND(4001, "模型不存在或已下线"),
    MODEL_DISABLED(4002, "模型已停用"),
    AI_GATEWAY_ERROR(4003, "AI服务调用失败"),

    // ========== 聊天 (5001-5999) ==========
    SESSION_NOT_FOUND(5001, "会话不存在"),
    SESSION_NOT_BELONG_TO_USER(5002, "会话不属于当前用户"),

    // ========== 订单 (6001-6999) ==========
    ORDER_NOT_FOUND(6001, "订单不存在"),
    ORDER_STATUS_ERROR(6002, "订单状态异常"),

    // ========== 系统内部 (9000+) ==========
    INTERNAL_ERROR(5000, "服务器内部错误");

    private final Integer code;
    private final String msg;
}
