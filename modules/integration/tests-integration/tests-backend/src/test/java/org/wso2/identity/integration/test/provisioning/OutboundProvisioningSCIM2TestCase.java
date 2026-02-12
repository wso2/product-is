/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.provisioning;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.base.TestDataHolder;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.IdentityProviderPOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Property;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.ProvisioningRequest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationPatchModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OutboundProvisioningConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ProvisioningConfiguration;
import org.wso2.identity.integration.test.restclients.IdpMgtRestClient;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.utils.CommonConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

public class OutboundProvisioningSCIM2TestCase extends ISIntegrationTest {

    private static final String IDP_NAME = "outbound-provisioning-connection";
    private static final String IDP_DESCRIPTION = "SCIM outbound provisioning connection";
    private static final String IDP_IMAGE = "assets/images/logos/outbound-provisioning.svg";
    private static final String SCIM2_CONNECTOR_ID = "U0NJTTI";
    private static final String SCIM2_CONNECTOR_NAME = "SCIM2";
    private static final String RESIDENT_APP_ID = "resident";

    private static final int DEFAULT_PORT = CommonConstants.IS_DEFAULT_HTTPS_PORT;
    private static final int PORT_OFFSET_1 = 1;
    private static final String HTTPS_LOCALHOST_SERVICES = "https://localhost:%s";
    private static final String SCIM2_USER_ENDPOINT_PATH = "/scim2/Users";
    private static final String SCIM2_GROUP_ENDPOINT_PATH = "/scim2/Groups";

    private IdpMgtRestClient idpMgtRestClient;
    private OAuth2RestClient oAuth2RestClient;
    private TestDataHolder testDataHolder;
    private String idpId;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();

        testDataHolder = TestDataHolder.getInstance();

        // Initialize IdP management REST client for server 0 (primary IS instance)
        idpMgtRestClient = new IdpMgtRestClient(serverURL, tenantInfo);
        oAuth2RestClient = new OAuth2RestClient(serverURL, tenantInfo);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        // Remove outbound provisioning from resident application before deleting the IdP.
        clearResidentAppOutboundProvisioning();

        if (idpId != null) {
            idpMgtRestClient.deleteIdp(idpId);
        }
        if (idpMgtRestClient != null) {
            idpMgtRestClient.closeHttpClient();
        }
        if (oAuth2RestClient != null) {
            oAuth2RestClient.closeHttpClient();
        }
    }

    @Test(alwaysRun = true, description = "Verify outbound provisioning IdP was created correctly")
    public void testVerifyIdPCreation() throws Exception {

        // Create the outbound provisioning IdP connection
        idpId = createOutboundProvisioningIdP();
        Assert.assertNotNull(idpId, "Identity Provider creation failed - returned null ID");

        JSONObject idpResponse = idpMgtRestClient.getIdentityProvider(idpId);
        Assert.assertNotNull(idpResponse, "Failed to retrieve created Identity Provider");

        // Verify basic IdP properties
        Assert.assertEquals(idpResponse.get("name"), IDP_NAME,
                "IdP name mismatch");
        Assert.assertEquals(idpResponse.get("description"), IDP_DESCRIPTION,
                "IdP description mismatch");
        Assert.assertEquals(idpResponse.get("image"), IDP_IMAGE,
                "IdP image mismatch");
        Assert.assertEquals(idpResponse.get("isPrimary"), false,
                "IdP isPrimary should be false");
        Assert.assertEquals(idpResponse.get("isFederationHub"), false,
                "IdP isFederationHub should be false");

        // Verify provisioning configuration
        JSONObject provisioning = (JSONObject) idpResponse.get("provisioning");
        Assert.assertNotNull(provisioning, "Provisioning configuration not found");

        JSONObject outboundConnectors = (JSONObject) provisioning.get("outboundConnectors");
        Assert.assertNotNull(outboundConnectors, "Outbound connectors not found");

        Assert.assertEquals(outboundConnectors.get("defaultConnectorId"), SCIM2_CONNECTOR_ID,
                "Default connector ID mismatch");

        JSONArray connectors = (JSONArray) outboundConnectors.get("connectors");
        Assert.assertNotNull(connectors, "Connectors array not found");
        Assert.assertEquals(connectors.size(), 1, "Expected exactly 1 connector");

        JSONObject connectorSummary = (JSONObject) connectors.get(0);
        Assert.assertEquals(connectorSummary.get("connectorId"), SCIM2_CONNECTOR_ID,
                "SCIM2 connector ID mismatch");
        Assert.assertEquals(connectorSummary.get("name"), SCIM2_CONNECTOR_NAME,
                "SCIM2 connector name mismatch");
        Assert.assertEquals(connectorSummary.get("isEnabled"), true,
                "SCIM2 connector should be enabled");

        // Fetch full connector details (properties require a separate API call)
        JSONObject scim2Connector = idpMgtRestClient.getIdpOutboundConnector(idpId, SCIM2_CONNECTOR_ID);
        Assert.assertNotNull(scim2Connector, "Failed to retrieve SCIM2 outbound connector details");

        Assert.assertEquals(scim2Connector.get("connectorId"), SCIM2_CONNECTOR_ID,
                "Connector detail - connectorId mismatch");
        Assert.assertEquals(scim2Connector.get("isEnabled"), true,
                "Connector detail - should be enabled");

        // Verify SCIM2 connector properties (skip secrets like password as they may be masked)
        JSONArray properties = (JSONArray) scim2Connector.get("properties");
        Assert.assertNotNull(properties, "SCIM2 connector properties not found");

        Map<String, String> propertyMap = new HashMap<>();
        for (Object prop : properties) {
            JSONObject property = (JSONObject) prop;
            propertyMap.put((String) property.get("key"), (String) property.get("value"));
        }

        Assert.assertEquals(propertyMap.get("scim2-user-ep"), getTargetScim2UserEndpoint(),
                "User endpoint mismatch");
        Assert.assertEquals(propertyMap.get("scim2-group-ep"), getTargetScim2GroupEndpoint(),
                "Group endpoint mismatch");
        Assert.assertEquals(propertyMap.get("scim2-user-store-domain"), "PRIMARY",
                "User store domain mismatch");
        Assert.assertEquals(propertyMap.get("scim2-enable-pwd-provisioning"), "true",
                "Password provisioning mismatch");
        Assert.assertEquals(propertyMap.get("scim2-authentication-mode"), "basic",
                "Authentication mode mismatch");
    }

    @Test(alwaysRun = true, dependsOnMethods = "testVerifyIdPCreation",
            description = "Enable outbound provisioning on the resident application")
    public void testEnableOutboundProvisioningOnResidentApp() throws Exception {

        enableResidentAppOutboundProvisioning();
    }

    private void enableResidentAppOutboundProvisioning() throws Exception {

        OutboundProvisioningConfiguration outboundConfig = new OutboundProvisioningConfiguration();
        outboundConfig.setIdp(IDP_NAME);
        outboundConfig.setConnector(SCIM2_CONNECTOR_NAME);
        outboundConfig.setBlocking(false);
        outboundConfig.setJit(true);
        outboundConfig.setRules(false);

        ProvisioningConfiguration provisioningConfig = new ProvisioningConfiguration();
        provisioningConfig.setOutboundProvisioningIdps(Collections.singletonList(outboundConfig));

        ApplicationPatchModel appPatch = new ApplicationPatchModel();
        appPatch.setProvisioningConfigurations(provisioningConfig);

        oAuth2RestClient.updateApplication(RESIDENT_APP_ID, appPatch);
    }

    private void clearResidentAppOutboundProvisioning() throws Exception {

        ProvisioningConfiguration provisioningConfig = new ProvisioningConfiguration();
        provisioningConfig.setOutboundProvisioningIdps(new ArrayList<>());

        ApplicationPatchModel appPatch = new ApplicationPatchModel();
        appPatch.setProvisioningConfigurations(provisioningConfig);

        oAuth2RestClient.updateApplication(RESIDENT_APP_ID, appPatch);
    }

    private String createOutboundProvisioningIdP() throws Exception {

        IdentityProviderPOSTRequest idpRequest = new IdentityProviderPOSTRequest();
        idpRequest.setName(IDP_NAME);
        idpRequest.setDescription(IDP_DESCRIPTION);
        idpRequest.setImage(IDP_IMAGE);
        idpRequest.setIsPrimary(false);
        idpRequest.setIsFederationHub(false);
        idpRequest.setHomeRealmIdentifier("");
        idpRequest.setIdpIssuerName("");
        idpRequest.setAlias("");

        // Build outbound provisioning connector properties
        List<Property> connectorProperties = new ArrayList<>();
        connectorProperties.add(createProperty("scim2-authentication-mode", "basic"));
        connectorProperties.add(createProperty("scim2-username", getSecondaryISAdmin().getUserName()));
        connectorProperties.add(createProperty("scim2-password", getSecondaryISAdmin().getPassword()));
        connectorProperties.add(createProperty("scim2-user-ep", getTargetScim2UserEndpoint()));
        connectorProperties.add(createProperty("scim2-group-ep", getTargetScim2GroupEndpoint()));
        connectorProperties.add(createProperty("scim2-user-store-domain", "PRIMARY"));
        connectorProperties.add(createProperty("scim2-enable-pwd-provisioning", "true"));

        // Build outbound connector
        ProvisioningRequest.OutboundProvisioningRequest.OutboundConnector scim2Connector =
                new ProvisioningRequest.OutboundProvisioningRequest.OutboundConnector();
        scim2Connector.setConnectorId(SCIM2_CONNECTOR_ID);
        scim2Connector.setName(SCIM2_CONNECTOR_NAME);
        scim2Connector.setIsEnabled(true);
        scim2Connector.setProperties(connectorProperties);

        List<ProvisioningRequest.OutboundProvisioningRequest.OutboundConnector> connectorList = new ArrayList<>();
        connectorList.add(scim2Connector);

        // Build outbound provisioning request
        ProvisioningRequest.OutboundProvisioningRequest outboundRequest =
                new ProvisioningRequest.OutboundProvisioningRequest();
        outboundRequest.setDefaultConnectorId(SCIM2_CONNECTOR_ID);
        outboundRequest.setConnectors(connectorList);

        // Build provisioning request
        ProvisioningRequest provisioningRequest = new ProvisioningRequest();
        provisioningRequest.setOutboundConnectors(outboundRequest);

        idpRequest.setProvisioning(provisioningRequest);

        return idpMgtRestClient.createIdentityProvider(idpRequest);
    }

    private Property createProperty(String key, String value) {

        Property property = new Property();
        property.setKey(key);
        property.setValue(value);
        return property;
    }

    /**
     * Get the base URI of the target server (second IS instance) for provisioning.
     */
    private String getSecondaryISURI() {

        return String.format(HTTPS_LOCALHOST_SERVICES, DEFAULT_PORT + PORT_OFFSET_1);
    }

    /**
     * Get the admin username of the target server (second IS instance) for provisioning.
     */
    private User getSecondaryISAdmin() throws XPathExpressionException {

        return testDataHolder.getAutomationContext().getSuperTenant().getTenantAdmin();
    }

    /**
     * Get the SCIM2 Users endpoint of the target server (second IS instance).
     */
    private String getTargetScim2UserEndpoint() {

        return getSecondaryISURI() + SCIM2_USER_ENDPOINT_PATH;
    }

    /**
     * Get the SCIM2 Groups endpoint of the target server (second IS instance).
     */
    private String getTargetScim2GroupEndpoint() {

        return getSecondaryISURI() + SCIM2_GROUP_ENDPOINT_PATH;
    }

}
