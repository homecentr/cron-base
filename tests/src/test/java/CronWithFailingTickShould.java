import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class CronWithFailingTickShould {
    private static final ContainerController _controller = new ContainerController();

    @BeforeClass
    public static void before() {
        _controller.startPushGateway();

        HashMap<String, String> envVars = new HashMap<>();
        envVars.put("CRON_SCHEDULE", "* * * * *");
        envVars.put("PUSH_GATEWAY_URL", "http://push_gateway:9091/metrics/job/cron/instance/base");

        _controller.startCron(TickScript.Failure, envVars);
    }

    @AfterClass
    public static void after() {
        _controller.cleanUp();
    }

    @Test
    public void executeNextTickScript() throws Exception {
        Helpers.waitForMessageInStdOut(_controller.getCronContainer(), "Execution finished", 80);
        Helpers.waitForMessageInStdOut(_controller.getCronContainer(), "Execution started", 80, 2);
    }

    @Test
    public void reportMetricsToPushGateway() throws IOException {
        URL root = new URL(String.format("http://%s:%d/metrics",
                _controller.getPushGatewayContainer().getContainerIpAddress(),
                _controller.getPushGatewayContainer().getMappedPort(9091)));

        HttpURLConnection connection = (HttpURLConnection)root.openConnection();
        connection.connect();

        List<String> lines = Helpers.readResponseAsText(connection.getInputStream());

        // The script has a hardcoded exit code 10
        assertTrue(lines.stream().filter(l -> l.contains("exit_code{instance=\"base\",job=\"cron\"} 10")).findAny().isPresent());
        assertTrue(lines.stream().filter(l -> l.contains("duration_seconds{instance=\"base\",job=\"cron\"}")).findAny().isPresent());
    }
}
