package com.cellc.productservice.dto;

import com.cellc.productservice.entity.Marca;

public record MarcaDto(
        Long id,
        String nombre
) {
    public static MarcaDto from(Marca marca) {
        return new MarcaDto(marca.getId(), marca.getNombre());
    }
}
