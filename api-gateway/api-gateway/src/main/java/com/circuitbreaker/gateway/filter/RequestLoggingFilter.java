package com.circuitbreaker.gateway.filter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestLoggingFilter implements Filter {

    private static final Logger logger =
            LoggerFactory.getLogger(RequestLoggingFilter.class);

    private final Counter gatewayRequestCounter;

    public RequestLoggingFilter(MeterRegistry meterRegistry) {

        this.gatewayRequestCounter = Counter.builder(
                        "gateway.requests.total"
                )
                .description(
                        "Total number of requests processed by the API Gateway"
                )
                .register(meterRegistry);
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest =
                (HttpServletRequest) request;

        HttpServletResponse httpResponse =
                (HttpServletResponse) response;

        String requestId =
                UUID.randomUUID().toString();

        httpResponse.setHeader(
                "X-Request-ID",
                requestId
        );

        long startTime =
                System.currentTimeMillis();

        // Count every request received by the gateway
        gatewayRequestCounter.increment();

        logger.info(
                "Request ID: {} | Incoming request: {} {}",
                requestId,
                httpRequest.getMethod(),
                httpRequest.getRequestURI()
        );

        try {

            chain.doFilter(request, response);

        } finally {

            long duration =
                    System.currentTimeMillis() - startTime;

            logger.info(
                    "Request ID: {} | Completed request: {} {} | Status: {} | Time: {}ms",
                    requestId,
                    httpRequest.getMethod(),
                    httpRequest.getRequestURI(),
                    httpResponse.getStatus(),
                    duration
            );
        }
    }
}