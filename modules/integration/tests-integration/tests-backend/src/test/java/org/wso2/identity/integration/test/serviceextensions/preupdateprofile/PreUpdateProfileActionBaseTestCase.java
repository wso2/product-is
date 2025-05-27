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

package org.wso2.identity.integration.test.serviceextensions.preupdateprofile;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.serviceextensions.common.ActionsBaseTestCase;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.AuthenticationType;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.common.model.Endpoint;
import org.wso2.identity.integration.test.rest.api.server.action.management.v1.preupdateprofile.model.PreUpdateProfileActionModel;
import org.wso2.identity.integration.test.utils.CarbonUtils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertTrue;
import static org.wso2.identity.integration.test.utils.OAuth2Constant.*;

public class PreUpdateProfileActionBaseTestCase extends ActionsBaseTestCase {

    private static final String SCIM2_USERS_API = "/scim2/Users";
    private static final String INTERNAL_USER_MANAGEMENT_UPDATE = "internal_user_mgt_update";

    protected static final String TEST_USER1_USERNAME = "preUpdateProfileTestUserName";
    protected static final String TEST_USER_PASSWORD = "TestPassword@123";
    protected static final String TEST_USER_CLAIM_VALUE = "testNickName";
    protected static final String TEST_USER_UPDATED_CLAIM_VALUE = "updateTestNickName";
    protected static final String TEST_USER_GIVEN_NAME = "test_user_given_name";
    protected static final String TEST_USER_LASTNAME = "test_user_last_name";
    protected static final String TEST_USER_EMAIL = "test.user@gmail.com";
    protected static final String NICK_NAME_USER_SCHEMA_NAME = "nickName";
    protected static final String NICK_NAME_CLAIM_URI = "http://wso2.org/claims/nickname";
    protected static final String GIVEN_NAME_CLAIM_URI = "http://wso2.org/claims/givenname";
    protected static final String PRIMARY_USER_STORE_ID = "UFJJTUFSWQ==";
    protected static final String PRIMARY_USER_STORE_NAME = "PRIMARY";
    protected static final String PRE_UPDATE_PROFILE_API_PATH = "preUpdateProfile";
    protected static final String ACTION_NAME = "Pre Update Profile Action";
    protected static final String ACTION_DESCRIPTION = "This is a test for pre update profile action type";
    protected static final String CLIENT_CREDENTIALS_GRANT_TYPE = "client_credentials";
    protected static final String MOCK_SERVER_ENDPOINT_RESOURCE_PATH = "/test/action";
    protected static final String EMPTY_STRING = "";

    protected CloseableHttpClient client;

    private final CookieStore cookieStore = new BasicCookieStore();

    /**
     * Initialize the test case.
     *
     * @param userMode User Mode
     * @throws Exception If an error occurred while initializing the clients.
     */
    protected void init(TestUserMode userMode) throws Exception {

        super.init(userMode);

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        client = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultCookieStore(cookieStore)
                .build();

    }

    protected String createPreUpdateProfileAction(String actionName, String actionDescription) throws IOException {

        AuthenticationType authentication = new AuthenticationType()
                .type(AuthenticationType.TypeEnum.BASIC)
                .putPropertiesItem(USERNAME_PROPERTY, MOCK_SERVER_AUTH_BASIC_USERNAME)
                .putPropertiesItem(PASSWORD_PROPERTY, MOCK_SERVER_AUTH_BASIC_PASSWORD);

        Endpoint endpoint = new Endpoint()
                .uri(EXTERNAL_SERVICE_URI)
                .authentication(authentication);

        PreUpdateProfileActionModel actionModel = new PreUpdateProfileActionModel();
        actionModel.setName(actionName);
        actionModel.setDescription(actionDescription);
        actionModel.setEndpoint(endpoint);
        actionModel.setAttributes(Collections.singletonList(NICK_NAME_CLAIM_URI));

        return createAction(PRE_UPDATE_PROFILE_API_PATH, actionModel);
    }

    protected String getTokenWithClientCredentialsGrant(String applicationId, String clientId, String clientSecret) throws Exception {

        if (!CarbonUtils.isLegacyAuthzRuntimeEnabled()) {
            authorizeSystemAPIs(applicationId, Collections.singletonList(SCIM2_USERS_API));
        }

        List<String> requestedScopes = new ArrayList<>();
        Collections.addAll(requestedScopes,INTERNAL_USER_MANAGEMENT_UPDATE);
        String scopes = String.join(" ", requestedScopes);

        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CLIENT_CREDENTIALS));
        parameters.add(new BasicNameValuePair("scope", scopes));

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(AUTHORIZATION_HEADER, OAuth2Constant.BASIC_HEADER + " " +
                getBase64EncodedString(clientId, clientSecret)));
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("User-Agent", OAuth2Constant.USER_AGENT));

        HttpResponse response = sendPostRequest(client, headers, parameters,
                getTenantQualifiedURL(ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        JSONObject jsonResponse = new JSONObject(responseString);

        assertTrue(jsonResponse.has("access_token"));
        return jsonResponse.getString("access_token");
    }
}

