package com.example.whatsapp.chat_reaction;

import com.example.whatsapp.chat_message.ChatMessage;
import com.example.whatsapp.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "chat_reaction")
@IdClass(ChatReactionId.class)
public class ChatReaction {

    @Id
    @ManyToOne
    @JoinColumn(name = "chatMessageId")
    private ChatMessage chatMessage;

    @Id
    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    @Enumerated(EnumType.STRING)
    private EMOJI emoji;
    private LocalDateTime createdAt;
}
