package com.aiplatform.controller.admin;

import com.aiplatform.common.R;
import com.aiplatform.entity.ApiUsageLog;
import com.aiplatform.mapper.ApiUsageLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理后台控制器 - 调用日志
 */
@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
public class AdminLogController {

    private final ApiUsageLogMapper usageLogMapper;

    /**
     * 获取调用日志(分页)
     */
    @GetMapping
    public R<Page<ApiUsageLog>> getLogs(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String modelName,
            @RequestParam(required = false) String status) {
        var page = new Page<ApiUsageLog>(current, size);
        LambdaQueryWrapper<ApiUsageLog> wrapper = new LambdaQueryWrapper<>();

        if (userId != null) wrapper.eq(ApiUsageLog::getUserId, userId);
        if (modelName != null && !modelName.isBlank()) wrapper.like(ApiUsageLog::getModelName, modelName);
        if (status != null && !status.isBlank()) wrapper.eq(ApiUsageLog::getStatus, status);

        return R.ok(usageLogMapper.selectPage(page, wrapper.orderByDesc(ApiUsageLog::getCreatedAt)));
    }
}
