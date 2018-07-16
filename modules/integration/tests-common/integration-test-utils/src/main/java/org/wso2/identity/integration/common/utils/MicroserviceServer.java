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

import org.wso2.msf4j.MicroservicesRunner;

/**
 * Implements microservice server to serve a test request.
 * Uses MSF4J as the microservice server.
 */
public class MicroserviceServer {

    private MicroservicesRunner microservicesRunner;
    private int port = 18080;
    private boolean isActive;

    public MicroserviceServer(int port) {

        this.port = port;
    }

    public void init() {

        microservicesRunner = new MicroservicesRunner(port);
    }

    public void addService(String path, Object service) {

        microservicesRunner.deploy(path, service);
    }

    public void addService(Object service) {

        microservicesRunner.deploy(service);
    }

    public void start() {

        isActive = true;
        microservicesRunner.start();
    }

    public void stop() {

        if (microservicesRunner != null) {
            microservicesRunner.stop();
        }
        isActive = false;
    }

    public void destroy() {

        this.stop();
        microservicesRunner = null;
    }

    public int getPort() {

        return port;
    }

    public boolean isActive() {

        return isActive;
    }
}
