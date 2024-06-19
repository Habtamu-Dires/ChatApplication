package com.example.whatsapp.registration;


import com.example.whatsapp.exception.InvalidRequestException;
import com.example.whatsapp.user.User;
import com.example.whatsapp.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserService userService;


    AuthenticationResponse register(RegisterRequest request){


        if(userService.isUsernameExists(request.username())){
            throw new InvalidRequestException(
                    "Username " + request.username() + " is taken"
            );
        }
        if(userService.isPhoneNumberTaken(request.phoneNumber())){
            throw new InvalidRequestException(
                    "PhoneNumber " + request.phoneNumber() + " is used"
            );
        }

        User user = User.builder()
                .username(request.username())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .password(request.password())
                .phoneNumber(request.phoneNumber())
                .build();

        var savedUser = userService.addUser(user);

        return new AuthenticationResponse(savedUser.getUsername(),
                savedUser.getPassword());
    }

    public AuthenticationResponse authenticate(LoginRequest request) {
        User user = userService.findUserByUsername(request.username());
        if(!user.getPassword().equals(request.password())){
            throw new InvalidRequestException("Wrong password");
        }

        return AuthenticationResponse.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .build();
    }
}


