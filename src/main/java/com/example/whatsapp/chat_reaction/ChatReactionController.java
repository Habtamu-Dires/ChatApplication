package com.example.whatsapp.chat_reaction;

import com.example.whatsapp.api_response.ApiResponse;
import com.example.whatsapp.chat_reaction.dtos.ChatReactionDTO;
import com.example.whatsapp.chat_reaction.dtos.ChatResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat-reaction")
@RequiredArgsConstructor
public class ChatReactionController {

    private final ChatReactionService chatReactionService;

    //get chat message reactions
    @GetMapping("/{chatMessageId}")
    public ResponseEntity<ApiResponse<List<ChatReactionDTO>>> getChatReactions(
           @PathVariable("chatMessageId") Long chatMessageId){
        List<ChatReactionDTO> chatReactions
                = chatReactionService.getChatReactions(chatMessageId);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                chatReactions,
                "success")
        );
    }

    // add/remove chat message reactions
    @PostMapping("add-remove/{chatMessageId}")
    public ResponseEntity<ApiResponse<List<ChatReactionDTO>>> addChatResponse(
            @PathVariable("chatMessageId") Long chatMessageId,
            @RequestBody ChatResponseDTO chatResponseDTO
    ){
        List<ChatReactionDTO> chatReactionDTOS
                = chatReactionService.addRemoveChatReaction(
                        chatMessageId,
                        chatResponseDTO.username(),
                        chatResponseDTO.emoji()
        );

        return ResponseEntity.ok(new ApiResponse<>(true,
                chatReactionDTOS, "success"));
    }

}
