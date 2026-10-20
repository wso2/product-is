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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Failure path integration tests for identity provider (connection) sharing.
 */
public class IdPSharingFailureTest extends IdPSharingBaseTest {

    private static final String ORG_P = "IdPShareFailOrgP";
    private static final String ORG_P1 = "IdPShareFailOrgP1";
    private static final String ORG_P1A = "IdPShareFailOrgP1a";

    private static final String IDP_NAME = "IdPSharingFailureEnterpriseIdP";
    private static final String TRUSTED_TOKEN_ISSUER_IDP_NAME = "IdPSharingFailureTrustedTokenIssuer";
    private static final String ADD_TRUSTED_TOKEN_ISSUER_IDP_JSON = "add-trusted-token-issuer-idp.json";
    private static final String USER_DEFINED_AUTHENTICATOR_IDP_NAME = "IdPSharingFailureUserDefinedAuthIdP";
    private static final String USER_DEFINED_AUTHENTICATOR_NAME = "custom-IdPSharingFailureAuthenticator";
    private static final String ADD_USER_DEFINED_AUTHENTICATOR_IDP_JSON = "add-user-defined-authenticator-idp.json";
    private static final String IDP_NAME_PLACEHOLDER = "<IDP_NAME>";
    private static final String AUTHENTICATOR_ID_PLACEHOLDER = "<AUTHENTICATOR_ID>";
    private static final String INVALID_POLICY = "INVALID_POLICY";

    private static final String INVALID_IDP_ID_ERROR_CODE = "IDP-60046";
    private static final String EMPTY_ORGANIZATIONS_ERROR_CODE = "IDP-60047";
    private static final String UNSUPPORTED_POLICY_ERROR_CODE = "IDP-60048";
    private static final String UNSUPPORTED_POLICY_FOR_SELECTIVE_SHARE_ERROR_CODE = "CONNECTION_SHARE-60008";
    private static final String CONNECTION_NOT_FOUND_ERROR_CODE = "CONNECTION_SHARE-60009";
    private static final String UNSUPPORTED_ATTRIBUTE_ERROR_CODE = "CONNECTION_SHARE-60010";
    private static final String CONNECTION_NOT_SHAREABLE_ERROR_CODE = "CONNECTION_SHARE-60012";
    private static final String PARENT_NOT_SHARED_ERROR_CODE = "CONNECTION_SHARE-60013";

    private String idpId;
    private String trustedTokenIssuerIdpId;
    private String userDefinedAuthenticatorIdpId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public IdPSharingFailureTest(TestUserMode userMode) throws Exception {

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

        addOrganization(ORG_P);
        addSubOrganization(ORG_P1, ORG_P);
        addSubOrganization(ORG_P1A, ORG_P1);

        idpId = createEnterpriseIdp(IDP_NAME);
        trustedTokenIssuerIdpId = createRootIdp(readResource(ADD_TRUSTED_TOKEN_ISSUER_IDP_JSON)
                .replace(IDP_NAME_PLACEHOLDER, TRUSTED_TOKEN_ISSUER_IDP_NAME));
        userDefinedAuthenticatorIdpId = createRootIdp(readResource(ADD_USER_DEFINED_AUTHENTICATOR_IDP_JSON)
                .replace(IDP_NAME_PLACEHOLDER, USER_DEFINED_AUTHENTICATOR_IDP_NAME)
                .replace(AUTHENTICATOR_ID_PLACEHOLDER, Base64.encodeBase64URLSafeString(
                        USER_DEFINED_AUTHENTICATOR_NAME.getBytes(StandardCharsets.UTF_8))));
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        setBasePath();
        if (idpId != null) {
            try {
                unshareIdpFromAllOrgs(idpId);
                for (String orgName : getOrgNames()) {
                    awaitIdpUnsharedFromOrg(orgName, IDP_NAME);
                }
            } catch (Exception e) {
                log.warn("Failed to unshare the identity provider during cleanup.", e);
            }
        }
        deleteRootIdp(idpId);
        deleteRootIdp(trustedTokenIssuerIdpId);
        deleteRootIdp(userDefinedAuthenticatorIdpId);
        deleteOrganizations();
        closeRestClients();
        super.conclude();
    }

    @Test
    public void testShareIdpWithEmptyIdpId() throws JSONException {

        Response response = shareIdpWithSelectedOrgs(StringUtils.EMPTY,
                Collections.singletonMap(ORG_P, POLICY_SELECTED_ORG_ONLY));
        assertErrorResponse(response, HttpStatus.SC_BAD_REQUEST, INVALID_IDP_ID_ERROR_CODE, null);
    }

    @Test
    public void testShareIdpWithEmptyOrganizations() throws JSONException {

        Response response = shareIdp(new JSONObject()
                .put("identityProviderId", idpId)
                .put(ORGANIZATIONS_FIELD, new JSONArray())
                .toString());
        assertErrorResponse(response, HttpStatus.SC_BAD_REQUEST, EMPTY_ORGANIZATIONS_ERROR_CODE, null);
    }

    @Test
    public void testShareIdpWithUnsupportedPolicy() throws JSONException {

        Response response = shareIdpWithSelectedOrgs(idpId, Collections.singletonMap(ORG_P, INVALID_POLICY));
        assertErrorResponse(response, HttpStatus.SC_BAD_REQUEST, UNSUPPORTED_POLICY_ERROR_CODE, null);
    }

    @Test
    public void testShareIdpWithSelectedOrgsUsingGeneralPolicy() throws JSONException {

        Response response = shareIdpWithSelectedOrgs(idpId,
                Collections.singletonMap(ORG_P, POLICY_ALL_EXISTING_AND_FUTURE_ORGS));
        assertErrorResponse(response, HttpStatus.SC_BAD_REQUEST, UNSUPPORTED_POLICY_FOR_SELECTIVE_SHARE_ERROR_CODE,
                null);
    }

    @Test
    public void testShareIdpWithAllOrgsUsingSelectivePolicy() throws JSONException {

        Response response = getResponseOfPost(IDP_SHARE_WITH_ALL_PATH, new JSONObject()
                .put("identityProviderId", idpId)
                .put("policy", POLICY_SELECTED_ORG_ONLY)
                .toString());
        assertErrorResponse(response, HttpStatus.SC_BAD_REQUEST, UNSUPPORTED_POLICY_ERROR_CODE, null);
    }

    @Test
    public void testShareNonExistingIdp() throws JSONException {

        Response response = shareIdpWithAllOrgs(UUID.randomUUID().toString());
        assertErrorResponse(response, HttpStatus.SC_BAD_REQUEST, CONNECTION_NOT_FOUND_ERROR_CODE, null);
    }

    @Test
    public void testShareTrustedTokenIssuerIdp() throws JSONException {

        Response response = shareIdpWithAllOrgs(trustedTokenIssuerIdpId);
        assertErrorResponse(response, HttpStatus.SC_BAD_REQUEST, CONNECTION_NOT_SHAREABLE_ERROR_CODE, null);
    }

    @Test
    public void testShareIdpWithUserDefinedAuthenticatorWithAllOrgs() throws JSONException {

        Response response = shareIdpWithAllOrgs(userDefinedAuthenticatorIdpId);
        assertAuthenticatorNotShareable(response, USER_DEFINED_AUTHENTICATOR_NAME);
    }

    @Test
    public void testShareIdpWithUserDefinedAuthenticatorWithSelectedOrgs() throws Exception {

        Response response = shareIdpWithSelectedOrgs(userDefinedAuthenticatorIdpId,
                Collections.singletonMap(ORG_P, POLICY_SELECTED_ORG_ONLY));
        assertAuthenticatorNotShareable(response, USER_DEFINED_AUTHENTICATOR_NAME);
        assertIdpNotSharedWithOrg(ORG_P, USER_DEFINED_AUTHENTICATOR_IDP_NAME);
    }

    @Test
    public void testShareIdpWithChildOrgWithoutSharingWithItsParent() throws Exception {

        Response response = shareIdpWithSelectedOrgs(idpId,
                Collections.singletonMap(ORG_P1A, POLICY_SELECTED_ORG_ONLY));
        assertErrorResponse(response, HttpStatus.SC_BAD_REQUEST, PARENT_NOT_SHARED_ERROR_CODE, null);
        assertIdpNotSharedWithOrg(ORG_P1A, IDP_NAME);
    }

    @Test
    public void testUnshareIdpWithEmptyOrganizations() throws JSONException {

        Response response = getResponseOfPost(IDP_UNSHARE_PATH, new JSONObject()
                .put("identityProviderId", idpId)
                .put("orgIds", new JSONArray())
                .toString());
        assertErrorResponse(response, HttpStatus.SC_BAD_REQUEST, EMPTY_ORGANIZATIONS_ERROR_CODE, null);
    }

    @Test
    public void testGetSharedOrganizationsWithUnsupportedAttribute() throws JSONException {

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put(ATTRIBUTES_QUERY_PARAM, "roles");
        Response response = getResponseOfGet(IDP_API_BASE_PATH + "/" + idpId + SHARE_PATH, queryParams);
        assertErrorResponse(response, HttpStatus.SC_BAD_REQUEST, UNSUPPORTED_ATTRIBUTE_ERROR_CODE, null);
    }

    /**
     * Asserts that sharing is rejected because the given federated authenticator of the identity provider is not
     * shareable with child organizations.
     */
    private void assertAuthenticatorNotShareable(Response response, String authenticatorName) {

        assertErrorResponse(response, HttpStatus.SC_BAD_REQUEST, CONNECTION_NOT_SHAREABLE_ERROR_CODE, null);
        String description = response.jsonPath().getString("description");
        Assert.assertTrue(description != null && description.contains("'" + authenticatorName + "'"),
                "Sharing should be rejected for the authenticator: " + authenticatorName + ". Response: "
                        + response.asString());
    }
}
