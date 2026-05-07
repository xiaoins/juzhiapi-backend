package com.aiplatform.service;

import com.aiplatform.dto.ModelCreateRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aiplatform.entity.AiModel;
import com.aiplatform.vo.ModelVO;

/**
 * 模型服务
 */
public interface ModelService {

    /**
     * 获取启用的模型列表(用户端)
     */
    java.util.List<ModelVO> getEnabledModels();

    /**
     * 获取所有模型列表(管理员，分页)
     */
    Page<AiModel> getModelList(int current, int size);

    /**
     * 获取模型详情
     */
    ModelVO getModelDetail(Long modelId);

    /**
     * 根据模型名称获取模型
     */
    AiModel getModelByName(String modelName);

    /**
     * 创建模型(管理员)
     */
    void createModel(ModelCreateRequest request);

    /**
     * 更新模型(管理员)
     */
    void updateModel(Long id, ModelCreateRequest request);

    /**
     * 删除模型(管理员)
     */
    void deleteModel(Long id);
}
