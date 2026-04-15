package scrollic.APIGateway.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class RedisSessionService {

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    private static final String SESSION_PREFIX = "session:";

    public Mono<Map<Object, Object>> getSession(String token) {
        String key = SESSION_PREFIX + token;
        return redisTemplate.opsForHash()
                .entries(key)
                .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                .filter(map -> !map.isEmpty());
    }

    public Mono<Boolean> sessionExists(String token) {
        String key = SESSION_PREFIX + token;
        return redisTemplate.hasKey(key);
    }
}
