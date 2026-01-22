package com.spa.backend.service.impl;

import com.spa.backend.dto.UserDTO;
import com.spa.backend.model.User;
import com.spa.backend.repository.UserRepository;
import com.spa.backend.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.time.OffsetDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDTO create(UserDTO dto) {
        User c = new User();
        c.setEmail(dto.getEmail());
        c.setName(dto.getName());
        c.setLastname(dto.getLastname());
        c.setPhone(dto.getPhone());
        c.setProveedorAuth("LOCAL");
        c.setRole(dto.getRole() != null ? dto.getRole() : "ROLE_USER");
        c.setCreatedAt(OffsetDateTime.now());
        c.setStatus("A");
        // Si se proporciona password, la encriptamos
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            c.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        User saved = userRepository.save(c);
        return toDto(saved);
    }

    @Override
    public UserDTO update(Long id, UserDTO dto) {
        return userRepository.findById(id).map(c -> {
            if (dto.getEmail() != null) c.setEmail(dto.getEmail());
            if (dto.getName() != null) c.setName(dto.getName());
            if (dto.getLastname() != null) c.setLastname(dto.getLastname());
            if (dto.getPhone() != null) c.setPhone(dto.getPhone());
            if (dto.getRole() != null) c.setRole(dto.getRole());
            if (dto.getStatus() != null) c.setStatus(dto.getStatus());
            if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                c.setPassword(passwordEncoder.encode(dto.getPassword()));
            }
            User saved = userRepository.save(c);
            return toDto(saved);
        }).orElse(null);
    }

    @Override
    public boolean delete(Long id) {
        return userRepository.findById(id).map(user -> {
            user.setStatus("I"); // Soft delete
            userRepository.save(user);
            return true;
        }).orElse(false);
    }

    @Override
    public List<UserDTO> findAll() {
        return userRepository.findAll().stream()
            .filter(u -> "A".equals(u.getStatus()))
            .map(this::toDto)
            .collect(Collectors.toList());
        }

        @Override
        public List<UserDTO> findAllInactivos() {
        return userRepository.findAll().stream()
            .filter(u -> "I".equals(u.getStatus()))
            .map(this::toDto)
            .collect(Collectors.toList());
        }

        @Override
        public List<UserDTO> findAllUsuarios() {
        return userRepository.findAll().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public UserDTO findById(Long id) {
        return userRepository.findById(id).map(this::toDto).orElse(null);
    }

    @Override
    public boolean isOwner(Long id, String email) {
        return userRepository.findById(id)
                .map(user -> user.getEmail().equals(email))
                .orElse(false);
    }

    private UserDTO toDto(User c) {
        return UserDTO.builder()
                .id(c.getId())
                .email(c.getEmail())
                .name(c.getName())
                .lastname(c.getLastname())
                .phone(c.getPhone())
                .role(c.getRole())
                .status(c.getStatus())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
