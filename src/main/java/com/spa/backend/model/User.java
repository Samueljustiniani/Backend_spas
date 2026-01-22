package com.spa.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Long id;

    @Column(name = "google_id", unique = true)
    private String googleId;

    @Column(name = "proveedor_auth", nullable = false)
    private String proveedorAuth;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "lastname")
    private String lastname;

    @Column(name = "phone")
    private String phone;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "status")
    private String status;

    @Column(name = "photo_url")
    private String photoUrl;
}
