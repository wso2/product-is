/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.identity.integration.test.rest.api.server.flow.execution.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import org.apache.commons.lang.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.wso2.identity.integration.test.rest.api.server.common.RESTAPIServerTestBase;
import org.wso2.identity.integration.test.rest.api.server.flow.execution.v1.model.FlowConfig;
import org.wso2.identity.integration.test.rest.api.server.flow.management.v1.model.FlowRequest;
import org.wso2.identity.integration.test.restclients.FlowManagementClient;

/**
 * This class contains the test cases for Registration Execution API.
 */
public class FlowExecutionTestBase extends RESTAPIServerTestBase {

    protected static final String STATUS_INCOMPLETE = "INCOMPLETE";
    protected static final String STATUS_COMPLETE = "COMPLETE";
    protected static final String TYPE_VIEW = "VIEW";
    protected static final String TYPE_REDIRECTION = "REDIRECTION";
    protected static final String REGISTRATION_FLOW = "registration-flow.json";
    protected static final String API_DEFINITION_NAME = "flow-execution.yaml";
    protected static final String API_VERSION = "v1";
    protected static final String API_PACKAGE_NAME = "org.wso2.carbon.identity.api.server.flow.execution.v1";
    protected static String swaggerDefinition;

    static {
        try {
            swaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (Exception e) {
            throw new RuntimeException("Unable to read the swagger definition " + API_DEFINITION_NAME + " from "
                    + API_PACKAGE_NAME, e);
        }
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        super.conclude();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    protected void addRegistrationFlow(FlowManagementClient client) throws Exception {

        String registrationFlowRequestJson = readResource(REGISTRATION_FLOW);
        FlowRequest flowRequest = new ObjectMapper()
                .readValue(registrationFlowRequestJson, FlowRequest.class);
        client.putFlow(flowRequest);
    }

    protected void enableFlow(String flowType, FlowManagementClient client) throws Exception {

        FlowConfig flowConfigDTO = new FlowConfig();
        flowConfigDTO.setIsEnabled(true);
        flowConfigDTO.setFlowType(FlowTypes.REGISTRATION);
        client.updateFlowConfig(flowConfigDTO);
    }

    protected void disableFlow(String flowType, FlowManagementClient client) throws Exception {

        FlowConfig flowConfigDTO = new FlowConfig();
        flowConfigDTO.setIsEnabled(false);
        flowConfigDTO.setFlowType(FlowTypes.REGISTRATION);
        client.updateFlowConfig(flowConfigDTO);
    }

    protected static class FlowTypes {

        public static final String REGISTRATION = "REGISTRATION";
    }
}
