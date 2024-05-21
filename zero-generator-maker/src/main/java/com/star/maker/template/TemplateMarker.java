package com.star.maker.template;

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
import com.star.maker.template.model.TemplateMarkFileConfig;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class TemplateMarker {

    /**
     * 制作末班和meta
     * @param newMeta
     * @param originProjectPath   文件原始项目路径
     * @param //fileInputPathList 文件输入路径列表
     * @param modelInfo
     * @param searchStr
     * @param id
     * @return
     */
    public static long makeTemplate(Meta newMeta, String originProjectPath, TemplateMarkFileConfig templateMarkFileConfig,
                                    Meta.ModelConfig.ModelInfo modelInfo, String searchStr, Long id) {
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

        // 2. 输入文件信息
        String sourRootPath = templatePath + File.separator + FileUtil.getLastPathEle(Paths.get(originProjectPath)).toString();
        sourRootPath = sourRootPath.replaceAll("\\\\", "/");

        List<Meta.FileConfig.FileInfo> newFileInfoList = new ArrayList<>();

        List<TemplateMarkFileConfig.FileInfoConfig> fileInfoConfigList = templateMarkFileConfig.getFiles();

        // 过滤 + 生成模板
        for (TemplateMarkFileConfig.FileInfoConfig fileInfoConfig : fileInfoConfigList) {
            // 获取文件的过滤信息
            String fileInputPath = fileInfoConfig.getPath();
            String inputFileAbsolutePath = sourRootPath + File.separator + fileInputPath;

            List<File> fileList = FileFilter.doFilter(inputFileAbsolutePath, fileInfoConfig.getFileFilterConfigList());
            for (File file : fileList) {
                Meta.FileConfig.FileInfo fileInfo = makeFileTemplate(modelInfo, searchStr, sourRootPath, file);
                newFileInfoList.add(fileInfo);
            }

        }



        // 三、生成配置文件
        String metaOutputPath = sourRootPath + File.separator + "meta.json";

        // 已经有meta，则直接修改原有的配置文件
        if (FileUtil.exist(metaOutputPath)) {

            Meta oldMeta = JSONUtil.toBean(FileUtil.readUtf8String(metaOutputPath), Meta.class);
            //1. 在旧有的meta上存储接着存放要生成的model
            List<Meta.FileConfig.FileInfo> fileInfoList = oldMeta.getFileConfig().getFiles();
            fileInfoList.addAll(newFileInfoList);

            List<Meta.ModelConfig.ModelInfo> modelInfoList = oldMeta.getModelConfig().getModels();
            modelInfoList.add(modelInfo);
            // 配置去重
            oldMeta.getModelConfig().setModels(distinctModels(modelInfoList));
            oldMeta.getFileConfig().setFiles(distinctFiles(fileInfoList));

            //2. 生成
            FileUtil.writeUtf8String(JSONUtil.toJsonStr(oldMeta), metaOutputPath);

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
            modelInfoList.add(modelInfo);

            //输出元信息文件
            FileUtil.writeUtf8String(JSONUtil.toJsonStr(newMeta), metaOutputPath);

        }
        return id;
    }

    /**
     * 制作单个模板
     * @param modelInfo
     * @param searchStr
     * @param sourRootPath
     * @param inputFile
     * @return
     */
    private static Meta.FileConfig.FileInfo makeFileTemplate(Meta.ModelConfig.ModelInfo modelInfo, String searchStr, String sourRootPath, File inputFile) {

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
        if (FileUtil.exist(fileOutputAbsolutePath)) {
            fileContent = FileUtil.readUtf8String(fileOutputAbsolutePath);;
        } else {
            fileContent = FileUtil.readUtf8String(fileInputAbsolutePath);;
        }


        String replacement = String.format("${%s}", modelInfo.getFieldName());
        String newFileContent = StrUtil.replace(fileContent, searchStr, replacement);
        // 输出模板文件

        //文件配置信息（具体的文件）
        Meta.FileConfig.FileInfo fileInfo = new Meta.FileConfig.FileInfo();
        fileInfo.setInputPath(fileInputPath);
        fileInfo.setType(FileTypeEnum.FILE.getValue());


        if (fileContent.equals(newFileContent)) {
            fileInfo.setGenerateType(FileGenerateTypeEnum.STATIC.getValue());
            fileInfo.setOutputPath(fileInputPath);
        } else {
            fileInfo.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());
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
        Collection<Meta.FileConfig.FileInfo> values = fileInfoList.stream().collect(Collectors.toMap(Meta.FileConfig.FileInfo::getInputPath, o -> o, (e, r) -> r)).values();

        return new ArrayList<>(values);
    }


    /**
     * 模型去重
     * @param modelInfoList
     * @return
     */
    public static List<Meta.ModelConfig.ModelInfo> distinctModels(List<Meta.ModelConfig.ModelInfo> modelInfoList) {

        return new ArrayList<>(modelInfoList.stream().collect(Collectors.toMap(Meta.ModelConfig.ModelInfo::getFieldName, o -> o, (e, r) -> r)).values());
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
        String fileInputPath2 = "src/main/java/com/yupi/springbootinit/controller";

        // 输入模型参数信息

        Meta meta = new Meta();
        meta.setName(name);
        meta.setDescription(description);


//        Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
//        modelInfo.setFieldName("outputText");
//        modelInfo.setType("String");
//        modelInfo.setDefaultValue("sum = ");
//        String str = "Sum is ";

        // 模板参数要挖坑的str， 挖成${classname}
        Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
        modelInfo.setFieldName("className");
        modelInfo.setType("String");
        String str = "BaseResponse";


        //文件顾虑配置
        TemplateMarkFileConfig.FileInfoConfig fileInfoConfig1 = new TemplateMarkFileConfig.FileInfoConfig();
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



        TemplateMarkFileConfig.FileInfoConfig fileInfoConfig2 = new TemplateMarkFileConfig.FileInfoConfig();
        fileInfoConfig2.setPath(fileInputPath2);

        List<TemplateMarkFileConfig.FileInfoConfig> fileInfoConfigList = Arrays.asList(fileInfoConfig1,fileInfoConfig2);
        TemplateMarkFileConfig templateMarkFileConfig = new TemplateMarkFileConfig();
        templateMarkFileConfig.setFiles(fileInfoConfigList);


        long id = makeTemplate(meta, originProjectPath, templateMarkFileConfig, modelInfo, str, 1792188308500115456L);
        System.out.println(id);


    }
}
