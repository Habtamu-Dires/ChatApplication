package com.example.whatsapp.chat;

import com.example.whatsapp.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "chat_message")
public class ChatMessage {

    @Id
    @SequenceGenerator(
            name = "chatMessage_sequence",
            sequenceName = "chatMessage_sequence",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "chatMessage_sequence"
    )
    private Long id;
    private String chatId;
    @ManyToOne
    @JoinColumn(name = "sender")
    private User sender;
    @ManyToOne
    @JoinColumn(name = "recipient")
    private User recipient;
    private String text;
    private String attachmentType;
    private String attachmentPath;
    private LocalDateTime timestamp;
}

