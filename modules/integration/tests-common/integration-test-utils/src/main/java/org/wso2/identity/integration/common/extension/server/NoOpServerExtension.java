/*
 * Copyright (c) 2024, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.common.extension.server;

import org.wso2.carbon.automation.extensions.servers.carbonserver.CarbonServerExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * No-Op Server Extension for testing against external/pre-running IS instances.
 *
 * This extension overrides lifecycle methods but does nothing - it disables all server
 * lifecycle management (start/stop/extraction).
 *
 * Use this when running tests against an external IS instance that's already running.
 *
 * Configuration:
 *   <class>
 *       <name>org.wso2.identity.integration.common.extension.server.NoOpServerExtension</name>
 *   </class>
 *
 * System Properties:
 *   -Dintegration.test.is.host=<your-external-is-host>
 *   -Dintegration.test.is.https.port=<your-external-is-https-port>
 *   -Dintegration.test.sample.host=<your-sample-app-host>
 *   -Dintegration.test.sample.http.port=<your-sample-app-http-port>
 */
public class NoOpServerExtension extends CarbonServerExtension {

    private Logger log = LoggerFactory.getLogger(NoOpServerExtension.class);

    @Override
    public void initiate() {
        // Override to prevent parent CarbonServerExtension from initializing server management
        log.info("NoOpServerExtension: Initiated. No server lifecycle management will be performed.");
    }

    @Override
    public void onExecutionStart() {
        // Override to prevent parent CarbonServerExtension from starting server
        log.info("NoOpServerExtension: onExecutionStart() - No action taken. Expecting external IS to be running.");
    }

    @Override
    public void onExecutionFinish() {
        // Override to prevent parent CarbonServerExtension from stopping server
        log.info("NoOpServerExtension: onExecutionFinish() - No action taken. External IS will remain running.");
    }
}


