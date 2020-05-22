import helpers.CronDockerTagResolver;
import io.homecentr.testcontainers.containers.GenericContainerEx;
import io.homecentr.testcontainers.containers.wait.strategy.WaitEx;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Paths;
import java.time.Duration;

import static io.homecentr.testcontainers.WaitLoop.waitFor;

public class CronWithoutApiGatewayShould {
    private static GenericContainerEx _cronContainer;

    @BeforeClass
    public static void before() {
        _cronContainer = new GenericContainerEx<>(new CronDockerTagResolver())
                .withEnv("CRON_SCHEDULE", "* * * * *")
                .withEnv("PUSH_GATEWAY_URL", "http://push_gateway:9091/metrics/job/cron/instance/base")
                .withRelativeFileSystemBind(Paths.get("..", "example", "success").toString(), "/cron")
                .waitingFor(WaitEx.forS6OverlayStart());

        _cronContainer.start();
    }

    @AfterClass
    public static void after() {
        _cronContainer.close();
    }

    @Test
    public void executeNextTickScript() throws Exception {
        waitFor(Duration.ofSeconds(80), () -> _cronContainer.getLogsAnalyzer().contains("Execution finished"));
        waitFor(Duration.ofSeconds(80), () -> _cronContainer.getLogsAnalyzer().contains("Execution started", 2));
    }
}