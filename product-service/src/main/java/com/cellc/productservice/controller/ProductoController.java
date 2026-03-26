package com.cellc.productservice.controller;

import com.cellc.productservice.dto.PageResponseDto;
import com.cellc.productservice.dto.ProductoDto;
import com.cellc.productservice.entity.Producto;
import com.cellc.productservice.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService service;

    private void requireAdmin(String role) {
        if (role == null || !role.trim().equalsIgnoreCase("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere rol ADMIN");
        }
    }

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

    @PostMapping({"", "/"})
    public Producto crear(@RequestHeader("X-User-Role") String role,
                          @RequestBody Producto producto) {
        System.out.println("DEBUG ProductoController.crear - role received: '" + role + "'");
        System.out.println("DEBUG ProductoController.crear - producto recibido: nombre='" + producto.getNombre()
                + "', categoriaId=" + producto.getCategoriaId()
                + ", marcaId=" + producto.getMarcaId() + ")");
        requireAdmin(role);
        return service.crear(producto);
    }

    @PutMapping("/{id}")
    public Producto actualizar(@RequestHeader("X-User-Role") String role,
                               @PathVariable Long id,
                               @RequestBody Producto producto) {
        requireAdmin(role);
        return service.actualizar(id, producto);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@RequestHeader("X-User-Role") String role,
                         @PathVariable Long id) {
        requireAdmin(role);
        service.eliminar(id);
    }
}
