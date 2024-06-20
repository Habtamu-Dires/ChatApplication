package com.example.app.user.dtos;


import lombok.Builder;

@Builder
public record ProfileUpdateDTO(
        String oldUsername,
        String newUsername,
        String password,
        String firstName,
        String lastName,
        String phoneNumber) {}

