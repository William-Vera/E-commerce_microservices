package com.cellc.productservice.service;

import com.cellc.productservice.dto.CategoriaDto;
import com.cellc.productservice.dto.MarcaDto;
import com.cellc.productservice.dto.PriceRangeDto;
import com.cellc.productservice.repository.CategoriaRepository;
import com.cellc.productservice.repository.MarcaRepository;
import com.cellc.productservice.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogoService {

    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;
    private final ProductoRepository productoRepository;

    public List<CategoriaDto> listarCategorias() {
        return categoriaRepository.findAll(Sort.by("nombre").ascending())
                .stream()
                .map(CategoriaDto::from)
                .toList();
    }

    public List<MarcaDto> listarMarcas() {
        return marcaRepository.findAll(Sort.by("nombre").ascending())
                .stream()
                .map(MarcaDto::from)
                .toList();
    }

    public PriceRangeDto obtenerRangoPrecios() {
        Double minPrice = productoRepository.findMinPriceByEstadoTrue();
        Double maxPrice = productoRepository.findMaxPriceByEstadoTrue();
        return new PriceRangeDto(minPrice, maxPrice);
    }
}
