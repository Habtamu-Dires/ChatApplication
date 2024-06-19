package com.example.whatsapp.registration;

import lombok.Builder;

@Builder
public record AuthenticationResponse( String username, String password) { }
