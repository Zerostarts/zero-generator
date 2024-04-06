package com.star.maker.cli.command;

import cn.hutool.core.bean.BeanUtil;
import com.star.maker.generator.file.MainGenerator;
import com.star.maker.model.DataModel;
import lombok.Data;
import picocli.CommandLine;

import java.util.concurrent.Callable;
@Data
@CommandLine.Command(name="generate", mixinStandardHelpOptions = true, description = "生成代码")
public class GenerateCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"-l", "--loop"}, arity = "0..1", description = "是否循环", interactive = true, echo = true)
    private boolean loop;

    @CommandLine.Option(names = {"-DynamicGenerator.java.ftl", "--author"}, arity = "0..1", description = "作者", interactive = true, echo = true)
    private String author = "star";

    @CommandLine.Option(names = {"-o", "--outputText"}, arity = "0..1", description = "输出文本", interactive = true, echo = true)
    private String outputText = "sum = ";


    @Override
    public Integer call() throws Exception {
        DataModel dataModel = new DataModel();
        BeanUtil.copyProperties(this, dataModel);
        MainGenerator.doGenerate(dataModel);
        return 0;
    }
}
