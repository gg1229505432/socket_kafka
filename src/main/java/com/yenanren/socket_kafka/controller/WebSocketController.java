package com.yenanren.socket_kafka.controller;

import com.yenanren.socket_kafka.entity.Messages;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class WebSocketController {

    /**
     * 用来接收用户链接通知的
     */
    @MessageMapping("/connectedNotification")
    public void handleConnectedNotification(@Payload Map<String, String> message) {
        String type = message.get("type");
        String userId = message.get("userId");

        if ("connected".equals(type)) {
            System.out.println("User with ID " + userId + " has connected.");
            // 此处可以添加其他逻辑，例如通知其他用户，存储日志等
        }
    }


    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/publicChatRoom")
    public Messages sendMessage(@Payload Messages chatMessage) {
        System.out.println("contoller : " + chatMessage.toString());
        return chatMessage;
    }

    /**
     * 中继聊天服务器
     */
    @MessageMapping("/chat.sendMessage/{userId}/{chatroomId}")
    @SendTo("/topic/chatrooms/{userId}_{chatroomId}")
    public Messages sendMessage(@DestinationVariable String userId,
                                @DestinationVariable String chatroomId,
                                @Payload Messages chatMessage) {
        System.out.println("contoller2 : " + chatMessage.toString());
        return chatMessage;
    }

}
