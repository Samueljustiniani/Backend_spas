package com.spa.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor             
@AllArgsConstructor             
public class UserDTO {

    private Long id;
    private String email;
    private String name;
    private String lastname;
    private String phone;
    private String role;
    private String status;
    private OffsetDateTime createdAt;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
}
