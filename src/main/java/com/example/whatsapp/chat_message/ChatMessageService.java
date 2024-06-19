package com.example.whatsapp.chat_message;


import com.example.whatsapp.chat_dto.ChatNotification;
import com.example.whatsapp.chat_reaction.EMOJI;
import com.example.whatsapp.chatroom.ChatRoom;
import com.example.whatsapp.chatroom.ChatRoomService;
import com.example.whatsapp.exception.ResourceNotFoundException;
import com.example.whatsapp.groupchat.GroupChatRoom;
import com.example.whatsapp.groupchat.GroupChatRoomRepository;
import com.example.whatsapp.kafka_config.KafkaConsumer;
import com.example.whatsapp.kafka_config.KafkaProducer;
import com.example.whatsapp.user.User;
import com.example.whatsapp.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomService chatRoomService;
    private final UserService userService;
    private final KafkaProducer kafkaProducer;
    private final GroupChatRoomRepository groupChatRoomRepository;

    //get chatMessage by chatId
    public ChatMessage findChatMessageById(UUID chatMessageId){
      return chatMessageRepository.findByChatMessageId(chatMessageId)
              .orElseThrow(()-> new ResourceNotFoundException(
                      "Chat message with id " + chatMessageId + " not found")
              );
    }

    //send message
    public void sendMessage(ChatNotification chatNotification){
        userService.findUserByUsername(chatNotification.sender());
        userService.findUserByUsername(chatNotification.recipient());
        kafkaProducer.sendMessage(chatNotification);
    }
    //save chat message
    public ChatMessage save(ChatNotification chatNotification) {

        User sender = userService.findUserByUsername(chatNotification.sender());
        User recipient = userService.findUserByUsername(chatNotification.recipient());

        ChatMessage chatMessage = ChatMessage.builder()
                .sender(sender)
                .recipient(recipient)
                .text(chatNotification.text())
                .fileName(chatNotification.fileName())
                .fileUrl(chatNotification.fileUrl())
                .type(chatNotification.type())
                .timestamp(LocalDateTime.now())
                .build();

        var chatRoom = chatRoomService
                .getChatRoom(chatMessage.getSender().getId(),
                        chatMessage.getRecipient().getId(),
                        true
                )
                .orElseThrow(RuntimeException::new);


        chatMessage.setChatRoom(chatRoom);
        chatMessageRepository.save(chatMessage);
        return chatMessage;
    }

    public ChatMessage saveGroupMessage(ChatMessage chatMessage){
        return chatMessageRepository.save(chatMessage);
    }

    public List<ChatNotification> findChatMessages(String senderUsername, String recipientUsername) {

        User sender  = userService.findUserByUsername(senderUsername);
        User recipient = userService.findUserByUsername(recipientUsername);

        Long senderId = sender.getId();
        Long recipientId = recipient.getId();

        Optional<ChatRoom> chatRoom = chatRoomService.getChatRoom(
                senderId, recipientId, false);
        List<ChatMessage> chatMessages = chatRoom.map(cr ->
                        chatMessageRepository.findByChatId(cr.getChatId())
                )
                .orElse(List.of());

        List<ChatNotification> chatNotificationList = new ArrayList<>();

        chatMessages.forEach(chatMessage -> {
            //get message reactions
            Map<String, EMOJI> rMap = new HashMap<>();
            chatMessage.getChatReactionList()
            .forEach(reaction ->
                    rMap.put(reaction.getUser().getUsername(),
                            reaction.getEmoji())
            );
            chatNotificationList.add(
                    ChatNotification.builder()
                            .id(chatMessage.getId())
                            .sender(chatMessage.getSender().getUsername())
                            .recipient(chatMessage.getRecipient().getUsername())
                            .text(chatMessage.getText())
                            .fileName(chatMessage.getFileName())
                            .fileUrl(chatMessage.getFileUrl())
                            .type(chatMessage.getType())
                            .reactions(rMap)
                            .build()
            );
        });

        return chatNotificationList;

    }

    public List<ChatMessage> findChatMessagesByGroupName(String groupName) {
        GroupChatRoom groupChatRoom = groupChatRoomRepository
                .findByGroupName(groupName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Group with " + groupName + " not found"
                ));

        Long groupId = groupChatRoom.getId();

        return chatMessageRepository.findByGroupId(groupId);

    }

    //delete chat message by
    public void deleteChatMessage(ChatMessage chatMessage){
        chatMessageRepository.delete(chatMessage);
    }
}
