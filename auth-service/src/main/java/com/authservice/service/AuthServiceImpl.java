package com.authservice.service;

import org.springframework.beans.factory.annotation.Autowired;
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
    	  System.out.println(">>> changePassword SERVICE method HIT");

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("OLD HASH = " + user.getPassword());


        if(!encoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password incorrect!");
        }

        user.setPassword(encoder.encode(req.getNewPassword()));

       
        userRepository.save(user);
        System.out.println("NEW HASH = " + user.getPassword());

        return "Password changed successfully";
    }
}
