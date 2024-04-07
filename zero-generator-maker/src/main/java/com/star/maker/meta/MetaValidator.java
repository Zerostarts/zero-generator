package com.star.maker.meta;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.star.maker.meta.enums.FileGenerateTypeEnum;
import com.star.maker.meta.enums.FileTypeEnum;
import com.star.maker.meta.enums.ModelTypeEnum;

import java.io.File;
import java.nio.file.Paths;

import java.util.List;

/**
 * 程序的健壮性
 * 程序的圈复杂度降低
 *  1.抽取方法
 *  2.卫语句
 *  3.使用代码工具判断
 */
public class MetaValidator {

    public static void validate(Meta meta) {
        //基础信息校验合法值
        validAndFillMetaRoot(meta);
        // fileConfig 校验
        validAndFillFileConfig(meta);
        //modelConfig 校验
        validAndFillModelConfig(meta);
    }
    private static void validAndFillModelConfig(Meta meta) {
        Meta.ModelConfig modelConfig = meta.getModelConfig();
        if (modelConfig == null) {
            return;
        }
        List<Meta.ModelConfig.ModelInfo> modelInfoList = modelConfig.getModels();
        if (CollectionUtil.isEmpty(modelInfoList)) {
            return;
        }
        for (Meta.ModelConfig.ModelInfo modelInfo : modelInfoList) {
            //输出路径默认值
            String fieldName = modelInfo.getFieldName();

            if (StrUtil.isBlank(fieldName)) {
                throw new MetaException("未填写fieldName");
            }
            //
            String modelInfoType = modelInfo.getType();
            if (StrUtil.isBlank(modelInfoType)) {
                modelInfo.setType(ModelTypeEnum.STRING.getValue());
            }

        }

    }

    private static void validAndFillFileConfig(Meta meta) {
        Meta.FileConfig fileConfig = meta.getFileConfig();
        if (fileConfig == null) {
            return;
        }
        String inputRootPath = fileConfig.getInputRootPath();
        String outputRootPath = fileConfig.getOutputRootPath();
        String sourceRootPath = fileConfig.getSourceRootPath();
        String fileConfigType = fileConfig.getType();
        if (StrUtil.isBlank(sourceRootPath)) {
            throw new MetaException("未填写sourceRootPath");
        }
        //inputRootPath = .source + sourceRootPath的最后一层文件的名字
        String defaultInputRootPath = ".source" + File.separator + FileUtil.getLastPathEle(
                Paths.get(sourceRootPath)).getFileName().toString();
        if (StrUtil.isBlank(inputRootPath)) {
            fileConfig.setInputRootPath(defaultInputRootPath);
        }
        if (StrUtil.isBlank(outputRootPath)) {
            outputRootPath = "generated";
            fileConfig.setOutputRootPath(outputRootPath);
        }
        String defaultType = FileTypeEnum.FILE.getValue();
        if (StrUtil.isBlank(fileConfigType)) {
            fileConfig.setType(defaultType);
        }
        //fileInfo默认值
        List<Meta.FileConfig.FileInfo> fileInfoList = fileConfig.getFiles();
        if (CollectionUtil.isEmpty(fileInfoList)) {
            return;
        }
        for (Meta.FileConfig.FileInfo fileInfo : fileInfoList) {
            String inputPath = fileInfo.getInputPath();
            String outputPath = fileInfo.getOutputPath();
            String type = fileInfo.getType();
            String generateType = fileInfo.getGenerateType();
            //inputPath必填
            if (StrUtil.isBlank(inputPath)) {
                throw new MetaException("未填写 inputPath");
            }

            //需要静态还是动态生成
            if (StrUtil.isBlank(generateType)) {
                if (StrUtil.endWith(inputPath, ".ftl")) {
                    generateType = FileGenerateTypeEnum.DYNAMIC.getValue();
                } else {
                    generateType = FileGenerateTypeEnum.STATIC.getValue();
                }
                fileInfo.setGenerateType(generateType);
            }

            //outputPath 同inputPath相同
            if (StrUtil.isBlank(outputPath)) {
                if (generateType.equals(FileGenerateTypeEnum.STATIC.getValue())) {
                    fileInfo.setOutputPath(inputPath);
                } else {
                    fileInfo.setOutputPath(inputPath.substring(0, inputPath.lastIndexOf(".ftl")));
                }

            }
            //type 如果有后缀命则是file
            if (StrUtil.isBlank(type)) {
                if (StrUtil.isBlank(FileUtil.getSuffix(inputPath))) {
                    fileInfo.setType(FileTypeEnum.DIR.getValue());
                } else {
                    fileInfo.setType(FileTypeEnum.FILE.getValue());
                }
            }

        }
    }

    private static void validAndFillMetaRoot(Meta meta) {
        // 校验并填充默认值
        String name = StrUtil.blankToDefault(meta.getName(), "zero-generator");
        String description = StrUtil.emptyToDefault(meta.getDescription(), "我的模板代码生成器");
        String author = StrUtil.emptyToDefault(meta.getAuthor(), "star");
        String basePackage = StrUtil.blankToDefault(meta.getBasePackage(), "com.star");
        String version = StrUtil.emptyToDefault(meta.getVersion(), "1.0");
        String createTime = StrUtil.emptyToDefault(meta.getCreateTime(), DateUtil.now());
        meta.setName(name);
        meta.setDescription(description);
        meta.setAuthor(author);
        meta.setBasePackage(basePackage);
        meta.setVersion(version);
        meta.setCreateTime(createTime);
    }


}
