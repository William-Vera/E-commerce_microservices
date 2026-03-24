package com.cellc.productservice.repository;

import com.cellc.productservice.entity.ImagenProducto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImagenProductoRepository extends JpaRepository<ImagenProducto, Long> {
    List<ImagenProducto> findImagenProductoById(Long productoId);

    List<ImagenProducto> findByProductoId(Long productoId);
}
