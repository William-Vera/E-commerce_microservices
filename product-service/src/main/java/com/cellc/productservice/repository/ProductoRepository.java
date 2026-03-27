package com.cellc.productservice.repository;

import com.cellc.productservice.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ProductoRepository extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {

    Page<Producto> findByEstadoTrue(Pageable pageable);

    Page<Producto> findByCategoriaId(Long categoriaId, Pageable pageable);

    @Query("select min(p.precio) from Producto p where p.estado = true")
    Double findMinPriceByEstadoTrue();

    @Query("select max(p.precio) from Producto p where p.estado = true")
    Double findMaxPriceByEstadoTrue();

}
