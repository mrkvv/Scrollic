package scrollic.APIGateway.routes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActionServiceRoutes {
    @Value("${action.service.url}")
    private String actionServiceUrl;

    @Bean
    public RouteLocator actionUrlRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("action_batch", r -> r
                        .path("/api/actions/batch")
                        .uri(actionServiceUrl))
                .route("action_like", r -> r
                        .path("/api/actions/like")
                        .uri(actionServiceUrl))
                .route("action_seen", r -> r
                        .path("/api/actions/seen")
                        .uri(actionServiceUrl))
                .route("action_get_status", r -> r
                        .path("/api/actions/status/{news_id}")
                        .uri(actionServiceUrl))
                .build();
    }
}
