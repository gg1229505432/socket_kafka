package com.yenanren.socket_kafka.manager;

import com.yenanren.socket_kafka.config.TaskSchedulerConfig;
import com.yenanren.socket_kafka.constant.WebSocketConst;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private static volatile SessionManager instance;
    private volatile ConcurrentHashMap<String, StompSession> sessions;

    private SessionManager() {
        this.sessions = new ConcurrentHashMap<>();
        System.out.println("SessionManager created at: " + System.currentTimeMillis());
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }

    public void addSession(String key, StompSession session) {
        sessions.put(key, session);
    }

    public StompSession getSession(String key) {
        return sessions.get(key);
    }

    public void removeSession(String key) {
        sessions.remove(key);
    }


    // ==========================================================
    public static WebSocketStompClient stompClient = stompClient();

    public static WebSocketStompClient stompClient() {
        List<Transport> transports = Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);

        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        stompClient.setTaskScheduler(TaskSchedulerConfig.taskScheduler()); // 设置TaskScheduler , 为了异步开启 session.send(headers, chatMessage)
        return stompClient;
    }

    public static StompSession stompSession() {
        try {
            return stompClient.connect(WebSocketConst.CHAT_URL, new StompSessionHandlerAdapter() {
            }).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
