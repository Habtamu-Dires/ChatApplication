package com.example.whatsapp.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


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
