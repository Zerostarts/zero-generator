package com.star.maker.template;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.star.maker.meta.Meta;
import com.star.maker.meta.enums.FileGenerateTypeEnum;
import com.star.maker.meta.enums.FileTypeEnum;
import com.star.maker.template.enums.FileFilterRangeEnum;
import com.star.maker.template.enums.FileFilterRuleEnum;
import com.star.maker.template.model.FileFilterConfig;
import com.star.maker.template.model.TemplateMakerFileConfig;
import com.star.maker.template.model.TemplateMakerModelConfig;
import freemarker.template.utility.StringUtil;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class TemplateMarker {

    /**
     * 制作模板和meta
     * @param newMeta
     * @param originProjectPath   文件原始项目路径
     * @param //fileInputPathList 文件输入路径列表
     * @param id
     * @return
     */
    public static long makeTemplate(Meta newMeta, String originProjectPath, TemplateMakerFileConfig templateMakerFileConfig,
                                    TemplateMakerModelConfig templateMakerModelConfig, Long id) {
        if (null == id) {
            id = IdUtil.getSnowflakeNextId();
        }
        // 指定项目原始路
        String projectPath = System.getProperty("user.dir");

        // 复制目录
        String tempDirPath = projectPath + File.separator + ".temp";
        String templatePath = tempDirPath + File.separator + id;
        if (!FileUtil.exist(templatePath)) {
            FileUtil.mkdir(templatePath);
        }
        FileUtil.copy(originProjectPath, templatePath, true);

        // 一 输入信息

        //处理模型信息
        List<TemplateMakerModelConfig.ModelInfoConfig> models = templateMakerModelConfig.getModels();

        //转化为配置文件接收的modeInfo对象
        List<Meta.ModelConfig.ModelInfo> inputModelInfoList = models.stream()
                .map(modelInfoConfig -> {
                    Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
                    BeanUtil.copyProperties(modelInfoConfig, modelInfo);
                    return modelInfo;
                }).collect(Collectors.toList());

        // 本次新增的模型列表
        List<Meta.ModelConfig.ModelInfo> newModelInfoList = new ArrayList<>();

        //如果是模型组
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();
        if (modelGroupConfig != null) {
            String condition = modelGroupConfig.getCondition();
            String groupKey = modelGroupConfig.getGroupKey();
            String groupName = modelGroupConfig.getGroupName();
            // 设置meta中的文件组
            Meta.ModelConfig.ModelInfo groupModelInfo = new Meta.ModelConfig.ModelInfo();
            groupModelInfo.setCondition(condition);
            groupModelInfo.setGroupKey(groupKey);
            groupModelInfo.setGroupName(groupName);
            // 模型全放到一个分组内
            groupModelInfo.setModels(inputModelInfoList);
            //文件组属于一级文件配置
            newModelInfoList = new ArrayList<>();
            newModelInfoList.add(groupModelInfo);
        } else {
            newModelInfoList.addAll(inputModelInfoList);
        }

        // 2. 输入文件信息
        String sourRootPath = templatePath + File.separator + FileUtil.getLastPathEle(Paths.get(originProjectPath)).toString();
        sourRootPath = sourRootPath.replaceAll("\\\\", "/");

        List<Meta.FileConfig.FileInfo> newFileInfoList = new ArrayList<>();

        List<TemplateMakerFileConfig.FileInfoConfig> fileInfoConfigList = templateMakerFileConfig.getFiles();

        // 过滤 + 生成模板
        for (TemplateMakerFileConfig.FileInfoConfig fileInfoConfig : fileInfoConfigList) {
            // 获取文件的过滤信息
            String fileInputPath = fileInfoConfig.getPath();
            String inputFileAbsolutePath = sourRootPath + File.separator + fileInputPath;

            List<File> fileList = FileFilter.doFilter(inputFileAbsolutePath, fileInfoConfig.getFileFilterConfigList());
            fileList = fileList.stream()
                    .filter(file -> {
                        return file.getAbsolutePath().endsWith(".ftl");
                    }).collect(Collectors.toList());
            for (File file : fileList) {
                Meta.FileConfig.FileInfo fileInfo = makeFileTemplate(templateMakerModelConfig, sourRootPath, file);
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
            groupFileInfo.setCondition(condition);
            groupFileInfo.setGroupKey(groupKey);
            groupFileInfo.setGroupName(groupName);
            groupFileInfo.setFiles(newFileInfoList);

            //文件组属于一级文件配置
            newFileInfoList = new ArrayList<>();
            newFileInfoList.add(groupFileInfo);
        }


        // 三、生成配置文件
        String metaOutputPath = sourRootPath + File.separator + "meta.json";

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

            //2. 生成


        } else {
            // 构造输出的meta

            Meta.FileConfig fileConfig = new Meta.FileConfig();
            fileConfig.setSourceRootPath(sourRootPath);

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
        //输出元信息文件
        FileUtil.writeUtf8String(JSONUtil.toJsonStr(newMeta), metaOutputPath);
        return id;
    }

    /**
     * 制作单个模板
     * @param templateMakerModelConfig
     * @param sourRootPath
     * @param inputFile
     * @return
     */
    private static Meta.FileConfig.FileInfo makeFileTemplate(TemplateMakerModelConfig templateMakerModelConfig, String sourRootPath, File inputFile) {

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
        fileInfo.setInputPath(fileInputPath);
        fileInfo.setOutputPath(fileOutputPath);
        fileInfo.setType(FileTypeEnum.FILE.getValue());
        fileInfo.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());

        //是否更改了文件内容


        boolean contentEquals = fileContent.equals(newFileContent);
        //之前不尊在模板文件，并且这次替换没有修改文档的内容，才是静态生成
        if (!hasTemplateFile) {
            if (contentEquals) {
                fileInfo.setGenerateType(FileGenerateTypeEnum.STATIC.getValue());
                fileInfo.setOutputPath(fileInputPath);

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
                            Collectors.toMap(Meta.FileConfig.FileInfo::getInputPath, o -> o, (e, r) -> r)
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
                        Collectors.toMap(Meta.FileConfig.FileInfo::getInputPath, o -> o, (e, r) -> r)
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

        resultList.addAll(modelInfoList.stream()
                .filter(modelInfo -> StrUtil.isBlank(modelInfo.getGroupKey()))
                .collect(
                        Collectors.toMap(Meta.ModelConfig.ModelInfo::getFieldName, o -> o, (e, r) -> r)
                ).values());

        return resultList;

    }


    public static void main(String[] args) {

        // 1. 项目基本信息
        String name = "acm-template-pro-generator";
        String description = "ACM 示例模板生成器";


        // 指定项目原始路
        String projectPath = System.getProperty("user.dir");
        // 挖坑的项目
        String originProjectPath = FileUtil.getAbsolutePath(new File(projectPath).getParentFile() + File.separator + "zero-generator-demo-projects/springboot-init");
        // 文件
        String fileInputPath1 = "src/main/java/com/yupi/springbootinit/common";
        String fileInputPath2 = "src/main/java/com/yupi/springbootinit/config";

        // 输入模型参数信息

        Meta meta = new Meta();
        meta.setName(name);
        meta.setDescription(description);


//        Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
//        modelInfo.setFieldName("outputText");
//        modelInfo.setType("String");
//        modelInfo.setDefaultValue("sum = ");
//        String str = "Sum is ";




        //文件过滤配置
        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig1 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig1.setPath(fileInputPath1);
        List<FileFilterConfig> fileFilterConfigList1 = new ArrayList<>();
        FileFilterConfig fileFilterConfig = FileFilterConfig
                .builder()
                .range(FileFilterRangeEnum.FILE_NAME.getValue())
                .rule(FileFilterRuleEnum.CONTAINS.getValue())
                .value("Base")
                .build();
        fileFilterConfigList1.add(fileFilterConfig);
        fileInfoConfig1.setFileFilterConfigList(fileFilterConfigList1);



        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig2 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig2.setPath(fileInputPath2);

        List<TemplateMakerFileConfig.FileInfoConfig> fileInfoConfigList = Arrays.asList(fileInfoConfig1,fileInfoConfig2);
        TemplateMakerFileConfig templateMakerFileConfig = new TemplateMakerFileConfig();
        templateMakerFileConfig.setFiles(fileInfoConfigList);

        // 分组配置
        TemplateMakerFileConfig.FileGroupConfig fileGroupConfig = new TemplateMakerFileConfig.FileGroupConfig();
        fileGroupConfig.setCondition("outputText");
        fileGroupConfig.setGroupKey("test");
        fileGroupConfig.setGroupName("测试分组");
        templateMakerFileConfig.setFileGroupConfig(fileGroupConfig);

        TemplateMakerModelConfig templateMakerModelConfig = new TemplateMakerModelConfig();

        // 模板参数要挖坑的str， 挖成${classname}
        Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
        modelInfo.setFieldName("className");
        modelInfo.setType("String");
        String str = "BaseResponse";

        List<TemplateMakerModelConfig.ModelInfoConfig> modelInfoConfigList
                = TemplateMakerModelConfig.templateModelInfoConfigAdapter(Arrays.asList(modelInfo), str);

        templateMakerModelConfig.setModels(modelInfoConfigList);


        long id = makeTemplate(meta, originProjectPath, templateMakerFileConfig, templateMakerModelConfig, 1792188308500115456L);
        System.out.println(id);


    }
}
