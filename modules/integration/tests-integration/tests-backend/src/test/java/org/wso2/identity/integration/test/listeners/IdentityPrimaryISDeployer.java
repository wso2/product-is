/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.listeners;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.ContextXpathConstants;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.extensions.ExtensionConstants;
import org.wso2.carbon.automation.extensions.FrameworkExtensionUtils;
import org.wso2.carbon.automation.extensions.servers.carbonserver.TestServerManager;

import javax.xml.xpath.XPathExpressionException;
import java.util.HashMap;
import java.util.Map;

/**
 * An Extension to provide the capability to start and stop a carbon server at the beginning of and at the end of a
 * test suite.
 */
public class IdentityPrimaryISDeployer implements ISuiteListener {

    private static final Log log = LogFactory.getLog(IdentityPrimaryISDeployer.class);

    private TestServerManager serverManager;
    private Map<String, String> primayISParameters = new HashMap<>();
    private AutomationContext automationContext;

    @Override
    public void onStart(ISuite iSuite) {

        try {
            initParameters(iSuite);
            if (StringUtils.isEmpty(primayISParameters.get(ExtensionConstants.SERVER_STARTUP_PORT_OFFSET_COMMAND))) {
                primayISParameters.put(ExtensionConstants.SERVER_STARTUP_PORT_OFFSET_COMMAND, Integer.toString(
                        ExtensionConstants.DEFAULT_CARBON_PORT_OFFSET));
            }

            // Initializing automation context.
            try {
                automationContext = new AutomationContext();
                serverManager = new TestServerManager(automationContext, null, primayISParameters);
            } catch (XPathExpressionException ex) {
                stopTestExecution("Error while initializing automation context.", ex);
            }

            // Start the server and set CARBON_HOME system property.
            String carbonHome = serverManager.startServer(primayISParameters.get(
                    IdentityListenerConstants.CONF_LOCATION));
            System.setProperty(ExtensionConstants.CARBON_HOME, carbonHome);
        } catch (AutomationFrameworkException ex) {
            stopTestExecution("Error while initializing the Automation Context", ex);
        }
    }

    @Override
    public void onFinish(ISuite iSuite) {
        try {
            serverManager.stopServer();
        } catch (AutomationFrameworkException ex) {
            stopTestExecution("Failed to stop the carbon server.", ex);
        }
    }

    private void initParameters(ISuite iSuite) throws AutomationFrameworkException {
        primayISParameters.put(ExtensionConstants.SERVER_STARTUP_PORT_OFFSET_COMMAND, iSuite.getParameter(
                ExtensionConstants.SERVER_STARTUP_PORT_OFFSET_COMMAND));
        primayISParameters.put(ExtensionConstants.SERVER_STARTUP_SETUP_COMMAND, iSuite.getParameter(
                ExtensionConstants.SERVER_STARTUP_SETUP_COMMAND));

        // If the confLocation is empty then it implies that there are no config changes required
        // for primaryIS deployment.
        String relativeConfLocation = iSuite.getParameter(IdentityListenerConstants.CONF_LOCATION_PARAMETER);
        if(StringUtils.isNotEmpty(relativeConfLocation)){
            primayISParameters.put(IdentityListenerConstants.CONF_LOCATION,
                    FrameworkExtensionUtils.getResourceLocation(relativeConfLocation));
        }
    }

    private static void stopTestExecution(String msg, Exception e) {
        log.error(msg, e);
        throw new RuntimeException(msg, e);
    }
}
