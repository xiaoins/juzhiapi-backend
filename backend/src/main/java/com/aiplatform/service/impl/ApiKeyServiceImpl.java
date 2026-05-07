package com.aiplatform.service.impl;

import com.aiplatform.common.ErrorCode;
import com.aiplatform.dto.CreateApiKeyRequest;
import com.aiplatform.entity.ApiKey;
import com.aiplatform.exception.BizException;
import com.aiplatform.mapper.ApiKeyMapper;
import com.aiplatform.service.ApiKeyService;
import com.aiplatform.utils.ApiUtils;
import com.aiplatform.vo.ApiKeyVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ApiKeyServiceImpl implements ApiKeyService {

    private final ApiKeyMapper apiKeyMapper;

    @Override
    public ApiKeyVO createKey(Long userId, CreateApiKeyRequest request) {
        String plainKey = ApiUtils.generateApiKey();

        ApiKey apiKey = new ApiKey();
        apiKey.setUserId(userId);
        apiKey.setName(request.getName() != null ? request.getName() : "默认Key");
        apiKey.setApiKey(ApiUtils.hashApiKey(plainKey));
        apiKey.setKeyPrefix(ApiUtils.getKeyPrefix(plainKey));
        apiKey.setStatus(1);
        apiKey.setTotalCalls(0L);
        apiKey.setTotalCost(0L);

        apiKeyMapper.insert(apiKey);

        ApiKeyVO vo = convertToVO(apiKey);
        vo.setApiKey(plainKey);
        return vo;
    }

    @Override
    public Page<ApiKeyVO> getKeyList(Long userId, int current, int size) {
        Page<ApiKey> page = new Page<>(current, size);
        Page<ApiKey> keyPage = apiKeyMapper.selectPage(page,
                new LambdaQueryWrapper<ApiKey>()
                        .eq(ApiKey::getUserId, userId)
                        .orderByDesc(ApiKey::getCreatedAt));

        Page<ApiKeyVO> result = new Page<>();
        result.setRecords(keyPage.getRecords().stream().map(k -> {
            ApiKeyVO vo = convertToVO(k);
            vo.setApiKey(null);
            return vo;
        }).toList());
        result.setTotal(keyPage.getTotal());
        result.setCurrent(keyPage.getCurrent());
        result.setSize(keyPage.getSize());
        return result;
    }

    @Override
    public void deleteKey(Long userId, Long keyId) {
        ApiKey apiKey = getKeyAndCheckOwner(userId, keyId);
        if (apiKey != null) {
            apiKeyMapper.deleteById(keyId);
        }
    }

    @Override
    public void disableKey(Long keyId) {
        ApiKey apiKey = apiKeyMapper.selectById(keyId);
        if (apiKey == null) throw new BizException(ErrorCode.API_KEY_INVALID);
        apiKey.setStatus(0);
        apiKeyMapper.updateById(apiKey);
    }

    @Override
    public void enableKey(Long keyId) {
        ApiKey apiKey = apiKeyMapper.selectById(keyId);
        if (apiKey == null) throw new BizException(ErrorCode.API_KEY_INVALID);
        apiKey.setStatus(1);
        apiKeyMapper.updateById(apiKey);
    }

    @Override
    public Long validateAndGetUserId(String apiKey) {
        if (apiKey == null || !apiKey.startsWith("sk-user_")) return null;

        String hashedKey = ApiUtils.hashApiKey(apiKey);
        ApiKey keyEntity = apiKeyMapper.selectOne(
                new LambdaQueryWrapper<ApiKey>()
                        .eq(ApiKey::getApiKey, hashedKey)
                        .eq(ApiKey::getStatus, 1));

        if (keyEntity == null) return null;
        return keyEntity.getUserId();
    }

    @Override
    public void updateKeyStats(Long keyId, Long cost) {
        if (keyId == null) return;

        ApiKey apiKey = apiKeyMapper.selectById(keyId);
        if (apiKey != null) {
            apiKey.setTotalCalls(apiKey.getTotalCalls() + 1);
            apiKey.setTotalCost(apiKey.getTotalCost() + cost);
            apiKey.setLastUsedAt(LocalDateTime.now());
            apiKeyMapper.updateById(apiKey);
        }
    }

    private ApiKey getKeyAndCheckOwner(Long userId, Long keyId) {
        ApiKey apiKey = apiKeyMapper.selectById(keyId);
        if (apiKey == null) throw new BizException(ErrorCode.API_KEY_INVALID);
        if (!apiKey.getUserId().equals(userId)) throw new BizException(ErrorCode.FORBIDDEN);
        return apiKey;
    }

    private ApiKeyVO convertToVO(ApiKey apiKey) {
        ApiKeyVO vo = new ApiKeyVO();
        vo.setId(apiKey.getId());
        vo.setName(apiKey.getName());
        vo.setKeyPrefix(apiKey.getKeyPrefix());
        vo.setStatus(apiKey.getStatus());
        vo.setTotalCalls(apiKey.getTotalCalls());
        vo.setTotalCost(apiKey.getTotalCost());
        vo.setLastUsedAt(apiKey.getLastUsedAt());
        vo.setCreatedAt(apiKey.getCreatedAt());
        return vo;
    }
}
