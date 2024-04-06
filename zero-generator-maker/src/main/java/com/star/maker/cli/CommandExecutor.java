package com.star.maker.cli;

import com.star.maker.cli.command.ConfigCommand;
import com.star.maker.cli.command.GenerateCommand;
import com.star.maker.cli.command.ListCommand;
import picocli.CommandLine;

/**
 * 命令执行器
 */
@CommandLine.Command(name = "zero", mixinStandardHelpOptions = true)
public class CommandExecutor implements Runnable{
    private final CommandLine commandLine;
    {
        commandLine = new CommandLine(this)
                .addSubcommand(new GenerateCommand())
                .addSubcommand(new ConfigCommand())
                .addSubcommand(new ListCommand());
    }
    @Override
    public void run() {
        //不输入子命令是，给出友好提示
        System.out.println("请输入具体命令， 或者输入 --help查看命令提示");
    }
    /**
     * 执行命令
     *
     * @param args
     * @return
     */
    public Integer doExecute(String[] args) {
        return commandLine.execute(args);
    }
}
