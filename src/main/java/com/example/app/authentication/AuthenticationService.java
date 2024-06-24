package com.example.app.authentication;


import com.example.app.exception.InvalidRequestException;
import com.example.app.jwt.JwtUtil;
import com.example.app.token.Token;
import com.example.app.token.TokenService;
import com.example.app.user.User;
import com.example.app.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;


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
                .password(passwordEncoder.encode(request.password()))
                .phoneNumber(request.phoneNumber())
                .build();

        var savedUser = userService.addUser(user);

        // create token and save token for the user
        String tokenValue = jwtUtil.createToken(savedUser.getUsername());
        Token tokenObj = Token.builder()
                .token(tokenValue)
                .user(savedUser)
                .revoked(false)
                .build();

        tokenService.saveToken(tokenObj);


        return new AuthenticationResponse(savedUser.getUsername(),
                tokenValue);
    }

    public AuthenticationResponse authenticate(LoginRequest request) {
        // authenticate using usernameAndPasswordAuthenticationFilter
        try{
            authenticationManager.authenticate(
                    new  UsernamePasswordAuthenticationToken(
                            request.username(), request.password()
                    )
            );
        } catch (AuthenticationException e){
            throw new InvalidRequestException("Username or Password Incorrect");
        }
        // at this point the user is authenticated
        // let create a token for the user and send it back to the user
        User user = userService.findUserByUsername(request.username());

        String tokenVal = jwtUtil.createToken(user.getUsername());
        Token tokenObj = Token.builder()
                .user(user)
                .token(tokenVal)
                .revoked(false)
                .build();

        tokenService.saveToken(tokenObj);

        return AuthenticationResponse.builder()
                .username(user.getUsername())
                .jwtToken(tokenVal)
                .build();
    }
}


