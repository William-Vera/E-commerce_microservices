package com.cellc.productservice.controller;

import com.cellc.productservice.dto.CategoriaDto;
import com.cellc.productservice.dto.CatalogItemRequest;
import com.cellc.productservice.dto.MarcaDto;
import com.cellc.productservice.service.CatalogoAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class AdminCatalogController {

    private final CatalogoAdminService catalogoAdminService;

    private void requireAdmin(String role) {
        if (role == null || !role.trim().equalsIgnoreCase("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere rol ADMIN");
        }
    }

    @PostMapping("/categorias")
    public CategoriaDto crearCategoria(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody CatalogItemRequest request
    ) {
        requireAdmin(role);
        return catalogoAdminService.crearCategoria(request);
    }

    @PutMapping("/categorias/{id}")
    public CategoriaDto actualizarCategoria(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id,
            @Valid @RequestBody CatalogItemRequest request
    ) {
        requireAdmin(role);
        return catalogoAdminService.actualizarCategoria(id, request);
    }

    @DeleteMapping("/categorias/{id}")
    public void eliminarCategoria(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id
    ) {
        requireAdmin(role);
        catalogoAdminService.eliminarCategoria(id);
    }

    @PostMapping("/marcas")
    public MarcaDto crearMarca(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody CatalogItemRequest request
    ) {
        requireAdmin(role);
        return catalogoAdminService.crearMarca(request);
    }

    @PutMapping("/marcas/{id}")
    public MarcaDto actualizarMarca(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id,
            @Valid @RequestBody CatalogItemRequest request
    ) {
        requireAdmin(role);
        return catalogoAdminService.actualizarMarca(id, request);
    }

    @DeleteMapping("/marcas/{id}")
    public void eliminarMarca(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id
    ) {
        requireAdmin(role);
        catalogoAdminService.eliminarMarca(id);
    }
}
