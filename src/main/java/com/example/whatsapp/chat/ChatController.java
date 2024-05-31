package com.example.whatsapp.chat;

import com.example.whatsapp.api_response.ApiResponse;
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
public class ChatController {

    private final ChatMessageService chatMessageService;
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<String>> sendMessage(
            @RequestBody ChatNotification chatNotification)
    {
        String filePath = chatNotification.attachmentPath();
        if(filePath != null  && !filePath.isBlank()){
           if(!Files.exists(Paths.get(filePath))) {
               return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                       .body(new ApiResponse<>(
                               false,
                               null,
                               "The required attachment was not sent successfully")
                       );
           }
        }
        chatMessageService.sendMessage(chatNotification);
        return ResponseEntity.ok(new ApiResponse<>(
                true,"message sent","success"));
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


