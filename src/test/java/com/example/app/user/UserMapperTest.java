package com.example.app.user;

import com.example.app.user.dtos.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserMapperTest {

    //tested class
    private UserMapper userMapper;


    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
    }

    @Test
    public void shouldMapUserToUserDto(){
        //Given
        User user = User.builder()
                .username("abe")
                .firstName("abeb")
                .lastName("KEBEDE")
                .phoneNumber("123457654")
                .build();
        //When
        UserDTO userDTO = userMapper.userToDTO(user);
        //Then
       assertEquals(user.getUsername(), userDTO.username());
       assertEquals(user.getFirstName(), userDTO.firstName());
       assertEquals(user.getLastName(), userDTO.lastName());
       assertEquals(user.getPhoneNumber(), userDTO.phoneNumber());
    }
    
    @Test
    public void shouldThrowNullPointerExceptionWhenUserIsNull(){
        var exp = assertThrows(NullPointerException.class, () -> userMapper.userToDTO(null));
        assertEquals(exp.getMessage(), "The User Should Not Be Null");
    }
}