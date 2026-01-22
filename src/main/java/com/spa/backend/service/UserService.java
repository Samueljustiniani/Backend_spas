package com.spa.backend.service;

import com.spa.backend.dto.UserDTO;

import java.util.List;

public interface UserService {
    List<UserDTO> findAll();
    List<UserDTO> findAllInactivos();
    List<UserDTO> findAllUsuarios();
    UserDTO findById(Long id);
    UserDTO create(UserDTO dto);
    UserDTO update(Long id, UserDTO dto);
    boolean delete(Long id);
    boolean isOwner(Long id, String email);
}
