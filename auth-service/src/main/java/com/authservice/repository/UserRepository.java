package com.authservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.authservice.model.User;

public interface UserRepository extends MongoRepository<User,String>{
	

}
