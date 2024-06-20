package com.example.app.chat_reaction;

import com.example.app.chat_message.ChatMessageService;
import com.example.app.chat_reaction.dtos.ChatReactionDTO;
import com.example.app.chat_reaction.dtos.ChatReactionReqResDTO;
import com.example.app.kafka_config.KafkaProducer;
import com.example.app.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatReactionService {

    private final ChatMessageService chatMessageService;
    private final ChatReactionRepository chatReactionRepository;
    private final UserService userService;
    private final KafkaProducer kafkaProducer;

    // get chat reaction
    public List<ChatReactionDTO> getChatReactions(UUID chatMessageId) {
        chatMessageService.findChatMessageById(chatMessageId);

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
    public void addRemoveChatReaction(
            ChatReactionReqResDTO dto
    ) {
        chatMessageService.findChatMessageById(dto.getChatMessageId());
        userService.findUserByUsername(dto.getUsername());

        findByChatMessageAndUser(dto.getChatMessageId(), dto.getUsername())
                .ifPresentOrElse(chatReaction -> {
                    if(chatReaction.getEmoji().toString().equals(dto.getEmoji())){
                        // status delete
                        dto.setStatus("DELETE");
                    } else {
                        // status update
                        dto.setStatus("UPDATE");
                    }
                } ,() -> {
                    // status new
                    dto.setStatus("ADD");
                }
        );

        kafkaProducer.sendMessage(dto);
    }

    //find by Message and User
    public Optional<ChatReaction> findByChatMessageAndUser(UUID chatMessageId, String username){
        return chatReactionRepository.findByChatMessageAndUser(chatMessageId,
                username);
    }

    //save
    public void save(ChatReaction chatReaction){
        chatReactionRepository.save(chatReaction);
    }

    //delete
    public void delete(ChatReaction chatReaction){
        chatReactionRepository.delete(chatReaction);
    }
}
