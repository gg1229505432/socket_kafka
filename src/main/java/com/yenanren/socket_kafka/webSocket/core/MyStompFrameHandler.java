package com.yenanren.socket_kafka.webSocket.core;

import cn.hutool.json.JSONUtil;
import com.yenanren.socket_kafka.kafka.core.MessageProducer;
import com.yenanren.socket_kafka.entity.Messages;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class MyStompFrameHandler implements StompFrameHandler {

    private MessageProducer messageProducer = MessageProducer.getInstance();

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return Messages.class;
    }


    /**
     * 用户接受到消息
     */
    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        Messages chatMessage = (Messages) payload;
        if (chatMessage.getIsSelf() == 1) {
            messageProducer.sendMessageToKafka(JSONUtil.toJsonStr(chatMessage));
        }

        System.out.println(chatMessage.getUsername() + ": " + chatMessage.getContent());
    }

}
