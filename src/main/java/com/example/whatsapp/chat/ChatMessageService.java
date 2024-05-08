package com.example.whatsapp.chat;


import com.example.whatsapp.chatroom.ChatRoomService;
import com.example.whatsapp.user.User;
import com.example.whatsapp.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageService {

    private final ChatMessageRepository repository;
    private final ChatRoomService chatRoomService;
    private final UserService userService;
    private final KafkaTemplate<String, ChatNotification> kafkaTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    //send message
    public void sendMessage(ChatNotification chatNotification){
        Message<ChatNotification> message = MessageBuilder
                .withPayload(chatNotification)
                .setHeader(KafkaHeaders.TOPIC, "whatsapp-topic")
                .build();
        kafkaTemplate.send(message);
    }

    // receive message
    @KafkaListener(topics = "whatsapp-topic", groupId = "myGroup")
    public void receiveMessage(ChatNotification chatNotification){
      User user =  userService
              .findUserById(Long.parseLong(chatNotification.getRecipient()));
      if(user != null){
          String username = SecurityContextHolder
                  .getContext()
                  .getAuthentication()
                  .getName();
          if (username != null && username.equals(user.getUsername())) {
              log.info(String.format("Consuming the message from hab-topic: %s",
                      chatNotification.getText())
              );
              //save message
             ChatMessage savedMsg = save(chatNotification);

              //send message notification to user
              messagingTemplate.convertAndSendToUser(
                      chatNotification.getRecipient(),
                      "/my-messages",
                      ChatNotification.builder()
                              .sender(savedMsg.getSender().getUsername())
                              .recipient(savedMsg.getRecipient().getUsername())
                              .text(savedMsg.getText())
                              .attachmentType(savedMsg.getAttachmentType())
                              .attachmentPath(savedMsg.getAttachmentPath())
                              .build()
              );
          }
      }
    }

    public ChatMessage save(ChatNotification chatNotification) {

        User sender = userService.findUserById(Long.parseLong(chatNotification.getSender()));
        User recipient = userService.findUserById(Long.parseLong(chatNotification.getRecipient()));

        ChatMessage chatMessage = ChatMessage.builder()
                .sender(sender)
                .recipient(recipient)
                .text(chatNotification.getText())
                .attachmentType(chatNotification.getAttachmentType())
                .attachmentPath(chatNotification.getAttachmentPath())
                .timestamp(LocalDateTime.now())
                .build();

        var chatId = chatRoomService
                .getChatRoomId(chatMessage.getSender().getId().toString(),
                        chatMessage.getRecipient().getId().toString(),
                        true
                )
                .orElseThrow(); // You can create your own dedicated exception
        chatMessage.setChatId(chatId);
        repository.save(chatMessage);
        return chatMessage;
    }

    public List<ChatNotification> findChatMessages(String senderId, String recipientId) {
        var chatId = chatRoomService.getChatRoomId(senderId, recipientId, false);
        List<ChatMessage> chatMessages = chatId.map(repository::findByChatId)
                .orElse(new ArrayList<>());

        List<ChatNotification> chatNotificationList = new ArrayList<>();
        chatMessages.forEach(chatMessage -> {
            chatNotificationList.add(
                    ChatNotification.builder()
                            .sender(chatMessage.getSender().getUsername())
                            .recipient(chatMessage.getRecipient().getUsername())
                            .text(chatMessage.getText())
                            .attachmentType(chatMessage.getAttachmentType())
                            .attachmentPath(chatMessage.getAttachmentPath())
                            .build()
            );
        });

        return chatNotificationList;
    }


}
