package com.cellc.orderservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CartClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final RequestHeaderProvider requestHeaderProvider;

    @Value("${app.cart-service.base-url}")
    private String cartServiceBaseUrl;

    public CartClient(RequestHeaderProvider requestHeaderProvider) {
        this.requestHeaderProvider = requestHeaderProvider;
    }

    public CartResponse getCart(Long userId) {
        HttpHeaders headers = requestHeaderProvider.buildHeaders();
        headers.set("X-User-Id", String.valueOf(userId));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<CartResponse> resp = restTemplate.exchange(
                cartServiceBaseUrl + "/cart",
                HttpMethod.GET,
                entity,
                CartResponse.class
        );

        return resp.getBody();
    }

    public void clearCart(Long userId) {
        HttpHeaders headers = requestHeaderProvider.buildHeaders();
        headers.set("X-User-Id", String.valueOf(userId));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        restTemplate.exchange(
                cartServiceBaseUrl + "/cart",
                HttpMethod.DELETE,
                entity,
                Object.class
        );
    }

    public record CartResponse(
            Long cartId,
            Long userId,
            java.util.List<CartItemDto> items,
            String promotionCode,
            AppliedPromotionDto appliedPromotion,
            Double subtotal,
            Double discountAmount,
            Double total
    ) {}

    public record CartItemDto(
            Long productId,
            Integer quantity,
            Double unitPrice,
            Double lineTotal
    ) {}

    public record AppliedPromotionDto(
            String code,
            String discountType,
            Double discountPercent,
            Double fixedAmount,
            Double minimumOrderAmount,
            Integer usageLimit,
            Long timesUsed,
            Boolean usedByUser
    ) {}
}
