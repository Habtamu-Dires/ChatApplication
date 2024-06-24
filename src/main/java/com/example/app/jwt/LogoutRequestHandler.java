package com.example.app.jwt;

import com.example.app.token.Token;
import com.example.app.token.TokenRepository;
import com.example.app.token.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LogoutRequestHandler implements LogoutHandler {

    private final TokenService tokenService;

    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) {

        String authHeader = request.getHeader("Authorization");
        if(StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")){
            String jwtToken = authHeader.substring(7);
            Optional<Token> tokenObj = tokenService.findByTokenValue(jwtToken);
            tokenObj.ifPresent(token -> {
                token.setRevoked(true);
                tokenService.saveToken(token);
            });
        }
    }
}
