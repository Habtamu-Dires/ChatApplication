package com.example.whatsapp.contact;

import com.example.whatsapp.user.User;
import com.example.whatsapp.user_contact.UserContact;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "contact")
public class Contact {

    @Id
    @SequenceGenerator(
            name = "user_contact_sequence",
            sequenceName = "user_contact_sequence",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
        generator = "user_contact_sequence"
    )
    private Long id;
    @OneToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "contact", fetch = FetchType.EAGER)
    public List<UserContact> userContacts;

}
