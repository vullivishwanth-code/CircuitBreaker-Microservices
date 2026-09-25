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

				// Product Circuit Breaker
				// Placed before the Load Balancer so discovery/load-balancer
				// failures can be observed by the circuit breaker.
				.filter(
						circuitBreaker(
								"productGatewayCircuitBreaker",
								URI.create("forward:/product-fallback")
						)
				)

				// Eureka Load Balancer
				.filter(
						lb("product-service")
				)

				// Product Bulkhead
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

				// Product Rate Limiter
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
                                          "message": "Product Service is temporarily unavailable."
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

				.route(
						request -> request.path()
								.startsWith("/api/inventory/"),
						http()
				)

				// Inventory Circuit Breaker
				.filter(
						circuitBreaker(
								"inventoryCircuitBreaker",
								URI.create("forward:/inventory-fallback")
						)
				)

				// Eureka Load Balancer
				.filter(
						lb("inventory-service")
				)

				.build();
	}


	// =========================================================
	// INVENTORY SERVICE FALLBACK
	// =========================================================

	@Bean
	public RouterFunction<ServerResponse> inventoryFallbackRoute() {

		return org.springframework.web.servlet.function.RouterFunctions.route()

				.GET(
						"/inventory-fallback",

						request -> ServerResponse
								.status(HttpStatus.SERVICE_UNAVAILABLE)
								.body("""
                                        {
                                          "status": "SERVICE_UNAVAILABLE",
                                          "message": "Inventory Service is temporarily unavailable."
                                        }
                                        """)
				)

				.build();
	}


	// =========================================================
	// RECOMMENDATION SERVICE ROUTE
	// =========================================================

	@Bean
	public RouterFunction<ServerResponse> recommendationRoute() {

		return route("recommendation-route")

				.route(
						request -> request.path()
								.startsWith("/api/recommendations/"),
						http()
				)

				// Recommendation Circuit Breaker
				.filter(
						circuitBreaker(
								"recommendationCircuitBreaker",
								URI.create("forward:/recommendation-fallback")
						)
				)

				// Eureka Load Balancer
				.filter(
						lb("recommendation-service")
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
								.status(HttpStatus.SERVICE_UNAVAILABLE)								.body("""
                                        {
                                          "status": "SERVICE_UNAVAILABLE",
                                          "message": "Recommendation Service is temporarily unavailable."
                                        }
                                        """)
				)

				.build();
	}
}