package com.example.whatsapp.chat_dto;

import com.example.whatsapp.chat_reaction.EMOJI;
import lombok.Builder;

import java.util.Map;
import java.util.UUID;

@Builder
public record ChatNotification(
        UUID id,
        String sender,
        String recipient,
        String groupName,
        String text,
        String fileName,
        String fileUrl,
        String type,
        Map<String,EMOJI> reactions
) {

}
