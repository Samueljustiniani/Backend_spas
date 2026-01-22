package com.spa.backend.rest;

import com.spa.backend.dto.SaleRequest;
import com.spa.backend.dto.SaleResponse;
import com.spa.backend.service.SaleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/v1/api/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SaleResponse>> list() {
        return ResponseEntity.ok(saleService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SaleResponse> get(@PathVariable Long id) {
        return saleService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isOwner(#userId, principal.username)")
    public ResponseEntity<List<SaleResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(saleService.findByUserId(userId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SaleResponse> create(@RequestBody SaleRequest request) {
        SaleResponse saved = saleService.create(request);
        return ResponseEntity.created(URI.create("/v1/api/sales/" + saved.getId())).body(saved);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SaleResponse> updateStatus(@PathVariable Long id, @RequestParam String status) {
        SaleResponse updated = saleService.updateStatus(id, status);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (saleService.findById(id).isEmpty()) return ResponseEntity.notFound().build();
        saleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
