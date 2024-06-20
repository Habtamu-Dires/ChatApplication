package com.example.app.registration;

import lombok.Builder;

@Builder
public record AuthenticationResponse( String username, String password) { }
