package com.cellc.userservice.dto;

import com.cellc.userservice.entity.User;

import java.time.LocalDateTime;

public record ProfileResponse(
        Long id,
        String nombre,
        String apellido,
        String email,
        String telefono,
        String direccion,
        Boolean estado,
        LocalDateTime fechaCreacion,
        String role
) {
    public static ProfileResponse from(User user, String role) {
        return new ProfileResponse(
                user.getId(),
                user.getNombre(),
                user.getApellido(),
                user.getEmail(),
                user.getTelefono(),
                user.getDireccion(),
                user.getEstado(),
                user.getFechaCreacion(),
                role
        );
    }
}
