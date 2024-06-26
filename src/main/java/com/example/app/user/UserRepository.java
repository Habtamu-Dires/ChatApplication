package com.example.app.user;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    @Query("SELECT u FROM User u WHERE u.username=:username")
    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.phoneNumber=:phoneNumber")
    Optional<User> findByPhoneNumber(String phoneNumber);

    @Query("SELECT u FROM User u WHERE u.username LIKE :username%")
    List<User> searchUsers(String username);
}
