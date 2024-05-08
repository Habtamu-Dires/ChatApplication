package com.example.whatsapp.user;


import java.util.List;

public class UserMapper {

    //user to userDTO
    public static UserDTO userToDTO(User user){
        return UserDTO.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .password(user.getPassword())
                .build();
    }

    public static List<UserDTO> userDTOS (List<User> users){
        return users.stream()
                .map(UserMapper::userToDTO)
                .toList();
    }

    //DTO to user
    public static User DtoToUser(UserDTO userDTO){
       return User.builder()
                .firstName(userDTO.firstName())
                .lastName(userDTO.lastName())
                .username(userDTO.username())
                .password(userDTO.password())
                .build();
    }
}
