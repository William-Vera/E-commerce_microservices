package com.cellc.cartservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ProductClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final RequestHeaderProvider requestHeaderProvider;

    @Value("${app.product-service.base-url}")
    private String productServiceBaseUrl;

    public ProductClient(RequestHeaderProvider requestHeaderProvider) {
        this.requestHeaderProvider = requestHeaderProvider;
    }

    public ProductPrice getProductPrice(Long productId) {
        String url = productServiceBaseUrl + "/products/" + productId;
        HttpEntity<Void> entity = new HttpEntity<>(requestHeaderProvider.buildHeaders());
        ResponseEntity<ProductPrice> response = restTemplate.exchange(url, HttpMethod.GET, entity, ProductPrice.class);
        return response.getBody();
    }

    public record ProductPrice(Long id, Double precio, Integer stock) {}
}
