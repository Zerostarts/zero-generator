package com.star.maker.template.model;

import lombok.Data;

/**
 * meta文件的输出规则类
 * 例如: 若生成前存在时覆盖还是不覆盖
 */

@Data
public class TemplateMakerOutputConfig {

    // 从未分组文件中移除组内的同名文件
    private boolean removeGroupFilesFromRoot = true;
}
