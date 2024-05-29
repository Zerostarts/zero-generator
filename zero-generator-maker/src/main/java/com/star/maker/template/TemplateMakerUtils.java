package com.star.maker.template;


import cn.hutool.core.util.StrUtil;
import com.star.maker.meta.Meta;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 模板制作工具类
 */
public class TemplateMakerUtils {

    /**
     * 从未分组文件中移除组内的同名文件
     *
     * @param fileInfoList   生成的meta文件中的文件列表
     * @return
     */
    public static List<Meta.FileConfig.FileInfo> removeGroupFilesFromRoot(List<Meta.FileConfig.FileInfo> fileInfoList) {
        // 现获取到所有分组
        List<Meta.FileConfig.FileInfo> groupFileInfoList  = fileInfoList.stream()
                .filter(fileInfo -> StrUtil.isNotBlank(fileInfo.getGroupKey()))
                .collect(Collectors.toList());
        // 获取所有分组的列表
        List<Meta.FileConfig.FileInfo> groupInnerFileInfoList  = groupFileInfoList.stream()
                .flatMap(fileInfo -> fileInfo.getFiles().stream())
                .collect(Collectors.toList());
        // 获取所有分组内文件的输入路径集合
        Set<String> fileInputPathSet  = groupInnerFileInfoList.stream()
                .map(Meta.FileConfig.FileInfo::getInputPath)
                .collect(Collectors.toSet());

        // 移除所有名称在 set 中的外层文件

        return fileInfoList.stream()
                .filter(fileInfo -> !fileInputPathSet.contains(fileInfo.getInputPath()))
                .collect(Collectors.toList());
    }
}
