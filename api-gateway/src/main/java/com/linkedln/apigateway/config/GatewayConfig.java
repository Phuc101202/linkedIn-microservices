package com.linkedln.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import com.linkedln.apigateway.filter.JwtAuthFilter;

@Configuration
public class GatewayConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public GatewayConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service-Auth", r -> r.path("/api/v1/auth/**")
                        .uri("http://localhost:8081"))
                .route("user-service", r -> r.path("/api/v1/users/**")
                        .filters(f -> f.filter(jwtAuthFilter.apply(new JwtAuthFilter.Config())))
                        .uri("http://localhost:8081"))
                .route("post-service", r -> r.path("/api/v1/posts/**")
                        .filters(f -> f.filter(jwtAuthFilter.apply(new JwtAuthFilter.Config())))
                        .uri("http://localhost:8082"))
                .route("feed-service", r -> r.path("/api/v1/feed/**")
                        .filters(f -> f.filter(jwtAuthFilter.apply(new JwtAuthFilter.Config())))
                        .uri("http://localhost:8083"))
                .route("search-service", r -> r.path("/api/v1/search/**")
                        .filters(f -> f.filter(jwtAuthFilter.apply(new JwtAuthFilter.Config())))
                        .uri("http://localhost:8084"))
                .build();
    }
}
