package com.example.whatsapp.groupchat;

import com.example.whatsapp.chat_message.ChatMessage;
import com.example.whatsapp.chat_message.ChatMessageService;
import com.example.whatsapp.chat_dto.ChatNotification;
import com.example.whatsapp.exception.InvalidRequestException;
import com.example.whatsapp.exception.ResourceNotFoundException;
import com.example.whatsapp.groupchat.dtos.AddToGroupDTO;
import com.example.whatsapp.groupchat.dtos.CreateGroupDTO;
import com.example.whatsapp.groupchat.dtos.CreateGroupResponse;
import com.example.whatsapp.groupchat.dtos.UpdateGroupNameDTO;
import com.example.whatsapp.kafka_config.KafkaProducer;
import com.example.whatsapp.user.User;
import com.example.whatsapp.user.dtos.UserDTO;
import com.example.whatsapp.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class GroupChatRoomService {

    private final GroupChatRoomRepository groupChatRoomRepository;
    private final KafkaProducer kafkaProducer;
    private final UserService userService;
    private final ChatMessageService chatMessageService;

    public GroupChatRoom findByGroupName(String groupName){
      return groupChatRoomRepository.findByGroupName(groupName)
            .orElseThrow(()-> new ResourceNotFoundException(
                    "Group with group name " + groupName + " not found")
            );
    }

    public CreateGroupResponse createGroup(CreateGroupDTO createGroupDTO){
        User user = userService.findUserByUsername(createGroupDTO.ownerName());
        GroupChatRoom group = groupChatRoomRepository
                .findByGroupName(createGroupDTO.groupName())
                .orElse(null);

        if(group != null ) {
            throw new InvalidRequestException("Group name "
                    + createGroupDTO.groupName() + " already taken");
        }
        GroupChatRoom groupChatRoom = GroupChatRoom.builder()
                .owner(user)
                .groupName(createGroupDTO.groupName())
                .users(List.of(user))
                .build();

        GroupChatRoom savedGroupChat
                = groupChatRoomRepository.save(groupChatRoom);

      return CreateGroupResponse.builder()
                .groupId(savedGroupChat.getId())
                .ownerName(savedGroupChat.getOwner().getUsername())
                .groupName(savedGroupChat.getGroupName())
                .build();
    }

    public void addToGroup(AddToGroupDTO addToGroupDTO) {

        GroupChatRoom groupChatRoom = findByGroupName(addToGroupDTO.groupName());
        User userTobeAdded = userService.findUserByUsername(addToGroupDTO.username());
        User adderUser = userService.findUserByUsername(addToGroupDTO.addedBy());

//        List<User> groupUsers = groupChatRoomRepository
//                .findUsersByGroupId(groupChatRoom.getId());
        List<User> userList = groupChatRoom.getUsers();
        if(!userList.contains(adderUser)){
           throw new InvalidRequestException(
                   "User " + adderUser.getUsername() + " not allowed to add users the group"
           );
        }

        userList.add(userTobeAdded);
        groupChatRoom.setUsers(userList);
        groupChatRoomRepository.save(groupChatRoom);

    }
    

    public void sendMessage(ChatNotification chatNotification) {
        userService.findUserByUsername(chatNotification.sender()); //check sender
        findByGroupName(chatNotification.groupName()); // check group
        if(!chatNotification.type().equals("group")) {
            throw new InvalidRequestException(
                    "Message type is not allowed"
            );
        }

        kafkaProducer.sendMessage(chatNotification);
    }

    public GroupChatRoom findGroupChatRoomById(Long id){
       return groupChatRoomRepository.findById(id).orElse(null);
    }

    public ChatMessage saveMessage(ChatNotification chatNotification,
                            GroupChatRoom groupChatRoom){
        User sender = userService.findUserByUsername(chatNotification.sender());

        ChatMessage chatMessage = ChatMessage.builder()
                .groupId(groupChatRoom)
                .sender(sender)
                .text(chatNotification.text())
                .attachmentType(chatNotification.attachmentType())
                .attachmentPath(chatNotification.attachmentPath())
                .type(chatNotification.type())
                .timestamp(LocalDateTime.now())
                .build();

        return chatMessageService.saveGroupMessage(chatMessage);
    }

    public List<User> getGroupMembers(Long groupId){
        return groupChatRoomRepository.findUsersByGroupId(groupId);
    }


    public List<ChatNotification> getGroupMessages(String groupName) {
        List<ChatNotification> chatNotificationList = new ArrayList<>();
        chatMessageService
                .findChatMessagesByGroupName(groupName)
                .forEach(chatMessage -> {
                chatNotificationList.add(
                        ChatNotification.builder()
                                .sender(chatMessage.getSender().getUsername())
                                .groupName(chatMessage.getGroupId().getGroupName())
                                .text(chatMessage.getText())
                                .attachmentType(chatMessage.getAttachmentType())
                                .attachmentPath(chatMessage.getAttachmentPath())
                                .type(chatMessage.getType())
                                .build()
                );
            });
        return chatNotificationList;

    }


    public void deleteGroupChat(String username, String groupName) {
        var groupChatRoom = findByGroupName(groupName);
        User owner = userService.findUserByUsername(username);
        if(!groupChatRoom.getOwner().getId().equals(owner.getId())){
            throw new InvalidRequestException(
              "The user " + username + " is not allowed to delete this group"
            );
        }
        List<ChatMessage> chatMessageLists = chatMessageService
                .findChatMessagesByGroupName(groupChatRoom.getGroupName());
        chatMessageLists.forEach(chatMessageService::deleteChatMessage);
        groupChatRoomRepository.delete(groupChatRoom);

    }
    // get group members
    public List<UserDTO> getMembersOfGroup(String groupName) {
        GroupChatRoom groupChatRoom = findByGroupName(groupName); //check if the group exists
        List<User> userList = groupChatRoomRepository
                .findUsersByGroupName(groupChatRoom.getGroupName());

        List<UserDTO> userDTOList = new ArrayList<>();
        userList.forEach(user -> {
            userDTOList.add(
                    UserDTO.builder()
                        .username(user.getUsername())
                        .phoneNumber(user.getPhoneNumber())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .build()
            );
        });
        return userDTOList;
    }

    public void updateGroupName(UpdateGroupNameDTO dto) {
        GroupChatRoom groupChatRoom = findByGroupName(dto.oldGroupName());
        User user = userService.findUserByUsername(dto.ownerName());
        if(!groupChatRoom.getOwner().getId().equals(user.getId())){
            throw new InvalidRequestException(
                    "The user " + dto.ownerName()
                     + " is not permitted to change group name"
            );
        }

        groupChatRoom.setGroupName(dto.newGroupName());
        groupChatRoomRepository.save(groupChatRoom);
    }
}
