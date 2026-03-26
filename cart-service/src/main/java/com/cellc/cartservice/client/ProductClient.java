package com.cellc.cartservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ProductClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.product-service.base-url}")
    private String productServiceBaseUrl;

    public ProductPrice getProductPrice(Long productId) {
        String url = productServiceBaseUrl + "/products/" + productId;
        return restTemplate.getForObject(url, ProductPrice.class);
    }

    public record ProductPrice(Long id, Double precio, Integer stock) {}
}

