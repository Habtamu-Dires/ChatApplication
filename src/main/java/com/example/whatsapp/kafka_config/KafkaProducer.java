package com.example.whatsapp.kafka_config;

import com.example.whatsapp.chat.ChatNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    //send to whatsapp-topic in kafka
    public void sendMessage(ChatNotification chatNotification){
        Message<ChatNotification> message = MessageBuilder
                .withPayload(chatNotification)
                .setHeader(KafkaHeaders.TOPIC, "whatsapp-topic")
                .build();
        kafkaTemplate.send(message);
    }

}
