package com.spa.backend.rest;

import com.spa.backend.dto.AuthRequest;
import com.spa.backend.dto.AuthResponse;
import com.spa.backend.model.User;
import com.spa.backend.service.AuthService;
import com.spa.backend.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        String token = authService.authenticate(request);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User customer) {
        User saved = authService.register(customer, customer.getPassword());
        saved.setPassword(null);
        return ResponseEntity.ok(saved);
    }

    /**
     * Endpoint que se llama después de login exitoso con Google OAuth2
     * Recibe el token como parámetro y lo devuelve
     */
    @GetMapping("/oauth2/success")
    public ResponseEntity<AuthResponse> oauth2Success(@RequestParam(required = false) String token) {
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
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
