package scrollic.APIGateway.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import scrollic.APIGateway.services.RedisSessionService;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private final RedisSessionService sessionService;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/logout"
    );

    public AuthFilter(RedisSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();

        if (isPublicPath(path)) { return chain.filter(exchange); }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        return sessionService.getSession(token)
                .flatMap(sessionData -> {
                    if (sessionData.isEmpty()) {
                        return unauthorized(exchange, "Invalid or expired token");
                    }

                    String userId = (String) sessionData.get("user_id");
                    String username = (String) sessionData.get("user_name");

                    if (userId == null || username == null) {
                        return unauthorized(exchange, "Invalid session data");
                    }

                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .request(r -> r
                                    .header("X-User-Id", userId)
                                    .header("X-Username", username)
                                    .headers(headers -> headers.remove("Authorization"))
                            )
                            .build();

                    return chain.filter(mutatedExchange);
                })
                .switchIfEmpty(unauthorized(exchange, "Session not found or expired"));
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String response = "{\"status\":\"error\",\"message\":\"" + message + "\"}";
        DataBuffer buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(response.getBytes(StandardCharsets.UTF_8));

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
