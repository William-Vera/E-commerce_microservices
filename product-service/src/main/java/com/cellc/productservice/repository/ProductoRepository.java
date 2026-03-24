package com.cellc.productservice.repository;

import com.cellc.productservice.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductoRepository extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {

    Page<Producto> findByEstadoTrue(Pageable pageable);

    Page<Producto> findByCategoriaId(Long categoriaId, Pageable pageable);

}
