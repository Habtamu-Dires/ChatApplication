package com.example.app.chat_reaction;

import com.example.app.chat_message.ChatMessage;
import com.example.app.chat_message.ChatMessageService;
import com.example.app.chat_reaction.dtos.ChatReactionDTO;
import com.example.app.chat_reaction.dtos.ChatReactionReqResDTO;
import com.example.app.exception.ActionNotAllowedException;
import com.example.app.exception.InvalidRequestException;
import com.example.app.kafka_config.KafkaProducer;
import com.example.app.security_config.SecurityCheck;
import com.example.app.user.User;
import com.example.app.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
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
       ChatMessage chatMessage = chatMessageService
               .findChatMessageById(chatMessageId);

       //is the user allowed to get the reactions
       String loggedUsername = SecurityContextHolder.getContext()
                                .getAuthentication().getName();

       if(chatMessage.getGroupId() == null) { //private chat
           if(!chatMessage.getSender().getUsername().equals(loggedUsername)
                && !chatMessage.getRecipient().getUsername().equals(loggedUsername)
           ) {
               throw new ActionNotAllowedException();
           }
       } else { //group message
           boolean isMember
                   =  chatMessage.getGroupId().getUsers()
                   .stream()
                   .anyMatch(u -> u.getUsername().equals(loggedUsername));

           if(!isMember){
               throw new ActionNotAllowedException();
           }
       }

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
        //is the user the one logged in ?
        if(SecurityCheck.isTheUserNotLoggedIn(dto.getUsername())){
            throw new ActionNotAllowedException();
        }
        // check if the chat message id and username are valid
        chatMessageService.findChatMessageById(dto.getChatMessageId());
        userService.findUserByUsername(dto.getUsername());

        //find chat message reaction by chat message id and username
        findByChatMessageAndUser(dto.getChatMessageId(), dto.getUsername())
                .ifPresentOrElse(chatReaction -> {
                    if(chatReaction.getEmoji().toString().equals(dto.getEmoji())){
                        // status delete
                        dto.setStatus("DELETE");
                    } else {
                        // status update
                        dto.setStatus("UPDATE");
                    }
                } ,() -> { // chat message reaction not found
                    //check if user is allowed to send reaction to the message
                  ChatMessage chatMessage = chatMessageService
                          .findChatMessageById(dto.getChatMessageId());
                  if(chatMessage.getGroupId() == null) { //private chat
                      if(!chatMessage.getSender().getUsername().equals(dto.getUsername())
                        && !chatMessage.getRecipient().getUsername().equals(dto.getUsername())){
                          throw new ActionNotAllowedException();
                      }
                  } else { //group message
                    boolean isMember =  chatMessage.getGroupId().getUsers()
                              .stream()
                              .anyMatch(u -> u.getUsername().equals(dto.getUsername()));

                    if(!isMember){
                        throw new ActionNotAllowedException();
                    }
                  }
                    // status new
                    dto.setStatus("ADD");
                }
        );

        kafkaProducer.sendMessage(dto);
    }

    //find by Message and User
    public Optional<ChatReaction> findByChatMessageAndUser(UUID chatMessageId, String username){
        return chatReactionRepository.findByChatMessageAndUser(
                chatMessageId,  username);
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
