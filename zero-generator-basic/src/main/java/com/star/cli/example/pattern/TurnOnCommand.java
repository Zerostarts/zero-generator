package com.star.cli.example.pattern;

public class TurnOnCommand implements Command{
    private Device device;

    public TurnOnCommand(Device device) {
        this.device = device;
    }

    @Override
    public void execute() {
        // 执行打开操作的命令
        device.turnOn();
    }
}
