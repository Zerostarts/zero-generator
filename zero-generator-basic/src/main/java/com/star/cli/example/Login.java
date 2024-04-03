package com.star.cli.example;

import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Option;

/**
 * 交互式的命令行参数(若交互式命令)
 * arity = "0..1" 表示参数是可选的0 或者 1
 * interactive = true 表示在执行命令时，需要与用户交互
 */
public class Login implements Callable<Integer> {

    @Option(names = {"-u", "--user"}, description = "User name")
    String user;

    @Option(names = {"-p", "--password"}, required = true, arity = "0..1",description = "Passphrase", interactive = true)
    String password;

    // 设置了 arity 参数，可选交互式
    @Option(names = {"-cp", "--checkPassword"}, arity = "0..1", description = "Check Password", interactive = true)
    String checkPassword;

    @Override
    public Integer call() throws Exception {
        System.out.println("password = " + password);
        System.out.println("checkPassword = "+ checkPassword);
        return 0;
    }

    public static void main(String[] args) {
        new CommandLine(new Login()).execute("-u", "user123", "-p" , "-cp");
        //new CommandLine(new Login()).execute("-u", "user123" , "-cp");
    }
}
