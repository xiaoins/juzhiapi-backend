package com.aiplatform.controller.admin;

import com.aiplatform.common.R;
import com.aiplatform.dto.ModelCreateRequest;
import com.aiplatform.service.ModelService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aiplatform.entity.AiModel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理后台控制器 - 模型管理
 */
@RestController
@RequestMapping("/api/admin/models")
@RequiredArgsConstructor
public class AdminModelController {

    private final ModelService modelService;

    /**
     * 获取模型列表(分页)
     */
    @GetMapping
    public R<Page<AiModel>> getModelList(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(modelService.getModelList(current, size));
    }

    /**
     * 创建模型
     */
    @PostMapping
    public R<Void> createModel(@RequestBody ModelCreateRequest request) {
        modelService.createModel(request);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> updateModel(@PathVariable("id") Long id, @RequestBody ModelCreateRequest request) {
        modelService.updateModel(id, request);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> deleteModel(@PathVariable("id") Long id) {
        modelService.deleteModel(id);
        return R.ok();
    }
}
