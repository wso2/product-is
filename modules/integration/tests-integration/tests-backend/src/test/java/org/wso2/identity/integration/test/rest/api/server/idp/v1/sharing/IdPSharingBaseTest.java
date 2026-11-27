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

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.wso2.identity.integration.test.rest.api.server.common.RESTAPIServerTestBase;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

/**
 * Base class for the identity provider (connection) sharing integration tests.
 */
public class IdPSharingBaseTest extends RESTAPIServerTestBase {

    protected static final String API_DEFINITION_NAME = "idp.yaml";
    protected static final String API_VERSION = "v1";
    protected static final String API_PACKAGE_NAME = "org.wso2.carbon.identity.api.server.idp.v1";
    protected static final String AUTHORIZED_APIS_JSON = "idp-sharing-apis.json";

    protected static final String IDP_API_BASE_PATH = "/identity-providers";
    protected static final String IDP_SHARE_PATH = IDP_API_BASE_PATH + "/share";
    protected static final String IDP_SHARE_WITH_ALL_PATH = IDP_API_BASE_PATH + "/share-with-all";
    protected static final String IDP_UNSHARE_PATH = IDP_API_BASE_PATH + "/unshare";
    protected static final String IDP_UNSHARE_WITH_ALL_PATH = IDP_API_BASE_PATH + "/unshare-with-all";
    protected static final String SHARE_PATH = "/share";
    protected static final String FEDERATED_AUTHENTICATORS_PATH = "/federated-authenticators";
    protected static final String CLAIMS_PATH = "/claims";
    protected static final String JIT_PATH = "/provisioning/jit";

    protected static final String POLICY_ALL_EXISTING_AND_FUTURE_ORGS = "ALL_EXISTING_AND_FUTURE_ORGS";
    protected static final String POLICY_SELECTED_ORG_ONLY = "SELECTED_ORG_ONLY";
    protected static final String POLICY_SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN =
            "SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN";

    protected static final String OIDC_AUTHENTICATOR_ID = "T3BlbklEQ29ubmVjdEF1dGhlbnRpY2F0b3I";
    protected static final String OIDC_AUTHENTICATOR_NAME = "OpenIDConnectAuthenticator";
    protected static final String CLIENT_ID_PROPERTY = "ClientId";
    protected static final String CLIENT_SECRET_PROPERTY = "ClientSecret";

    protected static final String PROCESSING_STATUS = "Processing";
    protected static final String STATUS_FIELD = "status";
    protected static final String ORGANIZATIONS_FIELD = "organizations";
    protected static final String IDENTITY_PROVIDERS_FIELD = "identityProviders";
    protected static final String IS_SHARED_FIELD = "isShared";
    protected static final String ATTRIBUTES_QUERY_PARAM = "attributes";
    protected static final String RECURSIVE_QUERY_PARAM = "recursive";
    protected static final String SHARING_MODE_ATTRIBUTE = "sharingMode";

    private static final String ADD_ENTERPRISE_IDP_JSON = "add-enterprise-idp.json";
    private static final String IDP_NAME_PLACEHOLDER = "<IDP_NAME>";
    private static final String IDENTITY_PROVIDER_ID_FIELD = "identityProviderId";
    private static final String POLICY_FIELD = "policy";
    private static final String ORG_ID_FIELD = "orgId";
    private static final String ORG_IDS_FIELD = "orgIds";
    private static final long SHARING_TIMEOUT_SECONDS = 60;
    private static final long SHARING_POLL_INTERVAL_MILLIS = 200;

    protected static String swaggerDefinition;

    protected OrgMgtRestClient orgMgtRestClient;
    protected OAuth2RestClient oAuth2RestClient;

    // Organization name -> organization ID, kept in the creation order (parents before children).
    private final Map<String, String> organizationIds = new LinkedHashMap<>();
    // Organization ID -> parent organization ID (null for first level organizations).
    private final Map<String, String> parentOrganizationIds = new HashMap<>();
    // Organization ID -> switched access token of the organization.
    private final Map<String, String> switchedTokens = new HashMap<>();

    static {
        try {
            swaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (IOException e) {
            Assert.fail("Failed to load API definition: " + API_DEFINITION_NAME, e);
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void setBasePath() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void clearBasePath() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    protected void initRestClients() throws Exception {

        oAuth2RestClient = new OAuth2RestClient(serverURL, tenantInfo);
        orgMgtRestClient = new OrgMgtRestClient(context, tenantInfo, serverURL,
                new JSONObject(readResource(AUTHORIZED_APIS_JSON)));
    }

    protected void closeRestClients() {

        try {
            if (oAuth2RestClient != null) {
                oAuth2RestClient.closeHttpClient();
            }
        } catch (Exception e) {
            log.warn("Failed to close the OAuth2 REST client during cleanup.", e);
        }
        try {
            if (orgMgtRestClient != null) {
                orgMgtRestClient.closeHttpClient();
            }
        } catch (Exception e) {
            log.warn("Failed to close the organization management REST client during cleanup.", e);
        }
    }

    // ----------------------------------------- Organizations -----------------------------------------

    protected String addOrganization(String orgName) throws Exception {

        String orgId = orgMgtRestClient.addOrganization(orgName);
        organizationIds.put(orgName, orgId);
        parentOrganizationIds.put(orgId, null);
        return orgId;
    }

    protected String addSubOrganization(String orgName, String parentOrgName) throws Exception {

        String parentOrgId = getOrgId(parentOrgName);
        String orgId = orgMgtRestClient.addSubOrganization(orgName, parentOrgId);
        organizationIds.put(orgName, orgId);
        parentOrganizationIds.put(orgId, parentOrgId);
        return orgId;
    }

    protected String getOrgId(String orgName) {

        String orgId = organizationIds.get(orgName);
        Assert.assertNotNull(orgId, "Organization: " + orgName + " is not created by the test.");
        return orgId;
    }

    protected List<String> getOrgNames() {

        return new ArrayList<>(organizationIds.keySet());
    }

    protected String getSwitchedToken(String orgName) throws Exception {

        String orgId = getOrgId(orgName);
        String token = switchedTokens.get(orgId);
        if (token == null) {
            token = orgMgtRestClient.switchM2MToken(orgId);
            switchedTokens.put(orgId, token);
        }
        return token;
    }

    /**
     * Deletes the organizations created by the test, children before their parents.
     */
    protected void deleteOrganizations() {

        List<String> orgNames = new ArrayList<>(organizationIds.keySet());
        Collections.reverse(orgNames);
        for (String orgName : orgNames) {
            String orgId = organizationIds.get(orgName);
            String parentOrgId = parentOrganizationIds.get(orgId);
            try {
                if (parentOrgId == null) {
                    orgMgtRestClient.deleteOrganization(orgId);
                } else {
                    orgMgtRestClient.deleteSubOrganization(orgId, parentOrgId);
                }
            } catch (Exception e) {
                log.warn("Failed to delete the organization: " + orgName + " during cleanup.", e);
            }
        }
        organizationIds.clear();
        parentOrganizationIds.clear();
        switchedTokens.clear();
    }

    // ----------------------------------- Root organization identity providers -----------------------------------

    /**
     * Creates an enterprise OIDC identity provider in the root organization.
     *
     * @param idpName Name of the identity provider.
     * @return ID of the created identity provider.
     */
    protected String createEnterpriseIdp(String idpName) throws IOException {

        String body = readResource(ADD_ENTERPRISE_IDP_JSON).replace(IDP_NAME_PLACEHOLDER, idpName);
        return createRootIdp(body);
    }

    protected String createRootIdp(String body) {

        Response response = getResponseOfPost(IDP_API_BASE_PATH, body);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        String location = response.getHeader(HttpHeaders.LOCATION);
        Assert.assertNotNull(location, "Location header is not returned for the created identity provider.");
        return location.substring(location.lastIndexOf("/") + 1);
    }

    protected void deleteRootIdp(String idpId) {

        if (idpId == null) {
            return;
        }
        try {
            getResponseOfDelete(IDP_API_BASE_PATH + "/" + idpId);
        } catch (Exception e) {
            log.warn("Failed to delete the identity provider: " + idpId + " during cleanup.", e);
        }
    }

    // -------------------------------------------- Sharing APIs --------------------------------------------

    protected Response shareIdpWithAllOrgs(String idpId) throws JSONException {

        JSONObject body = new JSONObject()
                .put(IDENTITY_PROVIDER_ID_FIELD, idpId)
                .put(POLICY_FIELD, POLICY_ALL_EXISTING_AND_FUTURE_ORGS);
        return getResponseOfPost(IDP_SHARE_WITH_ALL_PATH, body.toString());
    }

    /**
     * Shares the identity provider with the given organizations.
     *
     * @param idpId          ID of the identity provider.
     * @param orgNamePolicies Organization name -> sharing policy.
     * @return Response of the share request.
     */
    protected Response shareIdpWithSelectedOrgs(String idpId, Map<String, String> orgNamePolicies)
            throws JSONException {

        JSONArray organizations = new JSONArray();
        for (Map.Entry<String, String> orgNamePolicy : orgNamePolicies.entrySet()) {
            organizations.put(new JSONObject()
                    .put(ORG_ID_FIELD, getOrgId(orgNamePolicy.getKey()))
                    .put(POLICY_FIELD, orgNamePolicy.getValue()));
        }
        return shareIdp(new JSONObject()
                .put(IDENTITY_PROVIDER_ID_FIELD, idpId)
                .put(ORGANIZATIONS_FIELD, organizations)
                .toString());
    }

    protected Response shareIdp(String body) {

        return getResponseOfPost(IDP_SHARE_PATH, body);
    }

    protected Response unshareIdpFromSelectedOrgs(String idpId, List<String> orgNames) throws JSONException {

        JSONArray orgIds = new JSONArray();
        for (String orgName : orgNames) {
            orgIds.put(getOrgId(orgName));
        }
        return getResponseOfPost(IDP_UNSHARE_PATH, new JSONObject()
                .put(IDENTITY_PROVIDER_ID_FIELD, idpId)
                .put(ORG_IDS_FIELD, orgIds)
                .toString());
    }

    protected Response unshareIdpFromAllOrgs(String idpId) throws JSONException {

        return getResponseOfPost(IDP_UNSHARE_WITH_ALL_PATH, new JSONObject()
                .put(IDENTITY_PROVIDER_ID_FIELD, idpId)
                .toString());
    }

    protected Response getSharedOrganizations(String idpId, boolean includeSharingMode) {

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put(RECURSIVE_QUERY_PARAM, true);
        if (includeSharingMode) {
            queryParams.put(ATTRIBUTES_QUERY_PARAM, SHARING_MODE_ATTRIBUTE);
        }
        return getResponseOfGet(IDP_API_BASE_PATH + "/" + idpId + SHARE_PATH, queryParams);
    }

    protected void assertSharingProcessTriggered(Response response) {

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body(STATUS_FIELD, equalTo(PROCESSING_STATUS));
    }

    // ------------------------------------ Shared identity providers in sub-organizations ------------------------------------

    protected String getSubOrgIdpApiBasePath() {

        if (SUPER_TENANT_DOMAIN_NAME.equals(tenant)) {
            return serverURL + "o/api/server/v1" + IDP_API_BASE_PATH;
        }
        return serverURL + "t/" + tenant + "/o/api/server/v1" + IDP_API_BASE_PATH;
    }

    protected String getSubOrgIdpPath(String idpId) {

        return getSubOrgIdpApiBasePath() + "/" + idpId;
    }

    /**
     * Returns the identity provider with the given name in the given organization, or null if not available.
     */
    protected JSONObject findIdpInOrg(String orgName, String idpName) throws Exception {

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("filter", "name eq " + idpName);
        Response response = getResponseOfGetWithOAuth2(getSubOrgIdpApiBasePath(), getSwitchedToken(orgName),
                queryParams);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_OK,
                "Failed to list the identity providers of the organization: " + orgName + ". Response: "
                        + response.asString());
        JSONArray idps = new JSONObject(response.asString()).optJSONArray(IDENTITY_PROVIDERS_FIELD);
        if (idps == null) {
            return null;
        }
        for (int i = 0; i < idps.length(); i++) {
            JSONObject idp = idps.getJSONObject(i);
            if (idpName.equals(idp.optString("name"))) {
                return idp;
            }
        }
        return null;
    }

    /**
     * Waits until the identity provider is shared with the given organization.
     *
     * @return ID of the shared identity provider in the organization.
     */
    protected String awaitIdpSharedWithOrg(String orgName, String idpName) {

        AtomicReference<String> sharedIdpId = new AtomicReference<>();
        await().atMost(SHARING_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .pollInterval(SHARING_POLL_INTERVAL_MILLIS, TimeUnit.MILLISECONDS)
                .ignoreExceptions()
                .until(() -> {
                    JSONObject idp = findIdpInOrg(orgName, idpName);
                    if (idp == null) {
                        return false;
                    }
                    sharedIdpId.set(idp.getString("id"));
                    return true;
                });
        return sharedIdpId.get();
    }

    /**
     * Waits until the identity provider is no longer shared with the given organization.
     */
    protected void awaitIdpUnsharedFromOrg(String orgName, String idpName) {

        await().atMost(SHARING_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .pollInterval(SHARING_POLL_INTERVAL_MILLIS, TimeUnit.MILLISECONDS)
                .ignoreExceptions()
                .until(() -> findIdpInOrg(orgName, idpName) == null);
    }

    protected void assertIdpNotSharedWithOrg(String orgName, String idpName) throws Exception {

        Assert.assertNull(findIdpInOrg(orgName, idpName),
                "Identity provider: " + idpName + " should not be shared with the organization: " + orgName);
    }

    // --------------------------------------------- Assertions ---------------------------------------------

    /**
     * Asserts an error response by the HTTP status and the error code, and optionally the error message.
     */
    protected void assertErrorResponse(Response response, int httpStatus, String errorCode, String message) {

        Assert.assertEquals(response.getStatusCode(), httpStatus, "Unexpected HTTP status. Response: "
                + response.asString());
        Assert.assertEquals(response.jsonPath().getString("code"), errorCode,
                "Unexpected error code. Response: " + response.asString());
        if (message != null) {
            Assert.assertEquals(response.jsonPath().getString("message"), message,
                    "Unexpected error message. Response: " + response.asString());
        }
    }
}
