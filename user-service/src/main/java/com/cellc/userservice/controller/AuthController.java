package com.cellc.userservice.controller;

import com.cellc.userservice.dto.AuthResponse;
import com.cellc.userservice.dto.LoginRequest;
import com.cellc.userservice.dto.RefreshRequest;
import com.cellc.userservice.dto.RegisterRequest;
import com.cellc.userservice.entity.RefreshToken;
import com.cellc.userservice.service.AuthService;
import com.cellc.userservice.service.JwtService;
import com.cellc.userservice.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
        String newToken = jwtService.generateToken(rt.getUserId(), "USER");
        return ResponseEntity.ok(Map.of("accessToken", newToken));
    }
}
