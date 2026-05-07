package com.aiplatform.utils;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;

import java.security.SecureRandom;

/**
 * API Key 生成工具
 */
public class ApiUtils {

    /**
     * 生成 API Key
     * 格式: sk-user_{32位随机字符串}
     *
     * @return 完整的 API Key
     */
    public static String generateApiKey() {
        String randomPart = IdUtil.fastSimpleUUID().replace("-", "");
        return "sk-user_" + randomPart;
    }

    /**
     * 对 API Key 进行哈希(用于数据库存储)
     *
     * @param apiKey 明文 API Key
     * @return SHA-256 哈希值
     */
    public static String hashApiKey(String apiKey) {
        return SecureUtil.sha256(apiKey);
    }

    /**
     * 获取 API Key 的前缀(用于展示)
     *
     * @param apiKey 完整 API Key
     * @return 前12个字符
     */
    public static String getKeyPrefix(String apiKey) {
        if (apiKey == null || apiKey.length() <= 12) {
            return apiKey;
        }
        return apiKey.substring(0, 12) + "****";
    }

    /**
     * 生成订单号
     * 格式: {年月日时分秒}{8位随机数}
     *
     * @return 订单号
     */
    public static String generateOrderNo() {
        return IdUtil.getSnowflakeNextIdStr();
    }
}
