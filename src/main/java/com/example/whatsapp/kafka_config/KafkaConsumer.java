package com.example.whatsapp.kafka_config;

import com.example.whatsapp.chat.ChatMessage;
import com.example.whatsapp.chat.ChatMessageService;
import com.example.whatsapp.chat.ChatNotification;
import com.example.whatsapp.groupchat.GroupChatRoom;
import com.example.whatsapp.groupchat.GroupChatRoomService;
import com.example.whatsapp.user.User;
import com.example.whatsapp.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {

    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;
    private final GroupChatRoomService groupChatRoomService;

    @KafkaListener(topics = "whatsapp-topic", groupId = "myGroup")
    public void receiveMessage(ChatNotification chatNotification) {

        if(chatNotification.type().equals("private")){
            //User user =  userService.findUserByUsername(chatNotification.recipient());
            log.info(String.format("Consuming the message from whatsapp-topic: %s",
                    chatNotification.text())
            );
            //save message
            ChatMessage savedMsg = chatMessageService.save(chatNotification);

            //send message notification to receiver
            messagingTemplate.convertAndSendToUser(
                    chatNotification.recipient(),
                    "/my-messages",
                    ChatNotification.builder()
                            .sender(savedMsg.getSender().getUsername())
                            .recipient(savedMsg.getRecipient().getUsername())
                            .text(savedMsg.getText())
                            .attachmentType(savedMsg.getAttachmentType())
                            .attachmentPath(savedMsg.getAttachmentPath())
                            .build()
            );
        } else if(chatNotification.type().equals("group")){
            GroupChatRoom groupChatRoom =
                    groupChatRoomService.findByGroupName(chatNotification.groupName());
            //save message
            ChatMessage savedMsg =  groupChatRoomService
                    .saveMessage(chatNotification, groupChatRoom);

            groupChatRoomService.getGroupMembers(groupChatRoom.getId())
                .forEach(user -> {
                    //send message notification to user members
                    messagingTemplate.convertAndSendToUser(
                            user.getUsername(),
                            "/my-messages",
                            ChatNotification.builder()
                                .groupName(savedMsg.getGroupId().getGroupName())
                                .sender(savedMsg.getSender().getUsername())
                                .text(savedMsg.getText())
                                .attachmentType(savedMsg.getAttachmentType())
                                .attachmentPath(savedMsg.getAttachmentPath())
                                .build()
                    );
                });
        }


    }
}
