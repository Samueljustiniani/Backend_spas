package com.spa.backend.service.impl;

import com.spa.backend.dto.AuthRequest;
import com.spa.backend.model.User;
import com.spa.backend.repository.UserRepository;
import com.spa.backend.security.JwtUtil;
import com.spa.backend.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String authenticate(AuthRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        return jwtUtil.generateToken(request.getEmail());
    }

    @Override
    public User register(User user, String rawPassword) {
        user.setPassword(passwordEncoder.encode(rawPassword));
        if (user.getCreatedAt() == null) user.setCreatedAt(OffsetDateTime.now());
        if (user.getProveedorAuth() == null) user.setProveedorAuth("LOCAL");
        if (user.getRole() == null) user.setRole("ROLE_USER");
        if (user.getStatus() == null) user.setStatus("A");
        return userRepository.save(user);
    }
}
