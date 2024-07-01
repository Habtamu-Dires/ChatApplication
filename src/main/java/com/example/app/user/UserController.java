package com.example.app.user;


import com.example.app.api_response.ApiResponse;
import com.example.app.user.dtos.ProfileUpdateDTO;
import com.example.app.user.dtos.UserAndContactDto;
import com.example.app.user.dtos.UserDTO;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    //search users
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserDTO>>> searchUsers(
            @RequestParam String username
    ){
       List<UserDTO> userDTOList =  userService.searchUsers(username);
       return ResponseEntity.ok(new ApiResponse<>(
                true,userDTOList,"user profile-updated"));
    }

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
            @RequestHeader String Authorization,
            @RequestBody ProfileUpdateDTO dto){
        ProfileUpdateDTO userDTO = userService.updateUser(dto,Authorization);
        return ResponseEntity.ok(new ApiResponse<>(
                true,userDTO,"user profile-updated"));
    }

    // find chat group names of a user
    @GetMapping("/groups/{username}")
    public ResponseEntity<ApiResponse<List<String>>> findChatGroups(
            @PathVariable("username") String username){
        var chatGroupNames = userService.findChatGroupNames(username);
        return ResponseEntity.ok(new ApiResponse<>(
                true, chatGroupNames, "contacts found")
        );
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


