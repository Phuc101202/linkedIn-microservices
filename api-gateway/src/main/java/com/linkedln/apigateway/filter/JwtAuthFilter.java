package com.linkedln.apigateway.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;

import java.net.http.HttpHeaders;
import java.security.Key;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * JWT Authentication Filter
 * 
 * Applied to all routes except /api/v1/auth/**
 * 
 * FLOW:
 * 1. Extract JWT from Authorization header.
 * 2. Validate JWT signature and expiry.
 * 3. Extract userId from token claims.
 * 4. Add userId to request header for downstream services
 * 5. Forward request to correct service
 * 
 * If JWT is invalid or missing -> 401 Unauthorized
 */

@Component
@Slf4j
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    @Value("${jwt.secret-key}")
    private String secretKey;

    public JwtAuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(org.springframework.http.HttpHeaders.AUTHORIZATION);

            // No token - Reject
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization Header");
                return unauthorized(exchange);
            }

            String token = authHeader.substring(7);
            try {

                Claims claims = extractClaims(token);
                String userId = claims.get("userId", String.class);
                String email = claims.getSubject();

                log.info("JWT validated for user: {}", userId);

                // Add userId to request header for downstream services
                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(r -> r.header("X-User-Id", userId).header("X-User_Email", email)).build();
                return chain.filter(modifiedExchange);
            } catch (Exception e) {
                log.error("JWT validation failed: {}", e.getMessage());
                return unauthorized(exchange);
            }

        };
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    public static class Config {

    }
}
