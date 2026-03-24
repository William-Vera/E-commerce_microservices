package com.cellc.productservice.controller;

import com.cellc.productservice.dto.PageResponseDto;
import com.cellc.productservice.dto.ProductoDto;
import com.cellc.productservice.entity.Producto;
import com.cellc.productservice.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService service;

    @GetMapping("/buscar")
    public PageResponseDto<ProductoDto> buscar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long marcaId,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,

            @RequestParam(defaultValue = "precio") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return service.buscar(
                nombre,
                categoriaId,
                marcaId,
                precioMin,
                precioMax,
                page,
                size,
                sortBy,
                direction
        );
    }

    @GetMapping("/categoria/{id}")
    public PageResponseDto<ProductoDto> porCategoria(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.porCategoria(id, page, size);
    }

    @GetMapping("/{id}")
    public ProductoDto obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public Producto crear(@RequestBody Producto producto) {
        return service.crear(producto);
    }

    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id,
                               @RequestBody Producto producto) {
        return service.actualizar(id, producto);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
