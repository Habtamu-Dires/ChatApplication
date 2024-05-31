package com.example.whatsapp.chat_reaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatReactionRepository extends JpaRepository<ChatReaction, ChatReactionId> {

    @Query("SELECT cr FROM ChatReaction cr WHERE cr.chatMessage.id=:chatMessageId")
    List<ChatReaction> findByChatMessage(Long chatMessageId);

    @Query("""
            SELECT cr FROM ChatReaction cr 
            WHERE cr.chatMessage.id=:chatMessageId 
            AND cr.emoji.name()=:value
            """
    )
    Optional<ChatReaction> findByChatMessageAndEmoji(Long chatMessageId, String value);

    @Query("""
            SELECT cr FROM ChatReaction cr
            WHERE cr.chatMessage.id=:chatMessageId
            AND cr.user.username=:username
            """
    )
    Optional<ChatReaction> findByChatMessageAndUser(Long chatMessageId, String username);
}
