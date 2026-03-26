package com.cellc.favotiteservice.controller;

import com.cellc.favotiteservice.service.FavoriteService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService service;

    @GetMapping({"", "/"})
    public List<Long> list(
            @RequestHeader(name = "X-User-Id") Long userId
    ) {
        return service.listProductIds(userId);
    }

    @PostMapping("/products/{productId}")
    public Map<String, String> add(
            @RequestHeader(name = "X-User-Id") Long userId,
            @PathVariable @NotNull Long productId
    ) {
        service.addFavorite(userId, productId);
        return Map.of("status", "ok");
    }

    @DeleteMapping("/products/{productId}")
    public Map<String, String> remove(
            @RequestHeader(name = "X-User-Id") Long userId,
            @PathVariable @NotNull Long productId
    ) {
        service.removeFavorite(userId, productId);
        return Map.of("status", "ok");
    }
}
