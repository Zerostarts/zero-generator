package com.star.maker.template.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 过滤机制
 */
@Data
public class TemplateMakerFileConfig {

    private List<FileInfoConfig> files;

    private FileGroupConfig fileGroupConfig;


    @Data
    @NoArgsConstructor
    public static class FileInfoConfig {

        String path;

        private List<FileFilterConfig> fileFilterConfigList;

    }


    @Data
    @NoArgsConstructor
    public static class FileGroupConfig {

        private String condition;

        private String groupKey;

        private String groupName;

    }
}