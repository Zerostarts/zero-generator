package com.star.maker.generator.file;


import com.star.maker.model.DataModel;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

/**
 * 核心生成器
 */
public class MainGenerator {

    /**
     * 生成
     *
     * @param model 数据模型
     * @throws TemplateException
     * @throws IOException
     */
    public static void doGenerate(DataModel model) throws TemplateException, IOException {
        String projectPath = System.getProperty("user.dir");
        File parentFile = new File(projectPath).getParentFile();

        // 输入路径
        String inputPath = new File(parentFile, "zero-generator/zero-generator-demo-projects/acm-template").getAbsolutePath();
        String outputPath = projectPath;

        // 生成静态文件
        StaticFileGenerator.copyFilesByHutool(inputPath, outputPath);

        // 生成动态文件
        // 生成动态文件
        String inputDynamicFilePath = projectPath + File.separator + "zero-generator-maker/src/main/resources/templates/MainTemplate.java.ftl";
        String outputDynamicFilePath = outputPath + File.separator + "acm-template/src/com/star/acm/MainTemplate.java";
        DynamicFileGenerator.doGenerate(inputDynamicFilePath, outputDynamicFilePath, model);

    }

}
