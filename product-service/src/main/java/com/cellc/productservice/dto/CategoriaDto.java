package com.cellc.productservice.dto;

import com.cellc.productservice.entity.Categoria;

public record CategoriaDto(
        Long id,
        String nombre
) {
    public static CategoriaDto from(Categoria categoria) {
        return new CategoriaDto(categoria.getId(), categoria.getNombre());
    }
}
