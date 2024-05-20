package com.star.maker.template.model.enums;

import cn.hutool.core.util.ObjectUtil;

/**
 * 文件过滤范围
 */
public enum FileFilterRangeEnum {


    FILE_NAME("文件名称", "file_name"),

    FILE_CONTENT("文件内容", "file_content");

    private final String text;

    private final String value;

    FileFilterRangeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }
    public FileFilterRangeEnum getEnumByValues(String value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        for (FileFilterRangeEnum anEnum : FileFilterRangeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;

    }
}
