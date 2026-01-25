package com.spa.backend.config;

import com.spa.backend.security.JwtAuthenticationEntryPoint;
import com.spa.backend.security.JwtAuthenticationFilter;
import com.spa.backend.security.JwtUtil;
import com.spa.backend.service.impl.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final com.spa.backend.security.CustomOAuth2UserService customOAuth2UserService;
    private final JwtUtil jwtUtil;

    public SecurityConfig(
        JwtAuthenticationFilter jwtAuthenticationFilter,
        JwtAuthenticationEntryPoint unauthorizedHandler,
        com.spa.backend.security.CustomOAuth2UserService customOAuth2UserService,
        JwtUtil jwtUtil
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.unauthorizedHandler = unauthorizedHandler;
        this.customOAuth2UserService = customOAuth2UserService;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(org.springframework.security.config.Customizer.withDefaults())
            .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))
            // Cambiado a IF_REQUIRED para permitir sesiones durante OAuth2
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                // Permitir acceso sin autenticación solo a los endpoints necesarios para login/registro/OAuth
                .requestMatchers("/v1/api/auth/login", "/v1/api/auth/register", "/v1/api/auth/oauth2/success").permitAll()
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                .requestMatchers("/actuator/**", "/static/**", "/", "/index.html", "/login.html").permitAll()
                .requestMatchers(HttpMethod.GET, "/v1/api/public/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/v1/api/services/**").permitAll()
                .anyRequest().authenticated()
            )
            // OAuth2 login solo se activa cuando el usuario va a /oauth2/authorization/google
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcUserService()))
                .successHandler((request, response, authentication) -> {
                    // Obtener el email del usuario de Google
                    String email = null;
                    Object principal = authentication.getPrincipal();
                    if (principal instanceof org.springframework.security.oauth2.core.oidc.user.OidcUser) {
                        email = ((org.springframework.security.oauth2.core.oidc.user.OidcUser) principal).getEmail();
                    } else if (principal instanceof OAuth2User) {
                        email = ((OAuth2User) principal).getAttribute("email");
                    }
                    
                    // Generar JWT token
                    String token = jwtUtil.generateToken(email);
                    
                    // Invalidar la sesión HTTP después de obtener el token
                    request.getSession().invalidate();
                    
                    // Redirigir directamente al frontend con el token en query string
                    String frontendRedirect = "http://localhost:4200/auth/callback?token=" + token;
                    response.sendRedirect(frontendRedirect);
                })
            );

        // Filtro JWT se ejecuta antes para validar tokens en requests normales
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Servicio OIDC personalizado que guarda usuarios de Google en la base de datos
     */
    private org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService oidcUserService() {
        org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService delegate = 
            new org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService();
        
        return new org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService() {
            @Override
            public org.springframework.security.oauth2.core.oidc.user.OidcUser loadUser(
                    org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest userRequest) {
                
                org.springframework.security.oauth2.core.oidc.user.OidcUser oidcUser = delegate.loadUser(userRequest);
                
                // Llamar a nuestro servicio personalizado para guardar el usuario
                return customOAuth2UserService.processOidcUser(oidcUser);
            }
        };
    }
}
