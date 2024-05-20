package com.star.maker.template.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 过滤机制
 */
@Data
public class TemplateMarkFileConfig {

    private List<FileInfoConfig> files;


    @Data
    @NoArgsConstructor
    public static class FileInfoConfig {

        String path;

        private List<FileFilterConfig> fileFilterConfigList;

    }
}
