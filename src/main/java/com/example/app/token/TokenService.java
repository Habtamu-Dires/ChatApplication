package com.example.app.token;

import com.example.app.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;

    public Optional<Token> findByTokenValue(String token){
       return tokenRepository.findByTokenValue(token);
    }

    public void saveToken(Token token){
        tokenRepository.save(token);
    }

}
