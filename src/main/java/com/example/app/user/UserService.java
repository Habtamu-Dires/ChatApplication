package com.example.app.user;

import com.example.app.exception.InvalidRequestException;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.user.dtos.ProfileUpdateDTO;
import com.example.app.user_contact.UserContactService;
import com.example.app.contacts.Contacts;
import com.example.app.contacts.ContactsService;
import com.example.app.groupchat.GroupChatRoom;
import com.example.app.user.dtos.UserDTO;
import com.example.app.user_contact.UserContact;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserContactService userContactService;
    private final ContactsService contactsService;


    // save new user
    @Transactional
    public User addUser(User user){
       User savedUser = userRepository.save(user);
       contactsService.createNewContactContainer(savedUser);
       return savedUser;
    }

    //find use by id
    public User findUserById(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with user not found")
                );
    }

    //find user by username
    public User findUserByUsername(String username){
         return userRepository.findByUsername(username)
          .orElseThrow(() -> new ResourceNotFoundException(
                        "user with username " + username + " not found"
          ));
    }
    // does the username taken  for registration check
    public boolean isUsernameExists(String username){
        return userRepository.findByUsername(username).isPresent();
    }
    // does the phoneNumber taken - for registration check
    public boolean isPhoneNumberTaken(String phoneNumber){
        return userRepository.findByPhoneNumber(phoneNumber).isPresent();
    }



    // update a user profile
    public ProfileUpdateDTO updateUser(ProfileUpdateDTO dto) {
        if(!dto.newUsername().equals(dto.oldUsername())
            && isUsernameExists(dto.newUsername())){
            throw new InvalidRequestException("Username already taken");
        }
        User user = findUserByUsername(dto.oldUsername());

        if(!user.getPhoneNumber().equals(dto.phoneNumber()) &&
                isPhoneNumberTaken(dto.phoneNumber())){
            throw new InvalidRequestException("Phone number already taken");
        }

        user.setUsername(dto.newUsername());
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setPhoneNumber(dto.phoneNumber());
        // does password change requested
        if(dto.password() != null && !dto.password().isBlank() ){
            user.setPassword(dto.password());
        }

        return UserMapper.userToProfileUpdateDTO(userRepository.save(user));

    }
    // get user profile by username
    public UserDTO getUserProfile(String username) {
        User user = findUserByUsername(username);

        return UserMapper.userToDTO(user);
    }

    //find chat group names of a user by username
    public List<String> findChatGroupNames(String username) {
        User user = findUserByUsername(username);

        return user.getGroupChatRoomList().stream()
                .map(GroupChatRoom::getGroupName)
                .toList();
    }

    //get contacts of a user by username
    public List<UserDTO> getContacts(String username){
        User user = findUserByUsername(username);

        List<UserDTO> userDTOList = new ArrayList<>();
        Contacts contacts = contactsService.getContactByOwner(user);
//      userContactService
//       .getUserListsByContactId(contact.getId())
        contacts.userContacts
        .stream()
        .map(UserContact::getUser)
        .forEach(u -> {
            userDTOList.add(
                UserDTO.builder()
                    .username(u.getUsername())
                    .phoneNumber(u.getPhoneNumber())
                    .firstName(u.getFirstName())
                    .lastName(u.getLastName())
                    .build()
            );
        });
        return userDTOList;
    }
    // add a contact to a user
    public UserDTO addToContact(String username, String contactUsername) {
        User user = findUserByUsername(username);
        User contactUser = findUserByUsername(contactUsername);
        userContactService.createUserContact(user, contactUser);

        return UserMapper.userToDTO(contactUser);
    }

    // remove contact form
    public void removeContact(String username,
                              String contactUsername) {
        User user = findUserByUsername(username);
        User contactUser = findUserByUsername(contactUsername);
        userContactService.removeContact(user, contactUser);
    }

    public List<UserDTO> searchUsers(String username) {
        List<User> userList = userRepository.searchUsers(username);

        List<UserDTO> userDTOList = new ArrayList<>();
        userList.forEach(user -> {
          userDTOList.add(UserMapper.userToDTO(user));
        });
        return userDTOList;
    }
}
