package com.example.whatsapp.chatroom;

import com.example.whatsapp.user.User;
import com.example.whatsapp.user.UserService;
import lombok.RequiredArgsConstructor;
//import org.apache.kafka.common.quota.ClientQuotaAlteration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserService userService;

    public Optional<ChatRoom> getChatRoom(
            Long sender,
            Long recipient,
            boolean createNewRoomIfNotExists
    ) {
        return chatRoomRepository
                .findBySenderAndRecipient(sender, recipient)
                .or(() -> {
                    if(createNewRoomIfNotExists) {
                        var chatRoom = createChatId(sender, recipient);
                        return Optional.of(chatRoom);
                    }

                    return  Optional.empty();
                });
    }

    @Transactional
    private ChatRoom createChatId(Long senderId, Long recipientId) {
        var chatId = String.format("%d_%d", senderId, recipientId);
        User sender = userService.findUserById(senderId);
        User recipient = userService.findUserById(recipientId);

        ChatRoom senderRecipient = ChatRoom
                .builder()
                .chatId(chatId)
                .sender(sender)
                .recipient(recipient)
                .build();

        ChatRoom recipientSender = ChatRoom
                .builder()
                .chatId(chatId)
                .sender(recipient)
                .recipient(sender)
                .build();

        chatRoomRepository.save(senderRecipient);
        chatRoomRepository.save(recipientSender);

        return senderRecipient;

    }
}
