package com.cellc.cartservice.controller;

import com.cellc.cartservice.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService service;

    @PostMapping("/items")
    public CartService.CartResponse addItem(
            @RequestHeader(name = "X-User-Id") Long userId,
            @Valid @RequestBody AddToCartRequest request
    ) {
        service.addOrUpdateItem(userId, request.productId, request.quantity);
        return service.toResponse(service.getCartOrThrow(userId));
    }

    @GetMapping({"", "/"})
    public CartService.CartResponse getCart(
            @RequestHeader(name = "X-User-Id") Long userId
    ) {
        return service.toResponse(service.getCartOrThrow(userId));
    }

    @PutMapping("/items/{productId}")
    public CartService.CartResponse setQuantity(
            @RequestHeader(name = "X-User-Id") Long userId,
            @PathVariable Long productId,
            @Valid @RequestBody SetQuantityRequest request
    ) {
        service.setItemQuantity(userId, productId, request.quantity);
        return service.toResponse(service.getCartOrThrow(userId));
    }

    @DeleteMapping("/items/{productId}")
    public Map<String, String> removeItem(
            @RequestHeader(name = "X-User-Id") Long userId,
            @PathVariable Long productId
    ) {
        service.removeItem(userId, productId);
        return Map.of("status", "ok");
    }

    @DeleteMapping({"", "/"})
    public Map<String, String> clearCart(
            @RequestHeader(name = "X-User-Id") Long userId
    ) {
        service.clearCart(userId);
        return Map.of("status", "ok");
    }

    public record AddToCartRequest(
            @NotNull Long productId,
            @Min(1) Integer quantity
    ) {}

    public record SetQuantityRequest(
            @Min(0) Integer quantity
    ) {}
}
