package com.example.app.chat_message;

import com.example.app.api_response.ApiResponse;
import com.example.app.chat_dto.ChatNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<String>> sendMessage(
            @RequestBody ChatNotification chatNotification
    )
    {

        chatMessageService.sendMessage(chatNotification);
        return ResponseEntity.ok(new ApiResponse<>(
                true,"message sent","message send successfully"));
    }

    @GetMapping("/messages/{sender}/{recipient}")
    public ResponseEntity<ApiResponse<List<ChatNotification>>> findChatMessages(
            @PathVariable String sender,
            @PathVariable String recipient)
    {
        List<ChatNotification> chatMessagesList
                = chatMessageService.findChatMessages(sender, recipient);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                chatMessagesList,
                "success")
        );
    }

}


