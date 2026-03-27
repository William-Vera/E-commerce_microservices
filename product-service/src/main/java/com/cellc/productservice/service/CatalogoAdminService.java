package com.cellc.productservice.service;

import com.cellc.productservice.dto.CategoriaDto;
import com.cellc.productservice.dto.CatalogItemRequest;
import com.cellc.productservice.dto.MarcaDto;
import com.cellc.productservice.entity.Categoria;
import com.cellc.productservice.entity.Marca;
import com.cellc.productservice.repository.CategoriaRepository;
import com.cellc.productservice.repository.MarcaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatalogoAdminService {

    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;

    @Transactional
    public CategoriaDto crearCategoria(CatalogItemRequest request) {
        Categoria categoria = new Categoria();
        categoria.setNombre(normalizeName(request.nombre()));
        return CategoriaDto.from(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaDto actualizarCategoria(Long id, CatalogItemRequest request) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada: " + id));
        categoria.setNombre(normalizeName(request.nombre()));
        return CategoriaDto.from(categoriaRepository.save(categoria));
    }

    @Transactional
    public void eliminarCategoria(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new IllegalArgumentException("Categoria no encontrada: " + id);
        }
        categoriaRepository.deleteById(id);
    }

    @Transactional
    public MarcaDto crearMarca(CatalogItemRequest request) {
        Marca marca = new Marca();
        marca.setNombre(normalizeName(request.nombre()));
        return MarcaDto.from(marcaRepository.save(marca));
    }

    @Transactional
    public MarcaDto actualizarMarca(Long id, CatalogItemRequest request) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Marca no encontrada: " + id));
        marca.setNombre(normalizeName(request.nombre()));
        return MarcaDto.from(marcaRepository.save(marca));
    }

    @Transactional
    public void eliminarMarca(Long id) {
        if (!marcaRepository.existsById(id)) {
            throw new IllegalArgumentException("Marca no encontrada: " + id);
        }
        marcaRepository.deleteById(id);
    }

    private String normalizeName(String nombre) {
        return nombre == null ? null : nombre.trim();
    }
}
