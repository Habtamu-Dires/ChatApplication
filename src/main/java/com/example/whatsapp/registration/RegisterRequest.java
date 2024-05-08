package com.example.whatsapp.registration;

import lombok.Builder;


@Builder
public record RegisterRequest(
         String phoneNumber,
         String firstName,
         String lastName,
         String username,
         String password
) {

}