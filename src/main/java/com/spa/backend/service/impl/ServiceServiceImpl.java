package com.spa.backend.service.impl;

import com.spa.backend.model.ServiceEntity;
import com.spa.backend.repository.ServiceRepository;
import com.spa.backend.service.ServiceService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository repo;

    public ServiceServiceImpl(ServiceRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<ServiceEntity> findAll() { return repo.findAll(); }

    @Override
    public List<ServiceEntity> findByStatus(String status) { return repo.findByStatus(status); }

    @Override
    public Optional<ServiceEntity> findById(Long id) { return repo.findById(id); }

    @Override
    public ServiceEntity save(ServiceEntity service) {
        if (service.getStatus() == null || service.getStatus().isEmpty()) {
            service.setStatus("A");
        }
        return repo.save(service);
    }

    @Override
    public void deleteById(Long id) { repo.deleteById(id); }
}
