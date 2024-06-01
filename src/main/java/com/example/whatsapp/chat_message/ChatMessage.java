package com.example.whatsapp.chat_message;

import com.example.whatsapp.chat_reaction.ChatReaction;
import com.example.whatsapp.chatroom.ChatRoom;
import com.example.whatsapp.groupchat.GroupChatRoom;
import com.example.whatsapp.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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

    @ManyToOne
    @JoinColumn(name = "chat_id")
    private ChatRoom chatRoom;

    @ManyToOne
    @JoinColumn(name = "sender")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "recipient")
    private User recipient;

    private String text;
    private String attachmentType;
    private String attachmentPath;
    private String type;
    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private GroupChatRoom groupId;

    @OneToMany(mappedBy = "chatMessage")
    private List<ChatReaction> chatReactionList;
}

