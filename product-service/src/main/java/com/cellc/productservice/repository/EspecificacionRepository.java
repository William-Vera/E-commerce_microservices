package com.cellc.productservice.repository;

import com.cellc.productservice.entity.Especificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EspecificacionRepository extends JpaRepository<Especificacion, Long> {
    Optional<Especificacion> findByProductoId(Long productoId);
}
