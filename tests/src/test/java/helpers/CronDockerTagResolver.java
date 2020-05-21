package helpers;

import io.homecentr.testcontainers.images.EnvironmentImageTagResolver;

public class CronDockerTagResolver extends EnvironmentImageTagResolver {
    public CronDockerTagResolver() {
        super("homecentr/cron-base:local");
    }
}
