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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.identity.sso.saml.stub.IdentitySAMLSSOConfigServiceIdentityException;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.FederatedAuthenticatorRequest;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.FederatedAuthenticatorRequest.FederatedAuthenticator;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.IdentityProviderPOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Property;
import org.wso2.identity.integration.test.restclients.IdpMgtRestClient;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;

/**
 * This tests adds an Identity Provider using metadata file, checks for the validity of properties
 */
public class IDPMetadataTestCase extends ISIntegrationTest {

    private static final String SPENTITYID = "spEntityId";
    private static final String ENTITYID = "loadbalancer1-3.example.com";
    private static final String SSOURL = "https://LoadBalancer-3.example.com:9443/amserver/SSORedirect/metaAlias/idp";
    private static final String IS_AUTHN_REQ_SIGNED = "false";
    private static final String IS_LOGOUT_ENABLED = "true";
    private static final String LOGOUT_REQ_URL = "https://IdentityProvider.com/SAML/SLO/SOAP";
    private static final String IS_ENABLE_ASSERTION_ENCRYPTION = "true";
    private static final String IS_ENABLE_ASSERTION_SIGNING = "true";
    private static final String SAML_SSO_CONFIG_NAME = "SAMLSSOAuthenticator";
    private static final String ENCODED_SAML_SSO_CONFIG_NAME_ID = "U0FNTFNTT0F1dGhlbnRpY2F0b3I";
    private IdpMgtRestClient identityProviderMgtRestClient;
    private String idpId;


    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        identityProviderMgtRestClient = new IdpMgtRestClient(serverURL, tenantInfo);
    }

    @Test
    public void addIDPMetadata() throws Exception {

        String filePath = FrameworkPathUtil.getSystemResourceLocation() +
                "artifacts" + File.separator + "IS" + File.separator + "saml" + File.separator
                + "idp-metadata.xml";

        byte[] encoded = Files.readAllBytes(Paths.get(filePath));
        String metadataXml = new String(encoded, StandardCharsets.UTF_8);

        IdentityProviderPOSTRequest idpPostRequest = new IdentityProviderPOSTRequest()
                .name(ENTITYID)
                .federatedAuthenticators(buildSAMLAuthenticationConfiguration(metadataXml));

        idpId = identityProviderMgtRestClient.createIdentityProvider(idpPostRequest);
        JSONObject samlFedAuthenticatorConfig = identityProviderMgtRestClient.getIdpFederatedAuthenticator(idpId,
                        ENCODED_SAML_SSO_CONFIG_NAME_ID);

        Assert.assertNotNull(idpId);
        Assert.assertNotNull(samlFedAuthenticatorConfig);

        JSONArray properties = (JSONArray) samlFedAuthenticatorConfig.get("properties");

        for (Object property : properties) {
            String key = ((JSONObject) property).get("key").toString();

            if (IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID.equals(key)) {
                Assert.assertEquals(((JSONObject) property).get("value").toString(), ENTITYID);
            }

            if (IdentityApplicationConstants.Authenticator.SAML2SSO.SP_ENTITY_ID.equals(key)) {
                Assert.assertEquals(((JSONObject) property).get("value").toString(), SPENTITYID);
            }

            if (IdentityApplicationConstants.Authenticator.SAML2SSO.SSO_URL.equals(key)) {
                Assert.assertEquals(((JSONObject) property).get("value").toString(), SSOURL);
            }

            if (IdentityApplicationConstants.Authenticator.SAML2SSO.IS_AUTHN_REQ_SIGNED.equals(key)) {
                Assert.assertEquals(((JSONObject) property).get("value").toString(), IS_AUTHN_REQ_SIGNED);
            }

            if (IdentityApplicationConstants.Authenticator.SAML2SSO.IS_LOGOUT_ENABLED.equals(key)) {
                Assert.assertEquals(((JSONObject) property).get("value").toString(), IS_LOGOUT_ENABLED);
            }

            if (IdentityApplicationConstants.Authenticator.SAML2SSO.LOGOUT_REQ_URL.equals(key)) {
                Assert.assertEquals(((JSONObject) property).get("value").toString(), LOGOUT_REQ_URL);
            }

            if (IdentityApplicationConstants.Authenticator.SAML2SSO.IS_ENABLE_ASSERTION_ENCRYPTION.equals(key)) {
                Assert.assertEquals(((JSONObject) property).get("value").toString(), IS_ENABLE_ASSERTION_ENCRYPTION);
            }

            if (IdentityApplicationConstants.Authenticator.SAML2SSO.IS_ENABLE_ASSERTION_SIGNING.equals(key)) {
                Assert.assertEquals(((JSONObject) property).get("value").toString(), IS_ENABLE_ASSERTION_SIGNING);
            }
        }
    }

    @AfterClass(alwaysRun = true)
    public void endTest() throws RemoteException, IdentitySAMLSSOConfigServiceIdentityException {
        try {
            identityProviderMgtRestClient.deleteIdp(idpId);
        } catch (Exception ex) {
            throw new RemoteException("Error Deleting Identity Provider", ex);
        }
    }

    private static FederatedAuthenticatorRequest buildSAMLAuthenticationConfiguration(String metadataXML) {

        FederatedAuthenticator authenticator = new FederatedAuthenticator()
                .authenticatorId(ENCODED_SAML_SSO_CONFIG_NAME_ID)
                .name(SAML_SSO_CONFIG_NAME)
                .isEnabled(true)
                .addProperty(new Property()
                        .key(IdentityApplicationConstants.Authenticator.SAML2SSO.SP_ENTITY_ID)
                        .value("spEntityId"))
                .addProperty(new Property()
                        .key("meta_data_saml")
                        .value(metadataXML));

        return new FederatedAuthenticatorRequest()
                .defaultAuthenticatorId(ENCODED_SAML_SSO_CONFIG_NAME_ID)
                .addAuthenticator(authenticator);
    }
}
