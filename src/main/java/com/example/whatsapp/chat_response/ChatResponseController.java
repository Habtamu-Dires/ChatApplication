package com.example.whatsapp.chat_response;

import com.example.whatsapp.chat.ChatMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat-response")
@RequiredArgsConstructor
public class ChatResponseController {

    private final ChatResponseService chatResponseService;

    @GetMapping("/{chatMessageId}")
    public ResponseEntity<List<ChatResponse>> getChatResponses(
           @PathVariable("chatMessageId") Long chatMessageId){

        return ResponseEntity.ok(chatResponseService.getChatResponses(chatMessageId));
    }
}
