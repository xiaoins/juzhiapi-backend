package com.aiplatform.vo;

import lombok.Data;

/**
 * 模型信息
 */
@Data
public class ModelVO {

    private Long id;
    private String displayName;
    private String modelName;
    private String provider;
    private Long inputPrice;
    private Long outputPrice;
    private Integer sort;
    private Integer enabled;
    private Integer recommended;
}
