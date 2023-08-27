package com.yenanren.socket_kafka.kafka.core;

import cn.hutool.json.JSONUtil;
import com.yenanren.socket_kafka.constant.KafkaConst;
import com.yenanren.socket_kafka.webSocket.SessionManager;
import com.yenanren.socket_kafka.entity.Messages;
import com.yenanren.socket_kafka.util.GeneratorKeyUtil;
import com.yenanren.socket_kafka.worker.WebSocketJob;
import com.yenanren.socket_kafka.worker.Worker;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.*;

@Service
public class MessageConsumer {

    private static volatile MessageConsumer instance; // 使用volatile确保多线程可见性
    private final Consumer<String, String> consumer;
    private static Deque<String> slidingWindow = new LinkedList<>();


    public MessageConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConst.KAFKA_HOST);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConst.GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        // 设置max.poll.interval.ms，这里我们设置为5分钟，你可以根据你的需求进行调整
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 5 * 60 * 1000);

        // 设置max.poll.records，比如这里我们设置为10，这意味着一次poll()最多只会返回10条记录
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(KafkaConst.TOPIC));
    }


    public static MessageConsumer getInstance() {
        if (instance == null) { // 第一次检查
            synchronized (MessageConsumer.class) { // 同步块
                if (instance == null) { // 第二次检查
                    instance = new MessageConsumer();
                }
            }
        }
        return instance;
    }

    @Async
    public void run() {

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

            records.forEach(record -> {
                Messages messages = JSONUtil.toBean(record.value(), Messages.class);
                if (messages != null && !slidingWindow.contains(messages.getUuid())) {

                    Messages chatMessage = mockMessage(messages);

                    String conURL = GeneratorKeyUtil.makeKey(messages.getUserId(), messages.getChatroomId());
                    StompSession session = SessionManager.getInstance().getSession(conURL);
                    if (session != null) {
                        StompSession.Receiptable receipt = session.send(conURL, chatMessage);

                        // 添加一个钩子来监听回执
                        receipt.addReceiptLostTask(() -> {
                            System.err.println("Receipt for message lost. Message might not have been delivered.");
                            // 进一步的处理，例如重试发送或记录此事件
                        });

                        receipt.addReceiptTask(() -> {
                            System.out.println("Receipt for message received. Message was successfully delivered.");
                            // 例如，可以在此处确认消息已被成功处理，然后提交Kafka的offset
                            this.addToSetWithSlidingWindow(messages.getUuid()); // 记录ID
                            consumer.commitSync(); // 同步提交offset
                        });
                    }
                }
            });
        }
    }

    private Messages mockMessage(Messages messages) {
        String response = callOpenAIApi(messages.getContent());

        Messages chatMessage = new Messages();
        chatMessage.setUsername("AI");
        chatMessage.setContent(response);
        chatMessage.setIsSelf(0);
        chatMessage.setUserId(messages.getUserId());
        chatMessage.setChatroomId(messages.getChatroomId());

        return chatMessage;
    }

    private void addToSetWithSlidingWindow(String uuid) {
        slidingWindow.addLast(uuid);

        while (slidingWindow.size() > 10000) {
            slidingWindow.removeFirst();
        }
    }

    private String callOpenAIApi(String message) {
        // 这里是你的OpenAI API调用逻辑
        // 返回API的响应
        return "假设是AI的回复";
    }


}
