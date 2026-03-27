package com.cellc.productservice.controller;

import com.cellc.productservice.dto.CategoriaDto;
import com.cellc.productservice.dto.MarcaDto;
import com.cellc.productservice.dto.PriceRangeDto;
import com.cellc.productservice.service.CatalogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class CatalogoController {

    private final CatalogoService catalogoService;

    @GetMapping("/categorias")
    public List<CategoriaDto> listarCategorias() {
        return catalogoService.listarCategorias();
    }

    @GetMapping("/marcas")
    public List<MarcaDto> listarMarcas() {
        return catalogoService.listarMarcas();
    }

    @GetMapping("/precios/rango")
    public PriceRangeDto obtenerRangoPrecios() {
        return catalogoService.obtenerRangoPrecios();
    }
}
