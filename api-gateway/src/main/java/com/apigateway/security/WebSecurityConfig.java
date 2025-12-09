package com.apigateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable());

       
        http.addFilterBefore(new JwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        http.authorizeHttpRequests(auth -> auth
                
                .requestMatchers("/api/auth/**").permitAll()

                // ADMIN ONLY — add flights
                .requestMatchers(HttpMethod.POST, "/flights")
                        .hasAuthority("ROLE_ADMIN")

                // USER or ADMIN — other flight endpoints
                .requestMatchers("/flights/**")
                        .hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                // USER or ADMIN — booking endpoints
                .requestMatchers("/booking/**")
                        .hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                // everything else must be authenticated
                .anyRequest().authenticated()
        );

        return http.build();
    }
}
