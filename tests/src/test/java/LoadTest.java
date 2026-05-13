import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import java.util.UUID;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class LoadTest extends Simulation {

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
            .exec(http("Actions_Batch")
                    .post("/api/actions/batch")
                    .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI3IiwiZXhwIjoxNzc4NzAzNzQyfQ.wWG735SIA1pcx982O3B9Bkq4VjoUH_fyYFNVSxe7T6g")
                    .body(StringBody(session -> generateBatchBody()))
                    .check(status().in(200))
            ).pause(1);

    {
        setUp(
                scn.injectOpen(
                        rampUsers(50).during(Duration.ofMinutes(1)),
                        constantUsersPerSec(50).during(Duration.ofMinutes(15))
                ).protocols(httpProtocol)
        ).assertions(
                global().successfulRequests().percent().gt(90.0)
        );
    }
}
