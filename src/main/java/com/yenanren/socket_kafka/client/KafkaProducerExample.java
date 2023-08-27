package com.yenanren.socket_kafka.client;

import com.yenanren.socket_kafka.constant.KafkaConst;
import org.apache.kafka.clients.producer.*;

import java.util.Properties;

public class KafkaProducerExample {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", KafkaConst.KAFKA_HOST);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);
        for (int i = 0; i < 100; i++) {
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>(KafkaConst.TOPIC, Integer.toString(i), Integer.toString(i));
            producer.send(producerRecord);
        }

        producer.close();
    }
}