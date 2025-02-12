/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.actions;

import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.ActionModel;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.ActionUpdateModel;
import org.wso2.identity.integration.test.restclients.ActionsRestClient;

import java.io.IOException;

/**
 * Base test case for action-related tests.
 * This class extends {@link OAuth2ServiceAbstractIntegrationTest} and provides the necessary setup
 * and utility methods for testing actions via the {@link ActionsRestClient}.
 */
public class ActionsBaseTestCase extends OAuth2ServiceAbstractIntegrationTest {

    protected ActionsRestClient actionsRestClient;
    protected static final String USERNAME_PROPERTY = "username";
    protected static final String PASSWORD_PROPERTY = "password";
    protected static final String MOCK_SERVER_AUTH_BASIC_USERNAME = "test";
    protected static final String MOCK_SERVER_AUTH_BASIC_PASSWORD = "test";
    protected static final String EXTERNAL_SERVICE_URI = "http://localhost:8587/test/action";

    /**
     * Initialize the test case.
     *
     * @param userMode User Mode
     * @throws Exception If an error occurred while initializing the clients.
     */
    protected void init(TestUserMode userMode) throws Exception {

        super.init(userMode);

        actionsRestClient = new ActionsRestClient(serverURL, tenantInfo);

        setSystemproperties();
    }

    /**
     * Create action of different types.
     *
     * @param actionType  Type of action
     * @param actionModel Request object to create the action
     * @return ID of the created action
     * @throws IOException If an error occurred while creating the action
     */
    public String createAction(String actionType, ActionModel actionModel) throws IOException {

        return actionsRestClient.createActionType(actionModel, actionType);
    }

    /**
     * Update an action.
     *
     * @param actionType  Type of action
     * @param actionId    ID of the action
     * @param actionModel Request object to update the action
     * @return Status of the action update
     * @throws IOException If an error occurred while updating the action
     */
    public boolean updateAction(String actionType, String actionId, ActionUpdateModel actionModel) throws IOException {

        return actionsRestClient.updateAction(actionType, actionId, actionModel);
    }

    /**
     * Delete an action.
     *
     * @param actionType Type of action
     * @param actionId   ID of the action
     * @return Status code of the action creation
     * @throws IOException If an error occurred while deleting the action
     */
    public int deleteAction(String actionType, String actionId) throws IOException {

        return actionsRestClient.deleteActionType(actionType, actionId);
    }
}
