package com.example.whatsapp.chat_response;

import com.example.whatsapp.chat.ChatMessage;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Entity
@Table(name = "chat_response")
public class ChatResponse {

    @Id
    @SequenceGenerator(
            name = "response_sequence",
            sequenceName = "response_sequence",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "response_sequence")
    private Long id;
    @Enumerated(EnumType.STRING)
    private EMOJI emoji;
    private Integer count;

    @ManyToOne
    @JoinColumn(name = "chatMessage")
    private ChatMessage chatMessage;
}
