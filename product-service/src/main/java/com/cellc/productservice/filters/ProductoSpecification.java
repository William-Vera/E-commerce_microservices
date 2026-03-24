package com.cellc.productservice.filters;

import com.cellc.productservice.entity.Producto;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

public class ProductoSpecification {

    public static Specification<Producto> filtrar(
            String nombre,
            Long categoriaId,
            Long marcaId,
            Double precioMin,
            Double precioMax
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (nombre != null && !nombre.isEmpty()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("nombre")),
                                "%" + nombre.toLowerCase() + "%"
                        )
                );
            }

            if (categoriaId != null) {
                predicates.add(cb.equal(root.get("categoriaId"), categoriaId));
            }

            if (marcaId != null) {
                predicates.add(cb.equal(root.get("marcaId"), marcaId));
            }

            if (precioMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("precio"), precioMin));
            }

            if (precioMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("precio"), precioMax));
            }

            predicates.add(cb.equal(root.get("estado"), true));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
