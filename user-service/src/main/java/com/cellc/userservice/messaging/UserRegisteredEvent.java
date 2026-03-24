package com.cellc.userservice.messaging;

import java.time.Instant;

public record UserRegisteredEvent(
        Long userId,
        String nombre,
        String apellido,
        String email,
        Instant occurredAt
) {
}
