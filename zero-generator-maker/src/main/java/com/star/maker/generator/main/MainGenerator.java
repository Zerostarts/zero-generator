package com.star.maker.generator.main;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.StrUtil;
import com.star.maker.generator.JarGenerator;
import com.star.maker.generator.ScriptGenerator;
import com.star.maker.generator.file.DynamicFileGenerator;
import com.star.maker.meta.Meta;
import com.star.maker.meta.MetaManager;
import com.star.maker.meta.MetaValidator;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

public class MainGenerator extends GenerateTemplate{
//    public static void main(String[] args) throws TemplateException, IOException, InterruptedException {
//        Meta meta = MetaManager.getMetaObject();
//        MetaValidator.validate(meta);
//        System.out.println(meta);
//
//        //输出根路径
//        String projectPath = System.getProperty("user.dir");
//        String outputPath = projectPath + File.separator + "generated" + File.separator + meta.getName();
//        if (!FileUtil.exist(outputPath)) {
//            FileUtil.mkdir(outputPath);
//        }
//        System.out.println(outputPath);
//
//        //复制原始文件 (可移植性保证)
//        String sourceRootPath = meta.getFileConfig().getSourceRootPath();
//        String sourceCopyDestPath = outputPath + File.separator + ".source";
//        FileUtil.copy(sourceRootPath, sourceCopyDestPath, true);
//
//        //读取resource目录
//        ClassPathResource classPathResource = new ClassPathResource("");
//        String inputResourcePath = classPathResource.getAbsolutePath();
//        //Java包基础路径
//
//        String outputBasePackage = meta.getBasePackage();
//        String outputBasePackagePath = StrUtil.join("/", StrUtil.split(outputBasePackage, "."));
//
//        String outputBaseJavaPackagePath = outputPath + File.separator + "src/main/java"+ File.separator + outputBasePackagePath;
//
//        String inputFilePath;
//        String outputFilePath;
//
//        //模型类
//        inputFilePath = inputResourcePath + File.separator + "templates/java/model/DataModel.java.ftl";
//        outputFilePath = outputBaseJavaPackagePath + File.separator + "/model/DataModel.java";
//        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);
//
//
//        //command 类
//        inputFilePath = inputResourcePath + File.separator + "templates/java/cli/command/GenerateCommand.java.ftl";
//        outputFilePath = outputBaseJavaPackagePath + File.separator + "cli/command/GenerateCommand.java";
//        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);
//
//        inputFilePath = inputResourcePath + File.separator + "templates/java/cli/command/ListCommand.java.ftl";
//        outputFilePath = outputBaseJavaPackagePath + File.separator + "cli/command/ListCommand.java";
//        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);
//
//        inputFilePath = inputResourcePath + File.separator + "templates/java/cli/command/ConfigCommand.java.ftl";
//        outputFilePath = outputBaseJavaPackagePath + File.separator + "cli/command/ConfigCommand.java";
//        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);
//
//        // commandExecutor 类
//        inputFilePath = inputResourcePath + File.separator + "templates/java/cli/CommandExecutor.java.ftl";
//        outputFilePath = outputBaseJavaPackagePath + File.separator + "cli/CommandExecutor.java";
//        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);
//
//        //动态生成文件 类
//        inputFilePath = inputResourcePath + File.separator + "templates/java/generator/DynamicGenerator.java.ftl";
//        outputFilePath = outputBaseJavaPackagePath + File.separator + "generator/DynamicGenerator.java";
//        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);
//
//        //静态生成文件 类
//        inputFilePath = inputResourcePath + File.separator + "templates/java/generator/StaticGenerator.java.ftl";
//        outputFilePath = outputBaseJavaPackagePath + File.separator + "generator/StaticGenerator.java";
//        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);
//
//        //总生成 类
//        inputFilePath = inputResourcePath + File.separator + "templates/java/generator/MainGenerator.java.ftl";
//        outputFilePath = outputBaseJavaPackagePath + File.separator + "generator/MainGenerator.java";
//        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);
//
//        //pom文件
//        inputFilePath = inputResourcePath + File.separator + "templates/pom.xml.ftl";
//        outputFilePath = outputPath + File.separator + "pom.xml";
//        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);
//        //入口类
//        inputFilePath = inputResourcePath + File.separator + "templates/java/Main.java.ftl";
//        outputFilePath = outputBaseJavaPackagePath + File.separator + "Main.java";
//        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);
//
//        //生成README.md
//        inputFilePath = inputResourcePath + File.separator + "templates/README.md.ftl";
//        outputFilePath = outputPath + File.separator + "README.md";
//        DynamicFileGenerator
//                .doGenerate(inputFilePath, outputFilePath, meta);
//
//        //构建jar包
//        JarGenerator.doGenerate(outputPath);
//
//        //构建脚本
//        String shellOutputPath = outputPath + File.separator + "generator";
//        String jarName = String.format("%s-%s-jar-with-dependencies.jar", meta.getName(),meta.getVersion());
//        String jarPath = "target/"+ jarName;
//        ScriptGenerator.doGenerate(shellOutputPath, jarPath);
//
//    }


    @Override
    protected void buildDist(String outputPath, String sourceCopyDestPath, String jarPath, String shellOutputFilePath) {
        System.out.println("不要给我输出 dist 啦！");
    }


}
