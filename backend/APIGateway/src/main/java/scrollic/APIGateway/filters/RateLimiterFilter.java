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
import scrollic.APIGateway.services.RedisRateLimiterService;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class RateLimiterFilter implements GlobalFilter, Ordered {

    private final RedisRateLimiterService rateLimiterService;

    private static final Map<String, Integer> LIMITS = Map.of(
            "register", 5,
            "login",10,
            "feed", 10,
            "like", 200,
            "other", 100
    );

    public RateLimiterFilter(RedisRateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String action = defineAction(exchange.getRequest().getPath().toString());
        int limit = LIMITS.get(action);

        String ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");

        Mono<Boolean> ipCheck = rateLimiterService.checkIpLimit(action, ip, limit);
        Mono<Boolean> userCheck = (userId == null) ? Mono.just(true) :
                rateLimiterService.checkUserLimit(action, userId, limit);

        return Mono.zip(userCheck, ipCheck)
                .flatMap(results -> {
                    boolean isUserPassed = results.getT1();
                    boolean isIpPassed = results.getT2();

                    if (isUserPassed && isIpPassed) {
                        return chain.filter(exchange);
                    } else {
                        return this.tooManyRequests(exchange, limit);
                    }
                });
    }

    private String defineAction(String path) {
        if (path.contains("/api/auth/register")) return "register";
        if (path.contains("/api/auth/login")) return "login";
        if (path.contains("/api/feed")) return "feed";
        if (path.contains("/api/actions/like")) return "like";
        return "other";
    }

    private Mono<Void> tooManyRequests(ServerWebExchange exchange, int limit) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String response = "{\"status\":\"error\",\"message\":\"Too many requests. Limit: " + limit + " per minute\"}";
        DataBuffer buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(response.getBytes(StandardCharsets.UTF_8));

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
