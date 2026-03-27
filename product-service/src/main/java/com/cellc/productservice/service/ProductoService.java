package com.cellc.productservice.service;
import org.springframework.lang.NonNull;
import com.cellc.productservice.dto.PageResponseDto;
import com.cellc.productservice.dto.ProductoDto;
import com.cellc.productservice.entity.ImagenProducto;
import com.cellc.productservice.entity.ProcessedOrder;
import com.cellc.productservice.entity.Producto;
import com.cellc.productservice.filters.ProductoSpecification;
import com.cellc.productservice.mapper.ProductoMapper;
import com.cellc.productservice.repository.ImagenProductoRepository;
import com.cellc.productservice.repository.ProcessedOrderRepository;
import com.cellc.productservice.repository.ProductoRepository;
import com.cellc.productservice.messaging.OrderPaidEvent;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ImagenProductoRepository imagenRepo;
    private final ProcessedOrderRepository processedOrderRepository;
    private final ProductoMapper mapper;

    @Cacheable(value = "productos")
    public PageResponseDto<ProductoDto> buscar(
            String nombre,
            Long categoriaId,
            Long marcaId,
            Double precioMin,
            Double precioMax,
            int page,
            int size,
            String sortBy,
            String direction
    ) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Producto> spec = ProductoSpecification.filtrar(
                nombre, categoriaId, marcaId, precioMin, precioMax
        );

        Page<Producto> productos = productoRepository.findAll(spec, pageable);

        return getProductoDtoPageResponseDto(productos);
    }

    @CacheEvict(value = "productos", allEntries = true)
    public Producto crear(Producto producto) {
        producto.setFechaCreacion(LocalDateTime.now());
        return productoRepository.save(producto);
    }

    public ProductoDto obtener(Long id) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        List<ImagenProducto> imgs = imagenRepo.findByProductoId(id);
        return mapper.toDTO(p, imgs);
    }

    public PageResponseDto<ProductoDto> porCategoria(
            Long categoriaId,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());

        Page<Producto> productos = productoRepository
                .findByCategoriaId(categoriaId, pageable);

        return getProductoDtoPageResponseDto(productos);
    }

    @NonNull
    private PageResponseDto<ProductoDto> getProductoDtoPageResponseDto(Page<Producto> productos) {
        List<ProductoDto> lista = productos.getContent().stream().map(p -> {
            List<ImagenProducto> imgs = imagenRepo.findByProductoId(p.getId());
            return mapper.toDTO(p, imgs);
        }).toList();

        return new PageResponseDto<>(
                lista,
                productos.getNumber(),
                productos.getSize(),
                productos.getTotalElements(),
                productos.getTotalPages(),
                productos.isLast()
        );
    }

    @CacheEvict(value = "productos", allEntries = true)
    public Producto actualizar(Long id, Producto nuevo) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe"));

        p.setNombre(nuevo.getNombre());
        p.setDescripcion(nuevo.getDescripcion());
        p.setPrecio(nuevo.getPrecio());
        p.setStock(nuevo.getStock());

        return productoRepository.save(p);
    }

    @CacheEvict(value = "productos", allEntries = true)
    public void eliminar(Long id) {
        productoRepository.deleteById(id);
    }

    @Transactional
    @CacheEvict(value = "productos", allEntries = true)
    public void discountStock(Long orderId, List<OrderPaidEvent.OrderPaidItem> items) {
        if (orderId == null || processedOrderRepository.existsById(orderId)) {
            return;
        }

        for (OrderPaidEvent.OrderPaidItem item : items) {
            if (item.productId() == null || item.quantity() == null || item.quantity() <= 0) {
                continue;
            }

            Producto product = productoRepository.findById(item.productId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + item.productId()));

            int currentStock = product.getStock() == null ? 0 : product.getStock();
            int updatedStock = Math.max(0, currentStock - item.quantity());
            product.setStock(updatedStock);
            productoRepository.save(product);
        }

        ProcessedOrder processedOrder = new ProcessedOrder();
        processedOrder.setOrderId(orderId);
        processedOrder.setProcessedAt(LocalDateTime.now());
        processedOrderRepository.save(processedOrder);
    }
}
