package com.apigateway.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
    	
    	String path = request.getRequestURI();

        // SKIP AUTH APIs (register, login)
    	if (path.equals("/api/auth/signin") ||
    		    path.equals("/api/auth/signup")) {
    		    filterChain.doFilter(request, response);
    		    return;
    		}


        // SKIP PREFLIGHT REQUESTS
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

    	   System.out.println(">>> Incoming: " + request.getMethod() + " " + request.getRequestURI());

    	    String authHeader = request.getHeader("Authorization");
    	    System.out.println(">>> Authorization header: " + authHeader);

        String token = resolveToken(request);

        try {
            if (token != null) {

                // token = header.payload.signature
                String[] parts = token.split("\\.");
                if (parts.length != 3) {
                    throw new IllegalArgumentException("Invalid JWT format");
                }

                // decode payload (second part)
                String payloadJson = new String(
                        Base64.getUrlDecoder().decode(parts[1]),
                        StandardCharsets.UTF_8
                );

                // convert JSON payload to Map
                Map<String, Object> claims = objectMapper.readValue(
                        payloadJson,
                        new TypeReference<Map<String, Object>>() {}
                );

                String username = (String) claims.get("sub");
                Object rolesObj = claims.get("roles");

                List<String> roles;
                if (rolesObj instanceof List<?>) {
                    roles = ((List<?>) rolesObj).stream()
                            .map(String::valueOf)
                            .collect(Collectors.toList());
                } else {
                    roles = List.of();
                }

                var authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new) // ROLE_ADMIN, ROLE_USER
                        .collect(Collectors.toList());

                System.out.println("Gateway decoded JWT -> user: " + username +
                        ", roles: " + roles);

                //  Build Authentication
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                username, null, authorities);
                auth.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                //  Put it into a fresh SecurityContext
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(auth);
                SecurityContextHolder.setContext(context);
            }
        } catch (Exception e) {
            System.out.println("Gateway JWT decode error: " + e.getMessage());
            SecurityContextHolder.clearContext();
        }

        // continue chain
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
