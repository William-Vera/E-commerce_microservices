package com.cellc.orderservice.client;

import com.cellc.orderservice.dto.UserContactResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final RequestHeaderProvider requestHeaderProvider;

    @Value("${app.user-service.base-url}")
    private String userServiceBaseUrl;

    public UserClient(RequestHeaderProvider requestHeaderProvider) {
        this.requestHeaderProvider = requestHeaderProvider;
    }

    public UserContactResponse getUserContact(Long userId) {
        HttpHeaders headers = requestHeaderProvider.buildHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<UserContactResponse> response = restTemplate.exchange(
                userServiceBaseUrl + "/" + userId + "/contact",
                HttpMethod.GET,
                entity,
                UserContactResponse.class
        );

        return response.getBody();
    }
}
