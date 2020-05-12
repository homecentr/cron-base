import org.junit.*;
import org.testcontainers.containers.Container;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CronWithSuccessfulTickShould {
    private static final ContainerController _controller = new ContainerController();

    @BeforeClass
    public static void before() {
        _controller.startPushGateway();

        HashMap<String, String> envVars = new HashMap<>();
        envVars.put("CRON_SCHEDULE", "* * * * *");
        envVars.put("PUSH_GATEWAY_URL", "http://push_gateway:9091/metrics/job/cron/instance/base");

        _controller.startCron(TickScript.Success, envVars);
    }

    @AfterClass
    public static void after() {
        _controller.cleanUp();
    }

    @Test
    public void executeCronTickScript() throws Exception {
        Helpers.waitForMessageInStdOut(_controller.getCronContainer(), "Execution finished", 80);

        // Returns exit code 1 if the directory is empty
        Container.ExecResult execResult = _controller.getCronContainer().execInContainer("ash", "-c", "find /tmp -mindepth 1 | read");

        assertEquals(0, execResult.getExitCode());
    }

    @Test
    public void reportMetricsToPushGateway() throws IOException {
        URL root = new URL(String.format("http://%s:%d/metrics",
                _controller.getPushGatewayContainer().getContainerIpAddress(),
                _controller.getPushGatewayContainer().getMappedPort(9091)));

        HttpURLConnection connection = (HttpURLConnection)root.openConnection();
        connection.connect();

        List<String> lines = Helpers.readResponseAsText(connection.getInputStream());

        assertTrue(lines.stream().filter(l -> l.contains("exit_code{instance=\"base\",job=\"cron\"} 0")).findAny().isPresent());
        assertTrue(lines.stream().filter(l -> l.contains("duration_seconds{instance=\"base\",job=\"cron\"}")).findAny().isPresent());
    }
}
