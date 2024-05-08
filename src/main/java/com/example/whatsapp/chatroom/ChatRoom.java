package com.example.whatsapp.chatroom;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "chat_room")
public class ChatRoom {

    @Id
    @SequenceGenerator(
            name = "chatRoom_sequence",
            sequenceName = "chatRoom_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "chatRoom_sequence"
    )
    private Long id;
    private String chatId;
    private String senderId;
    private String recipientId;
}
