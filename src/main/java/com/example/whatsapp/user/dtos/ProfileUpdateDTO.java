package com.example.whatsapp.user.dtos;


import lombok.Builder;

@Builder
public record ProfileUpdateDTO(
        String oldUsername,
        String newUsername,
        String password,
        String firstName,
        String lastName,
        String phoneNumber) {}

