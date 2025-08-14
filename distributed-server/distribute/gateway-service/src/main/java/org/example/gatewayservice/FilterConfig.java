package org.example.gatewayservice;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Bean
    public RouteLocator gatewayRoutes(final RouteLocatorBuilder builder, Resilience4jRateLimiterFilter rateLimiterFilter) {
        return builder.routes()
                .route("backend-service", route -> route
                        .path("/request/**") // 이 주소로 요청이 올 경우
                        .filters(filter -> filter.filter(rateLimiterFilter))     // 필터 동작을 실행
                        .uri("lb://backend-service"))
                .build();
    }
}