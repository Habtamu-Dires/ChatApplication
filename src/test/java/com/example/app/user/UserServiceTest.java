package com.example.app.user;

import com.example.app.contacts.ContactsService;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.user.dtos.ProfileUpdateDTO;
import com.example.app.user_contact.UserContactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    // The tested class
    @InjectMocks
    private UserService userService;

    //The dependencies class
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserContactService userContactService;
    @Mock
    private ContactsService contactsService;
    @Mock
    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        // open the mocks or start the mocks
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldFindUserByUsername(){
        //Given
        String username = "abe";
        User storedUser = User.builder()
                .id(1L)
                .username("abe")
                .firstName("abebe")
                .lastName("kebede")
                .phoneNumber("1211212")
                .build();
        //mock the calls
        when(userRepository.findByUsername("abe"))
                .thenReturn(Optional.of(storedUser));
        //When
         User savedUser = userService.findUserByUsername(username);
        //Then
        assertEquals(savedUser.getUsername(), "abe");
        assertEquals(savedUser.getFirstName(), "abebe");
        assertEquals(savedUser.getPhoneNumber(), "1211212");

        verify(userRepository, times(1))
                .findByUsername(username);
    }

    @Test //findByUsername
    public void shouldThrowResourceNotFoundExceptionWhenUsernameNotFound(){
        //Given
        //mock call
        when(userRepository.findByUsername(any()))
                .thenReturn(Optional.empty());
        //when
        //then
        var exp = assertThrows(ResourceNotFoundException.class,
                    () -> userService.findUserByUsername(any()));

        assertEquals(exp.getMessage(),
                "user with username " + any() + " not found");
    }

    //update user
    @Test
    public void shouldUpdateUsers(){
        //Given
        ProfileUpdateDTO profileDTO = ProfileUpdateDTO.builder()
                .newUsername("alex")
                .oldUsername("abe")
                .phoneNumber("22222222")
                .build();

        User user = User.builder()
                .username("alex")
                .phoneNumber("22222222")
                .build();

        User oldUser = User.builder()
                .id(1L)
                .username("abe")
                .phoneNumber("44444444")
                .build();

        User savedUser = User.builder()
                .id(1L)
                .username("alex")
                .phoneNumber("22222222")
                .build();

        //mock calls
        when(userRepository.save(oldUser))
                .thenReturn(savedUser);
        when(userRepository.findByUsername(profileDTO.newUsername()))
                .thenReturn(Optional.empty()); // mocks doesUsername exists
        when(userRepository.findByPhoneNumber(profileDTO.phoneNumber()))
                .thenReturn(Optional.empty());  // mocks does phone number exists
        when(userRepository.findByUsername(profileDTO.oldUsername()))
                .thenReturn(Optional.of(oldUser));
        when(userMapper.userToProfileUpdateDTO(savedUser))
                .thenReturn(profileDTO);

        //When
        ProfileUpdateDTO updatedDTO = userService.updateUser(profileDTO);
        //Then
        assertNotNull(updatedDTO, "updatedProfileDto should not be null");
        assertEquals(updatedDTO.newUsername(), profileDTO.newUsername());
    }


}