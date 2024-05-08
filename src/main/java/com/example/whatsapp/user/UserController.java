package com.example.whatsapp.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public UserDTO getAllUsers(@RequestBody String username){
        return userService.getUserProfile(username);
    }

    @PutMapping("/profile-update")
    public UserDTO updateProfile(@RequestBody UserDTO userDTO){
        return userService.updateUser(userDTO);
    }


}


