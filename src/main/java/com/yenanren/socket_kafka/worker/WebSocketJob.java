package com.yenanren.socket_kafka.worker;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class WebSocketJob {
    private String userId;
    private String chatroomId;
    private String conURL;
    private Map<String, String> connectedMessage;
    private Map<String, String> disconnectedMessage;
    private String status;

    public WebSocketJob(String userId, String chatroomId) {
        this.userId = userId;
        this.chatroomId = chatroomId;
    }
}
