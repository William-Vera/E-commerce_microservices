package com.cellc.productservice.dto;

import jakarta.validation.constraints.NotBlank;

public record CatalogItemRequest(
        @NotBlank String nombre
) {
}
