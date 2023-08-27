package com.yenanren.socket_kafka.worker;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.messaging.simp.stomp.StompSession;

import java.util.Map;

@Data
@NoArgsConstructor
public class WebSocketJob {
    private String userId;
    private String chatroomId;
    private String conURL;
    private String type;
    private String status;
    private StompSession stompSession;

    public WebSocketJob(String userId, String chatroomId) {
        this.userId = userId;
        this.chatroomId = chatroomId;
    }
}
