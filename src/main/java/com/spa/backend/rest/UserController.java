package com.spa.backend.rest;

import com.spa.backend.dto.UserDTO;
import com.spa.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/v1/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // =========================
    // LISTADOS (ADMIN)
    // =========================

    // Usuarios ACTIVOS
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> list() {
        return ResponseEntity.ok(userService.findAll());
    }

    // Usuarios INACTIVOS
    @GetMapping("/inactivos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> listInactivos() {
        return ResponseEntity.ok(userService.findAllInactivos());
    }

    // TODOS (activos + inactivos)
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> listAll() {
        return ResponseEntity.ok(userService.findAllUsuarios());
    }

    // =========================
    // CRUD
    // =========================

    // OBTENER POR ID (SOLO NÚMEROS)
    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isOwner(#id, principal.username)")
    public ResponseEntity<UserDTO> get(@PathVariable("id") Long id) {
        UserDTO dto = userService.findById(id);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    // CREAR (ADMIN)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> create(@RequestBody UserDTO dto) {
        UserDTO created = userService.create(dto);
        return ResponseEntity
                .created(URI.create("/v1/api/users/" + created.getId()))
                .body(created);
    }

    // ACTUALIZAR (ADMIN o DUEÑO) — SOLO NÚMEROS
    @PutMapping("/{id:\\d+}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isOwner(#id, principal.username)")
    public ResponseEntity<UserDTO> update(
            @PathVariable("id") Long id,
            @RequestBody UserDTO dto) {

        UserDTO updated = userService.update(id, dto);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    // ELIMINAR (ADMIN) — SOLO NÚMEROS
    @DeleteMapping("/{id:\\d+}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        boolean removed = userService.delete(id);
        if (!removed) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    // =========================
    // RESTORE
    // =========================

    // RESTAURAR USUARIO INACTIVO (ADMIN) — SOLO NÚMEROS
    @PatchMapping("/restore/{id:\\d+}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> restore(@PathVariable("id") Long id) {
        UserDTO user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        user.setStatus("A");
        UserDTO restored = userService.update(id, user);
        return ResponseEntity.ok(restored);
    }

    // =========================
    // CONVERTIR USUARIO EN ADMIN (ADMIN)
    // =========================

    @PatchMapping("/{id}/make-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> makeAdmin(@PathVariable("id") Long id) {
        UserDTO user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        user.setRole("ROLE_ADMIN");
        UserDTO updated = userService.update(id, user);
        return ResponseEntity.ok(updated);
    }
}
