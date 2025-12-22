package com.authservice.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.authservice.dto.ChangePasswordRequest;
import com.authservice.model.User;

public interface UserRepository extends MongoRepository<User,String>{
	
	Optional<User> findByUsername(String username);
	
	Boolean existsByUsername(String username);
	
	Boolean existsByEmail(String email);
	
	Optional<User> findByEmail(String string);


}
