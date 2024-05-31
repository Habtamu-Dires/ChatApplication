package com.example.whatsapp.user;

import com.example.whatsapp.contact.Contact;
import com.example.whatsapp.contact.ContactService;
import com.example.whatsapp.exception.ResourceNotFoundException;
import com.example.whatsapp.groupchat.GroupChatRoom;
import com.example.whatsapp.user_contact.UserContact;
import com.example.whatsapp.user_contact.UserContactService;
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
    private final ContactService contactService;


    // save new user
    @Transactional
    public User addUser(User user){
       User savedUser = userRepository.save(user);
       contactService.createNewContactContainer(savedUser);
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



    // update a user
    public UserDTO updateUser(UserDTO userDTO) {
        User user = findUserByUsername(userDTO.username());

        user.setFirstName(userDTO.firstName());
        user.setLastName(userDTO.lastName());
        user.setPhoneNumber(userDTO.phoneNumber());

        return UserMapper.userToDTO(userRepository.save(user));

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
        Contact contact = contactService.getContactByOwner(user);
//      userContactService
//       .getUserListsByContactId(contact.getId())
        contact.userContacts
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
}
