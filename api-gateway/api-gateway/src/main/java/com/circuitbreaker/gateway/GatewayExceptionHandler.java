package com.circuitbreaker.gateway;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GatewayExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GatewayExceptionHandler.class);

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<Map<String, String>> handleHttpServerError(
            HttpServerErrorException exception,
            HttpServletRequest request) {

        logger.error(
                "Downstream service error: {} {} | Status: {} | Message: {}",
                request.getMethod(),
                request.getRequestURI(),
                exception.getStatusCode(),
                exception.getMessage()
        );

        Map<String, String> response = new LinkedHashMap<>();

        if (exception.getStatusCode().value() == 503
                && request.getRequestURI()
                .startsWith("/api/recommendations/")) {

            response.put("status", "SERVICE_UNAVAILABLE");
            response.put(
                    "message",
                    "Recommendation Service is temporarily unavailable."
            );

            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(response);
        }

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpectedError(
            Exception exception,
            HttpServletRequest request) {

        logger.error(
                "Unexpected gateway error: {} {} | Message: {}",
                request.getMethod(),
                request.getRequestURI(),
                exception.getMessage(),
                exception
        );

        Map<String, String> response = new LinkedHashMap<>();

        response.put(
                "status",
                "INTERNAL_SERVER_ERROR"
        );

        response.put(
                "message",
                "An unexpected error occurred in the API Gateway."
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}