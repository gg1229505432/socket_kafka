package com.yenanren.socket_kafka.webSocketHandler;

import org.springframework.messaging.simp.stomp.StompSession;

import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    // 使用ConcurrentHashMap存储所有的WebSocket sessions
    public static volatile ConcurrentHashMap<String, StompSession> sessions = new ConcurrentHashMap<>();

    public static void addSession(String key, StompSession session) {
        sessions.put(key, session);
    }

    public static StompSession getSession(String key) {
        return sessions.get(key);
    }

    public static void removeSession(String key) {
        sessions.remove(key);
    }
}
