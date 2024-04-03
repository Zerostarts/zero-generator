package com.star.generator;

import cn.hutool.core.io.FileUtil;
import cn.hutool.extra.template.TemplateException;
import com.star.model.MainTemplateConfig;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * 生成动态文件
 */
public class DynamicGenerator {


    public static void main(String[] args) throws freemarker.template.TemplateException, IOException {
        String projectPath = System.getProperty("user.dir");
        System.out.println(projectPath);
        String inputPath = projectPath + File.separator + "zero-generator-basic/src/main/resources/templates/MainTemplate.java.ftl";
        String outputPath = projectPath + File.separator + "MainTemplate.java";

        // 加载模板文件，创建 FreeMarker 模板对象
        MainTemplateConfig mainTemplateConfig = new MainTemplateConfig();
        mainTemplateConfig.setAuthor("yupi");
        mainTemplateConfig.setLoop(false);
        mainTemplateConfig.setOutputText("求和结果：");

        doGenerate(inputPath, outputPath, mainTemplateConfig);
        System.out.println(inputPath);
    }


    /**
     * 生成文件
     *
     * @param inputPath 模板文件输入路径
     * @param outputPath 输出路径
     * @param model 数据模型
     * @throws IOException
     * @throws TemplateException
     */
    public static void doGenerate(String inputPath, String outputPath, Object model) throws IOException, TemplateException, freemarker.template.TemplateException {
        // new 出 Configuration 对象，参数为 FreeMarker 版本号
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);
        // 指定模板文件所在的路径
        File templateDir = new File(inputPath).getParentFile();
        configuration.setDirectoryForTemplateLoading(templateDir);
        // 设置模板文件使用的字符集
        configuration.setDefaultEncoding("utf-8");
        //c创建模板对象，加载指定模板
        String templateName = new File(inputPath).getName();
        Template template = configuration.getTemplate(templateName);



        // 文件不存在则创建文件和父目录
        if (!FileUtil.exist(outputPath)) {
            FileUtil.touch(outputPath);
        }
        //生成
        Writer out = new FileWriter(outputPath);
        template.process(model, out);

        out.close();

    }


}
