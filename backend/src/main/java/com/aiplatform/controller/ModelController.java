package com.aiplatform.controller;

import com.aiplatform.common.R;
import com.aiplatform.service.ModelService;
import com.aiplatform.vo.ModelVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模型控制器
 */
@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
public class ModelController {

    private final ModelService modelService;

    /**
     * 获取启用的模型列表(用户端)
     */
    @GetMapping
    public R<List<ModelVO>> getEnabledModels() {
        return R.ok(modelService.getEnabledModels());
    }

    /**
     * 获取模型详情
     */
    @GetMapping("/{id}")
    public R<ModelVO> getModelDetail(@PathVariable("id") Long id) {
        return R.ok(modelService.getModelDetail(id));
    }
}
