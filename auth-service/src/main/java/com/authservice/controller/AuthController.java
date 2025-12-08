package com.authservice.controller;

import com.authservice.dto.*;
import com.authservice.model.ERole;
import com.authservice.model.User;
import com.authservice.repository.UserRepository;
import com.authservice.security.JwtUtils;
import com.authservice.service.UserDetailsImpl;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder encoder;
    @Autowired private JwtUtils jwtUtils;


    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtUtils.generateJwtToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList();

        JwtResponse jwtResponse = new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles
        );

        return ResponseEntity.ok(jwtResponse);
    }




    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest req) {

        if (userRepository.existsByUsername(req.getUsername())) {
            return badRequest("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(req.getEmail())) {
            return badRequest("Error: Email is already in use!");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setRoles(resolveRoles(req.getRoles()));

        userRepository.save(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new MessageResponse("User registered successfully!"));
    }


    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtUtils.getCleanJwtCookie().toString())
                .body(new MessageResponse("You've been signed out!"));
    }



    private ResponseEntity<?> badRequest(String message) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse(message));
    }

    private List<String> resolveRoles(List<String> rolesFromRequest) {

        if (rolesFromRequest == null || rolesFromRequest.isEmpty()) {
            return List.of(ERole.ROLE_USER.name());
        }

        List<String> roles = new ArrayList<>();
        rolesFromRequest.forEach(role -> {
            if ("admin".equalsIgnoreCase(role)) {
                roles.add(ERole.ROLE_ADMIN.name());
            } else {
                roles.add(ERole.ROLE_USER.name());
            }
        });

        return roles;
    }
}
