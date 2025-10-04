package com.example.chatpoc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // nouvelle syntaxe
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/ws-chat/**", "/app/**", "/topic/**").permitAll()
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}
