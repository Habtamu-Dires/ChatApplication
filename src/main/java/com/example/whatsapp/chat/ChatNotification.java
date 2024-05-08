package com.example.whatsapp.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatNotification {

    private String sender;
    private String recipient;
    private String text;
    private String attachmentType;
    private String attachmentPath;;
}
