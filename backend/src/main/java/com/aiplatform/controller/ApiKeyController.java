package com.aiplatform.controller;

import com.aiplatform.common.R;
import com.aiplatform.dto.CreateApiKeyRequest;
import com.aiplatform.service.ApiKeyService;
import com.aiplatform.utils.SecurityUtils;
import com.aiplatform.vo.ApiKeyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * API Key 控制器
 */
@RestController
@RequestMapping("/api/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    /**
     * 创建 API Key
     */
    @PostMapping
    public R<ApiKeyVO> createKey(@RequestBody CreateApiKeyRequest request) {
        Long userId = SecurityUtils.getRequiredUserId();
        ApiKeyVO vo = apiKeyService.createKey(userId, request);
        return R.ok("创建成功，请妥善保管", vo);
    }

    /**
     * 获取 API Key 列表
     */
    @GetMapping
    public R<com.baomidou.mybatisplus.extension.plugins.pagination.Page<ApiKeyVO>> getKeyList(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtils.getRequiredUserId();
        return R.ok(apiKeyService.getKeyList(userId, current, size));
    }

    /**
     * 删除 API Key
     */
    @DeleteMapping("/{id}")
    public R<Void> deleteKey(@PathVariable("id") Long keyId) {
        Long userId = SecurityUtils.getRequiredUserId();
        apiKeyService.deleteKey(userId, keyId);
        return R.ok();
    }

    @PutMapping("/{id}/disable")
    public R<Void> disableKey(@PathVariable("id") Long keyId) {
        apiKeyService.disableKey(keyId);
        return R.ok();
    }

    @PutMapping("/{id}/enable")
    public R<Void> enableKey(@PathVariable("id") Long keyId) {
        apiKeyService.enableKey(keyId);
        return R.ok();
    }
}
