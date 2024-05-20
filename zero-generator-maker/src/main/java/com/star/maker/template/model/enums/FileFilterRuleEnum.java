package com.star.maker.template.model.enums;

import cn.hutool.core.util.ObjectUtil;

/**
 * 文件过滤规则
 */
public enum FileFilterRuleEnum {


    CONTAINS("包含", "contains"),
    START_WITH("前缀匹配", "start_with"),

    END_WITH("后缀匹配", "end_with"),

    REGEX("正则", "regex"),

    EQUALS("相等", "equals");

    private final String text;

    private final String value;

    FileFilterRuleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }
    public FileFilterRuleEnum getEnumByValues(String value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        for (FileFilterRuleEnum anEnum : FileFilterRuleEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;

    }
}
