package com.example.app.user;

import com.example.app.exception.InvalidRequestException;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.jwt.JwtUtil;
import com.example.app.token.Token;
import com.example.app.token.TokenService;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserContactService userContactService;
    private final ContactsService contactsService;
    private final UserMapper userMapper;
    private final TokenService tokenService;
    private final JwtUtil jwtUtil;

    // save new user
    public User addUser(User user){
        //create contact for the new user.
        Contacts contacts = new Contacts();
        contacts.setOwner(user);
        user.setContactsBook(contacts);
       return userRepository.save(user);
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
    public boolean doesUsernameExists(String username){
        return userRepository.findByUsername(username).isPresent();
    }
    // does the phoneNumber taken - for registration check
    public boolean isPhoneNumberTaken(String phoneNumber){
        return userRepository.findByPhoneNumber(phoneNumber).isPresent();
    }

    // update a user profile
    public ProfileUpdateDTO updateUser(ProfileUpdateDTO dto, String authHeader) {
        // is the new username taken
        if(!dto.newUsername().equals(dto.oldUsername())
            && doesUsernameExists(dto.newUsername())){
            throw new InvalidRequestException("Username already taken");
        }
        User user = findUserByUsername(dto.oldUsername());
        // does the new phone number taken
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
        //save user
        User savedUser = userRepository.save(user);
        //invalidate old token
        String jwtToken = authHeader.substring(7);
        Optional<Token> tokenObj = tokenService.findByTokenValue(jwtToken);
        tokenObj.ifPresent(token ->{
            token.setRevoked(true);
            tokenService.saveToken(token);
        });

        //create and send new token
        String tokenVal = jwtUtil.createToken(savedUser.getUsername());
        tokenService.saveToken(Token.builder()
                .token(tokenVal)
                .user(savedUser)
                .revoked(false)
                .build()
        );

        return userMapper.userToProfileUpdateDTO(savedUser, tokenVal);
    }

    // get user profile by username
    public UserDTO getUserProfile(String username) {
        User user = findUserByUsername(username);

        return userMapper.userToDTO(user);
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

        return userMapper.userToDTO(contactUser);
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
          userDTOList.add(userMapper.userToDTO(user));
        });
        return userDTOList;
    }
}
