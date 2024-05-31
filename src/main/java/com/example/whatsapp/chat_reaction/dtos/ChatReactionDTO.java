package com.example.whatsapp.chat_reaction.dtos;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatReactionDTO(
        Long chatId,
        String username,
        String emoji,
        LocalDateTime createdAt
) { }
