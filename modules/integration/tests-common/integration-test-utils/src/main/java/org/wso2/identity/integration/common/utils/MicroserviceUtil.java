package org.wso2.identity.integration.common.utils;

import java.io.IOException;
import java.net.ServerSocket;

public class MicroserviceUtil {

    /**
     * Initializes the micro-service server.
     * Detects an available port from the system and use that for the microservice server.
     */
    public static MicroserviceServer initMicroserviceServer() throws IOException {

        ServerSocket s = new ServerSocket(0);
        int port = s.getLocalPort();
        s.close();

        MicroserviceServer microserviceServer = new MicroserviceServer(port);
        microserviceServer.init();
        return microserviceServer;
    }

    public static void deployService(MicroserviceServer microserviceServer, Object instance) {

        if (microserviceServer != null) {
            microserviceServer.addService(instance);
            microserviceServer.start();
        }
    }

    public static void destroyService(MicroserviceServer microserviceServer) {

        if (microserviceServer != null) {
            microserviceServer.stop();
            microserviceServer.destroy();
        }
    }
}
