/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
