/*
 * Copyright (c) 2015, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.saml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * Integration tests for SAML invalid issuer.
 */
public class SAMLInvalidIssuerTestCase extends AbstractSAMLSSOTestCase {

    private static final Log log = LogFactory.getLog(SAMLInvalidIssuerTestCase.class);

    // SAML Application attributes
    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    private static final String APPLICATION_NAME = "SAML-SSO-TestApplication";

    private static final String ACS_URL = "http://localhost:8490/travelocity.com/home.jsp";
    private static final String SAML_SSO_LOGIN_URL =
            "http://localhost:8490/travelocity.com/samlsso?SAML2.HTTPBinding=HTTP-Redirect";
    private static final String SAML_ERROR_NOTIFICATION_PATH = "/authenticationendpoint/samlsso_notification.do";

    private DefaultHttpClient httpClient;

    private boolean isSAMLReturned;

    private final SAMLConfig config;
    private String appId;
    private String userId;

    @Factory(dataProvider = "samlConfigProvider")
    public SAMLInvalidIssuerTestCase(SAMLConfig config) {

        if (log.isDebugEnabled()){
            log.debug("SAML SSO Test initialized for " + config.toString());
        }
        this.config = config;
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(config.getUserMode());

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        applicationMgtRestClient = new OAuth2RestClient(serverURL, tenantInfo);

        isSAMLReturned = false;

        httpClient = new DefaultHttpClient();
        httpClient.setRedirectHandler(new DefaultRedirectHandler() {
            @Override
            public URI getLocationURI(HttpResponse response,
                                      HttpContext context) throws ProtocolException {

                if (response == null) {
                    throw new IllegalArgumentException("HTTP Response should not be null");
                }
                //get the location header to find out where to redirect to
                Header locationHeader = response.getFirstHeader("Location");
                if (locationHeader == null) {
                    // got a redirect resp, but no location header
                    throw new ProtocolException(
                            "Received redirect resp " + response.getStatusLine()
                            + " but no location header");
                }

                URL url = null;
                try {
                    url = new URL(locationHeader.getValue());
                    if (SAML_ERROR_NOTIFICATION_PATH.equals(url.getPath()) &&
                        url.getQuery().contains("SAMLResponse")) {
                        isSAMLReturned = true;
                    }
                } catch (MalformedURLException e) {
                    throw new ProtocolException("Invalid redirect URI: " + locationHeader.getValue(), e);
                }

                return super.getLocationURI(response, context);
            }
        });

        userId = super.addUser(config);
        appId = addApplication(APPLICATION_NAME);
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {

        super.deleteUser(userId);
        deleteApp(appId);

        applicationMgtRestClient = null;
        scim2RestClient = null;
        httpClient = null;
    }

    @Test(alwaysRun = true, description = "Testing SAML SSO login", groups = "wso2.is",
          priority = 1)
    public void testSAMLSSOLogin() {

        try {
            HttpResponse response;

            response = Utils.sendGetRequest(SAML_SSO_LOGIN_URL, USER_AGENT, httpClient);

            String sessionKey = Utils.extractDataFromResponse(response, CommonConstants.SAML_RESPONSE_PARAM, 1);
            response = Utils.sendPOSTMessage(sessionKey, SAML_SSO_URL, USER_AGENT, ACS_URL, config.getApp()
                    .getArtifact(), config.getUser().getUsername(), config.getUser().getPassword(), httpClient);
            EntityUtils.consume(response.getEntity());

            Utils.sendRedirectRequest(response, USER_AGENT, ACS_URL, config.getApp().getArtifact(), httpClient);

            Assert.assertTrue(isSAMLReturned, "Sending SAML response to the samlsso_notification page failed");

        } catch (Exception e) {
            Assert.fail("SAML SSO Login test failed.", e);
        }
    }

    @DataProvider(name = "samlConfigProvider")
    public static Object[][] samlConfigProvider(){

        return  new SAMLConfig[][]{
                {new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, User.SUPER_TENANT_USER, HttpBinding.HTTP_REDIRECT,
                        ClaimType.NONE, App.SUPER_TENANT_APP_WITH_SIGNING)},
                {new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, User.SUPER_TENANT_USER, HttpBinding.HTTP_REDIRECT,
                        ClaimType.LOCAL, App.SUPER_TENANT_APP_WITH_SIGNING)},
                {new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, User.SUPER_TENANT_USER, HttpBinding.HTTP_POST,
                        ClaimType.NONE, App.SUPER_TENANT_APP_WITH_SIGNING)},
                {new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, User.SUPER_TENANT_USER, HttpBinding.HTTP_POST,
                        ClaimType.LOCAL, App.SUPER_TENANT_APP_WITH_SIGNING)},
                {new SAMLConfig(TestUserMode.TENANT_ADMIN, User.TENANT_USER, HttpBinding.HTTP_REDIRECT,
                        ClaimType.NONE, App.TENANT_APP_WITHOUT_SIGNING)},
                {new SAMLConfig(TestUserMode.TENANT_ADMIN, User.TENANT_USER, HttpBinding.HTTP_REDIRECT,
                        ClaimType.LOCAL, App.TENANT_APP_WITHOUT_SIGNING)},
                {new SAMLConfig(TestUserMode.TENANT_ADMIN, User.TENANT_USER, HttpBinding.HTTP_POST,
                        ClaimType.NONE, App.TENANT_APP_WITHOUT_SIGNING)},
                {new SAMLConfig(TestUserMode.TENANT_ADMIN, User.TENANT_USER, HttpBinding.HTTP_POST,
                        ClaimType.LOCAL, App.TENANT_APP_WITHOUT_SIGNING)},
        };
    }

    public String addApplication(String appName) throws Exception {

        ApplicationModel applicationModel = new ApplicationModel()
                .name(appName)
                .description("This is a test Service Provider")
                .claimConfiguration(getClaimConfigurations());

        return applicationMgtRestClient.createApplication(applicationModel);
    }
}
