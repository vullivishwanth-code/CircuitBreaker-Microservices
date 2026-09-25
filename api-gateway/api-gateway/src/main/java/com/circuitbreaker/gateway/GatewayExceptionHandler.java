package com.circuitbreaker.gateway;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GatewayExceptionHandler {

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<Map<String, String>> handleHttpServerError(
            HttpServerErrorException exception,
            HttpServletRequest request) {

        Map<String, String> response = new LinkedHashMap<>();

        // Recommendation Service unavailable
        if (exception.getStatusCode().value() == 503
                && request.getRequestURI()
                .startsWith("/api/recommendations/")) {

            response.put(
                    "status",
                    "SERVICE_UNAVAILABLE"
            );

            response.put(
                    "message",
                    "Recommendation Service is temporarily unavailable."
            );

            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(response);
        }

        // Product Service unavailable
        if (exception.getStatusCode().value() == 503
                && request.getRequestURI()
                .startsWith("/api/products/")) {

            response.put(
                    "status",
                    "SERVICE_UNAVAILABLE"
            );

            response.put(
                    "message",
                    "Product Service is temporarily unavailable."
            );

            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(response);
        }

        // Inventory Service unavailable
        if (exception.getStatusCode().value() == 503
                && request.getRequestURI()
                .startsWith("/api/inventory/")) {

            response.put(
                    "status",
                    "SERVICE_UNAVAILABLE"
            );

            response.put(
                    "message",
                    "Inventory Service is temporarily unavailable."
            );

            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(response);
        }

        // Other gateway/server errors
        response.put(
                "status",
                exception.getStatusCode().toString()
        );

        response.put(
                "message",
                exception.getMessage()
        );

        return ResponseEntity
                .status(exception.getStatusCode())
                .body(response);
    }
}