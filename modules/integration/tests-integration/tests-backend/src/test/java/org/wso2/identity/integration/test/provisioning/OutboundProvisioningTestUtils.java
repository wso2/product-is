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
import org.wso2.carbon.identity.application.common.model.xsd.JustInTimeProvisioningConfig;
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

    /**
     * Creates an identity provider with outbound SCIM2 provisioning connector.
     * The SCIM endpoints default to the secondary IS super tenant.
     *
     * @param idpMgtRestClient the IdP management REST client
     * @param idpName the name of the identity provider
     * @param idpDescription the description of the identity provider
     * @param idpImage the image URL for the identity provider
     * @param secondaryISAdmin the admin user credentials for the secondary IS
     * @param outboundProvisioningRoles the list of roles to provision
     * @return the ID of the created identity provider
     * @throws Exception if an error occurs during IdP creation
     */
    public static String createOutboundProvisioningIdP(IdpMgtRestClient idpMgtRestClient, String idpName,
                                                       String idpDescription, String idpImage,
                                                       User secondaryISAdmin,
                                                       List<String> outboundProvisioningRoles)
            throws Exception {

        return createOutboundProvisioningIdP(idpMgtRestClient, idpName, idpDescription, idpImage,
                secondaryISAdmin, outboundProvisioningRoles, getSecondaryISURI());
    }

    /**
     * Creates an identity provider with outbound SCIM2 provisioning connector pointing to a custom
     * SCIM base URL. Use this overload to target a specific tenant on the secondary IS — for example,
     * {@code "https://localhost:9854/t/outboundtenant.com/"}.
     *
     * @param idpMgtRestClient the IdP management REST client
     * @param idpName the name of the identity provider
     * @param idpDescription the description of the identity provider
     * @param idpImage the image URL for the identity provider
     * @param targetAdmin the admin user credentials for the target SCIM endpoint
     * @param outboundProvisioningRoles the list of roles to provision
     * @param scimBaseUrl the base URL for SCIM endpoints (must end with '/')
     * @return the ID of the created identity provider
     * @throws Exception if an error occurs during IdP creation
     */
    public static String createOutboundProvisioningIdP(IdpMgtRestClient idpMgtRestClient, String idpName,
                                                       String idpDescription, String idpImage,
                                                       User targetAdmin,
                                                       List<String> outboundProvisioningRoles,
                                                       String scimBaseUrl)
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
        connectorProperties.add(createProperty("scim2-username", targetAdmin.getUserName()));
        connectorProperties.add(createProperty("scim2-password", targetAdmin.getPassword()));
        connectorProperties.add(createProperty("scim2-user-ep", scimBaseUrl + SCIM2_USER_ENDPOINT_PATH));
        connectorProperties.add(createProperty("scim2-group-ep", scimBaseUrl + SCIM2_GROUP_ENDPOINT_PATH));
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

    /**
     * Enables outbound provisioning for the resident service provider.
     *
     * @param appMgtClient the application management service client
     * @param idpName the identity provider name to configure
     * @param jitEnabled whether outbound provisioning should fire for JIT-provisioned users
     * @throws Exception if an error occurs during configuration
     */
    public static void enableResidentAppOutboundProvisioning(ApplicationManagementServiceClient appMgtClient,
                                                             String idpName, boolean jitEnabled) throws Exception {

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

        if (jitEnabled) {
            JustInTimeProvisioningConfig jitConfig = new JustInTimeProvisioningConfig();
            jitConfig.setProvisioningEnabled(true);
            proIdp.setJustInTimeProvisioningConfig(jitConfig);
        }

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

    /**
     * Clears outbound provisioning configuration from the resident service provider.
     *
     * @param appMgtClient the application management service client
     * @throws Exception if an error occurs during cleanup
     */
    public static void clearResidentAppOutboundProvisioning(ApplicationManagementServiceClient appMgtClient)
            throws Exception {

        ServiceProvider serviceProvider = appMgtClient.getApplication(RESIDENT_SP_NAME);
        if (serviceProvider != null) {
            serviceProvider.setOutboundProvisioningConfig(new OutboundProvisioningConfig());
            appMgtClient.updateApplicationData(serviceProvider);
        }
    }

    /**
     * Enables outbound provisioning for a specific application.
     *
     * @param appMgtClient the application management service client
     * @param appName the name of the application
     * @param idpName the identity provider name to configure
     * @param jitEnabled whether outbound provisioning should fire for JIT-provisioned users
     * @throws Exception if an error occurs during configuration
     */
    public static void enableAppOutboundProvisioning(ApplicationManagementServiceClient appMgtClient,
                                                     String appName, String idpName,
                                                     boolean jitEnabled) throws Exception {

        ServiceProvider serviceProvider = appMgtClient.getApplication(appName);
        Assert.assertNotNull(serviceProvider, "Service provider not found: " + appName);

        InboundProvisioningConfig inboundProConfig = new InboundProvisioningConfig();
        inboundProConfig.setProvisioningUserStore("");
        serviceProvider.setInboundProvisioningConfig(inboundProConfig);

        org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider proIdp =
                new org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider();
        proIdp.setIdentityProviderName(idpName);

        if (jitEnabled) {
            JustInTimeProvisioningConfig jitConfig = new JustInTimeProvisioningConfig();
            jitConfig.setProvisioningEnabled(true);
            proIdp.setJustInTimeProvisioningConfig(jitConfig);
        }

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

    /**
     * Clears outbound provisioning configuration from an application.
     *
     * @param appMgtClient the application management service client
     * @param appName the name of the application
     * @throws Exception if an error occurs during cleanup
     */
    public static void clearAppOutboundProvisioning(ApplicationManagementServiceClient appMgtClient,
                                                    String appName) throws Exception {

        ServiceProvider serviceProvider = appMgtClient.getApplication(appName);
        if (serviceProvider != null) {
            serviceProvider.setOutboundProvisioningConfig(new OutboundProvisioningConfig());
            appMgtClient.updateApplicationData(serviceProvider);
        }
    }

    /**
     * Builds a SCIM2 user creation payload.
     *
     * @param userName the username
     * @param password the password
     * @param givenName the given name
     * @param familyName the family name
     * @param email the email address
     * @return the JSON payload as a string
     */
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

    /**
     * Builds a SCIM2 group creation payload with a single member.
     *
     * @param displayName the group display name
     * @param memberId the member user ID
     * @param memberDisplay the member display name
     * @return the JSON payload as a string
     */
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

    /**
     * Builds a SCIM2 group creation payload without members.
     *
     * @param displayName the group display name
     * @return the JSON payload as a string
     */
    public static String buildGroupPayloadNoMembers(String displayName) {

        JSONObject payload = new JSONObject();

        JSONArray schemas = new JSONArray();
        schemas.add("urn:ietf:params:scim:schemas:core:2.0:Group");
        payload.put("schemas", schemas);

        payload.put("displayName", displayName);

        return payload.toJSONString();
    }

    /**
     * Builds a SCIM2 patch operation payload to add a member to a group.
     *
     * @param memberId the member user ID to add
     * @param memberDisplay the member display name
     * @return the JSON payload as a string
     */
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

    /**
     * Builds a SCIM2 patch operation payload to remove a member from a group.
     *
     * @param memberIdToRemove the member user ID to remove
     * @return the JSON payload as a string
     */
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

    /**
     * Builds a SCIM2 patch operation payload to update a group's display name.
     *
     * @param newDisplayName the new display name
     * @return the JSON payload as a string
     */
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

    /**
     * Builds a URL-encoded SCIM2 filter for fetching users by username.
     *
     * @param username the username to filter by
     * @return the URL-encoded filter string
     * @throws UnsupportedEncodingException if encoding fails
     */
    public static String buildEncodedUserNameFilter(String username) throws UnsupportedEncodingException {

        String filter = String.format("userName eq \"%s\"", username);
        return URLEncoder.encode(filter, StandardCharsets.UTF_8.toString());
    }

    /**
     * Builds a URL-encoded SCIM2 filter for fetching groups by display name.
     *
     * @param displayName the display name to filter by
     * @return the URL-encoded filter string
     * @throws UnsupportedEncodingException if encoding fails
     */
    public static String buildEncodedDisplayNameFilter(String displayName) throws UnsupportedEncodingException {

        String filter = String.format("displayName eq \"%s\"", displayName);
        return URLEncoder.encode(filter, StandardCharsets.UTF_8.toString());
    }

    /**
     * Retrieves the user ID of a provisioned user by username.
     *
     * @param secondaryScim2RestClient the SCIM2 REST client for the secondary IS
     * @param userName the username to search for
     * @return the user ID if found, null otherwise
     * @throws Exception if an error occurs during retrieval
     */
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

    /**
     * Polls for a provisioned user by username with retries. Useful when outbound provisioning
     * may complete asynchronously (e.g., provisioning triggered by JIT).
     *
     * @param scim2RestClient the SCIM2 REST client targeting the provisioning destination
     * @param userName the username to search for
     * @param maxWaitSeconds maximum number of seconds to wait before giving up
     * @return the user ID if found within the timeout, null otherwise
     * @throws Exception if an unrecoverable error occurs
     */
    public static String waitForProvisionedUser(SCIM2RestClient scim2RestClient, String userName,
                                                int maxWaitSeconds) throws Exception {

        long deadline = System.currentTimeMillis() + maxWaitSeconds * 1000L;
        String userId = null;
        while (System.currentTimeMillis() < deadline) {
            userId = getProvisionedUserId(scim2RestClient, userName);
            if (userId != null) {
                return userId;
            }
            Thread.sleep(1000);
        }
        // Final attempt after the loop.
        return getProvisionedUserId(scim2RestClient, userName);
    }

    /**
     * Extracts display names from a JSON array of group members.
     *
     * @param members the JSON array of members
     * @return a list of member display names
     */
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

    /**
     * Returns the URI of the secondary Identity Server instance.
     *
     * @return the secondary IS URI
     */
    public static String getSecondaryISURI() {

        return String.format(HTTPS_LOCALHOST_SERVICES, DEFAULT_PORT + PORT_OFFSET_1);
    }

    /**
     * Creates a property object with the given key and value.
     *
     * @param key the property key
     * @param value the property value
     * @return the property object
     */
    public static Property createProperty(String key, String value) {

        Property property = new Property();
        property.setKey(key);
        property.setValue(value);
        return property;
    }
}
