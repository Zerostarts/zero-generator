package com.star.maker.template;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.star.maker.meta.Meta;
import com.star.maker.meta.enums.FileGenerateTypeEnum;
import com.star.maker.meta.enums.FileTypeEnum;
import com.star.maker.template.model.*;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class TemplateMaker {


    /**
     * 制作模板
     *
     * @param templateMakerConfig
     * @return
     */
    public static long makeTemplate(TemplateMakerConfig templateMakerConfig) {
        Meta meta = templateMakerConfig.getMeta();
        String originProjectPath = templateMakerConfig.getOriginProjectPath();
        TemplateMakerFileConfig templateMakerFileConfig = templateMakerConfig.getFileConfig();
        TemplateMakerModelConfig templateMakerModelConfig = templateMakerConfig.getModelConfig();
        TemplateMakerOutputConfig templateMakerOutputConfig = templateMakerConfig.getOutputConfig();
        Long id = templateMakerConfig.getId();

        return makeTemplate(meta, originProjectPath, templateMakerFileConfig, templateMakerModelConfig, templateMakerOutputConfig, id);
    }

    /**
     * 制作模板和meta
     * //@param newMeta
     * //@param originProjectPath   文件原始项目路径
     * @param //fileInputPathList 文件输入路径列表
     * //@param id
     * @return
     */
    public static long makeTemplate(Meta newMeta, String originProjectPath, TemplateMakerFileConfig templateMakerFileConfig,
                                    TemplateMakerModelConfig templateMakerModelConfig, TemplateMakerOutputConfig templateMakerOutputConfig, Long id) {


        if (ObjectUtil.isNull(id)) {
            id = IdUtil.getSnowflakeNextId();
        }
        // 指定项目原始路
        String projectPath = System.getProperty("user.dir");

        // 复制目录
        String tempDirPath = projectPath + File.separator + ".temp";
        String templatePath = tempDirPath + File.separator + id;
        if (!FileUtil.exist(templatePath)) {
            FileUtil.mkdir(templatePath);
            FileUtil.copy(originProjectPath, templatePath, true);
        }

        // 一 输入信息
        // 输入文件信息
        // 输入文件信息，获取到项目根目录
        String sourceRootPath = FileUtil.loopFiles(new File(templatePath), 1, null)
                .stream()
                .filter(File::isDirectory)
                .findFirst()
                .orElseThrow(RuntimeException::new)
                .getAbsolutePath();

        sourceRootPath = sourceRootPath.replaceAll("\\\\", "/");

        //制作文件模板
        List<Meta.FileConfig.FileInfo> newFileInfoList = makeFileTemplates(templateMakerFileConfig, templateMakerModelConfig, sourceRootPath);
        //处理模型信息
        List<Meta.ModelConfig.ModelInfo> newModelInfoList = getModelInfoList(templateMakerModelConfig);

        // 三、生成配置文件
        String metaOutputPath = templatePath + File.separator + "meta.json";

        // 已经有meta，则直接修改原有的配置文件
        if (FileUtil.exist(metaOutputPath)) {

            newMeta = JSONUtil.toBean(FileUtil.readUtf8String(metaOutputPath), Meta.class);
            //1. 在旧有的meta上存储接着存放要生成的model
            List<Meta.FileConfig.FileInfo> fileInfoList = newMeta.getFileConfig().getFiles();
            fileInfoList.addAll(newFileInfoList);

            List<Meta.ModelConfig.ModelInfo> modelInfoList = newMeta.getModelConfig().getModels();
            modelInfoList.addAll(newModelInfoList);
            // 配置去重
            newMeta.getModelConfig().setModels(distinctModels(modelInfoList));
            newMeta.getFileConfig().setFiles(distinctFiles(fileInfoList));

        } else {
            //1. 构造输出的meta
            Meta.FileConfig fileConfig = new Meta.FileConfig();
            fileConfig.setSourceRootPath(sourceRootPath);
            // 设置文件信息
            List<Meta.FileConfig.FileInfo> fileInfoList = new ArrayList<>();
            fileInfoList.addAll(newFileInfoList);
            fileConfig.setFiles(fileInfoList);
            newMeta.setFileConfig(fileConfig);
            // 设置模型信息
            Meta.ModelConfig modelConfig = new Meta.ModelConfig();
            newMeta.setModelConfig(modelConfig);
            List<Meta.ModelConfig.ModelInfo> modelInfoList = new ArrayList<>();
            modelConfig.setModels(modelInfoList);
            modelInfoList.addAll(newModelInfoList);
        }

        //2.额外的输出配置
        if (templateMakerOutputConfig != null) {
            // 全局文件去重
            if(templateMakerOutputConfig.isRemoveGroupFilesFromRoot()) {
                List<Meta.FileConfig.FileInfo> fileInfoList = newMeta.getFileConfig().getFiles();
                newMeta.getFileConfig().setFiles(TemplateMakerUtils.removeGroupFilesFromRoot(fileInfoList));
            }

        }

        //3.输出元信息文件
        FileUtil.writeUtf8String(JSONUtil.toJsonStr(newMeta), metaOutputPath);
        return id;
    }


    /**
     * 获取模型信息列表
     *
     * @param templateMakerModelConfig   模型信息
     * @return
     */
    private static List<Meta.ModelConfig.ModelInfo> getModelInfoList(TemplateMakerModelConfig templateMakerModelConfig) {
        // 本次新增的模型列表
        List<Meta.ModelConfig.ModelInfo> newModelInfoList = new ArrayList<>();

        if (templateMakerModelConfig == null) {
            return newModelInfoList;
        }

        //处理模型信息
        List<TemplateMakerModelConfig.ModelInfoConfig> models = templateMakerModelConfig.getModels();

        if (CollUtil.isEmpty(models)) {
            return newModelInfoList;
        }

        //转化为配置文件接收的modeInfo对象
        List<Meta.ModelConfig.ModelInfo> inputModelInfoList = models.stream()
                .map(modelInfoConfig -> {
                    Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
                    BeanUtil.copyProperties(modelInfoConfig, modelInfo);
                    return modelInfo;
                }).collect(Collectors.toList());

        //如果是模型组
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();
        if (modelGroupConfig != null) {
            // 复制变量
            Meta.ModelConfig.ModelInfo groupModelInfo = new Meta.ModelConfig.ModelInfo();
            BeanUtil.copyProperties(modelGroupConfig, groupModelInfo);
            // 模型全放到一个分组内
            groupModelInfo.setModels(inputModelInfoList);
            newModelInfoList.add(groupModelInfo);
        } else {
            newModelInfoList.addAll(inputModelInfoList);
        }
        return newModelInfoList;
    }


    private static List<Meta.FileConfig.FileInfo> makeFileTemplates(TemplateMakerFileConfig templateMakerFileConfig,
                                                                    TemplateMakerModelConfig templateMakerModelConfig,
                                                                    String sourceRootPath) {
        List<Meta.FileConfig.FileInfo> newFileInfoList = new ArrayList<>();

        //非空校验
        if (templateMakerFileConfig == null) {
            return newFileInfoList;
        }

        List<TemplateMakerFileConfig.FileInfoConfig> fileInfoConfigList = templateMakerFileConfig.getFiles();

        if (CollUtil.isEmpty(fileInfoConfigList)) {
            return newFileInfoList;
        }

        // 过滤 + 生成模板
        for (TemplateMakerFileConfig.FileInfoConfig fileInfoConfig : fileInfoConfigList) {
            // 获取文件的过滤信息
            String fileInputPath = fileInfoConfig.getPath();

            // 如果填的是相对路径，要改为绝对路径
            if (!fileInputPath.startsWith(sourceRootPath)) {
                fileInputPath = sourceRootPath + File.separator + fileInputPath;
            }

            List<File> fileList = FileFilter.doFilter(fileInputPath, fileInfoConfig.getFilterConfigList());
            fileList = fileList.stream()
                    .filter(file -> {
                        return !file.getAbsolutePath().endsWith(".ftl");
                    }).collect(Collectors.toList());
            for (File file : fileList) {
                Meta.FileConfig.FileInfo fileInfo = makeFileTemplate(templateMakerModelConfig, sourceRootPath, file, fileInfoConfig);
                //制作好的模板文件列表
                newFileInfoList.add(fileInfo);
            }

        }
        // 如果是文件组
        TemplateMakerFileConfig.FileGroupConfig fileGroupConfig = templateMakerFileConfig.getFileGroupConfig();

        if (fileGroupConfig != null) {
            String condition = fileGroupConfig.getCondition();
            String groupKey = fileGroupConfig.getGroupKey();
            String groupName = fileGroupConfig.getGroupName();
            // 设置meta中的文件组
            Meta.FileConfig.FileInfo groupFileInfo = new Meta.FileConfig.FileInfo();
            groupFileInfo.setType(FileTypeEnum.GROUP.getValue());
            groupFileInfo.setCondition(condition);
            groupFileInfo.setGroupKey(groupKey);
            groupFileInfo.setGroupName(groupName);

            groupFileInfo.setFiles(newFileInfoList);
            //文件组属于一级文件配置
            newFileInfoList = new ArrayList<>();
            newFileInfoList.add(groupFileInfo);
        }
        return newFileInfoList;
    }

    /**
     * 制作单个模板
     * @param templateMakerModelConfig
     * @param sourRootPath
     * @param inputFile
     * @return
     */
    private static Meta.FileConfig.FileInfo makeFileTemplate(TemplateMakerModelConfig templateMakerModelConfig, String sourRootPath,
                                                             File inputFile, TemplateMakerFileConfig.FileInfoConfig fileInfoConfig) {

        String fileInputAbsolutePath = inputFile.getAbsolutePath();
        // 绝对路径转义之后
        fileInputAbsolutePath = fileInputAbsolutePath.replaceAll("\\\\", "/");

        //相对路径
        String fileInputPath = fileInputAbsolutePath.replace(sourRootPath + "/", "" );

        // 要挖坑文件
        String fileOutputPath = fileInputPath + ".ftl";
        // 输入模型参数信息

        // 二、使用字符串替换，生成模板文件
        String fileOutputAbsolutePath = fileInputAbsolutePath + ".ftl";

        String fileContent;
        //如果已经有模板，表示不是第一次制作
        boolean hasTemplateFile = FileUtil.exist(fileOutputAbsolutePath);
        if (hasTemplateFile) {
            fileContent = FileUtil.readUtf8String(fileOutputAbsolutePath);;
        } else {
            fileContent = FileUtil.readUtf8String(fileInputAbsolutePath);;
        }

        //支持多个模型，对于同一个文件的内容，遍历模型进行多轮替换
        List<TemplateMakerModelConfig.ModelInfoConfig> models = templateMakerModelConfig.getModels();

        String newFileContent = fileContent;
        String replacement;

        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();

        for (TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig : models) {
            String fieldName = modelInfoConfig.getFieldName();
            //模型配置
            if (modelGroupConfig == null) {
                replacement = String.format("${%s}", fieldName);
            } else {
                //注意分组
                String groupKey = modelGroupConfig.getGroupKey();
                replacement = String.format("${%s.%s}", groupKey, fieldName);

            }
            newFileContent = StrUtil.replace(newFileContent, modelInfoConfig.getReplaceText(), replacement);
        }


        // 输出模板文件
        //文件配置信息（具体的文件）
        Meta.FileConfig.FileInfo fileInfo = new Meta.FileConfig.FileInfo();
        fileInfo.setInputPath(fileOutputPath);
        fileInfo.setOutputPath(fileInputPath);
        fileInfo.setCondition(fileInfoConfig.getCondition());
        fileInfo.setType(FileTypeEnum.FILE.getValue());
        fileInfo.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());

        //是否更改了文件内容
        boolean contentEquals = fileContent.equals(newFileContent);
        //之前不尊在模板文件，并且这次替换没有修改文档的内容，才是静态生成
        if (!hasTemplateFile) {
            if (contentEquals) {
                fileInfo.setGenerateType(FileGenerateTypeEnum.STATIC.getValue());
                fileInfo.setInputPath(fileInputPath);
            } else {
                FileUtil.writeUtf8String(newFileContent, fileOutputAbsolutePath);
            }
        } else if (!contentEquals) {
            //有模板文件，并且只能加了新坑，更新模板文件
            FileUtil.writeUtf8String(newFileContent, fileOutputAbsolutePath);

        }

        return fileInfo;
    }

    /**
     * 文件去重
     * @param fileInfoList
     * @return
     */

    public static List<Meta.FileConfig.FileInfo> distinctFiles(List<Meta.FileConfig.FileInfo> fileInfoList) {

        //1. 将所有的文件配置FileInfo分为有分组的和无分组的
        //先处理有分组的文件
        // {groupKey: a, files[1, 2], groupKey: a, files[3, 4]}  groupKey: b, files[4, 5]}
        // files[groupKey: a, files[[1, 2],[ 3, 4]]] ,[groupKey: b, files[4, 5]]
        Map<String, List<Meta.FileConfig.FileInfo>> groupKeyFileInfoListMap = fileInfoList.stream().
                filter(fileInfo -> StrUtil.isNotBlank(fileInfo.getGroupKey()))
                .collect(
                        Collectors.groupingBy(Meta.FileConfig.FileInfo::getGroupKey)
                );
        //2. 对于有分组的文件，如果有相同分组，同分组内的文件进行和并， 不同分组同时保留
        // 展开groupKey: a, file[groupkey: a,files[1, 2], file[groupkey: a,files[2, 3]]
        // 然后再变成去重

        Map<String, Meta.FileConfig.FileInfo> groupKeyMergedFileInfo = new HashMap<>();

        for (Map.Entry<String, List<Meta.FileConfig.FileInfo>> entry: groupKeyFileInfoListMap.entrySet()) {
            List<Meta.FileConfig.FileInfo> tempFileInfoList = entry.getValue();

            List<Meta.FileConfig.FileInfo> newFileInfoList = new ArrayList<>(tempFileInfoList.stream()
                    .flatMap(fileInfo -> fileInfo.getFiles().stream())
                    .collect(
                            Collectors.toMap(Meta.FileConfig.FileInfo::getOutputPath, o -> o, (e, r) -> r)
                    ).values());

            // 使用新的group配置
            Meta.FileConfig.FileInfo newFileInfo = CollUtil.getLast(tempFileInfoList);
            newFileInfo.setFiles(newFileInfoList);
            String groupKey = entry.getKey();
            groupKeyMergedFileInfo.put(groupKey, newFileInfo);
        }

        //3. 创建新的文件配置列表，先将合并后的分组添加到结果列表
        ArrayList<Meta.FileConfig.FileInfo> resultList = new ArrayList<>(groupKeyMergedFileInfo.values());
        //4. 在将无分组文件配置列表添加到结果列表

        resultList.addAll(fileInfoList.stream()
                 .filter(fileInfo -> StrUtil.isBlank(fileInfo.getGroupKey()))
                .collect(
                        Collectors.toMap(Meta.FileConfig.FileInfo::getOutputPath, o -> o, (e, r) -> r)
                ).values());

        return resultList;
    }


    /**
     * 模型去重
     * @param modelInfoList
     * @return
     */
    public static List<Meta.ModelConfig.ModelInfo> distinctModels(List<Meta.ModelConfig.ModelInfo> modelInfoList) {
        //1. 将所有的文件配置ModelInfo分为有分组的和无分组的
        //先处理有分组的文件
        // {groupKey: a, models[1, 2], groupKey: a, models[3, 4]}  groupKey: b, models[4, 5]}
        // models[groupKey: a, models[[1, 2],[ 3, 4]]] ,[groupKey: b, models[4, 5]]
        Map<String, List<Meta.ModelConfig.ModelInfo>> groupKeyModelInfoListMap = modelInfoList.stream().filter(modelInfo -> StrUtil.isNotBlank(modelInfo.getGroupKey()))
                .collect(
                        Collectors.groupingBy(Meta.ModelConfig.ModelInfo::getGroupKey)
                );


        //2. 对于有分组的文件，如果有相同分组，同分组内的文件进行和并， 不同分组同时保留
        // 展开groupKey: a, model[groupkey: a,models[1, 2], model[groupkey: a,models[2, 3]]
        // 然后再变成去重

        Map<String, Meta.ModelConfig.ModelInfo> groupKeyMergedModelInfo = new HashMap<>();

        for (Map.Entry<String, List<Meta.ModelConfig.ModelInfo>> entry: groupKeyModelInfoListMap.entrySet()) {
            List<Meta.ModelConfig.ModelInfo> tempModelInfoList = entry.getValue();

            List<Meta.ModelConfig.ModelInfo> newModelInfoList = new ArrayList<>(tempModelInfoList.stream()
                    .flatMap(modelInfo -> modelInfo.getModels().stream())
                    .collect(
                            Collectors.toMap(Meta.ModelConfig.ModelInfo::getFieldName, o -> o, (e, r) -> r)
                    ).values());

            // 使用新的group配置
            Meta.ModelConfig.ModelInfo newModelInfo = CollUtil.getLast(tempModelInfoList);
            newModelInfo.setModels(newModelInfoList);
            String groupKey = entry.getKey();
            groupKeyMergedModelInfo.put(groupKey, newModelInfo);
        }

        //3. 创建新的文件配置列表，先将合并后的分组添加到结果列表
        ArrayList<Meta.ModelConfig.ModelInfo> resultList = new ArrayList<>(groupKeyMergedModelInfo.values());
        //4. 在将无分组文件配置列表添加到结果列表

        List<Meta.ModelConfig.ModelInfo> noGroupModelInfoList = modelInfoList.stream()
                .filter(modelInfo -> StrUtil.isBlank(modelInfo.getGroupKey()))
                .collect(Collectors.toList());

        resultList.addAll(new ArrayList<>(noGroupModelInfoList.stream()
                .collect(
                        Collectors.toMap(Meta.ModelConfig.ModelInfo::getFieldName, o -> o, (e, r) -> r)
                ).values()));
        return resultList;



    }



}
