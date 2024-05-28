package com.star.maker.template.model;


import com.star.maker.meta.Meta;
import lombok.Data;

@Data
public class TemplateMakerConfig {

    private long id;


    private Meta meta = new Meta();

    private String originProjectPath;

    private TemplateMakerFileConfig templateMakerFileConfig = new TemplateMakerFileConfig();

    private TemplateMakerModelConfig templateMakerModelConfig = new TemplateMakerModelConfig();


}
