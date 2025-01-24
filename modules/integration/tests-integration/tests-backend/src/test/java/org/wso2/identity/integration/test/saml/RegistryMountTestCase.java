/*
 * Copyright (c) 2016, WSO2 LLC. (http://www.wso2.com).
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

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimConfiguration.DialectEnum;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimMappings;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.RequestedClaimConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAML2Configuration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAML2ServiceProvider;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAMLAssertionConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAMLAttributeProfile;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAMLResponseSigning;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SingleLogoutProfile;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SingleSignOnProfile;
import org.wso2.identity.integration.test.rest.api.server.tenant.management.v1.model.Owner;
import org.wso2.identity.integration.test.rest.api.server.tenant.management.v1.model.TenantModel;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.TenantMgtRestClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.UserUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RegistryMountTestCase extends ISIntegrationTest {

    // SAML Application attributes
    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    private static final String APPLICATION_NAME = "SAML-Registry-Mount-Application";
    private static final String ATTRIBUTE_CS_INDEX_VALUE = "1239245949";

    private static final String ACS_URL = "http://localhost:8490/%s/home.jsp";
    private static final String COMMON_AUTH_URL = "https://localhost:9853/commonauth";
    private static final String SAML_SSO_LOGIN_URL = "http://localhost:8490/%s/samlsso?SAML2.HTTPBinding=%s";

    private static final String NAMEID_FORMAT = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
    //Claim Uris
    private static final String firstNameClaimURI = "http://wso2.org/claims/givenname";
    private static final String lastNameClaimURI = "http://wso2.org/claims/lastname";
    private static final String emailClaimURI = "http://wso2.org/claims/emailaddress";

    private static final String TENANT_DOMAIN = "registrymount.com";
    private static final String TENANT_ADMIN_USERNAME = "admin@registrymount.com";
    private static final String TENANT_ADMIN_PASSWORD = "Admin_123";
    private static final String TENANT_ADMIN_TENANT_AWARE_USERNAME = "admin";
    private ServerConfigurationManager serverConfigurationManager;
    private final String artifact = "travelocity.com-registrymount";

    private Lookup<CookieSpecProvider> cookieSpecRegistry;
    private RequestConfig requestConfig;
    private CloseableHttpClient httpClient;

    private String resultPage;
    private String userId;

    private TenantMgtRestClient tenantMgtRestClient;
    private OAuth2RestClient applicationMgtRestClient;
    private String appId;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        serverConfigurationManager = new ServerConfigurationManager(isServer);

        tenantMgtRestClient = new TenantMgtRestClient(serverURL, tenantInfo);
        addRegistryMountTenant();

        cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .build();

        applicationMgtRestClient = new OAuth2RestClient(serverURL, buildRegistryMountTenantInfo());
        createApplication();

        userId = UserUtil.getUserId(MultitenantUtils.getTenantAwareUsername(TENANT_ADMIN_USERNAME),
                TENANT_DOMAIN, TENANT_ADMIN_USERNAME, TENANT_ADMIN_PASSWORD);
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception{

        deleteApplication();
        serverConfigurationManager.restoreToLastConfiguration(false);
        tenantMgtRestClient.closeHttpClient();
        applicationMgtRestClient.closeHttpClient();
        httpClient.close();
        httpClient = null;
    }

    @Test(alwaysRun = true, description = "Testing SAML SSO login", groups = "wso2.is")
    public void testSAMLSSOLogin() {

        try {
            HttpResponse response;

            response = sendGetRequest(
                    String.format(SAML_SSO_LOGIN_URL, artifact, "HTTP-Redirect"));

            String sessionKey = extractDataFromResponse(response, "name=\"sessionDataKey\"", 1);
            response = sendPOSTMessage(sessionKey);
            if (Utils.requestMissingClaims(response)) {

                String pastrCookie = Utils.getPastreCookie(response);
                Assert.assertNotNull(pastrCookie, "pastr cookie not found in response.");
                EntityUtils.consume(response.getEntity());

                response = Utils.sendPOSTConsentMessage(response, COMMON_AUTH_URL, USER_AGENT,
                        Utils.getRedirectUrl(response), httpClient, pastrCookie);
            }
            EntityUtils.consume(response.getEntity());

            response = sendRedirectRequest(response);
            String samlResponse = extractDataFromResponse(response, "SAMLResponse", 5);

            response = sendSAMLMessage(String.format(ACS_URL, artifact), "SAMLResponse",
                    samlResponse);
            resultPage = extractDataFromResponse(response);

            Assert.assertTrue(resultPage.contains("You are logged in as " + userId),
                    "SAML SSO Login failed for " + artifact);
        } catch (Exception e) {
            Assert.fail("SAML SSO Login test failed for " + artifact, e);
        }
    }

    private String extractDataFromResponse(HttpResponse response, String key, int token)
            throws IOException {

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));
        String line;
        String value = "";

        while ((line = rd.readLine()) != null) {
            if (line.contains(key)) {
                String[] tokens = line.split("'");
                value = tokens[token];
            }
        }
        rd.close();
        return value;
    }

    private HttpResponse sendPOSTMessage(String sessionKey) throws Exception {

        HttpPost post = new HttpPost(COMMON_AUTH_URL);
        post.setHeader("User-Agent", USER_AGENT);
        post.addHeader("Referer", String.format(ACS_URL, artifact));
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("username", TENANT_ADMIN_USERNAME));
        urlParameters.add(new BasicNameValuePair("password", TENANT_ADMIN_PASSWORD));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionKey));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(post);
    }

    private HttpResponse sendGetRequest(String url) throws Exception {

        HttpGet request = new HttpGet(url);
        request.addHeader("User-Agent", USER_AGENT);
        return httpClient.execute(request);
    }

    private HttpResponse sendSAMLMessage(String url, String samlMsgKey, String samlMsgValue) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        HttpPost post = new HttpPost(getTenantQualifiedURL(url, tenantInfo.getDomain()));
        post.setHeader("User-Agent", USER_AGENT);
        urlParameters.add(new BasicNameValuePair(samlMsgKey, samlMsgValue));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(post);
    }

    private HttpResponse sendRedirectRequest(HttpResponse response) throws IOException {

        Header[] headers = response.getAllHeaders();
        String url = "";
        for (Header header : headers) {
            if ("Location".equals(header.getName())) {
                url = header.getValue();
            }
        }

        HttpGet request = new HttpGet(url);
        request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("Referer", String.format(ACS_URL, artifact));
        return httpClient.execute(request);
    }

    private String extractDataFromResponse(HttpResponse response) throws IOException {

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }

    private void addRegistryMountTenant() throws Exception {

        Owner tenantAdminUser = new Owner();
        tenantAdminUser.setUsername(TENANT_ADMIN_TENANT_AWARE_USERNAME);
        tenantAdminUser.setPassword(TENANT_ADMIN_PASSWORD);
        tenantAdminUser.setEmail(TENANT_ADMIN_USERNAME);
        tenantAdminUser.setFirstname("Registry");
        tenantAdminUser.setLastname("Mount");
        tenantAdminUser.setProvisioningMethod("inline-password");

        TenantModel tenantReqModel = new TenantModel();
        tenantReqModel.setDomain(TENANT_DOMAIN);
        tenantReqModel.addOwnersItem(tenantAdminUser);

        tenantMgtRestClient.addTenant(tenantReqModel);
    }

    private Tenant buildRegistryMountTenantInfo() {

        User registryMountTenantAdmin = new User();
        registryMountTenantAdmin.setUserName(TENANT_ADMIN_USERNAME);
        registryMountTenantAdmin.setPassword(TENANT_ADMIN_PASSWORD);
        Tenant registryMountTenant =  new Tenant();
        registryMountTenant.setContextUser(registryMountTenantAdmin);
        registryMountTenant.setDomain(TENANT_DOMAIN);

        return registryMountTenant;
    }

    private void createApplication() throws Exception {

        ApplicationModel applicationCreationModel = new ApplicationModel()
                .name(APPLICATION_NAME)
                .description("This is a test Service Provider")
                .inboundProtocolConfiguration(new InboundProtocols()
                        .saml(getSAMLConfigurations()))
                .claimConfiguration(getClaimConfiguration());

        try {
            appId = applicationMgtRestClient.createApplication(applicationCreationModel);
        } catch (RuntimeException e) {
            log.error("Error while creating the application", e);
            throw new Exception("Error while creating the application", e);
        }
    }

    private void deleteApplication() throws Exception{

        applicationMgtRestClient.deleteApplication(appId);
    }

    private SAML2Configuration getSAMLConfigurations() {

        SAML2ServiceProvider serviceProvider = new SAML2ServiceProvider()
                .issuer(artifact)
                .addAssertionConsumerUrl(String.format(ACS_URL, artifact))
                .defaultAssertionConsumerUrl(String.format(ACS_URL, artifact))
                .attributeProfile(new SAMLAttributeProfile()
                        .enabled(false))
                .singleLogoutProfile(new SingleLogoutProfile()
                        .enabled(true))
                .responseSigning(new SAMLResponseSigning()
                        .enabled(false))
                .singleSignOnProfile(new SingleSignOnProfile()
                        .attributeConsumingServiceIndex(ATTRIBUTE_CS_INDEX_VALUE)
                        .assertion(new SAMLAssertionConfiguration().nameIdFormat(NAMEID_FORMAT)));

        return new SAML2Configuration().manualConfiguration(serviceProvider);
    }

    private ClaimConfiguration getClaimConfiguration() {

        return new ClaimConfiguration()
                .dialect(DialectEnum.LOCAL)
                .addClaimMappingsItem(new ClaimMappings()
                        .applicationClaim(emailClaimURI)
                        .localClaim(new Claim().uri(emailClaimURI)))
                .addClaimMappingsItem(new ClaimMappings()
                        .applicationClaim(firstNameClaimURI)
                        .localClaim(new Claim().uri(firstNameClaimURI)))
                .addClaimMappingsItem(new ClaimMappings()
                        .applicationClaim(lastNameClaimURI)
                        .localClaim(new Claim().uri(lastNameClaimURI)))
                .addRequestedClaimsItem(new RequestedClaimConfiguration()
                        .claim(new Claim().uri(emailClaimURI)))
                .addRequestedClaimsItem(new RequestedClaimConfiguration()
                        .claim(new Claim().uri(firstNameClaimURI)))
                .addRequestedClaimsItem(new RequestedClaimConfiguration()
                        .claim(new Claim().uri(lastNameClaimURI)));
    }
}
