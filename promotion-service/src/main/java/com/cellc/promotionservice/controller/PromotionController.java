package com.cellc.promotionservice.controller;

import com.cellc.promotionservice.entity.Promotion;
import com.cellc.promotionservice.service.PromotionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService service;

    private void requireAdmin(String role) {
        if (role == null || !role.trim().equalsIgnoreCase("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere rol ADMIN");
        }
    }

    @PostMapping({"", "/"})
    public Promotion create(@RequestHeader("X-User-Role") String role,
                            @Valid @RequestBody CreatePromotionRequest request) {
        requireAdmin(role);
        return service.create(request.code, request.discountPercent, request.active);
    }

    @GetMapping("/validate")
    public PromotionService.DiscountResponse validate(@RequestParam String code) {
        return service.validate(code);
    }

    public record CreatePromotionRequest(
            @NotBlank String code,
            @DecimalMin(value = "0.0", inclusive = true) Double discountPercent,
            Boolean active
    ) {}
}
