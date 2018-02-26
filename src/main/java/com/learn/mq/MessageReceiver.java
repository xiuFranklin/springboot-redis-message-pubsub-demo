package com.learn.mq;

import org.springframework.stereotype.Component;

/**
 * redis 消息处理器
 * Created by chao.du on 2018/2/23.
 */
@Component
public class MessageReceiver {

    /**接收消息的方法*/
    public void receiveMessage(String message){
        System.out.println("收到一条消息："+message);
    }

}
