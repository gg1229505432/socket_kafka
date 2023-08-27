
package com.yenanren.socket_kafka.worker;

import com.yenanren.socket_kafka.webSocket.SessionManager;
import com.yenanren.socket_kafka.constant.WebSocketConst;
import com.yenanren.socket_kafka.webSocket.core.MyStompFrameHandler;
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
        job.setType("connected");
        job.setStatus("before");
    }

    @Override
    public void process(WebSocketJob job) {
        stompSession.send("/app/connectedNotification", job);  // 通知服务器我已连接
        stompSession.subscribe(job.getConURL(), new MyStompFrameHandler());

        job.setStatus("process");
    }

    @Override
    public void after(WebSocketJob job) {
        String conURL = job.getConURL();
        if (StringUtils.hasLength(conURL)) {
            if (SessionManager.getInstance().getSession(conURL) == null) {
                SessionManager.getInstance().addSession(conURL, stompSession);
            }
        }

        scheduler.schedule(() -> this.onDown(job), 3, TimeUnit.MINUTES);

        job.setStatus("after");
    }

    @Override
    public void onDown(WebSocketJob job) {
        String conURL = job.getConURL();
        job.setType("disconnected");
        stompSession.send("/app/disconnectedNotification", job);  // 通知服务器我已断连接

        if (stompSession != null && stompSession.isConnected()) {
            stompSession.disconnect();
        }
        if (stompClient != null) {
            stompClient.stop();
        }


        scheduler.shutdown(); // Make sure to shutdown the scheduler
        if (StringUtils.hasLength(conURL)) {
            SessionManager.getInstance().removeSession(conURL);
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
        chatMessage.setUserId(Integer.valueOf(job.getUserId()));
        chatMessage.setChatroomId(Integer.valueOf(job.getChatroomId()));

        if (stompSession.isConnected()) {
            stompSession.send(job.getConURL(), chatMessage);
        } else {
            System.out.println("Connection has already closed");
        }
    }


}
