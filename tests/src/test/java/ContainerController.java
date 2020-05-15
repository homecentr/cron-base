import org.junit.AfterClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.nio.file.Paths;
import java.util.HashMap;

public class ContainerController {
    private static final Logger logger = LoggerFactory.getLogger(ContainerController.class);

    private GenericContainer _container;
    private GenericContainer _pushGatewayContainer;
    private Network _network;

    public ContainerController() {
        _network = Network.newNetwork();
    }

    public void startCron(TickScript tickScript, HashMap<String, String> envVars) {
        _container = createCronContainer(tickScript, envVars);
    }

    public void startPushGateway() {
        _pushGatewayContainer = createPushGatewayContainer();
    }

    @AfterClass
    public void cleanUp() {
        if(_pushGatewayContainer != null) {
            _pushGatewayContainer.stop();
            _pushGatewayContainer.close();
        }

        _container.stop();
        _container.close();
    }

    protected GenericContainer getCronContainer() {
        return _container;
    }

    protected GenericContainer getPushGatewayContainer() {
        return _pushGatewayContainer;
    }

    private GenericContainer createCronContainer(TickScript tickScript, HashMap<String, String> envVars) {
        String dockerImageTag = System.getProperty("image_tag");

        logger.info("Tested Docker image tag: {}", dockerImageTag);

        GenericContainer result = new GenericContainer<>(dockerImageTag)
                .withNetwork(_network)
                .withEnv(envVars)
                .withFileSystemBind(Paths.get(System.getProperty("user.dir"), "..", "example", getTickScriptDirectory(tickScript)).normalize().toString(), "/config")
                .waitingFor(Wait.forLogMessage(".*\\[services\\.d\\] done.*", 1));

        result.start();
        result.followOutput(new Slf4jLogConsumer(logger));

        return result;
    }

    private GenericContainer createPushGatewayContainer() {
        GenericContainer result = new GenericContainer<>("prom/pushgateway")
                .withNetwork(_network)
                .withNetworkAliases("push_gateway")
                .waitingFor(Wait.forLogMessage(".*listen_address=:9091.*", 1));

        result.start();
        result.followOutput(new Slf4jLogConsumer(logger));

        return result;
    }

    private String getTickScriptDirectory(TickScript script) {
        return script == TickScript.Success
                ? "success"
                : "failure";
    }
}