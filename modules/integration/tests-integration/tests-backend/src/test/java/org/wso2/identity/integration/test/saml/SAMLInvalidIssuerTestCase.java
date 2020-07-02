/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.saml;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
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
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class SAMLInvalidIssuerTestCase extends AbstractSAMLSSOTestCase {

    private static final Log log = LogFactory.getLog(SAMLInvalidIssuerTestCase.class);

    // SAML Application attributes
    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    private static final String ISSUER_NAME = "dummy.com";
    private static final String APPLICATION_NAME = "SAML-SSO-TestApplication";
    private static final String INBOUND_AUTH_TYPE = "samlsso";
    private static final String ATTRIBUTE_CS_INDEX_VALUE = "1239245949";
    private static final String ATTRIBUTE_CS_INDEX_NAME = "attrConsumServiceIndex";

    private static final String ACS_URL = "http://localhost:8490/travelocity.com/home.jsp";
    private static final String COMMON_AUTH_URL = "https://localhost:9853/commonauth";
    private static final String SAML_SSO_LOGIN_URL =
            "http://localhost:8490/travelocity.com/samlsso?SAML2.HTTPBinding=HTTP-Redirect";
    private static final String SAML_ERROR_NOTIFICATION_PATH = "/authenticationendpoint/samlsso_notification.do";

    private ApplicationManagementServiceClient applicationManagementServiceClient;
    private RemoteUserStoreManagerServiceClient remoteUSMServiceClient;
    private DefaultHttpClient httpClient;

    private boolean isSAMLReturned;

    private SAMLConfig config;

    @Factory(dataProvider = "samlConfigProvider")
    public SAMLInvalidIssuerTestCase(SAMLConfig config) {

        if (log.isDebugEnabled()){
            log.info("SAML SSO Test initialized for " + config);
        }
        this.config = config;
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(config.getUserMode());

        ConfigurationContext configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null
                        , null);
        applicationManagementServiceClient =
                new ApplicationManagementServiceClient(sessionCookie, backendURL, configContext);
        remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);

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

        super.createUser(config);
        createApplication(config, APPLICATION_NAME);
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {

        super.deleteUser(config);
        deleteApplication();

        applicationManagementServiceClient = null;
        remoteUSMServiceClient = null;
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

            Assert.assertTrue(isSAMLReturned,
                              "Sending SAML response to the samlsso_notification page failed");

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

    private void deleteApplication() throws Exception {

        applicationManagementServiceClient.deleteApplication(APPLICATION_NAME);
    }

    public void createApplication(SAMLConfig config, String appName) throws Exception {

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(appName);
        serviceProvider.setDescription("This is a test Service Provider");
        applicationManagementServiceClient.createApplication(serviceProvider);

        serviceProvider = applicationManagementServiceClient.getApplication(appName);

        serviceProvider.getClaimConfig().setClaimMappings(getClaimMappings());

        InboundAuthenticationRequestConfig requestConfig = new InboundAuthenticationRequestConfig();
        requestConfig.setInboundAuthType(INBOUND_AUTH_TYPE);
        requestConfig.setInboundAuthKey(ISSUER_NAME);

        Property attributeConsumerServiceIndexProp = new Property();
        attributeConsumerServiceIndexProp.setName(ATTRIBUTE_CS_INDEX_NAME);
        attributeConsumerServiceIndexProp.setValue(ATTRIBUTE_CS_INDEX_VALUE);
        requestConfig.setProperties(new Property[]{attributeConsumerServiceIndexProp});

        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        inboundAuthenticationConfig.setInboundAuthenticationRequestConfigs(
                new InboundAuthenticationRequestConfig[]{requestConfig});

        serviceProvider.setInboundAuthenticationConfig(inboundAuthenticationConfig);
        applicationManagementServiceClient.updateApplicationData(serviceProvider);
    }
}
