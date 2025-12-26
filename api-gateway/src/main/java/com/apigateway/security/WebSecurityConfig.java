package com.apigateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // Disable CSRF for APIs
            .csrf(csrf -> csrf.disable())

            // ENABLE CORS (VERY IMPORTANT)
            .cors(cors -> {})

            // Stateless (JWT)
            .sessionManagement(sess ->
                sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth

                // ALLOW PREFLIGHT REQUESTS
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
              //AUTH APIs (ONLY signin/signup)
                .requestMatchers(
                        "/api/auth/signin",
                        "/api/auth/signup"
                    ).permitAll()

                // CHANGE PASSWORD (PROTECTED)
                .requestMatchers("/api/auth/change-password")
                    .hasAnyAuthority("ROLE_USER")

                
                // Search flights public
                .requestMatchers(HttpMethod.POST, "/flights/search").permitAll()
                
             //  dropdown APIs
                .requestMatchers(HttpMethod.GET,
                    "/flights/sources",
                    "/flights/destinations"
                ).permitAll()

                // ADMIN only
                .requestMatchers(HttpMethod.POST, "/flights")
                    .hasAuthority("ROLE_ADMIN")

                // Flight APIs
                .requestMatchers(HttpMethod.GET, "/flights/**")
                    .hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                .requestMatchers(HttpMethod.POST, "/flights/**")
                    .hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")

                // Booking APIs
                .requestMatchers("/booking/**")
                    .hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")

                // Everything else secured
                .anyRequest().authenticated()
            )

            // JWT filter
            .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
