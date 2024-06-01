package com.example.whatsapp.user.dtos;

import lombok.Builder;

@Builder
public record UserDTO(
                        String firstName,
                        String lastName,
                        String username,
                        String phoneNumber) {}
