package org.example.gatewayservice;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;

import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import io.github.resilience4j.ratelimiter.RateLimiter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class Resilience4jRateLimiterFilter implements GatewayFilter, Ordered {

    private final RateLimiter rateLimiter;

    public Resilience4jRateLimiterFilter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return Mono.just(exchange)
                .transformDeferred(RateLimiterOperator.of(rateLimiter))
                .flatMap(e -> chain.filter(exchange))
                .onErrorResume(throwable -> {
                    // 요청 제한 초과 시 429 반환
                    exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    return exchange.getResponse().setComplete();
                });
    }

    @Override
    public int getOrder() {
        return -1; // 필터 순서 우선순위 (적절히 조정)
    }
}
