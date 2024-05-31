package com.example.whatsapp.chat;

import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.chatId=:chatId")
    List<ChatMessage> findByChatId(String chatId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.groupId.id=:groupId")
    List<ChatMessage> findByGroupId(Long groupId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.id=:chatMessageId")
    Optional<ChatMessage> findByChatMessageId(Long chatMessageId);
}
