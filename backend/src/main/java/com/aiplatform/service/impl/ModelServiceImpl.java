package com.aiplatform.service.impl;

import com.aiplatform.common.ErrorCode;
import com.aiplatform.dto.ModelCreateRequest;
import com.aiplatform.entity.AiModel;
import com.aiplatform.exception.BizException;
import com.aiplatform.mapper.AiModelMapper;
import com.aiplatform.service.ModelService;
import com.aiplatform.vo.ModelVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModelServiceImpl implements ModelService {

    private final AiModelMapper aiModelMapper;

    @Override
    public List<ModelVO> getEnabledModels() {
        List<AiModel> models = aiModelMapper.selectList(
                new LambdaQueryWrapper<AiModel>()
                        .eq(AiModel::getEnabled, 1)
                        .orderByAsc(AiModel::getSort));
        return models.stream().map(this::convertToVO).toList();
    }

    @Override
    public Page<AiModel> getModelList(int current, int size) {
        Page<AiModel> page = new Page<>(current, size);
        return aiModelMapper.selectPage(page,
                new LambdaQueryWrapper<AiModel>().orderByAsc(AiModel::getSort));
    }

    @Override
    public ModelVO getModelDetail(Long modelId) {
        AiModel model = aiModelMapper.selectById(modelId);
        if (model == null) throw new BizException(ErrorCode.MODEL_NOT_FOUND);
        return convertToVO(model);
    }

    @Override
    public AiModel getModelByName(String modelName) {
        return aiModelMapper.selectOne(
                new LambdaQueryWrapper<AiModel>()
                        .eq(AiModel::getModelName, modelName)
                        .eq(AiModel::getEnabled, 1));
    }

    @Override
    public void createModel(ModelCreateRequest request) {
        if (aiModelMapper.exists(
                new LambdaQueryWrapper<AiModel>().eq(AiModel::getModelName, request.getModelName()))) {
            throw new BizException("模型名称已存在");
        }

        AiModel model = new AiModel();
        model.setDisplayName(request.getDisplayName());
        model.setModelName(request.getModelName());
        model.setProvider(request.getProvider());
        model.setInputPrice(request.getInputPrice() != null ? request.getInputPrice() : 0L);
        model.setOutputPrice(request.getOutputPrice() != null ? request.getOutputPrice() : 0L);
        model.setSort(request.getSort() != null ? request.getSort() : 0);
        model.setEnabled(request.getEnabled() != null ? request.getEnabled() : 1);
        model.setRecommended(request.getRecommended() != null ? request.getRecommended() : 0);

        aiModelMapper.insert(model);
    }

    @Override
    public void updateModel(Long id, ModelCreateRequest request) {
        AiModel model = aiModelMapper.selectById(id);
        if (model == null) throw new BizException(ErrorCode.MODEL_NOT_FOUND);

        model.setDisplayName(request.getDisplayName());
        model.setProvider(request.getProvider());
        model.setInputPrice(request.getInputPrice() != null ? request.getInputPrice() : 0L);
        model.setOutputPrice(request.getOutputPrice() != null ? request.getOutputPrice() : 0L);
        model.setSort(request.getSort() != null ? request.getSort() : 0);
        model.setEnabled(request.getEnabled() != null ? request.getEnabled() : 1);
        model.setRecommended(request.getRecommended() != null ? request.getRecommended() : 0);

        aiModelMapper.updateById(model);
    }

    @Override
    public void deleteModel(Long id) {
        AiModel model = aiModelMapper.selectById(id);
        if (model == null) throw new BizException(ErrorCode.MODEL_NOT_FOUND);
        aiModelMapper.deleteById(id);
    }

    private ModelVO convertToVO(AiModel model) {
        ModelVO vo = new ModelVO();
        vo.setId(model.getId());
        vo.setDisplayName(model.getDisplayName());
        vo.setModelName(model.getModelName());
        vo.setProvider(model.getProvider());
        vo.setInputPrice(model.getInputPrice());
        vo.setOutputPrice(model.getOutputPrice());
        vo.setSort(model.getSort());
        vo.setEnabled(model.getEnabled());
        vo.setRecommended(model.getRecommended());
        return vo;
    }
}
