/*
 * Copyright (c) 2015, WSO2 LLC. (https://www.wso2.com).
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
package org.wso2.identity.integration.test.oauth2;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OAuth2ServiceErrorResponseTest extends OAuth2ServiceAbstractIntegrationTest {

    private String applicationId;
    private CloseableHttpClient client;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);

        setSystemproperties();
        client = HttpClients.createDefault();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApp(applicationId);
        consumerKey = null;
        applicationId = null;
        client.close();
        restClient.closeHttpClient();
    }

    @Test(groups = "wso2.is", description = "Check Oauth2 application registration")
    public void testRegisterApplication() throws Exception {

        ApplicationResponseModel application = addApplication();
        Assert.assertNotNull(application, "Application creation failed.");

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(application.getId());
        consumerKey = oidcConfig.getClientId();
        Assert.assertNotNull(consumerKey, "Application creation failed.");
        consumerSecret = oidcConfig.getClientSecret();

        applicationId = application.getId();
    }

    @Test(groups = "wso2.is", description = "Test unsupported grant type error response", dependsOnMethods =
            "testRegisterApplication")
    public void testUnsupportedGrantTypeErrorResponse() throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("response_type",
                                                 OAuth2Constant.OAUTH2_GRANT_TYPE_IMPLICIT));
        urlParameters.add(new BasicNameValuePair("client_id", consumerKey));
        urlParameters.add(new BasicNameValuePair("redirect_uri", OAuth2Constant.CALLBACK_URL));

        HttpResponse response =
                sendPostRequestWithParameters(client, urlParameters, getTenantQualifiedURL(
                        OAuth2Constant.APPROVAL_URL, tenantInfo.getDomain()));
        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null");

        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);

        String locationURI = locationHeader.getValue();

        Assert.assertTrue(locationURI.contains(OAuth2Constant.UNAUTHORIZED_CLIENT));
        Assert.assertTrue(locationURI.contains("not.authorized.to.use.requested.grant.type"));
    }

    public ApplicationResponseModel addApplication() throws Exception {

        ApplicationModel application = new ApplicationModel();

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, "");

        List<String> callBackUrls = new ArrayList<>();
        Collections.addAll(callBackUrls, OAuth2Constant.CALLBACK_URL);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setCallbackURLs(callBackUrls);

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setName(OAuth2Constant.OAUTH_APPLICATION_NAME);

        String appId = addApplication(application);

        return getApplication(appId);
    }
}
