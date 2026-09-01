package com.sa.api_gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthenticationFilter
        implements org.springframework.cloud.gateway.filter.GlobalFilter {

    private static final String SECRET_KEY =
            "s2i3hb3n1mo21jeo12en12k1mepo1j1x";

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {

        String path = exchange.getRequest()
                .getURI()
                .getPath();

        // Auth endpoints are public
        if (path.startsWith("/api/auth/")) {
            return chain.filter(exchange);
        }

        // Only protect screening endpoints for now
        if (path.startsWith("/api/screening/")) {

            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            // No Authorization header
            if (authHeader == null ||
                    !authHeader.startsWith("Bearer ")) {

                return unauthorized(exchange);
            }

            String token = authHeader.substring(7);

            // Invalid / expired JWT
            if (!isTokenValid(token)) {
                return unauthorized(exchange);
            }
        }

        return chain.filter(exchange);
    }

    private boolean isTokenValid(String token) {

        try {

            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(
                SECRET_KEY.getBytes(StandardCharsets.UTF_8)
        );
    }

    private Mono<Void> unauthorized(
            ServerWebExchange exchange) {

        exchange.getResponse()
                .setStatusCode(HttpStatus.UNAUTHORIZED);

        return exchange.getResponse()
                .setComplete();
    }
}