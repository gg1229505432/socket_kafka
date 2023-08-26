package com.yenanren.socket_kafka.kafka.core;

import cn.hutool.json.JSONUtil;
import com.yenanren.socket_kafka.constant.KafkaConst;
import com.yenanren.socket_kafka.webSocketHandler.SessionManager;
import com.yenanren.socket_kafka.entity.Messages;
import com.yenanren.socket_kafka.util.GeneratorKeyUtil;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

@Service
public class MessageConsumer implements Runnable {
    @PostConstruct
    public void init() {
        new Thread(this::run).start();
    }

    private static volatile MessageConsumer instance; // 使用volatile确保多线程可见性
    private final Consumer<String, String> consumer;

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

    @Override
    public void run() {
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            records.forEach(record -> {
                // 假设这是调用OpenAI API的方法

                Messages messages = JSONUtil.toBean(record.value(), Messages.class);
                if (messages != null) {

                    String response = callOpenAIApi(messages.getContent());

                    String conURL = GeneratorKeyUtil.makeKey(messages.getUserId(), messages.getChatroomId());
                    StompSession session = SessionManager.getSession(conURL);
                    if (session != null) {
                        session.send(conURL, response); // 发送到聊天室
                    }
                }
            });
        }
    }

    private String callOpenAIApi(String message) {
        // 这里是你的OpenAI API调用逻辑
        // 返回API的响应
        return "假设是AI的回复";
    }


}
