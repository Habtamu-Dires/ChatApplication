package com.example.whatsapp.chat_dto;

import lombok.Builder;


@Builder
public record ChatNotification(
        String sender,
        String recipient,
        String groupName,
        String text,
        String attachmentType,
        String attachmentPath,
        String type
) {

}
