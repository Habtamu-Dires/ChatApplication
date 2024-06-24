package com.example.app.security_config;

import com.example.app.jwt.JwtAuthenticationFilter;
import com.example.app.jwt.LogoutRequestHandler;
import com.example.app.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailService customUserDetailService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final LogoutRequestHandler logoutRequestHandler;

    @Value("${frontend.url}")
    String frontendUrl;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        http.authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/api/v1/auth/**").permitAll()
            .requestMatchers("/ws/**").permitAll()
            .anyRequest().authenticated()
        );
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        http.logout(logout -> logout
                .logoutUrl("/api/v1/logout")
                .addLogoutHandler(logoutRequestHandler)
                .logoutSuccessHandler(((request, response, authentication) -> {
                    SecurityContextHolder.clearContext();
                }))
        );

        http.cors(cors -> cors
            .configurationSource(request -> {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOriginPatterns(List.of(frontendUrl));
                configuration.setAllowedHeaders(List.of("Origin", "Content-Type", "Accept", "Authorization"));
                configuration.setAllowedMethods(List.of("*"));
                configuration.setAllowCredentials(true);
                return configuration;
            }));

        return http.build();
    }

    /*
     *  AuthenticationConfiguration:
            This is a Spring Security configuration object that holds  all the
            necessary configurations for authentication.
     * getAuthenticationManager():
            This method retrieves the AuthenticationManager instance that
            Spring Security has built based on the provided configurations
    * */

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
 /*custom Authentication Provider  --> not necessary though because
 The default provider ('DaoAuthenticationProvider') uses the UserDetailsService to retrieve user details and a
    password encoder to validate passwords.
    if one wants further customization he/she can use it the bellow configuration
    to make custom authentication provider which will be used by authentication manager
  */
//    @Bean
//    AuthenticationProvider authenticationProvider(){
//        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
//        authProvider.setUserDetailsService(customUserDetailService);
//        authProvider.setPasswordEncoder(passwordEncoder());
//        return authProvider;
//    }

}
