package scrollic.APIGateway.routes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeedServiceRoutes {
    @Value("${feed.service.url}")
    private String feedServiceUrl;

    @Bean
    public RouteLocator feedRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("feed_get", r -> r
                        .path("/api/feed")
                        .uri(feedServiceUrl))
                .build();
    }
}
