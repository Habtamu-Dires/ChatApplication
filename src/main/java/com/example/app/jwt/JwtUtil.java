package com.example.app.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${secret.key}")
    private String SECRET_KEY;

    //create token using username only
    public String createToken(String username){
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()
                            + 1000 * 60 * 60 * 24)) // expire after a day
                .signWith(getSignInKey(),SignatureAlgorithm.HS256)
                .compact();
    }

    //create token with extra claim
    public String createToken(String username, Map<String, Object> extraClaim){
        return Jwts.builder()
                .setSubject(username)
                .setClaims(extraClaim)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()
                            + 1000 * 60 * 60 * 24 )) // expire after a day
                .signWith(getSignInKey(),SignatureAlgorithm.HS256)
                .compact();
    }

     private Key getSignInKey(){
            byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
            return Keys.hmacShaKeyFor(keyBytes);
     }

     //extract all claims
    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey()) // set the key used to sign the token
                .build()
                .parseClaimsJws(token)  // parse the token
                .getBody(); // get the claims (payload) from the token
    }
    //extract specific claim form the token
    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        Claims claims = extractAllClaims(token); // extract all claims
        return claimResolver.apply(claims); // apply claim resolver
    }

    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }

    private Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date(System.currentTimeMillis()));
    }

    public boolean isTokenValid(String token, String username){
        String tokenUsername = extractUsername(token);
        return tokenUsername.equals(username) && !isTokenExpired(token);
    }
}
