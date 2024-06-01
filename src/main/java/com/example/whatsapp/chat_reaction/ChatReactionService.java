package com.example.whatsapp.chat_reaction;

import com.example.whatsapp.chat_message.ChatMessage;
import com.example.whatsapp.chat_message.ChatMessageService;
import com.example.whatsapp.chat_reaction.dtos.ChatReactionDTO;
import com.example.whatsapp.user.User;
import com.example.whatsapp.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatReactionService {

    private final ChatMessageService chatMessageService;
    private final ChatReactionRepository chatReactionRepository;
    private final UserService userService;

    // get chat reaction
    public List<ChatReactionDTO> getChatReactions(Long chatMessageId) {
        return chatReactionRepository.findByChatMessage(chatMessageId)
                .stream()
                .map(cr ->
                     ChatReactionDTO.builder()
                            .chatId(cr.getChatMessage().getId())
                            .emoji(cr.getEmoji().name())
                            .username(cr.getUser().getUsername())
                            .createdAt(cr.getCreatedAt())
                             .build()
                )
                .toList();
    }

    // add - remove chat reactions
    public List<ChatReactionDTO> addRemoveChatReaction(
            Long chatMessageId, String username,String emoji
    ) {

        ChatReaction chatReaction = chatReactionRepository
                .findByChatMessageAndUser(chatMessageId, username)
                .orElse(null);

        if(chatReaction == null){
            //create one
            User user = userService.findUserByUsername(username);
            ChatMessage chatMessage = chatMessageService
                    .findChatMessageById(chatMessageId);

            chatReactionRepository.save(
                ChatReaction.builder()
                    .chatMessage(chatMessage)
                    .user(user)
                    .emoji(EMOJI.valueOf(emoji))
                    .createdAt(LocalDateTime.now())
                    .build()
            );


        } else {
            if(chatReaction.getEmoji().name().equals(emoji)) {
                chatReactionRepository.delete(chatReaction);
            } else {
                chatReaction.setEmoji(EMOJI.valueOf(emoji));
                chatReaction.setCreatedAt(LocalDateTime.now());
                chatReactionRepository.save(chatReaction);
            }

        }

        return getChatReactions(chatMessageId);

    }
}
