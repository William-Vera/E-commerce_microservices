package com.cellc.productservice.service;

import com.cellc.productservice.dto.ProductImageRequest;
import com.cellc.productservice.dto.ProductImageResponse;
import com.cellc.productservice.entity.ImagenProducto;
import com.cellc.productservice.repository.ImagenProductoRepository;
import com.cellc.productservice.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductImageService {

    private final ImagenProductoRepository imagenProductoRepository;
    private final ProductoRepository productoRepository;

    @Value("${app.upload-dir:uploads/product-images}")
    private String uploadDir;

    public List<ProductImageResponse> listImages(Long productId) {
        ensureProductExists(productId);
        return imagenProductoRepository.findByProductoId(productId)
                .stream()
                .map(ProductImageResponse::from)
                .toList();
    }

    @Transactional
    @CacheEvict(value = "productos", allEntries = true)
    public ProductImageResponse addImageByUrl(Long productId, ProductImageRequest request) {
        ensureProductExists(productId);
        ImagenProducto image = buildImage(productId, request.url().trim(), request.esPrincipal());
        return ProductImageResponse.from(imagenProductoRepository.save(image));
    }

    @Transactional
    @CacheEvict(value = "productos", allEntries = true)
    public ProductImageResponse uploadImage(Long productId, MultipartFile file, Boolean esPrincipal) {
        ensureProductExists(productId);
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Debes enviar un archivo de imagen");
        }

        try {
            Path directory = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(directory);

            String originalName = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
            String extension = extractExtension(originalName);
            String fileName = UUID.randomUUID() + extension;
            Path target = directory.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            ImagenProducto image = buildImage(productId, "/api/products/images/" + fileName, esPrincipal);
            return ProductImageResponse.from(imagenProductoRepository.save(image));
        } catch (IOException ex) {
            throw new IllegalArgumentException("No fue posible guardar la imagen");
        }
    }

    @Transactional
    @CacheEvict(value = "productos", allEntries = true)
    public void deleteImage(Long productId, Long imageId) {
        ensureProductExists(productId);
        ImagenProducto image = imagenProductoRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Imagen no encontrada: " + imageId));

        if (!productId.equals(image.getProductoId())) {
            throw new IllegalArgumentException("La imagen no pertenece al producto indicado");
        }

        imagenProductoRepository.delete(image);
        deleteLocalFileIfManaged(image.getUrl());
    }

    public ImageResource loadImage(String filename) {
        try {
            Path file = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("Imagen no encontrada");
            }
            String contentType = Files.probeContentType(file);
            if (contentType == null || contentType.isBlank()) {
                contentType = "application/octet-stream";
            }
            return new ImageResource(resource, contentType);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Imagen no encontrada");
        } catch (IOException ex) {
            throw new IllegalArgumentException("No fue posible leer la imagen");
        }
    }

    private ImagenProducto buildImage(Long productId, String url, Boolean esPrincipal) {
        List<ImagenProducto> existing = imagenProductoRepository.findByProductoId(productId);
        boolean principal = Boolean.TRUE.equals(esPrincipal) || (esPrincipal == null && existing.isEmpty());
        if (principal) {
            existing.forEach(image -> image.setEsPrincipal(false));
            imagenProductoRepository.saveAll(existing);
        }

        ImagenProducto image = new ImagenProducto();
        image.setProductoId(productId);
        image.setUrl(url);
        image.setEsPrincipal(principal);
        return image;
    }

    private void ensureProductExists(Long productId) {
        productoRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productId));
    }

    private String extractExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0) {
            return "";
        }
        return filename.substring(lastDot);
    }

    private void deleteLocalFileIfManaged(String url) {
        if (url == null || !url.startsWith("/api/products/images/")) {
            return;
        }

        try {
            String filename = url.substring("/api/products/images/".length());
            Path file = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(filename).normalize();
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
        }
    }

    public record ImageResource(Resource resource, String contentType) {}
}
