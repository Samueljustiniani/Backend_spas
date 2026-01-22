package com.spa.backend.service;

import com.spa.backend.dto.AuthRequest;
import com.spa.backend.model.User;

public interface AuthService {
    String authenticate(AuthRequest request);
    User register(User customer, String rawPassword);
}
