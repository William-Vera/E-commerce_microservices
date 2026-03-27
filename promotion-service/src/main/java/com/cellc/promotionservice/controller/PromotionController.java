package com.cellc.promotionservice.controller;

import com.cellc.promotionservice.dto.PromotionSummaryResponse;
import com.cellc.promotionservice.entity.DiscountType;
import com.cellc.promotionservice.entity.Promotion;
import com.cellc.promotionservice.service.PromotionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

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
        return service.create(request);
    }

    @GetMapping({"", "/"})
    public List<PromotionSummaryResponse> listAll(@RequestHeader("X-User-Role") String role) {
        requireAdmin(role);
        return service.listAll();
    }

    @GetMapping("/validate")
    public PromotionService.DiscountResponse validate(
            @RequestHeader(name = "X-User-Id", required = false) Long userId,
            @RequestParam String code,
            @RequestParam(required = false) Double orderAmount
    ) {
        return service.validate(code, userId, orderAmount);
    }

    public record CreatePromotionRequest(
            @NotBlank String code,
            @DecimalMin(value = "0.0", inclusive = true) Double discountPercent,
            @DecimalMin(value = "0.0", inclusive = false) Double fixedAmount,
            DiscountType discountType,
            @DecimalMin(value = "0.0", inclusive = true) Double minimumOrderAmount,
            @Min(1) Integer usageLimit,
            LocalDate startDate,
            LocalDate endDate,
            Boolean active,
            Boolean customerUsable
    ) {}
}
