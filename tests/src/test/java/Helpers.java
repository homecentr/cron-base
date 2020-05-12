import org.testcontainers.containers.GenericContainer;
import org.testcontainers.shaded.org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public class Helpers {
    public static void waitForMessageInStdOut(GenericContainer container, String message, Integer timeout) throws Exception {
        waitForMessageInStdOut(container, message, timeout, 1);
    }

    public static void waitForMessageInStdOut(GenericContainer container, String message, Integer timeout, Integer times) throws Exception {
        long timeoutExpiredMs = System.currentTimeMillis() + (timeout * 1000);

        while(StringUtils.countMatches(container.getLogs(), message) < times) {
            long waitMillis = timeoutExpiredMs - System.currentTimeMillis();

            if (waitMillis <= 0) {
                throw new Exception("The container output did not print the expected message \"" + message + "\" ("+ times +"x) in time.");
            }

            Thread.sleep(1000);
        }
    }

    public static List<String> readResponseAsText(InputStream responseStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream))) {
            List<String> lines = reader.lines().collect(Collectors.toList());

            return lines;
        }
    }
}
