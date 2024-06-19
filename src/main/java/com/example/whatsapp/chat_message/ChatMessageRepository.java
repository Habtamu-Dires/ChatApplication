package com.example.whatsapp.chat_message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.chatId=:chatId")
    List<ChatMessage> findByChatId(String chatId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.groupId.id=:groupId")
    List<ChatMessage> findByGroupId(Long groupId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.id=:chatMessageId")
    Optional<ChatMessage> findByChatMessageId(UUID chatMessageId);
}
