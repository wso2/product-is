/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.rest.api.user.session.v1;

import io.restassured.RestAssured;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.sso.saml.SAMLSSOConfigServiceClient;
import org.wso2.identity.integration.test.rest.api.user.common.RESTAPIUserTestBase;
import org.wso2.identity.integration.test.util.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class UserSessionTest extends RESTAPIUserTestBase {

    protected static final String API_VERSION = "v1";
    private static final String SESSION_MGT_ENDPOINT_URI = "/%s/sessions";
    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    private static final String API_DEFINITION_NAME = "session.yaml";
    private static final String ISSUER_TRAVELOCITY_COM = "travelocity.com";
    private static final String ISSUER_AVIS_COM = "avis.com";
    private static final String SAMPLE_APP_URL = "http://localhost:8490/%s";
    protected static final String SERVICE_PROVIDER_NAME_TRAVELOCITY = "travelocity-sessionAPITest";
    protected static final String SERVICE_PROVIDER_NAME_AVIS = "avis-sessionAPITest";
    private static final String SERVICE_PROVIDER_DESC = "Service Provider to test the session management APIs";
    private static final String ACS_URL = "http://localhost:8490/%s/home.jsp";
    private static final String API_PACKAGE_NAME = "org.wso2.carbon.identity.rest.api.user.session.v1";
    private static final String COMMONAUTH = "commonauth";
    private static final String SAMLSSO = "samlsso";
    private static final String APP_HOMEPAGE = "/home.jsp";
    private static final String LOGIN_PAGE = "/carbon/admin/login.jsp";
    private static final String BASIC_AUTH_REQUEST_PATH_AUTHENTICATOR = "BasicAuthRequestPathAuthenticator";
    private static final String SAML2_BINDING = "?SAML2.HTTPBinding=HTTP-POST";
    private static final String SAML_REQUEST = "SAMLRequest";
    private static final String SEC_TOKEN = "sectoken";
    private static final String PATH_SEPARATOR = "/";
    protected static String swaggerDefinition;

    static {
        try {
            swaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (IOException e) {
            Assert.fail(String.format("Unable to read the swagger definition %s from %s", API_DEFINITION_NAME,
                    API_PACKAGE_NAME), e);
        }
    }

    protected String sessionsEndpointURI;
    protected String sessionEndpointURI;
    protected String adminUsername;
    protected String adminPassword;
    protected String session_test_user1;
    protected String session_test_user2;
    protected String TEST_USER_PASSWORD = "passWord1@";
    protected CloseableHttpClient client;
    private String ADMIN_ROLE = "admin";
    private String DEFAULT_PROFILE = "default";
    protected UserManagementClient userMgtClient;
    private String isURL;
    protected ApplicationManagementServiceClient appMgtclient;
    private SAMLSSOConfigServiceClient ssoConfigServiceClient;
    protected ServiceProvider serviceProviderTravelocity;
    protected ServiceProvider serviceProviderAvis;

    void initUrls(String pathParam) {

        this.sessionsEndpointURI = String.format(SESSION_MGT_ENDPOINT_URI, pathParam);
        this.sessionEndpointURI = this.sessionsEndpointURI + "/%s";
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    @BeforeClass(alwaysRun = true)
    public void testInitData() throws Exception {

        appMgtclient = new ApplicationManagementServiceClient(sessionCookie, backendURL, null);
        userMgtClient = new UserManagementClient(backendURL, sessionCookie);
        ssoConfigServiceClient = new SAMLSSOConfigServiceClient(backendURL, sessionCookie);

        isURL = backendURL.substring(0, backendURL.indexOf("services/"));

        serviceProviderTravelocity = createServiceProvider(ISSUER_TRAVELOCITY_COM, SERVICE_PROVIDER_NAME_TRAVELOCITY);
        serviceProviderAvis = createServiceProvider(ISSUER_AVIS_COM, SERVICE_PROVIDER_NAME_AVIS);

        createUsersForTesting();
        createSessionsForTesting();
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        super.conclude();
    }

    private SAMLSSOServiceProviderDTO createSAMLServiceProviderDTO(String issuer) {

        SAMLSSOServiceProviderDTO samlssoServiceProviderDTO = new SAMLSSOServiceProviderDTO();
        samlssoServiceProviderDTO.setIssuer(issuer);
        samlssoServiceProviderDTO.setAssertionConsumerUrls(new String[]
                {String.format(SAMPLE_APP_URL, issuer) + APP_HOMEPAGE});
        samlssoServiceProviderDTO.setDefaultAssertionConsumerUrl(String.format(SAMPLE_APP_URL, issuer) + APP_HOMEPAGE);
        samlssoServiceProviderDTO.setAttributeConsumingServiceIndex("1239245949");
        samlssoServiceProviderDTO.setNameIDFormat("urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress");
        samlssoServiceProviderDTO.setDoSignAssertions(true);
        samlssoServiceProviderDTO.setDoSignResponse(true);
        samlssoServiceProviderDTO.setDoSingleLogout(true);
        samlssoServiceProviderDTO.setLoginPageURL(LOGIN_PAGE);
        return samlssoServiceProviderDTO;
    }

    private void createUsersForTesting() throws RemoteException, UserAdminUserAdminException {

        userMgtClient.addUser(session_test_user1, TEST_USER_PASSWORD, new String[]{ADMIN_ROLE}, DEFAULT_PROFILE);
        userMgtClient.addUser(session_test_user2, TEST_USER_PASSWORD, null, DEFAULT_PROFILE);
    }

    private void createSessionsForTesting() throws Exception {

        client = HttpClientBuilder.create().build();
        loginUserToApplication(client, ISSUER_TRAVELOCITY_COM, session_test_user1, TEST_USER_PASSWORD);
        loginUserToApplication(client, ISSUER_AVIS_COM, session_test_user1, TEST_USER_PASSWORD);
        client.close();

        client = HttpClientBuilder.create().build();
        loginUserToApplication(client, ISSUER_TRAVELOCITY_COM, session_test_user1, TEST_USER_PASSWORD);
        client.close();

        client = HttpClientBuilder.create().build();
        loginUserToApplication(client, ISSUER_AVIS_COM, session_test_user2, TEST_USER_PASSWORD);
        client.close();
    }

    private void loginUserToApplication(CloseableHttpClient client, String application, String username,
                                        String password) throws Exception {

        HttpPost request = new HttpPost(String.format(SAMPLE_APP_URL, application) + PATH_SEPARATOR + SAMLSSO +
                SAML2_BINDING);
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("username", username + "@" + tenant));
        urlParameters.add(new BasicNameValuePair("password", password));
        request.setEntity(new UrlEncodedFormEntity(urlParameters));
        HttpResponse response = client.execute(request);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line;
        String samlRequest = "";
        String secToken = "";

        while ((line = rd.readLine()) != null) {
            if (line.contains("name='" + SAML_REQUEST + "'")) {
                String[] tokens = line.split("'");
                samlRequest = tokens[5];
            }
            if (line.contains("name='" + SEC_TOKEN + "'")) {
                String[] tokens = line.split("'");
                secToken = tokens[5];
            }
        }
        EntityUtils.consume(response.getEntity());
        request = new HttpPost(isURL + PATH_SEPARATOR + SAMLSSO);
        urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(SEC_TOKEN, secToken));
        urlParameters.add(new BasicNameValuePair(SAML_REQUEST, samlRequest));
        request.setEntity(new UrlEncodedFormEntity(urlParameters));
        response = client.execute(request);
        EntityUtils.consume(response.getEntity());

        if (Utils.requestMissingClaims(response)) {
            String pastrCookie = Utils.getPastreCookie(response);
            response = Utils.sendPOSTConsentMessage(response, isURL + PATH_SEPARATOR + COMMONAUTH,
                    USER_AGENT, String.format(ACS_URL, application), client, pastrCookie);
            EntityUtils.consume(response.getEntity());
            response = Utils.sendRedirectRequest(response, USER_AGENT, String.format(ACS_URL, application), "", client);
            EntityUtils.consume(response.getEntity());
        }
    }

    private ServiceProvider createServiceProvider(String issuer, String serviceProviderName) throws Exception {

        ssoConfigServiceClient.addServiceProvider(createSAMLServiceProviderDTO(issuer));
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(serviceProviderName);
        serviceProvider.setDescription(SERVICE_PROVIDER_DESC);
        appMgtclient.createApplication(serviceProvider);
        serviceProvider = appMgtclient.getApplication(serviceProviderName);

        InboundAuthenticationRequestConfig requestConfig = new InboundAuthenticationRequestConfig();
        requestConfig.setInboundAuthKey(issuer);
        requestConfig.setInboundAuthType(SAMLSSO);

        Property attributeConsumerServiceIndexProp = new Property();
        attributeConsumerServiceIndexProp.setName("attrConsumServiceIndex");
        attributeConsumerServiceIndexProp.setValue("1239245949");
        requestConfig.setProperties(new Property[]{attributeConsumerServiceIndexProp});

        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        inboundAuthenticationConfig
                .setInboundAuthenticationRequestConfigs(new InboundAuthenticationRequestConfig[]{requestConfig});

        serviceProvider.setInboundAuthenticationConfig(inboundAuthenticationConfig);
        RequestPathAuthenticatorConfig requestPathAuthenticatorConfig = new RequestPathAuthenticatorConfig();
        requestPathAuthenticatorConfig.setName(BASIC_AUTH_REQUEST_PATH_AUTHENTICATOR);

        serviceProvider.setRequestPathAuthenticatorConfigs(
                new RequestPathAuthenticatorConfig[]{requestPathAuthenticatorConfig});
        appMgtclient.updateApplicationData(serviceProvider);
        return appMgtclient.getApplication(serviceProviderName);
    }
}
