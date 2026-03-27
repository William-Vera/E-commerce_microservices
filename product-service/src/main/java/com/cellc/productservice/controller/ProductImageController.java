package com.cellc.productservice.controller;

import com.cellc.productservice.dto.ProductImageRequest;
import com.cellc.productservice.dto.ProductImageResponse;
import com.cellc.productservice.service.ProductImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    private void requireAdmin(String role) {
        if (role == null || !role.trim().equalsIgnoreCase("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere rol ADMIN");
        }
    }

    @GetMapping("/{productId}/images")
    public List<ProductImageResponse> listImages(@PathVariable Long productId) {
        return productImageService.listImages(productId);
    }

    @PostMapping("/{productId}/images/url")
    public ProductImageResponse addImageByUrl(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long productId,
            @Valid @RequestBody ProductImageRequest request
    ) {
        requireAdmin(role);
        return productImageService.addImageByUrl(productId, request);
    }

    @PostMapping(value = "/{productId}/images/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductImageResponse uploadImage(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long productId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(name = "esPrincipal", required = false) Boolean esPrincipal
    ) {
        requireAdmin(role);
        return productImageService.uploadImage(productId, file, esPrincipal);
    }

    @DeleteMapping("/{productId}/images/{imageId}")
    public void deleteImage(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long productId,
            @PathVariable Long imageId
    ) {
        requireAdmin(role);
        productImageService.deleteImage(productId, imageId);
    }

    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        ProductImageService.ImageResource image = productImageService.loadImage(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, image.contentType())
                .body(image.resource());
    }
}
