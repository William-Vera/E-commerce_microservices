package com.cellc.orderservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PromotionClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.promotion-service.base-url}")
    private String promotionServiceBaseUrl;

    public double getDiscountPercentOrZero(Long userId, String promotionCode) {
        if (promotionCode == null || promotionCode.isBlank()) {
            return 0.0;
        }

        String url = promotionServiceBaseUrl + "/promotions/validate?code=" + promotionCode;
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", String.valueOf(userId));
        headers.set("X-User-Role", "USER");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<PromotionValidateResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                PromotionValidateResponse.class
        );
        PromotionValidateResponse resp = response.getBody();
        if (resp == null || resp.discountPercent() == null) {
            return 0.0;
        }
        return resp.discountPercent();
    }

    public record PromotionValidateResponse(String code, Double discountPercent, Boolean active) {}
}

