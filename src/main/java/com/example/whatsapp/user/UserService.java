package com.example.whatsapp.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    public User findUserById(Long id){
        return userRepository.findById(id).orElse(null);
    }

    public User addUser(User user){
       return userRepository.save(user);
    }

    public User findUserByUsername(String username){
        return userRepository.findByUsername(username).orElse(null);
    }


    public UserDTO updateUser(UserDTO userDTO) {
        User user = userRepository.findByUsername(userDTO.username())
                .orElse(null);
        if(user == null){
            return null;
        }
        user.setFirstName(userDTO.firstName());
        user.setLastName(userDTO.lastName());
        user.setPhoneNumber(userDTO.phoneNumber());

        return UserMapper.userToDTO(userRepository.save(user));

    }

    public UserDTO getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElse(null);
        if(user == null){
            return null;
        }

        return UserMapper.userToDTO(user);
    }
}
