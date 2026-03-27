package com.cellc.productservice.dto;

import jakarta.validation.constraints.NotBlank;

public record ProductImageRequest(
        @NotBlank String url,
        Boolean esPrincipal
) {
}
