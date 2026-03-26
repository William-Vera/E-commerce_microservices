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

    @Value("${app.cart-service.base-url}")
    private String cartServiceBaseUrl;

    public CartResponse getCart(Long userId) {
        HttpHeaders headers = new HttpHeaders();
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
        HttpHeaders headers = new HttpHeaders();
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
            Double total
    ) {}

    public record CartItemDto(
            Long productId,
            Integer quantity,
            Double unitPrice,
            Double lineTotal
    ) {}
}

