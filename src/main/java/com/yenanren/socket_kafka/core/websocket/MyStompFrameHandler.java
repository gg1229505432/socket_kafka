package com.yenanren.socket_kafka.core.websocket;

import cn.hutool.json.JSONUtil;
import com.yenanren.socket_kafka.core.kafka.MessageProducer;
import com.yenanren.socket_kafka.entity.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

public class MyStompFrameHandler implements StompFrameHandler {

    private final MessageProducer messageProducer = MessageProducer.getInstance();

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return Messages.class;
    }


    /**
     * 用户接受到消息
     */
    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        System.out.println("headers : " + headers);
        Messages chatMessage = (Messages) payload;
        if (chatMessage.getIsSelf() == 1) {
            messageProducer.sendMessageToKafka(JSONUtil.toJsonStr(chatMessage));
        }

        System.out.println(chatMessage.getUsername() + ": " + chatMessage.getContent());
    }

}
