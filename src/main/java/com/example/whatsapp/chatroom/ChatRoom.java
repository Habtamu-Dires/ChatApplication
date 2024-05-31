package com.example.whatsapp.chatroom;

import com.example.whatsapp.user.User;
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
    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;
    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;
    private String chatId;
}
