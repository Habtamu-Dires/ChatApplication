package com.example.app.user.dtos;

import lombok.Builder;

@Builder
public record UserDTO(
                        String firstName,
                        String lastName,
                        String username,
                        String phoneNumber) {}
