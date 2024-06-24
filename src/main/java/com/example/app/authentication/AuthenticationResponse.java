package com.example.app.authentication;

import lombok.Builder;

@Builder
public record AuthenticationResponse(String username, String jwtToken) { }
