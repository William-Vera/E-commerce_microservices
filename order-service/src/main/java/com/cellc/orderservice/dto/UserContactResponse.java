package com.cellc.orderservice.dto;

public record UserContactResponse(
        Long id,
        String nombreCompleto,
        String email
) {
}
