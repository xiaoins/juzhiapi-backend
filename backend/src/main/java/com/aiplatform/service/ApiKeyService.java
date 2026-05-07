package com.aiplatform.service;

import com.aiplatform.dto.CreateApiKeyRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aiplatform.vo.ApiKeyVO;

/**
 * API Key 服务
 */
public interface ApiKeyService {

    /**
     * 创建 API Key
     *
     * @return 创建的 API Key (含完整Key，仅此一次返回)
     */
    ApiKeyVO createKey(Long userId, CreateApiKeyRequest request);

    /**
     * 获取用户的 API Key 列表(分页)
     */
    Page<ApiKeyVO> getKeyList(Long userId, int current, int size);

    /**
     * 删除 API Key
     */
    void deleteKey(Long userId, Long keyId);

    /**
     * 禁用 API Key
     */
    void disableKey(Long keyId);

    /**
     * 启用 API Key
     */
    void enableKey(Long keyId);

    /**
     * 验证 API Key 并获取用户ID
     *
     * @param apiKey 明文 API Key
     * @return 用户ID，验证失败返回 null
     */
    Long validateAndGetUserId(String apiKey);

    /**
     * 更新 API Key 调用统计
     *
     * @param keyId  Key ID
     * @param cost   消耗金额
     */
    void updateKeyStats(Long keyId, Long cost);
}
