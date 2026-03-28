package com.cellc.userservice.service;

import com.cellc.userservice.dto.AuthResponse;
import com.cellc.userservice.dto.LoginRequest;
import com.cellc.userservice.dto.ProfileResponse;
import com.cellc.userservice.dto.RegisterRequest;
import com.cellc.userservice.entity.Role;
import com.cellc.userservice.entity.User;
import com.cellc.userservice.entity.UserRole;
import com.cellc.userservice.messaging.UserEventPublisher;
import com.cellc.userservice.repository.RoleRepository;
import com.cellc.userservice.repository.UserRepository;
import com.cellc.userservice.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final UserRoleRepository userRoleRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserEventPublisher userEventPublisher;
    private final RefreshTokenService refreshService;

    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (userRepo.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email ya registrado");
        }

        User user = new User();
        user.setNombre(request.getNombre());
        user.setApellido(request.getApellido());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user = userRepo.save(user);

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

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (Boolean.FALSE.equals(user.getEstado())) {
            throw new RuntimeException("El usuario esta inactivo");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Credenciales invalidas");
        }

        String role = resolveRole(user.getId());
        String token = jwtService.generateToken(user.getId(), role);
        String refresh = jwtService.generateRefreshToken(user.getId());

        refreshService.create(user.getId(), refresh);

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refresh)
                .build();
    }

    public ProfileResponse getProfile(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ProfileResponse.from(user, resolveRole(userId));
    }

    private String resolveRole(Long userId) {
        return userRoleRepo.findByUserId(userId)
                .stream()
                .findFirst()
                .flatMap(ur -> roleRepo.findById(ur.getRoleId()))
                .map(Role::getNombre)
                .orElse("USER");
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }
}
