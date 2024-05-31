package com.example.whatsapp.user_contact;

import com.example.whatsapp.contact.Contact;
import com.example.whatsapp.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "user_contact")
@IdClass(UserContactId.class)
public class UserContact {

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "contact_id")
    private Contact contact;

    private LocalDateTime createdAt;
}
