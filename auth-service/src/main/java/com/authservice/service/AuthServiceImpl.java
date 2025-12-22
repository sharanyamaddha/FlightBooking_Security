package com.authservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.authservice.dto.ChangePasswordRequest;
import com.authservice.model.User;
import com.authservice.repository.UserRepository;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Override
    public String changePassword(ChangePasswordRequest req) {

        // Get logged-in username from JWT
        String username = SecurityContextHolder.getContext()
            .getAuthentication().getName();

        System.out.println("JWT USER = " + username);

        // Fetch user using username 
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("OLD HASH = " + user.getPassword());

        // Validate old password
        if (!encoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password incorrect");
        }

        // Encode & save new password
        user.setPassword(encoder.encode(req.getNewPassword()));
        userRepository.save(user);

        System.out.println("NEW HASH = " + user.getPassword());

        return "Password changed successfully";
    }

}
