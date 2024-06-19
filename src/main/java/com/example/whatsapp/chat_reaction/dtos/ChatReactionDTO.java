package com.example.whatsapp.chat_reaction.dtos;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ChatReactionDTO(
        UUID chatId,
        String username,
        String emoji,
        LocalDateTime createdAt
) { }
