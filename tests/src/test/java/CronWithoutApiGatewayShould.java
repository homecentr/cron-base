import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;

public class CronWithoutApiGatewayShould {
    private static final ContainerController _controller = new ContainerController();

    @BeforeClass
    public static void before() {
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
    public void executeNextTickScript() throws Exception {
        Helpers.waitForMessageInStdOut(_controller.getCronContainer(), "Execution finished", 80);
        Helpers.waitForMessageInStdOut(_controller.getCronContainer(), "Execution started", 80, 2);
    }
}