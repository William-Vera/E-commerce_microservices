package com.cellc.userservice.dto;

import com.cellc.userservice.entity.Role;
import com.cellc.userservice.entity.User;
import com.cellc.userservice.repository.RoleRepository;
import com.cellc.userservice.repository.UserRepository;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String refreshToken;

    public AuthResponse(String token) {
        this.token=token;
    }
}