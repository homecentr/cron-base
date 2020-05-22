import helpers.CronDockerTagResolver;
import helpers.PushGatewayContainerFactory;
import io.homecentr.testcontainers.containers.GenericContainerEx;
import io.homecentr.testcontainers.containers.HttpResponse;
import io.homecentr.testcontainers.containers.wait.strategy.WaitEx;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.Network;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;

import static io.homecentr.testcontainers.WaitLoop.waitFor;
import static org.junit.Assert.assertTrue;

public class CronWithFailingTickShould {
    private static GenericContainerEx _cronContainer;
    private static GenericContainerEx _pushGatewayContainer;
    private static Network _network;

    @BeforeClass
    public static void before() {
        _network = Network.newNetwork();

        _cronContainer = new GenericContainerEx<>(new CronDockerTagResolver())
                .withNetwork(_network)
                .withEnv("CRON_SCHEDULE", "* * * * *")
                .withEnv("PUSH_GATEWAY_URL", "http://push_gateway:9091/metrics/job/cron/instance/base")
                .withRelativeFileSystemBind(Paths.get("..", "example", "failure").toString(), "/cron")
                .waitingFor(WaitEx.forS6OverlayStart());

        _pushGatewayContainer = PushGatewayContainerFactory.create(_network);

        _pushGatewayContainer.start();
        _cronContainer.start();
    }

    @AfterClass
    public static void after() {
        _cronContainer.close();
        _pushGatewayContainer.close();
        _network.close();
    }

    @Test
    public void executeNextTickScript() throws Exception {
        // First tick
        waitFor(Duration.ofSeconds(80), () -> _cronContainer.getLogsAnalyzer().contains("Execution finished"));

        // Second tick
        waitFor(Duration.ofSeconds(80), () -> _cronContainer.getLogsAnalyzer().contains("Execution finished", 2));
    }

    @Test
    public void reportMetricsToPushGateway() throws IOException {
        HttpResponse response = _pushGatewayContainer.makeHttpRequest(9091, "/metrics");

        assertTrue(response.getResponseContent().contains("exit_code{instance=\"base\",job=\"cron\"} 10"));
        assertTrue(response.getResponseContent().contains("duration_seconds{instance=\"base\",job=\"cron\"}"));
    }
}
