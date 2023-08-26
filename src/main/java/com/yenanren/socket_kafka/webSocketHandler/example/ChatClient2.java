package com.yenanren.socket_kafka.webSocketHandler.example;

import com.yenanren.socket_kafka.worker.WebSocketJob;
import com.yenanren.socket_kafka.worker.Worker;

import java.util.Scanner;

/**
 * 模拟客户端 2
 */
public class ChatClient2 {

    public static void main(String[] args) {
        String userId = "someUserId"; // ggBOM
        String chatroomId = "someChatroomId"; // originalGPT
        int isSelf = 0;
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
                worker.sendMessage(username, message, isSelf, job);
            }
        } while (!"exit".equalsIgnoreCase(message));

        worker.onDown(job);
    }
}
