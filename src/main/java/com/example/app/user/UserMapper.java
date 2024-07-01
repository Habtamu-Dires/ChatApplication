package com.example.app.user;

import org.springframework.stereotype.Component;

import com.example.app.user.dtos.ProfileUpdateDTO;
import com.example.app.user.dtos.UserDTO;
import lombok.NoArgsConstructor;

import java.util.List;

@Component
@NoArgsConstructor
public class UserMapper {

    //user to userDTO
    public  UserDTO userToDTO(User user){
        if(user == null){
            throw new NullPointerException("The User Should Not Be Null");
        }
        return UserDTO.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    public  List<UserDTO> userDTOS (List<User> users){
        return users.stream()
                .map(this::userToDTO)
                .toList();
    }

    //DTO to user
    public  User DtoToUser(UserDTO userDTO){
       return User.builder()
                .firstName(userDTO.firstName())
                .lastName(userDTO.lastName())
                .username(userDTO.username())
                .phoneNumber(userDTO.phoneNumber())
                .build();
    }

    public  ProfileUpdateDTO userToProfileUpdateDTO(User user,String token) {
        return ProfileUpdateDTO.builder()
                .newUsername(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .jwtToken(token)
                .build();
    }
}
