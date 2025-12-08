package com.authservice.dto;

import java.util.List;

import lombok.Data;

@Data
public class JwtResponse {

	private String id;
	private String username;
    private String email;
    private List<String> roles;
}
