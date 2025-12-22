package com.authservice.service;

import com.authservice.dto.ChangePasswordRequest;

public interface AuthService {

	String changePassword(ChangePasswordRequest req);

}
