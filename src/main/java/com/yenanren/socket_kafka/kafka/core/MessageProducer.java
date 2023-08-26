package com.yenanren.socket_kafka.kafka.core;

import com.yenanren.socket_kafka.constant.KafkaConst;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.*;

import java.util.Properties;

public class MessageProducer {
    private static volatile MessageProducer instance; // 使用volatile确保多线程可见性
    private final Producer<String, String> producer;
    private final String topic;


    public MessageProducer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConst.KAFKA_HOST);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        this.producer = new KafkaProducer<>(props);
        this.topic = KafkaConst.TOPIC;
    }

    public static MessageProducer getInstance() {
        if (instance == null) { // 第一次检查
            synchronized (MessageProducer.class) { // 同步块
                if (instance == null) { // 第二次检查
                    instance = new MessageProducer();
                }
            }
        }
        return instance;
    }

    public void sendMessageToKafka(String message) {
        ProducerRecord<String, String> record = new ProducerRecord<>(this.topic, "callOpenAI", message);

        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                System.out.println(exception.getMessage());
                // Handle the exception here. Maybe log it or send a message back to user.
            }
        });
    }
}
