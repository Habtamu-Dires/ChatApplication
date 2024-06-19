package com.example.whatsapp.kafka_config;

import com.example.whatsapp.chat_dto.ChatNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@RequiredArgsConstructor
@Service
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    //send to whatsapp-topic in kafka
    public void sendMessage(Object notification){
        Message<Object> message = MessageBuilder
                .withPayload(notification)
                .setHeader(KafkaHeaders.TOPIC, "whatsapp-topic")
                .setHeader("messageType", "chatNotification")
                .build();
        kafkaTemplate.send(message);
    }

}
