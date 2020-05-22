import helpers.CronDockerTagResolver;
import helpers.PushGatewayContainerFactory;
import io.homecentr.testcontainers.containers.GenericContainerEx;
import io.homecentr.testcontainers.containers.HttpResponse;
import io.homecentr.testcontainers.containers.wait.strategy.WaitEx;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.Network;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;

import static io.homecentr.testcontainers.WaitLoop.waitFor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CronWithSuccessfulTickShould {
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
                .withRelativeFileSystemBind(Paths.get("..", "example", "success").toString(), "/cron")
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
    public void executeCronTickScript() throws Exception {
        waitFor(Duration.ofSeconds(80), () -> _cronContainer.getLogsAnalyzer().contains("Execution finished"));

        // Returns exit code 1 if the directory is empty
        Container.ExecResult execResult = _cronContainer.execInContainer("ash", "-c", "find /tmp -mindepth 1 | read");

        assertEquals(0, execResult.getExitCode());
    }

    @Test
    public void reportMetricsToPushGateway() throws IOException {
        HttpResponse response = _pushGatewayContainer.makeHttpRequest(9091, "/metrics");

        assertTrue(response.getResponseContent().contains("exit_code{instance=\"base\",job=\"cron\"} 0"));
        assertTrue(response.getResponseContent().contains("duration_seconds{instance=\"base\",job=\"cron\"}"));
    }
}
