/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.idp.mgt;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.client.handlers.BasicAuthSecurityHandler;
import org.eclipse.jetty.util.ajax.JSON;
import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.IOException;
import javax.ws.rs.core.MediaType;

public class IDPMgtRestTestCase extends ISIntegrationTest {

    public static final String IDP_ENDPOINT_SUFFIX = "/api/identity/idp-mgt/v0.8/idps/";
    private String isServerBackendUrl;
    private String idpEndpoint;
    RestClient restClient;
    private String sampleCertificate;
    private IdentityProviderMgtServiceClient idpMgtServiceClient;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        isServerBackendUrl = isServer.getContextUrls().getWebAppURLHttps();
        idpEndpoint = isServerBackendUrl + IDP_ENDPOINT_SUFFIX;
        ClientConfig clientConfig = new ClientConfig();
        BasicAuthSecurityHandler basicAuth = new BasicAuthSecurityHandler();
        basicAuth.setUserName(userInfo.getUserName());
        basicAuth.setPassword(userInfo.getPassword());
        clientConfig.handlers(basicAuth);

        restClient = new RestClient(clientConfig);
        ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        idpMgtServiceClient = new IdentityProviderMgtServiceClient(sessionCookie, backendURL, configContext);
        IdentityProvider idProvider = idpMgtServiceClient.getResidentIdP();
        Assert.assertNotNull(idProvider, "Resident identity provider retrieval failed");
        sampleCertificate = idProvider.getCertificate();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() {

    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Add Identity Provider")
    public void testAddIDP() {

        String name = "idp-facebook";
        String description = "facbook identity provider";
        addIDP(name, description);
    }

    @Test(dependsOnMethods = "testAddIDP", groups = "wso2.is", description = "Get outbound connector by name for a " +
            "given Identity Providers")
    public void testAddOutBoundConnectorIDPs() {

        addOutboundConnector();
        JSONObject result = getOutboundConnector("testName");
        Assert.assertNotNull(result);
    }

    @Test(dependsOnMethods = "testAddIDP", groups = "wso2.is", description = "Get authenticator by name for a " +
            "given Identity Providers")
    public void testAuthenticatorIDPs() {

        JSONObject result = getAuthenticator("OpenIDAuthenticator");
        Assert.assertNotNull(result);
    }

    private JSONObject getAuthenticator(String name) {

        Resource idpResource = restClient.resource(idpEndpoint +
                "idp-facebook/authenticators/?authName="+name);
        String response = idpResource.contentType(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON)
                .get(String.class);
        response = response.substring(1, response.length() - 1);
        return (JSONObject) JSONValue.parse(response);
    }

    private JSONObject getOutboundConnector(String name) {

        Resource idpResource = restClient.resource(idpEndpoint +
                "idp-facebook/outbound-provisioning-connector-configs/?connectorName="+name);
        String response = idpResource.contentType(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON)
                .get(String.class);
        response = response.substring(1, response.length() - 1);
        return (JSONObject) JSONValue.parse(response);
    }

    private JSONObject addOutboundConnector() {

        Resource idpResource = restClient.resource(idpEndpoint +
                "idp-facebook/outbound-provisioning-connector-configs");

        String addConnectorString = "{ \"blocking\": " + "\""+ false + "\"" +
                ", \"enabled\": "+ "\""+ false + "\""+
                ", \"name\": " + "\"testName\"" +
                ", \"provisioningProperties\": [ { \"advanced\": "+ "\""+ false + "\""+
                ", \"confidential\": "+ "\""+ false + "\""+
                ", \"defaultValue\": \"testName\", \"description\": \"testName\", \"displayName\": \"testName\"" +
                ", \"displayOrder\": "+ "\""+ 1 + "\""+
                ", \"name\": \"testName\", \"required\": "+ "\""+ false + "\""+
                ", \"type\": \"type\"" +
                ", \"value\": \"value\" } ], \"rulesEnabled\": "+ "\""+ false + "\""+
                ", \"valid\": false}";

        String response = idpResource.contentType(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON)
                .post(String.class, addConnectorString);

        return (JSONObject) JSONValue.parse(response);
    }

    private JSONObject addIDP(String name, String description) {

        Resource idpResource = restClient.resource(idpEndpoint);

        String testIdpRealmId = "localhost";
        String testFedAuthDispName = "openid";

        String testFedAuthPropName = "OpenIdUrl";
        String testFedAuthPropValue = "https://testDomain:9853/openid";
        String testFedAuthPropName2 = "IsUserIdInClaims";
        String testFedAuthPropValue2 = "false";
        String testFedAuthPropName3 = "RealmId";
        String testFedAuthPropValue3 = "localhost";

        String testProvisionConfName = "scim";

        String testProvisionPropName = "scim-user-ep";
        String testProvisionPropDisplayName = "userEndPoint";
        String testProvisionPropValue = "https://localhost:9853/testProvisionLink";

        String testProvisionPropName2 = "scim-username";
        String testProvisionPropDisplayName2 = "userName";
        String testProvisionPropValue2 = "admin";

        String testProvisionPropName3 = "scim-password";
        String testProvisionPropDisplayName3 = "userPassword";
        String testProvisionPropValue3 = "admin";
        String testFedAuthName = "OpenIDAuthenticator";

        String addIDPString = "{\"homeRealmId\": " + "\"" + testIdpRealmId + "\"" +
                ", \"enable\": " + "\"" + true + "\" " +
                ", \"identityProviderDescription\": \"" + description + "\"" +
                ", \"identityProviderName\": \"" + name + "\"" +
                ", \"certificate\": \"" + sampleCertificate + "\"" +
                ", \"federationHub\": \"" + false + "\"" +
                ", \"primary\": \"" + false + "\"" +
                ", \"federatedAuthenticatorConfigs\": [{ \"displayName\": " + "\"" + testFedAuthDispName + "\"" +
                ", \"enabled\": " + "\""+ true + "\"" +
                ", \"name\": " + "\""+ testFedAuthName + "\"" +
                ", \"propertyList\": [{\"name\": " + "\""+ testFedAuthPropName + "\"" +
                ", \"value\": " + "\""+ testFedAuthPropValue + "\"}, {\"name\": " + "\""+ testFedAuthPropName2 + "\"" +
                ", \"value\": " + "\""+ testFedAuthPropValue2 + "\"}, {\"name\": " + "\""+ testFedAuthPropName3 + "\"" +
                ", \"value\": " + "\""+ testFedAuthPropValue3 + "\"}]}]" +
                ", \"justInTimeProvisioningConfig\": {\"promptConsent\": \"" + false + "\"}" +
                ", \"provisioningConnectorConfigs\": [{\"name\": " + "\""+ testProvisionConfName + "\"" +
                ", \"valid\": " + "\""+ false + "\"" +
                ", \"blocking\": " + "\""+ false + "\"" +
                ", \"enabled\": " + "\""+ true + "\"" +
                ", \"provisioningProperties\": [{\"name\": " + "\""+ testProvisionPropName + "\"" +
                ", \"displayName\": " + "\""+ testProvisionPropDisplayName + "\"" +
                ", \"value\": " + "\""+ testProvisionPropValue + "\"}, {\"name\": " + "\""+ testProvisionPropName2 + "\"" +
                ", \"displayName\": " + "\""+ testProvisionPropDisplayName2 + "\"" +
                ", \"value\": " + "\""+ testProvisionPropValue2 + "\"}, {\"name\": " + "\""+ testProvisionPropName3 + "\"" +
                ", \"displayName\": " + "\""+ testProvisionPropDisplayName3 + "\"" +
                ", \"value\": " + "\""+ testProvisionPropValue3 + "\"}]}]}";

        String response = idpResource.contentType(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON)
                .post(String.class, addIDPString);

        return (JSONObject) JSONValue.parse(response);
    }
}
