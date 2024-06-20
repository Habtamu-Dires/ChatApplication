package com.example.app.groupchat;

import com.example.app.chat_dto.ChatNotification;
import com.example.app.chat_message.ChatMessage;
import com.example.app.chat_message.ChatMessageService;
import com.example.app.exception.InvalidRequestException;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.groupchat.dtos.AddToGroupDTO;
import com.example.app.groupchat.dtos.CreateGroupDTO;
import com.example.app.groupchat.dtos.UpdateGroupNameDTO;
import com.example.app.kafka_config.KafkaProducer;
import com.example.app.user.User;
import com.example.app.user.UserService;
import com.example.app.user.dtos.UserDTO;
import com.example.app.chat_reaction.EMOJI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupChatRoomService {

    private final GroupChatRoomRepository groupChatRoomRepository;
    private final KafkaProducer kafkaProducer;
    private final UserService userService;
    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;


    public GroupChatRoom findByGroupName(String groupName){
      return groupChatRoomRepository.findByGroupName(groupName)
            .orElseThrow(()-> new ResourceNotFoundException(
                    "Group with group name " + groupName + " not found")
            );
    }

    public void createGroup(CreateGroupDTO createGroupDTO){
        userService.findUserByUsername(createGroupDTO.ownerName());
        GroupChatRoom group = groupChatRoomRepository
                .findByGroupName(createGroupDTO.groupName())
                .orElse(null);

        if(group != null ) {
            throw new InvalidRequestException("Group name "
                    + createGroupDTO.groupName() + " already taken");
        }
        if(createGroupDTO.groupName() == null
                || createGroupDTO.groupName().isBlank()) {
            throw new InvalidRequestException("Group Name is required");
        }

        kafkaProducer.sendMessage(createGroupDTO);

    }

    public void addToGroup(AddToGroupDTO addToGroupDTO) {

        GroupChatRoom groupChatRoom = findByGroupName(addToGroupDTO.groupName());
        userService.findUserByUsername(addToGroupDTO.username());
        User adderUser = userService.findUserByUsername(addToGroupDTO.addedBy());

        List<User> userList = groupChatRoom.getUsers();
        if(!userList.contains(adderUser)){
           throw new InvalidRequestException(
                   "User " + adderUser.getUsername() + " not allowed to add users the group"
           );
        }
        kafkaProducer.sendMessage(addToGroupDTO);
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
       return groupChatRoomRepository.findById(id)
               .orElseThrow(()-> new ResourceNotFoundException(
                  "Group with group id " + id + " not found"
               ));
    }

    public ChatMessage saveMessage(ChatNotification chatNotification,
                                   GroupChatRoom groupChatRoom){
        User sender = userService.findUserByUsername(chatNotification.sender());

        ChatMessage chatMessage = ChatMessage.builder()
                .groupId(groupChatRoom)
                .sender(sender)
                .text(chatNotification.text())
                .fileName(chatNotification.fileName())
                .fileUrl(chatNotification.fileUrl())
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

        //find chat reactions
        chatMessageService
                .findChatMessagesByGroupName(groupName)
                .forEach(chatMessage -> {
                    //let get the chat reaction of each message
                    Map<String, EMOJI> rMap = new HashMap<>();
                    if(chatMessage.getChatReactionList() != null){
                        chatMessage.getChatReactionList()
                                .forEach(cr -> {
                                    rMap.put(cr.getUser().getUsername(),
                                            cr.getEmoji());
                                });
                    }
                    chatNotificationList.add(
                            ChatNotification.builder()
                                    .id(chatMessage.getId())
                                    .sender(chatMessage.getSender().getUsername())
                                    .groupName(chatMessage.getGroupId().getGroupName())
                                    .text(chatMessage.getText())
                                    .fileName(chatMessage.getFileName())
                                    .fileUrl(chatMessage.getFileUrl())
                                    .type(chatMessage.getType())
                                    .reactions(rMap)
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

        //send event
        ChatNotification chatNotif = ChatNotification.builder()
                .groupName(groupName)
                .sender(username)
                .recipient("DELETE")
                .type("group")
                .text("DELETE " + groupName)
                .build();
        kafkaProducer.sendMessage(chatNotif);

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

    public void deleteGroup(GroupChatRoom groupChatRoom){
        groupChatRoomRepository.delete(groupChatRoom);
    }

    public void saveGroup(GroupChatRoom groupChatRoom){
        groupChatRoomRepository.save(groupChatRoom);
    }
}
