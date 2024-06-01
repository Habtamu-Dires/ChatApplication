package com.example.whatsapp.user;


import com.example.whatsapp.chat_reaction.ChatReaction;
import com.example.whatsapp.contacts.Contacts;
import com.example.whatsapp.groupchat.GroupChatRoom;
import com.example.whatsapp.user_contact.UserContact;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "_user")
public class User {

    @Id
    @SequenceGenerator(
            name = "user_sequence",
            sequenceName = "user_sequence",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
    generator = "user_sequence")
    private Long id;
    @NotBlank
    @Column(unique = true)
    private String username;
    @NotBlank
    @Column(unique = true)
    @Size(min = 9, max = 20)
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String password;
    private String about;
    private String lastSeen;

    @OneToMany(mappedBy = "owner")
    private List<GroupChatRoom> groupChatRoomOwned;

    @ManyToMany(mappedBy = "users")
    private List<GroupChatRoom> groupChatRoomList;

    @OneToMany(mappedBy = "user")
    private List<UserContact> userContactList;

    @OneToOne(mappedBy = "owner", cascade = {CascadeType.PERSIST,CascadeType.REMOVE})
    private Contacts contactsBook;

    @OneToMany(mappedBy = "user")
    private List<ChatReaction> chatReactionList;
    
}
