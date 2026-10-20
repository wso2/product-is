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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.rest.api.server.idp.v1.sharing;

import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationPatchModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationSequence;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthenticationStep;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Authenticator;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

/**
 * Success path integration tests for identity provider (connection) sharing.
 * <p>
 * Organization hierarchy used by the tests:
 * <pre>
 * Root
 * ├── IdPShareOrgA
 * │   └── IdPShareOrgA1
 * │       └── IdPShareOrgA1a
 * └── IdPShareOrgB
 *     └── IdPShareOrgB1
 * </pre>
 * IdPShareOrgC and IdPShareOrgA2 are created after the identity provider is shared with all organizations.
 */
public class IdPSharingSuccessTest extends IdPSharingBaseTest {

    private static final String ORG_A = "IdPShareOrgA";
    private static final String ORG_A1 = "IdPShareOrgA1";
    private static final String ORG_A1A = "IdPShareOrgA1a";
    private static final String ORG_A2 = "IdPShareOrgA2";
    private static final String ORG_B = "IdPShareOrgB";
    private static final String ORG_B1 = "IdPShareOrgB1";
    private static final String ORG_C = "IdPShareOrgC";

    private static final String IDP_NAME = "IdPSharingEnterpriseIdP";
    private static final String RENAMED_IDP_NAME = "IdPSharingRenamedEnterpriseIdP";
    private static final String UPDATED_DESCRIPTION = "Updated description of the shared enterprise connection.";
    private static final String UPDATE_SHARED_IDP_CLAIMS_JSON = "update-shared-idp-claims.json";
    private static final String UPDATE_SHARED_IDP_JIT_JSON = "update-shared-idp-jit.json";
    private static final String SUB_ORG_APP_NAME = "IdPSharingSubOrgApp";
    private static final String CALLBACK_URL = "https://localhost:8490/idp-sharing/callback";

    private static final String RESTRICTED_UPDATE_ERROR_CODE = "IDP-60016";
    private static final String RESTRICTED_SHARED_IDP_DELETE_ERROR_CODE = "IDP-60018";
    private static final String RESTRICTED_SHARED_PARENT_DELETE_ERROR_CODE = "IDP-60019";
    private static final String CONNECTION_NOT_SHAREABLE_ERROR_CODE = "CONNECTION_SHARE-60012";
    private static final String RESTRICTED_ATTRIBUTE_MESSAGE =
            "Attribute: '%s' is not allowed to be modified as it is inherited from the parent.";
    private static final String ENABLE_WITH_DISABLED_PARENT_MESSAGE =
            "Cannot enable the shared idp as the parent is disabled.";

    private String idpId;
    private String idpName = IDP_NAME;
    private String subOrgAppId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public IdPSharingSuccessTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
        // The base path is set per test method, hence set it to invoke the APIs during the class initialization.
        setBasePath();
        initRestClients();

        addOrganization(ORG_A);
        addSubOrganization(ORG_A1, ORG_A);
        addSubOrganization(ORG_A1A, ORG_A1);
        addOrganization(ORG_B);
        addSubOrganization(ORG_B1, ORG_B);

        idpId = createEnterpriseIdp(IDP_NAME);
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        setBasePath();
        try {
            if (subOrgAppId != null) {
                getResponseOfDeleteWithOAuth2(getSubOrgApplicationsPath() + "/" + subOrgAppId,
                        getSwitchedToken(ORG_A1));
            }
        } catch (Exception e) {
            log.warn("Failed to delete the sub organization application during cleanup.", e);
        }
        if (idpId != null) {
            try {
                unshareIdpFromAllOrgs(idpId);
                for (String orgName : getOrgNames()) {
                    awaitIdpUnsharedFromOrg(orgName, idpName);
                }
            } catch (Exception e) {
                log.warn("Failed to unshare the identity provider during cleanup.", e);
            }
            deleteRootIdp(idpId);
        }
        deleteOrganizations();
        closeRestClients();
        super.conclude();
    }

    @Test
    public void testShareIdpWithAllOrganizations() throws JSONException {

        assertSharingProcessTriggered(shareIdpWithAllOrgs(idpId));

        for (String orgName : Arrays.asList(ORG_A, ORG_A1, ORG_A1A, ORG_B, ORG_B1)) {
            String sharedIdpId = awaitIdpSharedWithOrg(orgName, idpName);
            Assert.assertNotEquals(sharedIdpId, idpId,
                    "Shared identity provider should have its own ID in the organization: " + orgName);
        }
    }

    @Test(dependsOnMethods = "testShareIdpWithAllOrganizations")
    public void testGetSharedOrganizationsOfIdp() throws JSONException {

        Response response = getSharedOrganizations(idpId, true);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        Assert.assertEquals(response.jsonPath().getString("sharingMode.policy"), POLICY_ALL_EXISTING_AND_FUTURE_ORGS,
                "Sharing mode of the identity provider should be the general sharing policy.");
        List<String> sharedOrgIds = response.jsonPath().getList(ORGANIZATIONS_FIELD + ".id", String.class);
        for (String orgName : Arrays.asList(ORG_A, ORG_A1, ORG_A1A, ORG_B, ORG_B1)) {
            Assert.assertTrue(sharedOrgIds.contains(getOrgId(orgName)),
                    "Organization: " + orgName + " should be listed in the shared organizations.");
        }
    }

    @Test(dependsOnMethods = "testGetSharedOrganizationsOfIdp")
    public void testSharedIdpDetailsInOrganizations() throws Exception {

        JSONObject parentIdp = new JSONObject(getResponseOfGet(IDP_API_BASE_PATH + "/" + idpId).asString());

        for (String orgName : Arrays.asList(ORG_A, ORG_A1, ORG_A1A, ORG_B, ORG_B1)) {
            JSONObject sharedIdp = getSharedIdp(orgName);
            Assert.assertEquals(sharedIdp.getString("name"), idpName,
                    "Shared identity provider should have the name of the parent identity provider.");
            Assert.assertTrue(sharedIdp.optBoolean(IS_SHARED_FIELD),
                    "Shared identity provider should be marked as shared in the organization: " + orgName);
            Assert.assertTrue(sharedIdp.getBoolean("isEnabled"),
                    "Shared identity provider should be enabled in the organization: " + orgName);
            Assert.assertEquals(sharedIdp.optString("description"), parentIdp.getString("description"),
                    "Description of the shared identity provider should be inherited from the parent.");
        }
        Assert.assertFalse(parentIdp.optBoolean(IS_SHARED_FIELD),
                "Parent identity provider should not be marked as shared.");
    }

    @Test(dependsOnMethods = "testSharedIdpDetailsInOrganizations")
    public void testSensitiveInformationNotExposedInSharedIdp() throws Exception {

        // The parent identity provider in the root organization exposes its authenticator configuration.
        Response parentAuthenticator = getResponseOfGet(IDP_API_BASE_PATH + "/" + idpId
                + FEDERATED_AUTHENTICATORS_PATH + "/" + OIDC_AUTHENTICATOR_ID);
        Assert.assertEquals(parentAuthenticator.getStatusCode(), HttpStatus.SC_OK);
        Assert.assertTrue(getPropertyKeys(new JSONObject(parentAuthenticator.asString())).contains(CLIENT_ID_PROPERTY),
                "Parent identity provider should expose its authenticator properties.");

        for (String orgName : Arrays.asList(ORG_A, ORG_A1A)) {
            String token = getSwitchedToken(orgName);
            JSONObject sharedIdp = getSharedIdp(orgName);
            String sharedIdpId = sharedIdp.getString("id");

            Assert.assertFalse(sharedIdp.has("certificate") && !sharedIdp.isNull("certificate"),
                    "Certificate of the parent identity provider should not be exposed in the shared identity "
                            + "provider of the organization: " + orgName);
            Assert.assertNotEquals(sharedIdp.optString("homeRealmIdentifier"), "enterprise-idp-home-realm",
                    "Home realm identifier of the parent should not be exposed in the organization: " + orgName);
            Assert.assertNotEquals(sharedIdp.optString("alias"), "https://enterprise.idp.test/token",
                    "Alias of the parent should not be exposed in the organization: " + orgName);

            Response sharedAuthenticator = getResponseOfGetWithOAuth2(getSubOrgIdpPath(sharedIdpId)
                    + FEDERATED_AUTHENTICATORS_PATH + "/" + OIDC_AUTHENTICATOR_ID, token);
            Assert.assertEquals(sharedAuthenticator.getStatusCode(), HttpStatus.SC_OK,
                    "Failed to get the authenticator of the shared identity provider. Response: "
                            + sharedAuthenticator.asString());
            List<String> propertyKeys = getPropertyKeys(new JSONObject(sharedAuthenticator.asString()));
            for (String sensitiveProperty : Arrays.asList(CLIENT_ID_PROPERTY, CLIENT_SECRET_PROPERTY,
                    "OAuth2AuthzEPUrl", "OAuth2TokenEPUrl", "callbackUrl")) {
                Assert.assertFalse(propertyKeys.contains(sensitiveProperty), "Authenticator property: "
                        + sensitiveProperty + " should not be exposed in the organization: " + orgName);
            }
        }
    }

    @Test(dependsOnMethods = "testSensitiveInformationNotExposedInSharedIdp")
    public void testIdpSharedWithOrganizationsCreatedAfterSharing() throws Exception {

        addOrganization(ORG_C);
        addSubOrganization(ORG_A2, ORG_A);

        awaitIdpSharedWithOrg(ORG_C, idpName);
        awaitIdpSharedWithOrg(ORG_A2, idpName);
    }

    @DataProvider(name = "restrictedAttributeProvider")
    public static Object[][] restrictedAttributeProvider() {

        return new Object[][]{
                {"/name", "UpdatedSharedIdPName", "name"},
                {"/description", "Updated description", "description"},
                {"/image", "assets/images/logos/updated.svg", "image URL"},
                {"/homeRealmIdentifier", "updated-home-realm", "home realm identifier"},
                {"/alias", "https://updated.idp.test/token", "alias"}
        };
    }

    @Test(dependsOnMethods = "testIdpSharedWithOrganizationsCreatedAfterSharing",
            dataProvider = "restrictedAttributeProvider")
    public void testUpdateRestrictedAttributeOfSharedIdp(String path, String value, String attributeName)
            throws Exception {

        Response response = patchSharedIdp(ORG_A, path, value);
        assertErrorResponse(response, HttpStatus.SC_BAD_REQUEST, RESTRICTED_UPDATE_ERROR_CODE,
                String.format(RESTRICTED_ATTRIBUTE_MESSAGE, attributeName));
    }

    @Test(dependsOnMethods = "testUpdateRestrictedAttributeOfSharedIdp")
    public void testUpdateClaimConfigOfSharedIdp() throws Exception {

        String sharedIdpId = getSharedIdp(ORG_A).getString("id");
        Response updateResponse = getResponseOfPutWithOAuth2(getSubOrgIdpPath(sharedIdpId) + CLAIMS_PATH,
                readResource(UPDATE_SHARED_IDP_CLAIMS_JSON), getSwitchedToken(ORG_A));
        Assert.assertEquals(updateResponse.getStatusCode(), HttpStatus.SC_OK,
                "Failed to update the claim configuration of the shared identity provider. Response: "
                        + updateResponse.asString());

        Response claimsResponse = getResponseOfGetWithOAuth2(getSubOrgIdpPath(sharedIdpId) + CLAIMS_PATH,
                getSwitchedToken(ORG_A));
        Assert.assertEquals(claimsResponse.jsonPath().getString("userIdClaim.uri"), "sub",
                "Claim configuration of the shared identity provider should be updated.");

        // The update should be local to the organization.
        Response otherOrgClaims = getResponseOfGetWithOAuth2(
                getSubOrgIdpPath(getSharedIdp(ORG_B).getString("id")) + CLAIMS_PATH, getSwitchedToken(ORG_B));
        Assert.assertNotEquals(otherOrgClaims.jsonPath().getString("userIdClaim.uri"), "sub",
                "Claim configuration update should not affect the shared identity provider of other organizations.");
        Response parentClaims = getResponseOfGet(IDP_API_BASE_PATH + "/" + idpId + CLAIMS_PATH);
        Assert.assertNotEquals(parentClaims.jsonPath().getString("userIdClaim.uri"), "sub",
                "Claim configuration update should not affect the parent identity provider.");
    }

    @Test(dependsOnMethods = "testUpdateClaimConfigOfSharedIdp")
    public void testUpdateJitConfigOfSharedIdp() throws Exception {

        String sharedIdpId = getSharedIdp(ORG_A).getString("id");
        Response updateResponse = getResponseOfPutWithOAuth2(getSubOrgIdpPath(sharedIdpId) + JIT_PATH,
                readResource(UPDATE_SHARED_IDP_JIT_JSON), getSwitchedToken(ORG_A));
        Assert.assertEquals(updateResponse.getStatusCode(), HttpStatus.SC_OK,
                "Failed to update the JIT provisioning configuration of the shared identity provider. Response: "
                        + updateResponse.asString());

        Response jitResponse = getResponseOfGetWithOAuth2(getSubOrgIdpPath(sharedIdpId) + JIT_PATH,
                getSwitchedToken(ORG_A));
        Assert.assertTrue(jitResponse.jsonPath().getBoolean("isEnabled"),
                "JIT provisioning of the shared identity provider should be enabled.");

        Response otherOrgJit = getResponseOfGetWithOAuth2(
                getSubOrgIdpPath(getSharedIdp(ORG_B).getString("id")) + JIT_PATH, getSwitchedToken(ORG_B));
        Assert.assertFalse(otherOrgJit.jsonPath().getBoolean("isEnabled"),
                "JIT provisioning update should not affect the shared identity provider of other organizations.");
    }

    @Test(dependsOnMethods = "testUpdateJitConfigOfSharedIdp")
    public void testDisableAndEnableSharedIdpInOrganization() throws Exception {

        Response disableResponse = patchSharedIdp(ORG_B, "/isEnabled", false);
        Assert.assertEquals(disableResponse.getStatusCode(), HttpStatus.SC_OK,
                "Failed to disable the shared identity provider. Response: " + disableResponse.asString());
        Assert.assertFalse(getSharedIdp(ORG_B).getBoolean("isEnabled"),
                "Shared identity provider should be disabled in the organization.");
        Assert.assertTrue(getSharedIdp(ORG_B1).getBoolean("isEnabled"),
                "Disabling the shared identity provider should not affect other organizations.");

        Response enableResponse = patchSharedIdp(ORG_B, "/isEnabled", true);
        Assert.assertEquals(enableResponse.getStatusCode(), HttpStatus.SC_OK,
                "Failed to enable the shared identity provider. Response: " + enableResponse.asString());
        Assert.assertTrue(getSharedIdp(ORG_B).getBoolean("isEnabled"),
                "Shared identity provider should be enabled in the organization.");
    }

    @Test(dependsOnMethods = "testDisableAndEnableSharedIdpInOrganization")
    public void testEnableSharedIdpWhenParentIdpIsDisabled() throws Exception {

        // Disable the shared identity provider in the organization and then the parent identity provider.
        Response disableResponse = patchSharedIdp(ORG_B, "/isEnabled", false);
        Assert.assertEquals(disableResponse.getStatusCode(), HttpStatus.SC_OK,
                "Failed to disable the shared identity provider. Response: " + disableResponse.asString());
        patchRootIdp("/isEnabled", false);
        try {
            Assert.assertFalse(getSharedIdp(ORG_B1).getBoolean("isEnabled"),
                    "Shared identity provider should be disabled when the parent identity provider is disabled.");

            Response enableResponse = patchSharedIdp(ORG_B, "/isEnabled", true);
            assertErrorResponse(enableResponse, HttpStatus.SC_BAD_REQUEST, RESTRICTED_UPDATE_ERROR_CODE,
                    ENABLE_WITH_DISABLED_PARENT_MESSAGE);
        } finally {
            patchRootIdp("/isEnabled", true);
        }
        Assert.assertTrue(getSharedIdp(ORG_B1).getBoolean("isEnabled"),
                "Shared identity provider should be enabled once the parent identity provider is enabled.");
        Assert.assertFalse(getSharedIdp(ORG_B).getBoolean("isEnabled"),
                "Locally disabled shared identity provider should remain disabled.");

        // Once the parent identity provider is enabled, the shared identity provider can be enabled.
        Response enableResponse = patchSharedIdp(ORG_B, "/isEnabled", true);
        Assert.assertEquals(enableResponse.getStatusCode(), HttpStatus.SC_OK,
                "Failed to enable the shared identity provider. Response: " + enableResponse.asString());
        Assert.assertTrue(getSharedIdp(ORG_B).getBoolean("isEnabled"),
                "Shared identity provider should be enabled in the organization.");
    }

    @Test(dependsOnMethods = "testEnableSharedIdpWhenParentIdpIsDisabled")
    public void testParentIdpUpdatesReflectedInSharedIdp() throws JSONException {

        patchRootIdp("/description", UPDATED_DESCRIPTION);
        await().atMost(30, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .ignoreExceptions()
                .until(() -> UPDATED_DESCRIPTION.equals(getSharedIdp(ORG_A1A).optString("description")));

        patchRootIdp("/name", RENAMED_IDP_NAME);
        idpName = RENAMED_IDP_NAME;
        for (String orgName : getOrgNames()) {
            awaitIdpSharedWithOrg(orgName, RENAMED_IDP_NAME);
        }
    }

    @Test(dependsOnMethods = "testParentIdpUpdatesReflectedInSharedIdp")
    public void testDeleteSharedIdpInOrganizationIsRestricted() throws Exception {

        String sharedIdpId = getSharedIdp(ORG_A).getString("id");
        Response response = getResponseOfDeleteWithOAuth2(getSubOrgIdpPath(sharedIdpId), getSwitchedToken(ORG_A));
        assertErrorResponse(response, HttpStatus.SC_BAD_REQUEST, RESTRICTED_SHARED_IDP_DELETE_ERROR_CODE, null);
        Assert.assertNotNull(findIdpInOrg(ORG_A, idpName),
                "Shared identity provider should not be deleted from the organization.");
    }

    @Test(dependsOnMethods = "testDeleteSharedIdpInOrganizationIsRestricted")
    public void testDeleteSharedParentIdpIsRestricted() throws JSONException {

        Response response = getResponseOfDelete(IDP_API_BASE_PATH + "/" + idpId);
        assertErrorResponse(response, HttpStatus.SC_BAD_REQUEST, RESTRICTED_SHARED_PARENT_DELETE_ERROR_CODE, null);
        getResponseOfGet(IDP_API_BASE_PATH + "/" + idpId)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test(dependsOnMethods = "testDeleteSharedParentIdpIsRestricted")
    public void testReshareSharedIdpFromOrganizationIsRestricted() throws Exception {

        String sharedIdpId = getSharedIdp(ORG_A).getString("id");
        String body = new JSONObject()
                .put("identityProviderId", sharedIdpId)
                .put("policy", POLICY_ALL_EXISTING_AND_FUTURE_ORGS)
                .toString();
        Response response = getResponseOfPostWithOAuth2(getSubOrgIdpApiBasePath() + "/share-with-all", body,
                getSwitchedToken(ORG_A));
        assertErrorResponse(response, HttpStatus.SC_BAD_REQUEST, CONNECTION_NOT_SHAREABLE_ERROR_CODE, null);
    }

    @Test(dependsOnMethods = "testReshareSharedIdpFromOrganizationIsRestricted")
    public void testAssignSharedIdpToOrganizationApplication() throws Exception {

        String token = getSwitchedToken(ORG_A1);
        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.addGrantTypesItem("authorization_code");
        oidcConfig.addCallbackURLsItem(CALLBACK_URL);
        ApplicationModel application = new ApplicationModel()
                .name(SUB_ORG_APP_NAME)
                .inboundProtocolConfiguration(new InboundProtocols().oidc(oidcConfig))
                .advancedConfigurations(new AdvancedApplicationConfiguration().skipLoginConsent(true));
        subOrgAppId = oAuth2RestClient.createOrganizationApplication(application, token);

        AuthenticationSequence authSequence = new AuthenticationSequence()
                .type(AuthenticationSequence.TypeEnum.USER_DEFINED)
                .addStepsItem(new AuthenticationStep()
                        .id(1)
                        .addOptionsItem(new Authenticator().idp(idpName).authenticator(OIDC_AUTHENTICATOR_NAME)));
        ApplicationPatchModel patchModel = new ApplicationPatchModel();
        patchModel.setAuthenticationSequence(authSequence);
        oAuth2RestClient.updateSubOrgApplication(subOrgAppId, patchModel, token);

        ApplicationResponseModel updatedApplication = oAuth2RestClient.getOrganizationApplication(subOrgAppId, token);
        Authenticator configuredAuthenticator = updatedApplication.getAuthenticationSequence().getSteps().get(0)
                .getOptions().get(0);
        Assert.assertEquals(configuredAuthenticator.getIdp(), idpName,
                "Shared identity provider should be configured in the application authentication sequence.");
        Assert.assertEquals(configuredAuthenticator.getAuthenticator(), OIDC_AUTHENTICATOR_NAME,
                "Authenticator of the shared identity provider should be configured in the application.");
    }

    @Test(dependsOnMethods = "testAssignSharedIdpToOrganizationApplication")
    public void testUnshareIdpFromAllOrganizations() throws JSONException {

        assertSharingProcessTriggered(unshareIdpFromAllOrgs(idpId));
        for (String orgName : getOrgNames()) {
            awaitIdpUnsharedFromOrg(orgName, idpName);
        }

        Response response = getSharedOrganizations(idpId, false);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        List<?> sharedOrgs = response.jsonPath().getList(ORGANIZATIONS_FIELD);
        Assert.assertTrue(sharedOrgs == null || sharedOrgs.isEmpty(),
                "Identity provider should not be shared with any organization.");
    }

    @Test(dependsOnMethods = "testUnshareIdpFromAllOrganizations")
    public void testShareIdpWithSelectedOrganizations() throws Exception {

        Map<String, String> orgPolicies = new LinkedHashMap<>();
        orgPolicies.put(ORG_A, POLICY_SELECTED_ORG_ONLY);
        orgPolicies.put(ORG_B, POLICY_SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN);
        assertSharingProcessTriggered(shareIdpWithSelectedOrgs(idpId, orgPolicies));

        awaitIdpSharedWithOrg(ORG_A, idpName);
        awaitIdpSharedWithOrg(ORG_B, idpName);
        awaitIdpSharedWithOrg(ORG_B1, idpName);
        for (String orgName : Arrays.asList(ORG_A1, ORG_A1A, ORG_A2, ORG_C)) {
            assertIdpNotSharedWithOrg(orgName, idpName);
        }

        Response response = getSharedOrganizations(idpId, true);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        Map<String, String> sharedOrgPolicies = new HashMap<>();
        JSONArray sharedOrgs = new JSONObject(response.asString()).getJSONArray(ORGANIZATIONS_FIELD);
        for (int i = 0; i < sharedOrgs.length(); i++) {
            JSONObject sharedOrg = sharedOrgs.getJSONObject(i);
            JSONObject sharingMode = sharedOrg.optJSONObject(SHARING_MODE_ATTRIBUTE);
            sharedOrgPolicies.put(sharedOrg.getString("id"), sharingMode != null ? sharingMode.optString("policy")
                    : null);
        }
        Assert.assertEquals(sharedOrgPolicies.keySet().size(), 3,
                "Identity provider should be shared with the selected organizations and their children.");
        Assert.assertEquals(sharedOrgPolicies.get(getOrgId(ORG_B)),
                POLICY_SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN,
                "Sharing policy of the organization should be returned.");
        Assert.assertTrue(sharedOrgPolicies.containsKey(getOrgId(ORG_A)));
        Assert.assertTrue(sharedOrgPolicies.containsKey(getOrgId(ORG_B1)));
    }

    @Test(dependsOnMethods = "testShareIdpWithSelectedOrganizations")
    public void testShareIdpWithChildOrganizationAlongWithItsParent() throws Exception {

        Map<String, String> orgPolicies = new LinkedHashMap<>();
        orgPolicies.put(ORG_A1, POLICY_SELECTED_ORG_ONLY);
        orgPolicies.put(ORG_A1A, POLICY_SELECTED_ORG_ONLY);
        assertSharingProcessTriggered(shareIdpWithSelectedOrgs(idpId, orgPolicies));

        awaitIdpSharedWithOrg(ORG_A1, idpName);
        awaitIdpSharedWithOrg(ORG_A1A, idpName);
        assertIdpNotSharedWithOrg(ORG_A2, idpName);
    }

    @Test(dependsOnMethods = "testShareIdpWithChildOrganizationAlongWithItsParent")
    public void testUnshareIdpFromSelectedOrganizationRemovesFromDescendants() throws Exception {

        assertSharingProcessTriggered(unshareIdpFromSelectedOrgs(idpId, Collections.singletonList(ORG_A)));

        for (String orgName : Arrays.asList(ORG_A, ORG_A1, ORG_A1A)) {
            awaitIdpUnsharedFromOrg(orgName, idpName);
        }
        Assert.assertNotNull(findIdpInOrg(ORG_B, idpName),
                "Identity provider should remain shared with the organizations that are not unshared.");
        Assert.assertNotNull(findIdpInOrg(ORG_B1, idpName),
                "Identity provider should remain shared with the organizations that are not unshared.");
    }

    @Test(dependsOnMethods = "testUnshareIdpFromSelectedOrganizationRemovesFromDescendants")
    public void testDeleteParentIdpAfterUnsharing() throws JSONException {

        assertSharingProcessTriggered(unshareIdpFromAllOrgs(idpId));
        for (String orgName : getOrgNames()) {
            awaitIdpUnsharedFromOrg(orgName, idpName);
        }

        getResponseOfDelete(IDP_API_BASE_PATH + "/" + idpId)
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        idpId = null;
    }

    private JSONObject getSharedIdp(String orgName) throws Exception {

        JSONObject sharedIdp = findIdpInOrg(orgName, idpName);
        Assert.assertNotNull(sharedIdp, "Identity provider: " + idpName + " is not shared with the organization: "
                + orgName);
        Response response = getResponseOfGetWithOAuth2(getSubOrgIdpPath(sharedIdp.getString("id")),
                getSwitchedToken(orgName));
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_OK,
                "Failed to get the shared identity provider. Response: " + response.asString());
        return new JSONObject(response.asString());
    }

    private Response patchSharedIdp(String orgName, String path, Object value) throws Exception {

        String sharedIdpId = getSharedIdp(orgName).getString("id");
        return getResponseOfPatchWithOAuth2(getSubOrgIdpPath(sharedIdpId), buildReplacePatch(path, value),
                getSwitchedToken(orgName));
    }

    private void patchRootIdp(String path, Object value) throws JSONException {

        getResponseOfPatch(IDP_API_BASE_PATH + "/" + idpId, buildReplacePatch(path, value))
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    private String buildReplacePatch(String path, Object value) throws JSONException {

        return new JSONArray()
                .put(new JSONObject()
                        .put("operation", "REPLACE")
                        .put("path", path)
                        .put("value", String.valueOf(value)))
                .toString();
    }

    private List<String> getPropertyKeys(JSONObject authenticator) throws JSONException {

        JSONArray properties = authenticator.optJSONArray("properties");
        List<String> keys = new ArrayList<>();
        if (properties != null) {
            for (int i = 0; i < properties.length(); i++) {
                keys.add(properties.getJSONObject(i).optString("key"));
            }
        }
        return keys;
    }

    private String getSubOrgApplicationsPath() {

        return getSubOrgIdpApiBasePath().replace(IDP_API_BASE_PATH, "/applications");
    }
}
