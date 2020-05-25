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

public class CronWithAnyTickShould {
    private static GenericContainerEx _cronContainer;

    @BeforeClass
    public static void before() {
        _cronContainer = new GenericContainerEx<>(new CronDockerTagResolver())
                .withEnv("PUID", "7088")
                .withEnv("PGID", "7099")
                .withEnv("CRON_SCHEDULE", "* * * * *")
                .withRelativeFileSystemBind(Paths.get("..", "example", "long-success").toString(), "/cron")
                .waitingFor(WaitEx.forS6OverlayStart());

        _cronContainer.start();
    }

    @AfterClass
    public static void after() {
        _cronContainer.close();
    }

    @Test
    public void executeCronTickScriptWithPassedPuid() throws Exception {
        waitFor(Duration.ofSeconds(80), () -> _cronContainer.getLogsAnalyzer().contains("Execution started"));

        int puid = _cronContainer.getProcessUid("ash /usr/sbin/cron-tick");

        assertEquals(7088, puid);
    }

    @Test
    public void executeCronTickScriptWithPassedPgid() throws Exception {
        waitFor(Duration.ofSeconds(80), () -> _cronContainer.getLogsAnalyzer().contains("Execution started"));

        int pgid = _cronContainer.getProcessGid("ash /usr/sbin/cron-tick");

        assertEquals(7099, pgid);
    }
}