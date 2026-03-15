/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.constant;

/**
 * Constants for the V2 organization user sharing integration tests.
 */
public class UserSharingConstants {

    // -------------------------------------------------------------------------
    // API Metadata
    // -------------------------------------------------------------------------

    public static final String API_DEFINITION_NAME = "organization-user-share-v2.yaml";
    public static final String AUTHORIZED_APIS_JSON = "user-sharing-apis-v2.json";
    public static final String API_VERSION = "v2";
    public static final String API_PACKAGE_NAME =
            "org.wso2.carbon.identity.api.server.organization.user.sharing.management.v2";

    // -------------------------------------------------------------------------
    // API Base Paths
    // -------------------------------------------------------------------------

    public static final String API_SERVER_BASE_PATH = "/api/server";
    public static final String API_SERVER_V1_BASE_PATH = "/api/server/v1";
    public static final String API_SERVER_V2_BASE_PATH = "/api/server/v2";
    public static final String ORGANIZATION_API_PATH = "/o";
    public static final String USER_SHARING_API_BASE_PATH = "/users";

    // -------------------------------------------------------------------------
    // User Sharing Endpoint Paths
    // -------------------------------------------------------------------------

    public static final String SHARE_PATH = "/share";
    public static final String SHARE_WITH_ALL_PATH = "/share-with-all";
    public static final String UNSHARE_PATH = "/unshare";
    public static final String UNSHARE_WITH_ALL_PATH = "/unshare-with-all";
    public static final String SHARED_ORGANIZATIONS_PATH = "/share";
    public static final String PATCH_SHARED_ORGANIZATIONS_PATH = "/share";

    // -------------------------------------------------------------------------
    // Path / Query Param Utilities
    // -------------------------------------------------------------------------

    public static final String PATH_SEPARATOR = "/";
    public static final String QUERY_PARAM_SEPARATOR = "?";
    public static final String QUERY_PARAM_VALUE_SEPARATOR = "=";
    public static final String UNDERSCORE = "_";

    // -------------------------------------------------------------------------
    // HTTP Headers
    // -------------------------------------------------------------------------

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_AUTHORIZATION_VALUE_BEARER = "Bearer ";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    // -------------------------------------------------------------------------
    // Shared Type Values
    // -------------------------------------------------------------------------

    public static final String SHARED_TYPE_SHARED = "SHARED";
    public static final String SHARED_TYPE_OWNER = "OWNER";
    public static final String SHARED_TYPE_INVITED = "INVITED";

    // -------------------------------------------------------------------------
    // Query Parameters
    // -------------------------------------------------------------------------

    public static final String PATH_PARAM_USER_ID = "userId";
    public static final String QUERY_PARAM_LIMIT = "limit";
    public static final String QUERY_PARAM_AFTER = "after";
    public static final String QUERY_PARAM_BEFORE = "before";
    public static final String QUERY_PARAM_FILTER = "filter";
    public static final String QUERY_PARAM_RECURSIVE = "recursive";

    /**
     * The "attributes" query parameter controls which optional fields are included in the GET response.
     * Accepted values: {@link #ATTRIBUTE_ROLES}, {@link #ATTRIBUTE_SHARING_MODE}, or both.
     */
    public static final String QUERY_PARAM_ATTRIBUTES = "attributes";

    // -------------------------------------------------------------------------
    // Attribute Values for GET ?attributes=
    // -------------------------------------------------------------------------

    /** Includes per-org roles[] in the GET /shared-organizations response. */
    public static final String ATTRIBUTE_ROLES = "roles";

    /**
     * Includes sharingMode (per-org for WITH_CHILDREN policy; top-level for general policies)
     * in the GET /shared-organizations response.
     */
    public static final String ATTRIBUTE_SHARING_MODE = "sharingMode";

    // -------------------------------------------------------------------------
    // Error Codes
    // -------------------------------------------------------------------------

    public static final String ERROR_CODE_BAD_REQUEST = "UE-10000";
    public static final String ERROR_CODE_INVALID_PAGINATION_CURSOR = "ORG-60026";
    public static final String ERROR_CODE_SERVER_ERROR = "SE-50000";

    // -------------------------------------------------------------------------
    // Organization Names
    // -------------------------------------------------------------------------

    public static final String ROOT_ORG_NAME = "Super";
    public static final String L1_ORG_1_NAME = "L1 - Organization 1";
    public static final String L1_ORG_2_NAME = "L1 - Organization 2";
    public static final String L1_ORG_3_NAME = "L1 - Organization 3";
    public static final String L2_ORG_1_NAME = "L2 - Organization 1";
    public static final String L2_ORG_2_NAME = "L2 - Organization 2";
    public static final String L2_ORG_3_NAME = "L2 - Organization 3";
    public static final String L3_ORG_1_NAME = "L3 - Organization 1";

    public static final String ROOT_ORG_ID = "10084a8d-113f-4211-a0d5-efe36b082211";

    // -------------------------------------------------------------------------
    // Application Names
    // -------------------------------------------------------------------------

    public static final String APP_1_NAME = "App 1";
    public static final String APP_2_NAME = "App 2";

    // -------------------------------------------------------------------------
    // Audience Types
    // -------------------------------------------------------------------------

    public static final String APPLICATION_AUDIENCE = "application";
    public static final String ORGANIZATION_AUDIENCE = "organization";

    // -------------------------------------------------------------------------
    // Role Names
    // -------------------------------------------------------------------------

    public static final String APP_ROLE_1 = "app-role-1";
    public static final String APP_ROLE_2 = "app-role-2";
    public static final String APP_ROLE_3 = "app-role-3";
    public static final String ORG_ROLE_1 = "org-role-1";
    public static final String ORG_ROLE_2 = "org-role-2";
    public static final String ORG_ROLE_3 = "org-role-3";

    // -------------------------------------------------------------------------
    // User Domain
    // -------------------------------------------------------------------------

    public static final String USER_DOMAIN_PRIMARY = "PRIMARY";

    // -------------------------------------------------------------------------
    // User Names
    // -------------------------------------------------------------------------

    public static final String ROOT_ORG_USER_1_USERNAME = "rootUser1";
    public static final String ROOT_ORG_USER_2_USERNAME = "rootUser2";
    public static final String ROOT_ORG_USER_3_USERNAME = "rootUser3";
    public static final String L1_ORG_1_USER_1_USERNAME = "l1Org1User1";
    public static final String L1_ORG_1_USER_2_USERNAME = "l1Org1User2";
    public static final String L1_ORG_1_USER_3_USERNAME = "l1Org1User3";
    public static final String ROOT_ORG_USER_DUPLICATED_USERNAME = "rootUserDuplicated";

    // -------------------------------------------------------------------------
    // Map Keys — Selective Share Input
    // -------------------------------------------------------------------------

    public static final String MAP_KEY_SELECTIVE_ORG_ID = "orgId";
    public static final String MAP_KEY_SELECTIVE_ORG_NAME = "orgName";
    public static final String MAP_KEY_SELECTIVE_POLICY = "selectivePolicy";

    /**
     * Key for the {@link org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.RoleAssignments}
     * object in a selective share organization details map.
     * Replaces the flat role list used in V1 (MAP_KEY_SELECTIVE_ROLES).
     */
    public static final String MAP_KEY_SELECTIVE_ROLE_ASSIGNMENTS = "selectiveRoleAssignments";

    // -------------------------------------------------------------------------
    // Map Keys — General Share Input
    // -------------------------------------------------------------------------

    public static final String MAP_KEY_GENERAL_POLICY = "generalPolicy";

    /**
     * Key for the {@link org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.RoleAssignments}
     * object in a general share policy map.
     * Replaces the flat role list used in V1 (MAP_KEY_GENERAL_ROLES).
     */
    public static final String MAP_KEY_GENERAL_ROLE_ASSIGNMENTS = "generalRoleAssignments";

    // -------------------------------------------------------------------------
    // Map Keys — Expected GET Results
    // -------------------------------------------------------------------------

    public static final String MAP_KEY_EXPECTED_ORG_COUNT = "expectedOrgCount";
    public static final String MAP_KEY_EXPECTED_ORG_IDS = "expectedOrgIds";
    public static final String MAP_KEY_EXPECTED_ORG_NAMES = "expectedOrgNames";

    /**
     * Key for {@code Map<String, List<RoleWithAudience>>} where the map key is the orgId.
     * Every expected org must have an entry; use an empty list for orgs with no roles.
     */
    public static final String MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG = "expectedRolesPerExpectedOrg";

    /**
     * Key for {@code Map<String, ExpectedSharingMode>} where the map key is the orgId.
     * A {@code null} value for an orgId key means the per-org sharingMode field must be absent.
     * The outer map itself may be {@code null} when no per-org sharingMode is expected for any org.
     */
    public static final String MAP_KEY_EXPECTED_PER_ORG_SHARING_MODE = "expectedPerOrgSharingMode";

    /**
     * Key for an {@link org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model.ExpectedSharingMode}
     * object describing the top-level sharingMode in the GET response.
     * {@code null} means the top-level sharingMode field must be absent.
     */
    public static final String MAP_KEY_EXPECTED_TOP_LEVEL_SHARING_MODE = "expectedTopLevelSharingMode";

    // -------------------------------------------------------------------------
    // Map Keys — Org Details
    // -------------------------------------------------------------------------

    public static final String MAP_ORG_DETAILS_KEY_ORG_NAME = "orgName";
    public static final String MAP_ORG_DETAILS_KEY_ORG_ID = "orgId";
    public static final String MAP_ORG_DETAILS_KEY_PARENT_ORG_ID = "parentOrgId";
    public static final String MAP_ORG_DETAILS_KEY_ORG_SWITCH_TOKEN = "orgSwitchToken";
    public static final String MAP_ORG_DETAILS_KEY_ORG_LEVEL = "orgLevel";

    // -------------------------------------------------------------------------
    // Map Keys — App Details
    // -------------------------------------------------------------------------

    public static final String MAP_APP_DETAILS_KEY_APP_NAME = "appName";
    public static final String MAP_APP_DETAILS_KEY_APP_ID = "appId";
    public static final String MAP_APP_DETAILS_KEY_APP_AUDIENCE = "appAudience";
    public static final String MAP_APP_DETAILS_KEY_CLIENT_ID = "clientId";
    public static final String MAP_APP_DETAILS_KEY_CLIENT_SECRET = "clientSecret";
    public static final String MAP_APP_DETAILS_KEY_ROLE_NAMES = "roleNames";
    public static final String MAP_APP_DETAILS_KEY_ROLE_IDS_BY_NAME = "roleIdsByName";
    public static final String MAP_APP_DETAILS_KEY_APP_DETAILS_OF_SUB_ORGS = "appDetailsOfSubOrgs";
    public static final String MAP_APP_DETAILS_KEY_APP_SUB_ORG_NAME = "subOrgName";

    // -------------------------------------------------------------------------
    // Map Keys — User Details
    // -------------------------------------------------------------------------

    public static final String MAP_USER_DETAILS_KEY_DOMAIN_QUALIFIED_USER_NAME = "domainQualifiedUserName";
    public static final String MAP_USER_DETAILS_KEY_USER_NAME = "userName";
    public static final String MAP_USER_DETAILS_KEY_USER_ID = "userId";
    public static final String MAP_USER_DETAILS_KEY_USER_DOMAIN = "userDomain";
    public static final String MAP_USER_DETAILS_KEY_USER_ORG_NAME = "userOrgName";
    public static final String MAP_USER_DETAILS_KEY_USER_ORG_ID = "userOrgId";
    public static final String MAP_USER_DETAILS_KEY_USER_ORG_LEVEL = "userOrgLevel";
    public static final String MAP_USER_DETAILS_KEY_IS_ROOT_ORG_USER = "isRootOrgUser";

    public static final String MAP_USER_DOMAIN_QUALIFIED_USER_NAME_USER_NAME = "userName";
    public static final String MAP_USER_DOMAIN_QUALIFIED_USER_NAME_USER_DOMAIN = "userDomain";

    // -------------------------------------------------------------------------
    // OAuth2 Scopes
    // -------------------------------------------------------------------------

    public static final String SCOPE_INTERNAL_USER_SHARE = "internal_user_share";
    public static final String SCOPE_INTERNAL_USER_UNSHARE = "internal_user_unshare";
    public static final String SCOPE_INTERNAL_USER_SHARED_ACCESS_VIEW = "internal_user_shared_access_view";
    public static final String SCOPE_INTERNAL_ORG_USER_SHARE = "internal_org_user_share";
    public static final String SCOPE_INTERNAL_ORG_USER_UNSHARE = "internal_org_user_unshare";
    public static final String SCOPE_INTERNAL_ORG_USER_SHARED_ACCESS_VIEW = "internal_org_user_shared_access_view";

    // -------------------------------------------------------------------------
    // OAuth2 Grant Types
    // -------------------------------------------------------------------------

    public static final String GRANT_AUTHORIZATION_CODE = "authorization_code";
    public static final String GRANT_IMPLICIT = "implicit";
    public static final String GRANT_PASSWORD = "password";
    public static final String GRANT_CLIENT_CREDENTIALS = "client_credentials";
    public static final String GRANT_REFRESH_TOKEN = "refresh_token";
    public static final String GRANT_ORGANIZATION_SWITCH = "organization_switch";

    // -------------------------------------------------------------------------
    // Claim URIs
    // -------------------------------------------------------------------------

    public static final String CLAIM_EMAIL_URI = "http://wso2.org/claims/emailaddress";
    public static final String CLAIM_COUNTRY_URI = "http://wso2.org/claims/country";
    public static final String CLAIM_ROLES_URI = "http://wso2.org/claims/roles";
    public static final String CLAIM_GROUPS_URI = "http://wso2.org/claims/groups";

    // -------------------------------------------------------------------------
    // User Attributes
    // -------------------------------------------------------------------------

    public static final String ATTRIBUTE_USER_PASSWORD = "Admin123";
    public static final String ATTRIBUTE_USER_EMAIL_DOMAIN = "@gmail.com";
    public static final String ATTRIBUTE_USER_SCHEMA_SCIM2_USER = "urn:ietf:params:scim:schemas:core:2.0:User";

    // -------------------------------------------------------------------------
    // POST Response Field Names and Expected Values
    // -------------------------------------------------------------------------

    public static final String RESPONSE_STATUS = "status";
    public static final String RESPONSE_DETAILS = "details";
    public static final String RESPONSE_STATUS_VALUE = "Processing";
    public static final String RESPONSE_DETAIL_VALUE_SHARING = "User sharing process triggered successfully.";
    public static final String RESPONSE_DETAIL_VALUE_UNSHARING = "User unsharing process triggered successfully.";
    public static final String RESPONSE_DETAIL_VALUE_PATCH = "Shared user attributes patch process triggered successfully.";

    // -------------------------------------------------------------------------
    // GET Response — Top-Level Fields
    // -------------------------------------------------------------------------

    /** Path to the organizations list in the GET /shared-organizations response. */
    public static final String RESPONSE_ORGANIZATIONS = "organizations";

    /** Path to organizations list size. */
    public static final String RESPONSE_ORGANIZATIONS_SIZE = "organizations.size()";

    /** Path to the flat list of orgId values across all organizations entries. */
    public static final String RESPONSE_ORGANIZATIONS_ORG_ID = "organizations.orgId";

    /** Path to the flat list of orgName values across all organizations entries. */
    public static final String RESPONSE_ORGANIZATIONS_ORG_NAME = "organizations.orgName";

    /** Path to the flat list of sharedUserId values across all organizations entries. */
    public static final String RESPONSE_ORGANIZATIONS_SHARED_USER_ID = "organizations.sharedUserId";

    /** Path to the flat list of sharedType values across all organizations entries. */
    public static final String RESPONSE_ORGANIZATIONS_SHARED_TYPE = "organizations.sharedType";

    // -------------------------------------------------------------------------
    // GET Response — Top-Level sharingMode (general share only)
    // -------------------------------------------------------------------------

    /** Present when attributes includes sharingMode AND policy is ALL_EXISTING_AND_FUTURE_ORGS. */
    public static final String RESPONSE_TOP_LEVEL_SHARING_MODE = "sharingMode";
    public static final String RESPONSE_TOP_LEVEL_SHARING_MODE_POLICY = "sharingMode.policy";
    public static final String RESPONSE_TOP_LEVEL_SHARING_MODE_RA_MODE = "sharingMode.roleAssignment.mode";

    /**
     * Present when mode = SELECTED. Absent (null in assertion) when mode = NONE.
     * Assert with {@code nullValue()} when mode is NONE — do NOT assert {@code equalTo(emptyList())}.
     */
    public static final String RESPONSE_TOP_LEVEL_SHARING_MODE_RA_ROLES = "sharingMode.roleAssignment.roles";

    // -------------------------------------------------------------------------
    // GET Response — Per-Org Fields (Groovy finder format strings, use String.format)
    // -------------------------------------------------------------------------

    /**
     * Groovy path to a single org entry by orgId. %s = orgId.
     * Not typically used directly; use the more specific paths below.
     */
    public static final String RESPONSE_PER_ORG_ENTRY_FORMAT =
            "organizations.find { it.orgId == '%s' }";

    /** Per-org roles list. %s = orgId. Present only when attributes includes "roles". */
    public static final String RESPONSE_PER_ORG_ROLES_FORMAT =
            "organizations.find { it.orgId == '%s' }.roles";

    /** Size of per-org roles list. %s = orgId. */
    public static final String RESPONSE_PER_ORG_ROLES_SIZE_FORMAT =
            "organizations.find { it.orgId == '%s' }.roles.size()";

    /** Per-org role display names. %s = orgId. */
    public static final String RESPONSE_PER_ORG_ROLES_DISPLAY_NAME_FORMAT =
            "organizations.find { it.orgId == '%s' }.roles.displayName";

    /** Per-org role audience display names. %s = orgId. */
    public static final String RESPONSE_PER_ORG_ROLES_AUDIENCE_DISPLAY_FORMAT =
            "organizations.find { it.orgId == '%s' }.roles.audience.display";

    /** Per-org role audience types. %s = orgId. */
    public static final String RESPONSE_PER_ORG_ROLES_AUDIENCE_TYPE_FORMAT =
            "organizations.find { it.orgId == '%s' }.roles.audience.type";

    /**
     * Per-org sharingMode. %s = orgId.
     * Present only when attributes includes "sharingMode" AND this org is the policyHoldingOrgId
     * of a SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN policy.
     * Assert with {@code nullValue()} for all other orgs.
     */
    public static final String RESPONSE_PER_ORG_SHARING_MODE_FORMAT =
            "organizations.find { it.orgId == '%s' }.sharingMode";

    /** Per-org sharingMode.policy. %s = orgId. */
    public static final String RESPONSE_PER_ORG_SHARING_MODE_POLICY_FORMAT =
            "organizations.find { it.orgId == '%s' }.sharingMode.policy";

    /** Per-org sharingMode.roleAssignment.mode. %s = orgId. */
    public static final String RESPONSE_PER_ORG_SHARING_MODE_RA_MODE_FORMAT =
            "organizations.find { it.orgId == '%s' }.sharingMode.roleAssignment.mode";

    /**
     * Per-org sharingMode.roleAssignment.roles. %s = orgId.
     * Assert with {@code nullValue()} when mode = NONE.
     */
    public static final String RESPONSE_PER_ORG_SHARING_MODE_RA_ROLES_FORMAT =
            "organizations.find { it.orgId == '%s' }.sharingMode.roleAssignment.roles";

    // -------------------------------------------------------------------------
    // GET Response — links[]
    // -------------------------------------------------------------------------

    public static final String RESPONSE_LINKS_SIZE = "links.size()";
    public static final String RESPONSE_LINKS_EMPTY = "links[0].isEmpty()";

    // -------------------------------------------------------------------------
    // PATCH Path Format
    // -------------------------------------------------------------------------

    /**
     * Format string for the PATCH path value targeting a specific org's roles.
     * Usage: {@code String.format(PATCH_PATH_ORG_ROLES_FORMAT, orgId)}
     */
    public static final String PATCH_PATH_ORG_ROLES_FORMAT = "organizations[orgId eq %s].roles";

    // -------------------------------------------------------------------------
    // JSON Path — sharedUserId lookup by org name
    // -------------------------------------------------------------------------

    public static final String SHARED_USER_ID_JSON_PATH =
            "organizations.find { it.orgName == '%s' }.sharedUserId";

    // -------------------------------------------------------------------------
    // Misc
    // -------------------------------------------------------------------------

    public static final String ERROR_SETUP_SWAGGER_DEFINITION = "Unable to read the swagger definition %s from %s";
}
