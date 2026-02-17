package com.spa.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final com.spa.backend.service.impl.CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, com.spa.backend.service.impl.CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        
        log.debug("=== JWT FILTER === {} {}", method, requestUri);
        
        String header = request.getHeader("Authorization");
        
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            log.debug("Token found in Authorization header");
            
            if (jwtUtil.validate(token)) {
                String email = jwtUtil.getSubject(token);
                log.info("JWT validated successfully for user: {}", email);
                
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
                
                log.debug("User {} authenticated successfully", email);
            } else {
                log.warn("Invalid JWT token for request: {} {}", method, requestUri);
            }
        } else {
            log.debug("No Bearer token found in request: {} {}", method, requestUri);
        }
        
        filterChain.doFilter(request, response);
    }
}
