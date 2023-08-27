package com.yenanren.socket_kafka.core.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

@Service
public class MessageConsumerRunner implements ApplicationRunner {

    private final MessageConsumer messageConsumer;

    @Autowired
    public MessageConsumerRunner(MessageConsumer messageConsumer) {
        this.messageConsumer = messageConsumer;
    }

    @Override
    public void run(ApplicationArguments args) {
        messageConsumer.run();
    }
}
