package com.example.app.kafka_config;

import com.example.app.chat_dto.ChatNotification;
import com.example.app.chat_message.ChatMessage;
import com.example.app.chat_message.ChatMessageService;
import com.example.app.chat_reaction.ChatReactionService;
import com.example.app.chat_reaction.dtos.ChatReactionReqResDTO;
import com.example.app.groupchat.dtos.AddToGroupDTO;
import com.example.app.groupchat.dtos.CreateGroupDTO;
import com.example.app.user.User;
import com.example.app.user.UserService;
import com.example.app.chat_reaction.ChatReaction;
import com.example.app.chat_reaction.EMOJI;
import com.example.app.groupchat.GroupChatRoom;
import com.example.app.groupchat.GroupChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;
    private final GroupChatRoomService groupChatRoomService;
    private final ChatReactionService chatReactionService;
    private final UserService userService;

    @KafkaListener(topics = "whatsapp-topic", groupId = "myGroup")
    public void receiveMessage(@Payload ConsumerRecord<String, Object> record,
                             Acknowledgment acknowledgment
                             // , @Header("messageType") String messageType
    ) {
        Object obj = record.value();
//        if(messageType.equals("chatNotification")) {
        if(obj instanceof ChatNotification){
            processPrivateAndGroupMessages((ChatNotification) obj);
            acknowledgment.acknowledge();  // manual acknowledgment
        } else if(obj instanceof ChatReactionReqResDTO){
            processChatReaction((ChatReactionReqResDTO) obj);
            acknowledgment.acknowledge(); // manual acknowledgment
        } else if(obj instanceof CreateGroupDTO) {
            //store event of create group
            processCreateGroupEvent((CreateGroupDTO) obj);
            acknowledgment.acknowledge(); // manual acknowledgment
        } else if(obj instanceof AddToGroupDTO) {
            //event of add to a group
            processAddToGroupEvent((AddToGroupDTO) obj);
            acknowledgment.acknowledge(); // manual
        }
        else {
            System.out.println("unknown  type " + obj.getClass());
        }
    }

    private void processAddToGroupEvent(AddToGroupDTO obj) {
        GroupChatRoom groupChatRoom
                = groupChatRoomService.findByGroupName(obj.groupName());
        User userTobeAdded = userService.findUserByUsername(obj.username());

        List<User> userList = groupChatRoom.getUsers();
        userList.add(userTobeAdded);
        groupChatRoom.setUsers(userList);
        groupChatRoomService.saveGroup(groupChatRoom);
    }

    private void processCreateGroupEvent(CreateGroupDTO obj) {
        User user = userService.findUserByUsername(obj.ownerName());
        GroupChatRoom groupChatRoom = GroupChatRoom.builder()
                .owner(user)
                .groupName(obj.groupName())
                .users(List.of(user))
                .build();

         groupChatRoomService.saveGroup(groupChatRoom);
    }

    //process chat reaction
    public void processChatReaction(ChatReactionReqResDTO dto){

        chatReactionService
                .findByChatMessageAndUser(dto.getChatMessageId(), dto.getUsername())
                .ifPresentOrElse((chatReaction) ->{
                    if(chatReaction.getEmoji().name().equals(dto.getEmoji())) {
                        chatReactionService.delete(chatReaction);
                    } else {
                        chatReaction.setEmoji(EMOJI.valueOf(dto.getEmoji()));
                        chatReaction.setCreatedAt(LocalDateTime.now());
                        chatReactionService.save(chatReaction);
                    }
                }, () -> {
                    //create one
                    User user = userService.findUserByUsername(dto.getUsername());
                    ChatMessage chatMessage = chatMessageService
                        .findChatMessageById(dto.getChatMessageId());

                    chatReactionService.save(
                            ChatReaction.builder()
                                .chatMessage(chatMessage)
                                .user(user)
                                .emoji(EMOJI.valueOf(dto.getEmoji()))
                                .createdAt(LocalDateTime.now())
                                .build()
                    );
                });

        //send notification.
        //identify if it is group message or not
       ChatMessage chatMessage =
               chatMessageService.findChatMessageById(dto.getChatMessageId());

        //private message so, send notification to both sender and receiver
       if(chatMessage.getGroupId() == null) {
           String sendToUsername = "";
        if(chatMessage.getSender().getUsername().equals(dto.getUsername())){
            sendToUsername = chatMessage.getRecipient().getUsername();
        } else if(chatMessage.getRecipient().getUsername().equals(dto.getUsername())){
            sendToUsername = chatMessage.getSender().getUsername();
        }

       //send message notification to receiver
       messagingTemplate.convertAndSendToUser(
           sendToUsername,
           "/message-reaction",
           ChatNotification.builder()
                   .sender(dto.getUsername())
                   .recipient(sendToUsername)
                   .build()
       );
       //send message notification to sender
       messagingTemplate.convertAndSendToUser(
           dto.getUsername(),
           "/message-reaction",
           ChatNotification.builder()
                   .sender(dto.getUsername())
                   .recipient(sendToUsername)
                   .build()
       );

       } else {
           // it is group message
           GroupChatRoom groupChatRoom = groupChatRoomService
                   .findGroupChatRoomById(chatMessage.getGroupId().getId());

           // send a web socket notification for each group members
           groupChatRoomService
               .getGroupMembers(groupChatRoom.getId())
               .forEach(user ->{
                   messagingTemplate.convertAndSendToUser(
                       user.getUsername(),
                       "/message-reaction",
                       ChatNotification.builder()
                               .sender(dto.getUsername())
                               .groupName(groupChatRoom.getGroupName())
                               .build()
                   );
               });
       }
    }
    // process private and group chat and send web socket notifications
    public void processPrivateAndGroupMessages(ChatNotification chatNotification){
        if(chatNotification.type().equals("private")){
            log.info(String.format("Consuming the message from whatsapp-topic: %s",
                    chatNotification.text())
            );
            //save message
            ChatMessage savedMsg = chatMessageService.save(chatNotification);

            // add reactions for the message
            Map<String, EMOJI> rMap = new HashMap<>();
            if(savedMsg.getChatReactionList() != null){
                savedMsg.getChatReactionList()
                .forEach(reaction ->
                        rMap.put(reaction.getUser().getUsername(),
                                reaction.getEmoji())
                );
            }

            //send message notification to receiver
            messagingTemplate.convertAndSendToUser(
                chatNotification.recipient(),
                "/my-messages",
                ChatNotification.builder()
                        .id(savedMsg.getId())
                        .sender(savedMsg.getSender().getUsername())
                        .recipient(savedMsg.getRecipient().getUsername())
                        .text(savedMsg.getText())
                        .fileName(savedMsg.getFileName())
                        .fileUrl(savedMsg.getFileUrl())
                        .reactions(rMap)
                        .build()
            );
            //send notification to the sender
            messagingTemplate.convertAndSendToUser(
                chatNotification.sender(),
                "/my-messages",
                ChatNotification.builder()
                        .id(savedMsg.getId())
                        .sender(savedMsg.getSender().getUsername())
                        .recipient(savedMsg.getRecipient().getUsername())
                        .text(savedMsg.getText())
                        .fileName(savedMsg.getFileName())
                        .fileUrl(savedMsg.getFileUrl())
                        .reactions(rMap)
                        .build()
            );
        }
        // group notification
        else if(chatNotification.type().equals("group")){
            GroupChatRoom groupChatRoom =
                    groupChatRoomService.findByGroupName(chatNotification.groupName());

            // it is a delete a group request
            if(chatNotification.recipient() != null && chatNotification.recipient().equals("DELETE")){
                List<ChatMessage> chatMessageLists = chatMessageService
                        .findChatMessagesByGroupName(groupChatRoom.getGroupName());
                chatMessageLists.forEach(chatMessageService::deleteChatMessage);
                //send notification
                groupChatRoomService.getGroupMembers(groupChatRoom.getId())
                    .forEach(user -> {
                        //send message notification to user members
                        messagingTemplate.convertAndSendToUser(
                                user.getUsername(),
                                "/my-groups",
                                ChatNotification.builder()
                                        .id(null)
                                        .groupName(chatNotification.groupName())
                                        .sender(chatNotification.sender())
                                        .recipient("DELETE")
                                        .text(null)
                                        .fileName(null)
                                        .fileUrl(null)
                                        .build()
                            );
                    });
                groupChatRoomService.deleteGroup(groupChatRoom);
            } else {    // it is a group message
                //save message
                ChatMessage savedMsg =  groupChatRoomService
                        .saveMessage(chatNotification, groupChatRoom);

                //add message reaction
                Map<String, EMOJI> rMap = new HashMap<>();
                if(savedMsg.getChatReactionList() != null){
                    savedMsg.getChatReactionList()
                            .forEach(cr ->{
                                rMap.put(cr.getUser().getUsername(),
                                        cr.getEmoji());
                            });
                }

                groupChatRoomService.getGroupMembers(groupChatRoom.getId())
                    .forEach(user -> {
                        //send message notification to group members
                        messagingTemplate.convertAndSendToUser(
                            user.getUsername(),
                            "/my-groups",
                            ChatNotification.builder()
                                    .id(savedMsg.getId())
                                    .sender(savedMsg.getSender().getUsername())
                                    .groupName(savedMsg.getGroupId().getGroupName())
                                    .text(savedMsg.getText())
                                    .fileName(savedMsg.getFileName())
                                    .fileUrl(savedMsg.getFileUrl())
                                    .reactions(rMap)
                                    .build()
                        );
                    });
            }

        }
    }

}
