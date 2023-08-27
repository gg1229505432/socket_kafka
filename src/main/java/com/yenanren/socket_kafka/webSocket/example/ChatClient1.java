package com.yenanren.socket_kafka.webSocket.example;

import com.yenanren.socket_kafka.worker.WebSocketJob;
import com.yenanren.socket_kafka.worker.Worker;

import java.util.Scanner;

/**
 * 模拟客户端 1
 */
public class ChatClient1 {

    public static void main(String[] args) {
        String userId = "0"; // ggBOM
        String chatroomId = "1"; // originalGPT
        int isSelf = 1;
        Worker worker = new Worker();

        WebSocketJob job = new WebSocketJob(userId, chatroomId);
        try {
            worker.before(job);
            worker.process(job);
            worker.after(job);
        } catch (Exception e) {
            worker.onError(job);
            System.out.println(e.getMessage());
        }


        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your username: ");
        String username = scanner.nextLine();
        System.out.println("Start chatting! (Type 'exit' to disconnect)");

        String message;
        do {
            message = scanner.nextLine();
            if (!"exit".equalsIgnoreCase(message)) {
                worker.sendMessage(username, message, isSelf, job); // 发送消息到 Kafka 或者 websocket
            }
        } while (!"exit".equalsIgnoreCase(message));

        worker.onDown(job);
    }
}
