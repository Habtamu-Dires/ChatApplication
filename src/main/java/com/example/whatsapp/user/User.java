package com.example.whatsapp.user;


import jakarta.persistence.*;
import lombok.*;


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
    @Column(unique = true)
    private String username;
    @Column(unique = true)
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String password;
    private String about;
    private String lastSeen;

}
