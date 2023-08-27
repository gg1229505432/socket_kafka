package com.yenanren.socket_kafka.controller;

import com.yenanren.socket_kafka.manager.SessionManager;
import com.yenanren.socket_kafka.core.websocket.MyStompFrameHandler;
import com.yenanren.socket_kafka.worker.WebSocketJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebSocketController {

    /**
     * 用来接收用户链接通知的
     */
    @MessageMapping("/connectedNotification")
    public void handleConnectedNotification(@Payload WebSocketJob job) {
        try {
            String type = job.getType();
            String conURL = job.getConURL();
            if ("connected".equals(type)) {
                // 放入 Spring 容器中 SessionManager
                StompSession stompSession = SessionManager.stompSession();
                stompSession.subscribe(conURL, new MyStompFrameHandler());
                SessionManager.getInstance().addSession(conURL, stompSession);

                System.out.println("User with ID " + job.getUserId() + " has connected." + conURL);
                // 此处可以添加其他逻辑，例如通知其他用户，存储日志等
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @MessageMapping("/disconnectedNotification")
    public void handleDisconnectedNotification(@Payload WebSocketJob job) {
        String type = job.getType();
        if ("disconnected".equals(type)) {
            // 从 Spring 容器中 SessionManager 取出 , 关闭 , 移除
            StompSession session = SessionManager.getInstance().getSession(job.getConURL());
            session.disconnect();
            SessionManager.getInstance().removeSession(job.getConURL());
        }
    }

//    @MessageMapping("/chat.sendMessage")
//    @SendTo("/topic/publicChatRoom")
//    public Messages sendMessage(@Payload Messages chatMessage) {
//        System.out.println("contoller : " + chatMessage.toString());
//        return chatMessage;
//    }


    /**
     * 中继聊天服务器
     */
//    @MessageMapping("/chat.sendMessage/{userId}/{chatroomId}")
//    @SendTo("/topic/chatrooms/{userId}_{chatroomId}")
//    public Messages sendMessage(@DestinationVariable Integer userId,
//                                @DestinationVariable Integer chatroomId,
//                                @Payload Messages chatMessage) {
//        System.out.println("contoller2 : " + chatMessage.toString());
//        return chatMessage;
//    }

}
