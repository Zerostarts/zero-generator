package com.star.pattern;


/**
 * 控制遥控器的客户端
 */
public class Client {

    public static void main(String[] args) {
        //创建一个接收者对象
        Device tv = new Device("TV");
        Device stereo = new Device("Stereo");

        TurnOnCommand turnOn = new TurnOnCommand(tv);
        TurnOffCommand turnOff = new TurnOffCommand(stereo);

        //创建调用者
        RemoteControl remote = new RemoteControl();

        remote.setCommand(turnOn);
        remote.pressButton();

        remote.setCommand(turnOff);
        remote.pressButton();


    }

}
