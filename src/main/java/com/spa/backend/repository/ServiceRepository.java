package com.spa.backend.repository;

import com.spa.backend.model.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
	java.util.List<ServiceEntity> findByStatus(String status);
}
