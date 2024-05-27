package com.star.maker.template.model;

import cn.hutool.core.bean.BeanUtil;
import com.star.maker.meta.Meta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 模板制作的模型配置
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateMakerModelConfig {

    private List<ModelInfoConfig> models;

    private ModelGroupConfig modelGroupConfig;


    @Data
    @NoArgsConstructor
    public static class ModelInfoConfig {

        private String fieldName;
        private String type;
        private String description;
        private Object defaultValue;
        private String abbr;


        //用于替换哪些文本
        private String replaceText;





    }


    @Data
    @NoArgsConstructor
    public static class ModelGroupConfig {

        private String condition;

        private String groupKey;

        private String groupName;

    }



    // TODO 有点小问题，每个model组里边应该是都有一个replacement
    public static List<ModelInfoConfig> templateModelInfoConfigAdapter(List<Meta.ModelConfig.ModelInfo> metaModelInfoList,String replacement) {



         return metaModelInfoList.stream().map(modelInfo -> {
            ModelInfoConfig modelInfoConfig = new ModelInfoConfig();
            BeanUtil.copyProperties(modelInfo,modelInfoConfig);
            modelInfoConfig.setReplaceText(replacement);
            return modelInfoConfig;
        }).collect(Collectors.toList());




    }
}
