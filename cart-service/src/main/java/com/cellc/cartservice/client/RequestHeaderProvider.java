package com.cellc.cartservice.client;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class RequestHeaderProvider {

    public HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        HttpServletRequest request = getCurrentRequest();

        if (request == null) {
            return headers;
        }

        copyHeaderIfPresent(request, headers, HttpHeaders.AUTHORIZATION);
        copyHeaderIfPresent(request, headers, "X-User-Id");
        copyHeaderIfPresent(request, headers, "X-User-Role");
        return headers;
    }

    private HttpServletRequest getCurrentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }

    private void copyHeaderIfPresent(HttpServletRequest request, HttpHeaders headers, String headerName) {
        String value = request.getHeader(headerName);
        if (value != null && !value.isBlank()) {
            headers.set(headerName, value);
        }
    }
}
