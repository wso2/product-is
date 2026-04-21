/*
 * Copyright (c) 2016, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.oidc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.oidc.bean.OIDCApplication;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimConfiguration.DialectEnum;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimMappings;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OIDCLogoutConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.RequestedClaimConfiguration;
import org.wso2.identity.integration.test.rest.api.user.common.model.ListObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.RoleItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.identity.integration.test.utils.OAuth2Constant.AUTHORIZE_ENDPOINT_URL;

/**
 * This class defines basic functionality needed to initiate an OIDC test.
 */
public class OIDCAbstractIntegrationTest extends OAuth2ServiceAbstractIntegrationTest {

    public static final int TOMCAT_PORT = 8490;

    private static final Log log = LogFactory.getLog(OIDCAbstractIntegrationTest.class);
    protected SCIM2RestClient scim2RestClient;
    protected String userId;
    protected String roleId;

    @Override
    protected void init(TestUserMode userMode) throws Exception {

        super.init(userMode);
        setSystemproperties();

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
    }

    /**
     * Clear the initialized clients.
     */
    public void clear() {

        restClient = null;
        scim2RestClient = null;
    }

    /**
     * Creates a user.
     *
     * @param user user instance.
     * @throws Exception If an error occurred while creating a user.
     */
    public void createUser(UserObject user) throws Exception {

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        userId = scim2RestClient.createUser(user);

        RoleItemAddGroupobj rolePatchReqObject = new RoleItemAddGroupobj();
        rolePatchReqObject.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        rolePatchReqObject.setPath("users");
        rolePatchReqObject.addValue(new ListObject().value(userId));

        roleId = scim2RestClient.getRoleIdByName("everyone");
        scim2RestClient.updateUserRole(new PatchOperationRequestObject().addOperations(rolePatchReqObject), roleId);
    }

    /**
     * Deletes a user.
     *
     * @param user user instance.
     * @throws Exception If an error occurred while deleting a user.
     */
    public void deleteUser(UserObject user) throws Exception {

        log.info("Deleting User " + user.getUserName());
        scim2RestClient.deleteUser(userId);
    }

    /**
     * Create an OIDC application.
     *
     * @param application application instance.
     * @throws Exception If an error creating an application.
     */
    public void createApplication(OIDCApplication application) throws Exception {

        ApplicationModel applicationModel = new ApplicationModel();
        createApplication(applicationModel, application);
    }

    public void createApplication(ApplicationModel applicationModel, OIDCApplication application) throws Exception {

        log.info("Creating application " + application.getApplicationName());

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, "authorization_code", "implicit", "password", "client_credentials",
                "refresh_token", "urn:ietf:params:oauth:grant-type:saml2-bearer", "iwa:ntlm");

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.addCallbackURLsItem(application.getCallBackURL());
        OIDCLogoutConfiguration logoutConfig = new OIDCLogoutConfiguration();
        logoutConfig.setFrontChannelLogoutUrl(application.getFrontChannellogoutURI());
        oidcConfig.setLogout(logoutConfig);

        ClaimConfiguration applicationClaimConfiguration = new ClaimConfiguration().dialect(DialectEnum.CUSTOM);
        for (String claimUri : application.getRequiredClaims()) {
            ClaimMappings claimMapping = new ClaimMappings().applicationClaim(claimUri);
            claimMapping.setLocalClaim(new Claim().uri(claimUri));

            RequestedClaimConfiguration requestedClaim = new RequestedClaimConfiguration();
            requestedClaim.setClaim(new Claim().uri(claimUri));

            applicationClaimConfiguration.addClaimMappingsItem(claimMapping);
            applicationClaimConfiguration.addRequestedClaimsItem(requestedClaim);
        }

        applicationModel.setName(application.getApplicationName());
        applicationModel.setInboundProtocolConfiguration(new InboundProtocols().oidc(oidcConfig));
        applicationModel.setClaimConfiguration(applicationClaimConfiguration);

        String applicationId = addApplication(applicationModel);
        oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);

        application.setApplicationId(applicationId);
        application.setClientId(oidcConfig.getClientId());
        application.setClientSecret(oidcConfig.getClientSecret());
    }

    /**
     * Deletes the registered OIDC application in OP.
     *
     * @param application application instance.
     * @throws Exception If an error deleting an application.
     */
    public void deleteApplication(OIDCApplication application) throws Exception {

        log.info("Deleting application " + application.getApplicationName());
        deleteApp(application.getApplicationId());
    }

    /**
     * Sends Authentication Request for an OIDC Flow.
     *
     * @param application                  application.
     * @param isFirstAuthenticationRequest true if the request is the first authentication request.
     * @param client                       http client.
     * @param cookieStore                  cookie store.
     * @throws Exception throws if an error occurs when sending the authentication request.
     */
    public void testSendAuthenticationRequest(OIDCApplication application, boolean isFirstAuthenticationRequest,
                                              HttpClient client, CookieStore cookieStore)
            throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("response_type", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("client_id", application.getClientId()));
        urlParameters.add(new BasicNameValuePair("redirect_uri", application.getCallBackURL()));

        urlParameters.add(new BasicNameValuePair("scope", "openid"));

        HttpResponse response = sendPostRequestWithParameters(client, urlParameters,
                getTenantQualifiedURL(AUTHORIZE_ENDPOINT_URL, tenantInfo.getDomain()));

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        if (isFirstAuthenticationRequest) {
            OIDCUtilTest.setSessionDataKey(response, keyPositionMap);
        } else {
            Assert.assertFalse(Utils.requestMissingClaims(response));
        }
    }
}
