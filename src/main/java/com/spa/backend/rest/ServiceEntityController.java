package com.spa.backend.rest;

import com.spa.backend.model.ServiceEntity;
import com.spa.backend.service.ServiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/v1/api/services")
public class ServiceEntityController {

    private final ServiceService serviceService;

    public ServiceEntityController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }


    @GetMapping
    public ResponseEntity<List<ServiceEntity>> list(@RequestParam(value = "status", required = false) String status) {
        if (status == null) {
            return ResponseEntity.ok(serviceService.findAll());
        } else {
            return ResponseEntity.ok(serviceService.findByStatus(status));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceEntity> get(@PathVariable Long id) {
        return serviceService.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ServiceEntity> create(@RequestBody ServiceEntity service) {
        ServiceEntity saved = serviceService.save(service);
        return ResponseEntity.created(URI.create("/v1/api/services/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ServiceEntity> update(@PathVariable Long id, @RequestBody ServiceEntity service) {
        return serviceService.findById(id).map(existing -> {
            service.setId(existing.getId());
            ServiceEntity saved = serviceService.save(service);
            return ResponseEntity.ok(saved);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return serviceService.findById(id).map(service -> {
            try {
                service.setStatus("I");
                ServiceEntity updated = serviceService.save(service);
                if ("I".equals(updated.getStatus())) {
                    return ResponseEntity.noContent().<Void>build();
                } else {
                    return ResponseEntity.status(500).<Void>build();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(500).<Void>build();
            }
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ServiceEntity> restore(@PathVariable Long id) {
        return serviceService.findById(id).map(service -> {
            service.setStatus("A");
            ServiceEntity restored = serviceService.save(service);
            return ResponseEntity.ok(restored);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
