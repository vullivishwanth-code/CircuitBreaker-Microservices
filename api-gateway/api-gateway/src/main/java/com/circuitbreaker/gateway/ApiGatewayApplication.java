package com.circuitbreaker.gateway;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;
import java.time.Duration;

import static org.springframework.cloud.gateway.server.mvc.filter.Bucket4jFilterFunctions.rateLimit;
import static org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions.circuitBreaker;
import static org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions.lb;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}


	// =========================================================
	// PRODUCT SERVICE ROUTE
	// =========================================================

	@Bean
	public RouterFunction<ServerResponse> rateLimitedProductRoute(
			Bulkhead productBulkhead) {

		return route("rate-limited-product-route")

				.route(
						request -> request.path()
								.startsWith("/api/products/"),
						http()
				)

				// Eureka / Load-balanced Product Service
				.filter(
						lb("product-service")
				)

				// Circuit Breaker + Fallback
				.filter(
						circuitBreaker(
								"productGatewayCircuitBreaker",
								URI.create("forward:/product-fallback")
						)
				)

				// Bulkhead - maximum 3 concurrent Product requests
				.filter((request, next) -> {

					try {

						return productBulkhead.executeCallable(
								() -> next.handle(request)
						);

					} catch (BulkheadFullException e) {

						return ServerResponse
								.status(HttpStatus.TOO_MANY_REQUESTS)
								.body(
										"Bulkhead limit reached - too many concurrent requests"
								);
					}
				})

				// Rate Limiter - 5 requests every 10 seconds
				.filter(
						rateLimit(
								5,
								Duration.ofSeconds(10),
								request -> "product-client"
						)
				)

				.build();
	}


	// =========================================================
	// PRODUCT SERVICE FALLBACK
	// =========================================================

	@Bean
	public RouterFunction<ServerResponse> productFallbackRoute() {

		return org.springframework.web.servlet.function.RouterFunctions.route()

				.GET(
						"/product-fallback",

						request -> ServerResponse
								.status(HttpStatus.SERVICE_UNAVAILABLE)
								.body("""
                                        {
                                          "status": "SERVICE_UNAVAILABLE",
                                          "message": "Product Service is taking too long. Please try again."
                                        }
                                        """)
				)

				.build();
	}


	// =========================================================
	// INVENTORY SERVICE ROUTE
	// =========================================================

	@Bean
	public RouterFunction<ServerResponse> inventoryRoute() {

		return route("inventory-route")

				// Incoming request:
				// /api/inventory/{productId}
				.route(
						request -> request.path()
								.startsWith("/api/inventory/"),
						http()
				)

				// Eureka / Load-balanced Inventory Service
				.filter(
						lb("inventory-service")
				)

				.build();
	}


	// =========================================================
	// RECOMMENDATION SERVICE ROUTE
	// =========================================================

	@Bean
	public RouterFunction<ServerResponse> recommendationRoute() {

		return route("recommendation-route")

				// Incoming request:
				// /api/recommendations/{productId}
				.route(
						request -> request.path()
								.startsWith("/api/recommendations/"),
						http()
				)

				// Eureka / Load-balanced Recommendation Service
				.filter(
						lb("recommendation-service")
				)

				// Circuit Breaker catches failure from Recommendation Service
				// and forwards request to fallback route
				.filter(
						circuitBreaker(
								"recommendationCircuitBreaker",
								URI.create("forward:/recommendation-fallback")
						)
				)

				.build();
	}


	// =========================================================
	// RECOMMENDATION SERVICE FALLBACK
	// =========================================================

	@Bean
	public RouterFunction<ServerResponse> recommendationFallbackRoute() {

		return org.springframework.web.servlet.function.RouterFunctions.route()

				.GET(
						"/recommendation-fallback",

						request -> ServerResponse
								.status(HttpStatus.SERVICE_UNAVAILABLE)
								.body("""
                                        {
                                          "status": "SERVICE_UNAVAILABLE",
                                          "message": "Recommendation Service is temporarily unavailable."
                                        }
                                        """)
				)

				.build();
	}
}