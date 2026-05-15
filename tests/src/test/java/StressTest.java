import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import java.util.UUID;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class StressTest extends Simulation {

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Gatling/3.15.0");

    private static String generateBatchBody() {
        String newsId1 = UUID.randomUUID().toString();
        String newsId2 = UUID.randomUUID().toString();
        String batchId = UUID.randomUUID().toString();
        String actionId1 = UUID.randomUUID().toString();
        String actionId2 = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();

        return String.format("""
            {
              "batch_id": "%s",
              "client_timestamp": %d,
              "actions": [
                {
                  "action_id": "%s",
                  "news_id": "%s",
                  "action": "like",
                  "timestamp": %d
                },
                {
                  "action_id": "%s",
                  "news_id": "%s",
                  "action": "seen",
                  "timestamp": %d
                }
              ]
            }
            """,
                batchId, now,
                actionId1, newsId1, now,
                actionId2, newsId2, now + 1000
        );
    }

    ScenarioBuilder scn = scenario("Default")
            .exec(http("Login")
                    .post("/api/auth/login")
                    .body(StringBody("""
                            {
                                "name": "test111",
                                "password": "test111"
                            }
                            """))
                    .check(status().is(200))
                    .check(jsonPath("$.access_token").saveAs("token"))
            )
            .pause(2)

            .doIf(session -> session.contains("token"))
            .then(
                    repeat(10).on(
                            exec(http("GetFeed")
                                    .get("/api/feed?limit=50")
                                    .header("Authorization", "Bearer #{token}")
                                    .check(status().is(200))
                            )
                    )
                            .exec(http("Actions_Batch")
                                .post("/api/actions/batch")
                                .header("Authorization", "Bearer #{token}")
                                .body(StringBody(session -> generateBatchBody()))
                                .check(status().in(200, 202))
                            )
            );

    {
        setUp(
                scn.injectOpen(
                        rampUsersPerSec(1).to(30).during(Duration.ofMinutes(5)),

                        constantUsersPerSec(30).during(Duration.ofMinutes(1)),

                        incrementUsersPerSec(3.0)
                                .times(20)
                                .eachLevelLasting(Duration.ofSeconds(10))
                                .startingFrom(30)
                ).protocols(httpProtocol)
        ).assertions(
                global().successfulRequests().percent().gt(75.0)
        );
    }
}