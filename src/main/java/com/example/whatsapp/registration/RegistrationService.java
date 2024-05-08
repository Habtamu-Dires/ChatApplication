package com.example.whatsapp.registration;


import com.example.whatsapp.user.User;
import com.example.whatsapp.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;


    AuthenticationResponse register(RegisterRequest request){

        User user = User.builder()
                .username(request.username())
                .phoneNumber(request.phoneNumber())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .password(passwordEncoder.encode(request.password()))
                .build();

        var savedUser = userService.addUser(user);

        return new AuthenticationResponse(savedUser.getUsername());
    }


}


