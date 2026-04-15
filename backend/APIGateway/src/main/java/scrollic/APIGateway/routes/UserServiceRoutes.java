package scrollic.APIGateway.routes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserServiceRoutes {
    @Value("${user.service.url}")
    private String userServiceUrl;

    @Bean
    public RouteLocator userRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user_register", r -> r
                        .path("/api/auth/register")
                        .uri(userServiceUrl))
                .route("user_login", r -> r
                        .path("/api/auth/login")
                        .uri(userServiceUrl))
                .route("user_logout", r -> r
                        .path("/api/auth/logout")
                        .uri(userServiceUrl))
                .route("user_refresh_token", r -> r
                        .path("/api/auth/refresh")
                        .uri(userServiceUrl))
                .route("user_me", r -> r
                        .path("/api/users/me")
                        .uri(userServiceUrl))
                .route("user_change_password", r -> r
                        .path("/api/users/me/password")
                        .uri(userServiceUrl))
                .build();
    }
}
