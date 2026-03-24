package com.cellc.productservice.mapper;

import com.cellc.productservice.dto.ProductoDto;
import com.cellc.productservice.entity.ImagenProducto;
import com.cellc.productservice.entity.Producto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductoMapper {

    public ProductoDto toDTO(Producto p, List<ImagenProducto> imagenes) {
        ProductoDto dto = new ProductoDto();

        dto.setId(p.getId());
        dto.setNombre(p.getNombre());
        dto.setDescripcion(p.getDescripcion());
        dto.setPrecio(p.getPrecio());
        dto.setStock(p.getStock());
        dto.setEstado(p.getEstado());
        dto.setCategoriaId(p.getCategoriaId());
        dto.setMarcaId(p.getMarcaId());

        dto.setImagenes(
                imagenes.stream().map(ImagenProducto::getUrl).toList()
        );

        return dto;
    }
}
