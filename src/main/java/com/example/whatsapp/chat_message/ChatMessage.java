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
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "chat_message")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

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
    private String fileName;
    private String fileUrl;
    private String type;
    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private GroupChatRoom groupId;

    @OneToMany(mappedBy = "chatMessage")
    private List<ChatReaction> chatReactionList;
}

