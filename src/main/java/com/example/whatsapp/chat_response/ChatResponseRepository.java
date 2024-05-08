package com.example.whatsapp.chat_response;

import com.example.whatsapp.chat.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatResponseRepository extends JpaRepository<ChatResponse, Long> {

    @Query("SELECT cr FROM ChatResponse cr WHERE cr.chatMessage.id=:chatMessageId")
    List<ChatResponse> findByChatMessage(Long chatMessageId);
}
