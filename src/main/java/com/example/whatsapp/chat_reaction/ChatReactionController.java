package com.example.whatsapp.chat_reaction;

import com.example.whatsapp.api_response.ApiResponse;
import com.example.whatsapp.chat_reaction.dtos.ChatReactionDTO;
import com.example.whatsapp.chat_reaction.dtos.ChatReactionReqResDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat-reaction")
@RequiredArgsConstructor
public class ChatReactionController {

    private final ChatReactionService chatReactionService;

    //get chat message reactions
    @GetMapping("/{chatMessageId}")
    public ResponseEntity<ApiResponse<List<ChatReactionDTO>>> getChatReactions(
           @PathVariable("chatMessageId") UUID chatMessageId){
        List<ChatReactionDTO> chatReactions
                = chatReactionService.getChatReactions(chatMessageId);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                chatReactions,
                "success")
        );
    }

    // add/remove chat message reactions
    @PostMapping("/add-remove")
    public ResponseEntity<ApiResponse<String>> addChatResponse(
            @RequestBody ChatReactionReqResDTO chatReactionReqResDTO
    ){
        chatReactionService.addRemoveChatReaction(chatReactionReqResDTO);

        return ResponseEntity.ok(new ApiResponse<>(
                true, null, "success"
        ));
    }

}
