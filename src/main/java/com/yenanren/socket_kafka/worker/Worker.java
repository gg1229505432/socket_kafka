
package com.yenanren.socket_kafka.worker;

import com.yenanren.socket_kafka.webSocketHandler.SessionManager;
import com.yenanren.socket_kafka.constant.WebSocketConst;
import com.yenanren.socket_kafka.webSocketHandler.core.MyStompFrameHandler;
import com.yenanren.socket_kafka.entity.Messages;
import com.yenanren.socket_kafka.util.GeneratorKeyUtil;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Worker implements WorkerInterface<WebSocketJob> {
    private final String URL;
    private final ScheduledExecutorService scheduler;
    private WebSocketStompClient stompClient;
    private StompSession stompSession;

    public Worker() {
        try {
            this.URL = WebSocketConst.CHAT_URL;

            this.scheduler = Executors.newScheduledThreadPool(1);

            this.stompClient = new WebSocketStompClient(new SockJsClient(
                    Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()))
            ));
            this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());

            this.stompSession = stompClient.connect(URL, new StompSessionHandlerAdapter() {
            }).get();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void before(WebSocketJob job) {
        String userId = job.getUserId();
        String chatroomId = job.getChatroomId();
        String conURL;
        if (StringUtils.hasLength(userId) && StringUtils.hasLength(chatroomId)) {
            conURL = "/topic/chatrooms/" + userId + "_" + chatroomId;
        } else {
            conURL = "/topic/publicChatRoom";
        }
        job.setConURL(conURL);

        Map<String, String> connectedMessage = new HashMap<>();
        connectedMessage.put("type", "connected");
        connectedMessage.put("userId", userId);
        connectedMessage.put("chatroomId", chatroomId);
        job.setConnectedMessage(connectedMessage);

        job.setStatus("before");
    }

    @Override
    public void process(WebSocketJob job) {
        Map<String, String> connectedMessage = job.getConnectedMessage();
        String conURL = job.getConURL();

        stompSession.send("/app/connectedNotification", connectedMessage);  // 通知服务器我已连接
        stompSession.subscribe(conURL, new MyStompFrameHandler());

        job.setStatus("process");
    }

    @Override
    public void after(WebSocketJob job) {
        String conURL = job.getConURL();
        if (StringUtils.hasLength(conURL)) {
            if (SessionManager.getSession(conURL) == null) {
                SessionManager.addSession(conURL, stompSession);
            }
        }
        System.out.println(SessionManager.sessions);

        scheduler.schedule(() -> this.onDown(job), 1, TimeUnit.MINUTES);

        job.setStatus("after");
    }

    @Override
    public void onDown(WebSocketJob job) {
        String conURL = job.getConURL();
        if (stompSession != null && stompSession.isConnected()) {
            stompSession.disconnect();
        }
        if (stompClient != null) {
            stompClient.stop();
        }

        Map<String, String> disconnectedMessage = new HashMap<>();
        disconnectedMessage.put("type", "disconnected");
        disconnectedMessage.putAll(GeneratorKeyUtil.parseKeyToMap(conURL)); // 放 user chatroomId 用的
        stompSession.send("/app/disconnectedNotification", disconnectedMessage);  // 通知服务器我已断连接

        scheduler.shutdown(); // Make sure to shutdown the scheduler
        if (StringUtils.hasLength(conURL)) {
            SessionManager.removeSession(conURL);
        }

        job.setStatus("onDown");
    }

    @Override
    public void onError(WebSocketJob job) {
        if (ObjectUtils.nullSafeEquals(job.getStatus(), "before")) {
//            process(job);
        } else if (ObjectUtils.nullSafeEquals(job.getStatus(), "process")) {
//            after(job);
        } else if (ObjectUtils.nullSafeEquals(job.getStatus(), "after")) {
//            onDown(job);
        } else if (ObjectUtils.nullSafeEquals(job.getStatus(), "onDown")) {

        } else {
            throw new RuntimeException("Unknown WebSocketJob Error");
        }
        throw new RuntimeException("Unknown WebSocketJob Error: " + job);

    }

    public void sendMessage(String sender, String content, int isSelf, WebSocketJob job) {
        Messages chatMessage = new Messages();
        chatMessage.setUsername(sender);
        chatMessage.setContent(content);
        chatMessage.setIsSelf(isSelf);

        String sendURL = job.getConURL();

        if (stompSession.isConnected()) {
            stompSession.send(sendURL, chatMessage);
        } else {
            System.out.println("Connection has already closed");
        }
    }


}
