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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.engine.frameworkutils.TestFrameworkUtils;
import org.wso2.carbon.automation.extensions.ExtensionConstants;
import org.wso2.carbon.automation.extensions.servers.carbonserver.MultipleServersManager;
import org.wso2.identity.integration.common.utils.CarbonTestServerManager;
import org.wso2.identity.integration.test.utils.CommonConstants;

import javax.xml.xpath.XPathExpressionException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An Extension to provide the capability to deploy additional identity servers at the beginning of
 * and at the end of a test suite.
 */
public class IdentityServersDeployer implements ISuiteListener{

    private static final Log log = LogFactory.getLog(IdentityServersDeployer.class);

    private MultipleServersManager serversManager = new MultipleServersManager();
    private Map<Integer, AutomationContext> automationContextMap = new HashMap<>();

    // Using ThreadLocal variable to pass Automation Contexts of the deployed ISs.
    private static final ThreadLocal<Map<Integer, AutomationContext>> testSuiteThreadLocal = new ThreadLocal<>();

    public static Map<Integer, AutomationContext> getThreadLocalAutomationContextMap() {
        return testSuiteThreadLocal.get();
    }

    @Override
    public void onStart(ISuite iSuite) {
        try {
            startCarbonServers(getIdentityServersParams(iSuite));
        } catch (AutomationFrameworkException ex) {
            stopTestExecution("Error while starting additional ISs.", ex);
        }

        // Set unmodifiable automationContextMap to threadLocal variable in order to be accessed within test classes.
        testSuiteThreadLocal.set(Collections.unmodifiableMap(automationContextMap));
    }

    @Override
    public void onFinish(ISuite iSuite) {
        try {
            serversManager.stopAllServers();
        } catch (AutomationFrameworkException ex) {
            stopTestExecution("Error while stopping additional ISs", ex);
        } finally {
            testSuiteThreadLocal.remove();
        }
    }

    /**
     * Start carbon servers with given contexts and parameters.
     *
     * @param identityServerParams parameter bean array.
     * @throws AutomationFrameworkException when failed to create AutomationContext.
     */
    private void startCarbonServers(IdentityServerParamsBean[] identityServerParams)
            throws AutomationFrameworkException {

        CarbonTestServerManager[] carbonServers = new CarbonTestServerManager[identityServerParams.length];
        int index = 0;
        for(IdentityServerParamsBean ISparam : identityServerParams ){
            // Set startup parameters.
            Map<String, String> startupParams = new HashMap<>();
            startupParams.put(ExtensionConstants.PORT_OFFSET_COMMAND, String.valueOf(CommonConstants
                    .IS_DEFAULT_OFFSET + ISparam.getPortOffset()));

            // Create automation context and add to the automation contextMap against portOffset.
            AutomationContext automationContext = null;
            try {
                automationContext = new AutomationContext(ISparam.getProducGroupName(), ISparam
                        .getInstanceName(), ISparam.getTestUserMode());
            } catch (XPathExpressionException e) {
                throw new AutomationFrameworkException("Error While creating Automation context from productGroup: "
                        + ISparam.getProducGroupName() + ", instanceName: " + ISparam.getInstanceName() + ", " +
                        "testUserMode: " + ISparam.getTestUserMode());
            }
            automationContextMap.put(ISparam.getPortOffset(), automationContext);

            // Create CarbonTestServerManger and add it to the servers array.
            carbonServers[index] = new CarbonTestServerManager(automationContext, System.getProperty
                    (ExtensionConstants.SYSTEM_PROPERTY_CARBON_ZIP_LOCATION), startupParams);
            index++;
        }
        serversManager.startServers(carbonServers);
    }

    /**
     * Bean class for Additional IS parameters.
     */
    public static class IdentityServerParamsBean{
        private String producGroupName;
        private String instanceName;
        private TestUserMode testUserMode;
        private int portOffset;

        public String getProducGroupName() {
            return producGroupName;
        }

        public void setProducGroupName(String producGroupName) {
            this.producGroupName = producGroupName;
        }

        public String getInstanceName() {
            return instanceName;
        }

        public void setInstanceName(String instanceName) {
            this.instanceName = instanceName;
        }

        public TestUserMode getTestUserMode() {
            return testUserMode;
        }

        public void setTestUserMode(TestUserMode testUserMode) {
            this.testUserMode = testUserMode;
        }

        public int getPortOffset() {
            return portOffset;
        }

        public void setPortOffset(int portOffset) {
            this.portOffset = portOffset;
        }
    }

    private IdentityServerParamsBean[] getIdentityServersParams(ISuite iSuite) throws AutomationFrameworkException {
        int ISsCount;
        String[] portOffsets;
        String[] productGroupNames;
        String[] instanceNames;
        String[] testUserModes;

        // AdditionalIsCount should be a number.
        try {
            ISsCount = Integer.parseInt(iSuite.getParameter(IdentityListenerConstants.ADDITIONAL_IS_COUNT_PARAMETER));
        } catch (NumberFormatException ex) {
            throw new AutomationFrameworkException(IdentityListenerConstants.ADDITIONAL_IS_COUNT_PARAMETER + " should" +
                    " be a number.", ex);
        }

        // Additional ISs deployment parameters should be not null and each parameter values count should be equal to
        // number of additional ISs count.
        IdentityServerParamsBean[] identityServersParams = new IdentityServerParamsBean[ISsCount];
        try {
            portOffsets = iSuite.getParameter(IdentityListenerConstants.ADDITIONAL_IS_PORT_OFFSETS_PARAMETER)
                    .split(IdentityListenerConstants.PARAMETER_SEPARATOR);
            productGroupNames = iSuite.getParameter(
                    IdentityListenerConstants.ADDITIONAL_IS_PROUDUCT_GROUP_NAMES_PARAMETER)
                    .split(IdentityListenerConstants.PARAMETER_SEPARATOR);
            instanceNames = iSuite.getParameter(IdentityListenerConstants.ADDITIONAL_IS_INSTANCE_NAMES_PARAMETER)
                    .split(IdentityListenerConstants.PARAMETER_SEPARATOR);
            testUserModes = iSuite.getParameter(IdentityListenerConstants.ADDITIONAL_IS_TEST_USER_MODES_PARAMETER)
                    .split(IdentityListenerConstants.PARAMETER_SEPARATOR);
            if (ISsCount != portOffsets.length || ISsCount != productGroupNames.length
                    || ISsCount != instanceNames.length || ISsCount != testUserModes.length) {
                throw new AutomationFrameworkException("Incorrect additional ISs deployment parameters.");
            }
        } catch (NullPointerException ex) {
            throw new AutomationFrameworkException("Additional ISs deployment parameters cannot be null.", ex);
        }

        Map<String, TestUserMode> testUserModeMap = TestFrameworkUtils.getTestUserModeMap();

        for(int index = 0; index < ISsCount; index++){
            IdentityServerParamsBean ISParamsBean = new IdentityServerParamsBean();

            // If the configured portOffset is not a number throw Exception.
            try {
                ISParamsBean.setPortOffset(Integer.parseInt(portOffsets[index]));
            } catch (NumberFormatException ex) {
                stopTestExecution(portOffsets[index] + "is not a valid port offset.");
            }

            ISParamsBean.setProducGroupName(productGroupNames[index]);
            ISParamsBean.setInstanceName(instanceNames[index]);

            // Get the corresponding TestUserMode enum for the testUserMode configured in test-suite.xml
            TestUserMode testUserMode = testUserModeMap.get(testUserModes[index]);
            if (testUserMode != null) {
                ISParamsBean.setTestUserMode(testUserModeMap.get(testUserModes[index]));
            } else {
                stopTestExecution("Test User Mode: " + testUserModes[index] + " is not a supported user mode type.");
            }

            // Set additional identity servers parameters.
            identityServersParams[index] = ISParamsBean;
        }

        return identityServersParams;
    }

    private static void stopTestExecution(String msg, Exception e) {
        log.error(msg, e);
        throw new RuntimeException(msg, e);
    }

    private static void stopTestExecution(String msg) {
        log.error(msg);
        throw new RuntimeException(msg);
    }
}
