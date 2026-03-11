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
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.identity.application.common.model.xsd.InboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.xsd.OutboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ProvisioningConnectorConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.IdentityProviderPOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Property;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.ProvisioningRequest;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Roles;
import org.wso2.identity.integration.test.restclients.IdpMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.utils.CommonConstants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class OutboundProvisioningTestUtils {

    public static final String SCIM2_CONNECTOR_ID = "U0NJTTI";
    public static final String SCIM2_CONNECTOR_NAME = "SCIM2";
    public static final String RESIDENT_SP_NAME = "wso2carbon-local-sp";

    private static final int DEFAULT_PORT = CommonConstants.IS_DEFAULT_HTTPS_PORT;
    private static final int PORT_OFFSET_1 = 1;
    private static final String HTTPS_LOCALHOST_SERVICES = "https://localhost:%s/";
    private static final String SCIM2_USER_ENDPOINT_PATH = "scim2/Users";
    private static final String SCIM2_GROUP_ENDPOINT_PATH = "scim2/Groups";

    public static String createOutboundProvisioningIdP(IdpMgtRestClient idpMgtRestClient, String idpName,
                                                       String idpDescription, String idpImage,
                                                       User secondaryISAdmin,
                                                       List<String> outboundProvisioningRoles)
            throws Exception {

        IdentityProviderPOSTRequest idpRequest = new IdentityProviderPOSTRequest();
        idpRequest.setName(idpName);
        idpRequest.setDescription(idpDescription);
        idpRequest.setImage(idpImage);
        idpRequest.setIsPrimary(false);
        idpRequest.setIsFederationHub(false);
        idpRequest.setHomeRealmIdentifier("");
        idpRequest.setIdpIssuerName("");
        idpRequest.setAlias("");

        // Set outbound provisioning roles if provided.
        if (outboundProvisioningRoles != null && !outboundProvisioningRoles.isEmpty()) {
            Roles roles = new Roles();
            roles.setOutboundProvisioningRoles(outboundProvisioningRoles);
            idpRequest.setRoles(roles);
        }

        // Build outbound provisioning connector properties.
        List<Property> connectorProperties = new ArrayList<>();
        connectorProperties.add(createProperty("scim2-authentication-mode", "basic"));
        connectorProperties.add(createProperty("scim2-username", secondaryISAdmin.getUserName()));
        connectorProperties.add(createProperty("scim2-password", secondaryISAdmin.getPassword()));
        connectorProperties.add(createProperty("scim2-user-ep", getSecondaryISURI() + SCIM2_USER_ENDPOINT_PATH));
        connectorProperties.add(createProperty("scim2-group-ep", getSecondaryISURI() + SCIM2_GROUP_ENDPOINT_PATH));
        connectorProperties.add(createProperty("scim2-user-store-domain", "PRIMARY"));
        connectorProperties.add(createProperty("scim2-enable-pwd-provisioning", "false"));
        connectorProperties.add(createProperty("scim2-default-pwd", "DefaultPassword@123"));

        // Build outbound connector.
        ProvisioningRequest.OutboundProvisioningRequest.OutboundConnector scim2Connector =
                new ProvisioningRequest.OutboundProvisioningRequest.OutboundConnector();
        scim2Connector.setConnectorId(SCIM2_CONNECTOR_ID);
        scim2Connector.setName(SCIM2_CONNECTOR_NAME);
        scim2Connector.setIsEnabled(true);
        scim2Connector.setBlockingEnabled(true);
        scim2Connector.setIsDefault(true);
        scim2Connector.setProperties(connectorProperties);

        List<ProvisioningRequest.OutboundProvisioningRequest.OutboundConnector> connectorList = new ArrayList<>();
        connectorList.add(scim2Connector);

        // Build outbound provisioning request.
        ProvisioningRequest.OutboundProvisioningRequest outboundRequest =
                new ProvisioningRequest.OutboundProvisioningRequest();
        outboundRequest.setDefaultConnectorId(SCIM2_CONNECTOR_ID);
        outboundRequest.setConnectors(connectorList);

        // Build provisioning request.
        ProvisioningRequest provisioningRequest = new ProvisioningRequest();
        provisioningRequest.setOutboundConnectors(outboundRequest);

        idpRequest.setProvisioning(provisioningRequest);

        return idpMgtRestClient.createIdentityProvider(idpRequest);
    }

    public static void enableResidentAppOutboundProvisioning(ApplicationManagementServiceClient appMgtClient,
                                                             String idpName) throws Exception {

        ServiceProvider serviceProvider = appMgtClient.getApplication(RESIDENT_SP_NAME);
        if (serviceProvider == null) {
            serviceProvider = new ServiceProvider();
            serviceProvider.setApplicationName(RESIDENT_SP_NAME);
            try {
                appMgtClient.createApplication(serviceProvider);
                serviceProvider = appMgtClient.getApplication(RESIDENT_SP_NAME);
            } catch (Exception error) {
                throw new Exception("Error occurred while creating resident application.", error);
            }
        }
        Assert.assertNotNull(serviceProvider, "Resident service provider not found");

        InboundProvisioningConfig inboundProConfig = new InboundProvisioningConfig();
        inboundProConfig.setProvisioningUserStore("");
        serviceProvider.setInboundProvisioningConfig(inboundProConfig);

        org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider proIdp =
                new org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider();
        proIdp.setIdentityProviderName(idpName);

        ProvisioningConnectorConfig proCon = new ProvisioningConnectorConfig();
        proCon.setName(SCIM2_CONNECTOR_NAME);
        proCon.setBlocking(true);
        proIdp.setDefaultProvisioningConnectorConfig(proCon);

        OutboundProvisioningConfig outboundProConfig = new OutboundProvisioningConfig();
        outboundProConfig.setProvisioningIdentityProviders(
                new org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider[]{proIdp});
        serviceProvider.setOutboundProvisioningConfig(outboundProConfig);

        appMgtClient.updateApplicationData(serviceProvider);
    }

    public static void clearResidentAppOutboundProvisioning(ApplicationManagementServiceClient appMgtClient)
            throws Exception {

        ServiceProvider serviceProvider = appMgtClient.getApplication(RESIDENT_SP_NAME);
        if (serviceProvider != null) {
            serviceProvider.setOutboundProvisioningConfig(new OutboundProvisioningConfig());
            appMgtClient.updateApplicationData(serviceProvider);
        }
    }

    public static String buildUserPayload(String userName, String password, String givenName, String familyName,
                                           String email) {

        JSONObject payload = new JSONObject();

        JSONObject name = new JSONObject();
        name.put("familyName", familyName);
        name.put("givenName", givenName);
        payload.put("name", name);

        payload.put("userName", userName);
        payload.put("password", password);

        JSONObject primaryEmail = new JSONObject();
        primaryEmail.put("value", email);
        primaryEmail.put("primary", true);

        JSONArray emails = new JSONArray();
        emails.add(primaryEmail);
        payload.put("emails", emails);

        return payload.toJSONString();
    }

    public static String buildGroupPayload(String displayName, String memberId, String memberDisplay) {

        JSONObject payload = new JSONObject();

        JSONArray schemas = new JSONArray();
        schemas.add("urn:ietf:params:scim:schemas:core:2.0:Group");
        payload.put("schemas", schemas);

        payload.put("displayName", displayName);

        JSONArray members = new JSONArray();

        JSONObject member = new JSONObject();
        member.put("value", memberId);
        member.put("display", memberDisplay);
        members.add(member);

        payload.put("members", members);

        return payload.toJSONString();
    }

    public static String buildGroupPayloadNoMembers(String displayName) {

        JSONObject payload = new JSONObject();

        JSONArray schemas = new JSONArray();
        schemas.add("urn:ietf:params:scim:schemas:core:2.0:Group");
        payload.put("schemas", schemas);

        payload.put("displayName", displayName);

        return payload.toJSONString();
    }

    public static String buildGroupPatchAddMemberPayload(String memberId, String memberDisplay) {

        JSONObject payload = new JSONObject();

        JSONArray schemas = new JSONArray();
        schemas.add("urn:ietf:params:scim:api:messages:2.0:PatchOp");
        payload.put("schemas", schemas);

        JSONArray operations = new JSONArray();

        JSONObject op = new JSONObject();
        op.put("op", "add");

        JSONObject opValue = new JSONObject();
        JSONArray members = new JSONArray();
        JSONObject member = new JSONObject();
        member.put("display", memberDisplay);
        member.put("value", memberId);
        members.add(member);
        opValue.put("members", members);

        op.put("value", opValue);
        operations.add(op);

        payload.put("Operations", operations);

        return payload.toJSONString();
    }

    public static String buildGroupPatchRemoveMemberPayload(String memberIdToRemove) {

        JSONObject payload = new JSONObject();

        JSONArray schemas = new JSONArray();
        schemas.add("urn:ietf:params:scim:api:messages:2.0:PatchOp");
        payload.put("schemas", schemas);

        JSONArray operations = new JSONArray();

        JSONObject op = new JSONObject();
        op.put("op", "remove");
        op.put("path", "members[value eq \"" + memberIdToRemove + "\"]");
        operations.add(op);

        payload.put("Operations", operations);

        return payload.toJSONString();
    }

    public static String buildGroupPatchDisplayNamePayload(String newDisplayName) {

        JSONObject payload = new JSONObject();

        JSONArray schemas = new JSONArray();
        schemas.add("urn:ietf:params:scim:api:messages:2.0:PatchOp");
        payload.put("schemas", schemas);

        JSONArray operations = new JSONArray();

        JSONObject op = new JSONObject();
        op.put("op", "replace");
        op.put("path", "displayName");
        op.put("value", newDisplayName);
        operations.add(op);

        payload.put("Operations", operations);

        return payload.toJSONString();
    }

    public static String buildEncodedUserNameFilter(String username) throws UnsupportedEncodingException {

        String filter = String.format("userName eq \"%s\"", username);
        return URLEncoder.encode(filter, StandardCharsets.UTF_8.toString());
    }

    public static String buildEncodedDisplayNameFilter(String displayName) throws UnsupportedEncodingException {

        String filter = String.format("displayName eq \"%s\"", displayName);
        return URLEncoder.encode(filter, StandardCharsets.UTF_8.toString());
    }

    public static String getProvisionedUserId(SCIM2RestClient secondaryScim2RestClient, String userName)
            throws Exception {

        JSONObject secondaryUsers = secondaryScim2RestClient.filterUsers(buildEncodedUserNameFilter(userName));
        if (secondaryUsers == null) {
            return null;
        }

        long totalResults = (long) secondaryUsers.get("totalResults");
        if (totalResults == 0) {
            return null;
        }

        JSONArray resources = (JSONArray) secondaryUsers.get("Resources");
        if (resources == null || resources.size() == 0) {
            return null;
        }

        JSONObject user = (JSONObject) resources.get(0);
        return (String) user.get("id");
    }

    public static List<String> extractMemberDisplayNames(JSONArray members) {

        List<String> displayNames = new ArrayList<>();
        for (Object item : members) {
            JSONObject member = (JSONObject) item;
            String display = (String) member.get("display");
            if (display != null) {
                displayNames.add(display);
            }
        }
        return displayNames;
    }

    public static String getSecondaryISURI() {

        return String.format(HTTPS_LOCALHOST_SERVICES, DEFAULT_PORT + PORT_OFFSET_1);
    }

    public static Property createProperty(String key, String value) {

        Property property = new Property();
        property.setKey(key);
        property.setValue(value);
        return property;
    }
}
