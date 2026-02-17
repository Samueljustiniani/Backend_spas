package com.spa.backend.rest;

import com.spa.backend.dto.AuthRequest;
import com.spa.backend.dto.AuthResponse;
import com.spa.backend.model.User;
import com.spa.backend.service.AuthService;
import com.spa.backend.repository.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        log.info("=== LOGIN REQUEST ===");
        log.info("Email: {}", request.getEmail());
        
        try {
            String token = authService.authenticate(request);
            log.info("Login successful for user: {}", request.getEmail());
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (Exception e) {
            log.error("Login failed for user {}: {}", request.getEmail(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User customer) {
        log.info("=== REGISTER REQUEST ===");
        log.info("Email: {}", customer.getEmail());
        log.info("Name: {}", customer.getName());
        
        try {
            User saved = authService.register(customer, customer.getPassword());
            saved.setPassword(null);
            log.info("User registered successfully: {}", saved.getEmail());
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error("Registration failed for {}: {}", customer.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Endpoint que se llama después de login exitoso con Google OAuth2
     * Recibe el token como parámetro y lo devuelve
     */
    @GetMapping("/oauth2/success")
    public ResponseEntity<AuthResponse> oauth2Success(@RequestParam(required = false) String token) {
        log.info("=== OAUTH2 SUCCESS ===");
        
        if (token == null || token.isEmpty()) {
            log.warn("OAuth2 success called without token");
            return ResponseEntity.status(401).build();
        }
        
        log.info("OAuth2 token received successfully");
        return ResponseEntity.ok(new AuthResponse(token));
    }

    /**
     * Endpoint temporal para verificar usuarios registrados (SOLO PARA DEBUG)
     */
    @GetMapping("/debug/users")
    public ResponseEntity<List<User>> debugUsers() {
        List<User> users = userRepository.findAll();
        // Ocultar passwords
        users.forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(users);
    }

    /**
     * Devuelve el usuario actualmente autenticado (sin password).
     * Se puede usar desde el frontend tras obtener el JWT.
     */
    @GetMapping("/me")
    public ResponseEntity<User> me(java.security.Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        String email = principal.getName();
        return userRepository.findByEmail(email)
                .map(u -> {
                    u.setPassword(null);
                    return ResponseEntity.ok(u);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
