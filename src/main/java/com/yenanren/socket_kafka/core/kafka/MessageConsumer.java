package com.yenanren.socket_kafka.core.kafka;

import cn.hutool.json.JSONUtil;
import com.yenanren.socket_kafka.config.TaskSchedulerConfig;
import com.yenanren.socket_kafka.constant.KafkaConst;
import com.yenanren.socket_kafka.manager.SessionManager;
import com.yenanren.socket_kafka.entity.Messages;
import com.yenanren.socket_kafka.util.GeneratorKeyUtil;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
                    StompHeaders headers = new StompHeaders();
                    headers.setDestination(conURL);
//                    headers.setReceipt(UUID.randomUUID().toString()); // 添加回执ID

                    if (session != null) {
                        session.send(headers, chatMessage);
                        heartBeat(conURL);
                        this.addToSetWithSlidingWindow(messages.getUuid()); // 记录ID
                        consumer.commitSync(); // 同步提交offset
                    }
                }
            });
        }
    }

    /**
     * 发送心跳包
     */
    private void heartBeat(String conURL) {
        ThreadPoolTaskScheduler scheduler = TaskSchedulerConfig.taskScheduler();
        new StompSessionHandlerAdapter() {
            private final AtomicBoolean receivedHeartbeatResponse = new AtomicBoolean(true);

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                // Handle transport errors here, e.g., when the WebSocket connection is lost
                scheduler.shutdown(); // Shut down the scheduler when an error occurs
            }

            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                // Schedule the heartbeat task after the connection is established
                scheduler.scheduleAtFixedRate(() -> {
                    if (session != null && session.isConnected()) {
                        if (!receivedHeartbeatResponse.get()) {
                            session.disconnect(); // Disconnect if no proper response received
                            scheduler.shutdown(); // Shut down the scheduler
                            return;
                        }

                        receivedHeartbeatResponse.set(false); // Reset the flag

                        StompHeaders headers = new StompHeaders();
                        headers.setDestination(conURL);
                        String uniqueHeartbeatId = UUID.randomUUID().toString();
//                        headers.set("heartbeat-id", uniqueHeartbeatId);
                        session.send(headers, "heartbeat"); // 发送心跳消息

                        // Listen for the response
                        session.subscribe(conURL, new StompFrameHandler() {
                            @Override
                            public Type getPayloadType(StompHeaders headers) {
                                return Messages.class;
                            }


                            @Override
                            public void handleFrame(StompHeaders headers, Object payload) {
                                Messages chatMessage = (Messages) payload;

                                if (chatMessage != null) {
                                    receivedHeartbeatResponse.set(true);
                                }
                            }
                        });
                    } else {
                        scheduler.shutdown(); // If the session is not connected, shut down the scheduler
                    }
                }, 5 * 1000);
            }
        };

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
