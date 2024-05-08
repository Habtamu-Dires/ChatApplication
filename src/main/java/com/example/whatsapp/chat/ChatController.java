package com.example.whatsapp.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatMessageService chatMessageService;
    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody ChatNotification chatNotification){
        chatMessageService.sendMessage(chatNotification);
        return ResponseEntity.ok("send");
    }

    @GetMapping("/messages/{sender}/{recipient}")
    public ResponseEntity<List<ChatNotification>> findChatMessages(
            @PathVariable String sender,
            @PathVariable String recipient)
    {
        return ResponseEntity
                .ok(chatMessageService.findChatMessages(sender, recipient));
    }

}


