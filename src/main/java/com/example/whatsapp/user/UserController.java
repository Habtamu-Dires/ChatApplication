package com.example.whatsapp.user;


import com.example.whatsapp.api_response.ApiResponse;
import com.example.whatsapp.user.dtos.ProfileUpdateDTO;
import com.example.whatsapp.user.dtos.UserAndContactDto;
import com.example.whatsapp.user.dtos.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    //get a profile
    @GetMapping("/profile/{username}")
    public ResponseEntity<ApiResponse<UserDTO>> getAllUsers(
            @PathVariable("username") String username){
        UserDTO userDto = userService.getUserProfile(username);
          return ResponseEntity
                  .ok(new ApiResponse<>(true, userDto,"User Found"));
    }

    //update profile
    @PutMapping("/profile-update")
    public ResponseEntity<ApiResponse<ProfileUpdateDTO>> updateProfile(
            @RequestBody ProfileUpdateDTO dto){
        ProfileUpdateDTO updateDTO = userService.updateUser(dto);
        return ResponseEntity.ok(new ApiResponse<>(
                true,updateDTO,"user profile-updated"));
    }

    // find chat group names of a user
    @GetMapping("/groups/{username}")
    public List<String> findChatGroups(
            @PathVariable("username") String username){
        return userService.findChatGroupNames(username);
    }

    // find contacts of a user
    @GetMapping("/contacts/{username}")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getContacts(
            @PathVariable("username") String username){

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                userService.getContacts(username),
                "contacts found")
        );
    }

    //add a user as a contact
    @PostMapping("/add-to-contact")
    public ResponseEntity<ApiResponse<UserDTO>> addToContacts(
            @RequestBody UserAndContactDto addToContact
    ) {
      var userDto = userService
              .addToContact(addToContact.username(),addToContact.contactName());
        return ResponseEntity.ok(new ApiResponse<>(
                true, userDto, "contact successful added"));
    }

    // remove contact
    @PutMapping("/remove-contact")
    public ResponseEntity<ApiResponse<String>> removeContact(
        @RequestBody UserAndContactDto userAndContactDto
    ) {
            userService.removeContact(
                    userAndContactDto.username(),userAndContactDto.contactName()
            );

            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "User contact " + userAndContactDto.contactName(),
                    "Contact successfully Removed"
            ));
    }

}


