package com.spa.backend.security;

import com.spa.backend.model.User;
import com.spa.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    /**
     * Procesa usuarios OIDC (Google usa OIDC, no OAuth2 simple)
     */
    @Transactional
    public OidcUser processOidcUser(OidcUser oidcUser) {
        String email = oidcUser.getEmail();
        String fullName = oidcUser.getFullName();
        String googleId = oidcUser.getSubject();
        String photoUrl = oidcUser.getPicture();

        log.info("========================================");
        log.info("=== GOOGLE OIDC LOGIN ===");
        log.info("Email: {}", email);
        log.info("Name: {}", fullName);
        log.info("Google ID: {}", googleId);
        log.info("Photo URL: {}", photoUrl);
        log.info("========================================");

        // Separar nombre y apellido
        final String firstName;
        final String lastName;
        if (fullName != null && fullName.contains(" ")) {
            String[] parts = fullName.split(" ", 2);
            firstName = parts[0];
            lastName = parts[1];
        } else {
            firstName = fullName != null ? fullName : "Usuario";
            lastName = null;
        }

        // Buscar o crear usuario
        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user == null) {
            log.info(">>> Usuario NO existe, CREANDO nuevo usuario...");
            User newUser = User.builder()
                .email(email)
                .name(firstName)
                .lastname(lastName)
                .googleId(googleId)
                .proveedorAuth("GOOGLE")
                .photoUrl(photoUrl)
                .role("ROLE_USER")
                .status("A")
                .createdAt(OffsetDateTime.now())
                .build();
            user = userRepository.save(newUser);
            log.info(">>> Usuario CREADO con ID: {}", user.getId());
        } else {
            log.info(">>> Usuario YA existe con ID: {}", user.getId());
            // Actualizar foto si cambió
            boolean updated = false;
            if (photoUrl != null && !photoUrl.equals(user.getPhotoUrl())) {
                user.setPhotoUrl(photoUrl);
                updated = true;
            }
            if (googleId != null && user.getGoogleId() == null) {
                user.setGoogleId(googleId);
                user.setProveedorAuth("GOOGLE");
                updated = true;
            }
            if (updated) {
                userRepository.save(user);
                log.info(">>> Usuario ACTUALIZADO");
            }
        }

        return oidcUser;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        log.info("=== OAuth2 loadUser called (non-OIDC) ===");
        return super.loadUser(userRequest);
    }
}
