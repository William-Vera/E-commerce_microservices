package com.cellc.userservice.controller;

import com.cellc.userservice.dto.AuthResponse;
import com.cellc.userservice.dto.LoginRequest;
import com.cellc.userservice.dto.LogoutRequest;
import com.cellc.userservice.dto.ProfileResponse;
import com.cellc.userservice.dto.RefreshRequest;
import com.cellc.userservice.dto.RegisterRequest;
import com.cellc.userservice.entity.RefreshToken;
import com.cellc.userservice.entity.Role;
import com.cellc.userservice.service.AuthService;
import com.cellc.userservice.service.JwtService;
import com.cellc.userservice.service.RefreshTokenService;
import com.cellc.userservice.repository.RoleRepository;
import com.cellc.userservice.repository.UserRoleRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;
    private final RefreshTokenService refreshService;
    private final JwtService jwtService;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@RequestBody RefreshRequest request) {
        RefreshToken rt = refreshService.validate(request.getRefreshToken());
        String role = userRoleRepository.findByUserId(rt.getUserId())
                .stream()
                .findFirst()
                .flatMap(userRole -> roleRepository.findById(userRole.getRoleId()))
                .map(Role::getNombre)
                .orElse("USER");
        String newToken = jwtService.generateToken(rt.getUserId(), role);
        return ResponseEntity.ok(Map.of("accessToken", newToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestBody LogoutRequest request) {
        refreshService.revoke(request.getRefreshToken());
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> profile(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return ResponseEntity.ok(service.getProfile(userId));
    }
}
