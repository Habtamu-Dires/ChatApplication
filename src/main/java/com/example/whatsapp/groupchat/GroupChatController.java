package com.example.whatsapp.groupchat;

import com.example.whatsapp.api_response.ApiResponse;
import com.example.whatsapp.chat_dto.ChatNotification;
import com.example.whatsapp.exception.InvalidRequestException;
import com.example.whatsapp.groupchat.dtos.AddToGroupDTO;
import com.example.whatsapp.groupchat.dtos.CreateGroupDTO;
import com.example.whatsapp.groupchat.dtos.CreateGroupResponse;
import com.example.whatsapp.groupchat.dtos.UpdateGroupNameDTO;
import com.example.whatsapp.user.dtos.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/v1/group-chat")
@RequiredArgsConstructor
public class GroupChatController {

    private final GroupChatRoomService groupChatRoomService;

    // send message to a group
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<String>> sendMessage(
            @RequestBody ChatNotification chatNotification){
        var fileUrl = chatNotification.fileUrl();
        if(fileUrl != null && !fileUrl.isBlank()) {
            if(!Files.exists(Paths.get(fileUrl))) {
                throw new InvalidRequestException(
                        "The required attachment was not sent successfully"
                );
            }
        }
        groupChatRoomService.sendMessage(chatNotification);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "",
                "Message Send Successfully")
        );
    }
    //get group chat messages
    @GetMapping("/messages/{groupName}")
    public ResponseEntity<ApiResponse<List<ChatNotification>>> findGroupMessages(
            @PathVariable("groupName") String groupName
    ){
       List<ChatNotification> chatNotificationList
                = groupChatRoomService.getGroupMessages(groupName);

        return ResponseEntity.ok(new ApiResponse<>(
                true,chatNotificationList,"Message fetched successfully"
        ));
    }
    // create group
    @PostMapping("/create-group")
    public ResponseEntity<ApiResponse<String>> createGroup(
            @RequestBody CreateGroupDTO createGroupDto
    ){
        groupChatRoomService.createGroup(createGroupDto);

        return ResponseEntity.ok(new ApiResponse<>(true,null,
                "group successfully created"));
    }

    // add a user to group
    @PostMapping("add-to-group")
    public ResponseEntity<ApiResponse<String>> addUserToGroupChat(
            @RequestBody AddToGroupDTO addToGroupDTO)
    {
        groupChatRoomService.addToGroup(addToGroupDTO);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                addToGroupDTO.username() + " added to " + addToGroupDTO.groupName(),
                "success")
        );
    }

    //update groups name
    @PutMapping("change-name")
    public ResponseEntity<ApiResponse<String>> changeGroupName(
            @RequestBody UpdateGroupNameDTO dto
            ){
        groupChatRoomService.updateGroupName(dto);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                dto.ownerName() +  " change group name to " + dto.newGroupName(),
                "success")
        );
    }

    // get users of a group
    @GetMapping("members/{groupName}")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getMembersOfGroup(
            @PathVariable("groupName") String groupName
    ){
        List<UserDTO> membersOfGroup
                = groupChatRoomService.getMembersOfGroup(groupName);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                membersOfGroup,
                "Members of a group found"
        ));
    }

    //delete a group
    @DeleteMapping("delete-group/{groupName}/{ownerName}")
    public ResponseEntity<ApiResponse<String>> deleteGroup(
           @PathVariable("groupName") String groupName,
           @PathVariable("ownerName") String ownerName){
       groupChatRoomService.deleteGroupChat(ownerName, groupName);

       return ResponseEntity.ok(
               new ApiResponse<>(
                       true, groupName + " deleted", "Success"
               )
       );

    }

}
