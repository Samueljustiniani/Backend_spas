package com.spa.backend.service;

import com.spa.backend.model.ServiceEntity;

import java.util.List;
import java.util.Optional;

public interface ServiceService {
    List<ServiceEntity> findAll();
    Optional<ServiceEntity> findById(Long id);
    ServiceEntity save(ServiceEntity service);
    void deleteById(Long id);
}
