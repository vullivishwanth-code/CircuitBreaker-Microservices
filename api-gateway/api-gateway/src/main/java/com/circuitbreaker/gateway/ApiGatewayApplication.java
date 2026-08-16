package com.circuitbreaker.gateway;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

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

	@Bean
	public RouterFunction<ServerResponse> rateLimitedProductRoute(
			Bulkhead productBulkhead) {

		return route("rate-limited-product-route")
				.route(
						request -> request.path().startsWith("/api/products/"),
						http()
				)

				// Eureka / Load-balanced Product Service
				.filter(lb("product-service"))

				// Circuit Breaker + 3-second TimeLimiter + Fallback
				.filter(circuitBreaker(
						"productTimeLimiter",
						"/product-fallback"
				))

				// Bulkhead - maximum 3 concurrent Product requests
				.filter((request, next) -> {
					try {
						return productBulkhead.executeCallable(
								() -> next.handle(request)
						);
					} catch (BulkheadFullException e) {
						return ServerResponse
								.status(HttpStatus.TOO_MANY_REQUESTS)
								.body("Bulkhead limit reached - too many concurrent requests");
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
}