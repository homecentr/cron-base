package helpers;

import io.homecentr.testcontainers.containers.GenericContainerEx;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

public class PushGatewayContainerFactory {
    public static GenericContainerEx create(Network network) {
        GenericContainerEx result = new GenericContainerEx<>("prom/pushgateway")
                .withNetwork(network)
                .withNetworkAliases("push_gateway")
                .waitingFor(Wait.forLogMessage(".*listen_address=:9091.*", 1));

        return result;
    }
}
