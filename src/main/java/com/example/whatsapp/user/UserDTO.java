package com.example.whatsapp.user;

import lombok.Builder;

import java.util.List;

@Builder
public record UserDTO(
                        String phoneNumber,
                        String firstName,
                        String lastName,
                        String username,
                        String password) {}
