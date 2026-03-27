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
    private final RequestHeaderProvider requestHeaderProvider;

    @Value("${app.promotion-service.base-url}")
    private String promotionServiceBaseUrl;

    public PromotionClient(RequestHeaderProvider requestHeaderProvider) {
        this.requestHeaderProvider = requestHeaderProvider;
    }

    public PromotionValidateResponse validatePromotion(Long userId, String promotionCode, Double orderAmount) {
        if (promotionCode == null || promotionCode.isBlank()) {
            return PromotionValidateResponse.inactive();
        }

        String url = promotionServiceBaseUrl + "/promotions/validate?code=" + promotionCode + "&orderAmount=" + orderAmount;
        HttpHeaders headers = requestHeaderProvider.buildHeaders();
        headers.set("X-User-Id", String.valueOf(userId));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<PromotionValidateResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                PromotionValidateResponse.class
        );
        PromotionValidateResponse resp = response.getBody();
        return resp == null ? PromotionValidateResponse.inactive() : resp;
    }

    public record PromotionValidateResponse(
            String code,
            String discountType,
            Double discountPercent,
            Double fixedAmount,
            Double minimumOrderAmount,
            Integer usageLimit,
            Long timesUsed,
            Boolean usedByUser,
            Boolean active
    ) {
        public static PromotionValidateResponse inactive() {
            return new PromotionValidateResponse(null, null, 0.0, 0.0, null, null, 0L, false, false);
        }
    }
}
