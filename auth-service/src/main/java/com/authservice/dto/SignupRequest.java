package com.authservice.dto;

import java.util.List;

import lombok.Data;

@Data
public class SignupRequest {

	private String username;
	private String email;
	private String password;
	
	private List<String> roles;
	
	
}
