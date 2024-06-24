package com.example.app.jwt;

import com.example.app.security_config.SecurityUser;
import com.example.app.token.Token;
import com.example.app.token.TokenService;
import com.example.app.user.User;
import com.example.app.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.util.StringUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain) throws ServletException, IOException {

        // extract token from request header
        String token = getJwtTokenFromRequest(request);
        if(token == null || jwtUtil.isTokenExpired(token)){
            filterChain.doFilter(request, response);
            return;
        }

        //extract username from token
        String username = jwtUtil.extractUsername(token);

        // get user by username from the database
        User user = userService.findUserByUsername(username);

        // check  token valid by token value from the database
         boolean isTokenRevoked =
                 tokenService.findByTokenValue(token)
                 .map(Token::isRevoked)
                 .orElse(true);

         if(!isTokenRevoked && jwtUtil.isTokenValid(token, user.getUsername())){
             SecurityUser securityUser = new SecurityUser(user);
             UsernamePasswordAuthenticationToken authToken =
                 new UsernamePasswordAuthenticationToken(
                      securityUser,
                      null,
                      securityUser.getAuthorities()
                 );
             authToken.setDetails(
                     new WebAuthenticationDetailsSource().buildDetails(request)
             );
             // set the security context holder
             SecurityContextHolder.getContext().setAuthentication(authToken);
         }

        filterChain.doFilter(request, response);    // pass to the next filter

    }

    private String getJwtTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")){
            return  bearerToken.substring(7);
        }
        return null;
    }
}
