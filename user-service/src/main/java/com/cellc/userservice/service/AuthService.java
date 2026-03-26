package com.cellc.userservice.service;

import com.cellc.userservice.dto.AuthResponse;
import com.cellc.userservice.dto.LoginRequest;
import com.cellc.userservice.dto.RegisterRequest;
import com.cellc.userservice.entity.Role;
import com.cellc.userservice.entity.User;
import com.cellc.userservice.entity.UserRole;
import com.cellc.userservice.messaging.UserEventPublisher;
import com.cellc.userservice.repository.RoleRepository;
import com.cellc.userservice.repository.UserRepository;
import com.cellc.userservice.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final UserRoleRepository userRoleRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserEventPublisher userEventPublisher;

    public AuthResponse register(RegisterRequest request) {

        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email ya registrado");
        }

        User user = new User();
        user.setNombre(request.getNombre());
        user.setApellido(request.getApellido());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user = userRepo.save(user);

        // El primer usuario es ADMIN, los demás USER
        long userCount = userRepo.count();
        String roleName = (userCount == 1) ? "ADMIN" : "USER";

        Role role = roleRepo.findByNombre(roleName)
                .orElseThrow(() -> new RuntimeException("Rol " + roleName + " no encontrado en BD"));

        userRoleRepo.save(new UserRole(user.getId(), role.getId()));
        userEventPublisher.publishUserRegistered(user);

        String token = jwtService.generateToken(user.getId(), roleName);

        return AuthResponse.builder()
                .token(token)
                .build();
    }

    private final RefreshTokenService refreshService;

    public AuthResponse login(LoginRequest request) {

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        String role = userRoleRepo.findByUserId(user.getId())
                .stream()
                .findFirst()
                .flatMap(ur -> roleRepo.findById(ur.getRoleId()))
                .map(Role::getNombre)
                .orElse("USER");

        String token = jwtService.generateToken(user.getId(), role);
        String refresh = jwtService.generateRefreshToken(user.getId());

        refreshService.create(user.getId(), refresh);

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refresh)
                .build();
    }
}
