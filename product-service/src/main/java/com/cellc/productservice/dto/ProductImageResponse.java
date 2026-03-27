package com.cellc.productservice.dto;

import com.cellc.productservice.entity.ImagenProducto;

public record ProductImageResponse(
        Long id,
        String url,
        Boolean esPrincipal,
        Long productoId
) {
    public static ProductImageResponse from(ImagenProducto image) {
        return new ProductImageResponse(
                image.getId(),
                image.getUrl(),
                image.getEsPrincipal(),
                image.getProductoId()
        );
    }
}
