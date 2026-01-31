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
 * Constants for organization user sharing V2 API.
 */
public class UserSharingConstants {

    public static final String API_DEFINITION_NAME = "organization-user-share-v2.yaml";
    public static final String AUTHORIZED_APIS_JSON = "user-sharing-apis.json";
    public static final String API_VERSION = "v2";
    public static final String API_PACKAGE_NAME =
            "org.wso2.carbon.identity.api.server.organization.user.sharing.management.v2";

    public static final String API_SERVER_V2_BASE_PATH = "/api/server/v2";
    public static final String ORGANIZATION_API_PATH = "/o";
    public static final String USER_SHARING_API_BASE_PATH = "/users";
    
    // V2 Endpoint Paths
    public static final String SELECTIVE_SHARE_PATH = "/share";
    public static final String GENERAL_SHARE_PATH = "/share-with-all";
    public static final String SELECTIVE_UNSHARE_PATH = "/unshare";
    public static final String GENERAL_UNSHARE_PATH = "/unshare-with-all";
    public static final String SHARE_PATCH_PATH = "/share";
    public static final String SHARED_ORGANIZATIONS_PATH = "/shared-organizations";
    public static final String SHARED_ROLES_PATH = "/shared-roles";

    // Aliases for backward compatibility with V1-style naming
    public static final String SHARE_PATH = SELECTIVE_SHARE_PATH;
    public static final String SHARE_WITH_ALL_PATH = GENERAL_SHARE_PATH;
    public static final String UNSHARE_PATH = SELECTIVE_UNSHARE_PATH;
    public static final String UNSHARE_WITH_ALL_PATH = GENERAL_UNSHARE_PATH;

    public static final String PATH_SEPARATOR = "/";
    public static final String QUERY_PARAM_SEPARATOR = "?";
    public static final String QUERY_PARAM_VALUE_SEPARATOR = "=";
    public static final String QUERY_PARAM_AMPERSAND = "&";
    public static final String UNDERSCORE = "_";

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_AUTHORIZATION_VALUE_BEARER = "Bearer ";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    public static final String SHARED_TYPE_SHARED = "SHARED";
    public static final String SHARED_TYPE_OWNER = "OWNER";
    public static final String SHARED_TYPE_INVITED = "INVITED";

    public static final String PATH_PARAM_USER_ID = "userId";
    public static final String QUERY_PARAM_ORG_ID = "orgId";
    public static final String QUERY_PARAM_LIMIT = "limit";
    public static final String QUERY_PARAM_AFTER = "after";
    public static final String QUERY_PARAM_BEFORE = "before";
    public static final String QUERY_PARAM_FILTER = "filter";
    public static final String QUERY_PARAM_RECURSIVE = "recursive";
    public static final String QUERY_PARAM_ATTRIBUTES = "attributes";
    public static final String QUERY_PARAM_ATTRIBUTES_ROLES = "roles";
    public static final String QUERY_PARAM_ATTRIBUTES_SHARING_MODE = "sharingMode";

    public static final String ERROR_CODE_BAD_REQUEST = "UE-10000";
    public static final String ERROR_CODE_INVALID_PAGINATION_CURSOR = "ORG-60026";
    public static final String ERROR_CODE_SERVER_ERROR = "SE-50000";
    public static final String ERROR_CODE_NOT_FOUND = "US-60001";

    public static final String ROOT_ORG_NAME = "Super";
    public static final String L1_ORG_1_NAME = "L1 - Organization 1";
    public static final String L1_ORG_2_NAME = "L1 - Organization 2";
    public static final String L1_ORG_3_NAME = "L1 - Organization 3";
    public static final String L2_ORG_1_NAME = "L2 - Organization 1";
    public static final String L2_ORG_2_NAME = "L2 - Organization 2";
    public static final String L2_ORG_3_NAME = "L2 - Organization 3";
    public static final String L3_ORG_1_NAME = "L3 - Organization 1";

    public static final String ROOT_ORG_ID = "10084a8d-113f-4211-a0d5-efe36b082211";

    public static final String APP_1_NAME = "App 1";
    public static final String APP_2_NAME = "App 2";

    public static final String APPLICATION_AUDIENCE = "application";
    public static final String ORGANIZATION_AUDIENCE = "organization";

    public static final String APP_ROLE_1 = "app-role-1";
    public static final String APP_ROLE_2 = "app-role-2";
    public static final String APP_ROLE_3 = "app-role-3";
    public static final String ORG_ROLE_1 = "org-role-1";
    public static final String ORG_ROLE_2 = "org-role-2";
    public static final String ORG_ROLE_3 = "org-role-3";

    public static final String USER_DOMAIN_PRIMARY = "PRIMARY";

    public static final String ROOT_ORG_USER_1_USERNAME = "rootUser1V2";
    public static final String ROOT_ORG_USER_2_USERNAME = "rootUser2V2";
    public static final String ROOT_ORG_USER_3_USERNAME = "rootUser3V2";
    public static final String L1_ORG_1_USER_1_USERNAME = "l1Org1User1V2";
    public static final String L1_ORG_1_USER_2_USERNAME = "l1Org1User2V2";
    public static final String L1_ORG_1_USER_3_USERNAME = "l1Org1User3V2";
    public static final String ROOT_ORG_USER_DUPLICATED_USERNAME = "rootUserDuplicatedV2";

    // Role Assignment Modes
    public static final String ROLE_ASSIGNMENT_MODE_NONE = "NONE";
    public static final String ROLE_ASSIGNMENT_MODE_SELECTED = "SELECTED";

    // Selective Sharing Policies
    public static final String POLICY_SELECTED_ORG_ONLY = "SELECTED_ORG_ONLY";
    public static final String POLICY_SELECTED_ORG_WITH_ALL_EXISTING_CHILDREN_ONLY =
            "SELECTED_ORG_WITH_ALL_EXISTING_CHILDREN_ONLY";
    public static final String POLICY_SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN =
            "SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN";
    public static final String POLICY_SELECTED_ORG_WITH_EXISTING_IMMEDIATE_CHILDREN_ONLY =
            "SELECTED_ORG_WITH_EXISTING_IMMEDIATE_CHILDREN_ONLY";
    public static final String POLICY_SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN =
            "SELECTED_ORG_WITH_EXISTING_IMMEDIATE_AND_FUTURE_CHILDREN";

    // General Sharing Policies
    public static final String POLICY_ALL_EXISTING_ORGS_ONLY = "ALL_EXISTING_ORGS_ONLY";
    public static final String POLICY_ALL_EXISTING_AND_FUTURE_ORGS = "ALL_EXISTING_AND_FUTURE_ORGS";
    public static final String POLICY_IMMEDIATE_EXISTING_ORGS_ONLY = "IMMEDIATE_EXISTING_ORGS_ONLY";
    public static final String POLICY_IMMEDIATE_EXISTING_AND_FUTURE_ORGS = "IMMEDIATE_EXISTING_AND_FUTURE_ORGS";

    // PATCH Operations
    public static final String PATCH_OP_ADD = "add";
    public static final String PATCH_OP_REMOVE = "remove";
    public static final String PATCH_PATH_TEMPLATE = "organizations[orgId eq %s].roles";

    // Map keys for test data
    public static final String MAP_KEY_SELECTIVE_ORG_ID = "orgId";
    public static final String MAP_KEY_SELECTIVE_ORG_NAME = "orgName";
    public static final String MAP_KEY_SELECTIVE_POLICY = "selectivePolicy";
    public static final String MAP_KEY_SELECTIVE_ROLE_ASSIGNMENT = "selectiveRoleAssignment";

    public static final String MAP_KEY_GENERAL_POLICY = "generalPolicy";
    public static final String MAP_KEY_GENERAL_ROLE_ASSIGNMENT = "generalRoleAssignment";

    public static final String MAP_KEY_EXPECTED_ORG_COUNT = "expectedOrgCount";
    public static final String MAP_KEY_EXPECTED_ORG_IDS = "expectedOrgIds";
    public static final String MAP_KEY_EXPECTED_ORG_NAMES = "expectedOrgNames";
    public static final String MAP_KEY_EXPECTED_ROLES_PER_EXPECTED_ORG = "expectedRolesPerExpectedOrg";
    public static final String MAP_KEY_EXPECTED_SHARING_MODE_PER_ORG = "expectedSharingModePerOrg";

    public static final String MAP_ORG_DETAILS_KEY_ORG_NAME = "orgName";
    public static final String MAP_ORG_DETAILS_KEY_ORG_ID = "orgId";
    public static final String MAP_ORG_DETAILS_KEY_PARENT_ORG_ID = "parentOrgId";
    public static final String MAP_ORG_DETAILS_KEY_ORG_SWITCH_TOKEN = "orgSwitchToken";
    public static final String MAP_ORG_DETAILS_KEY_ORG_LEVEL = "orgLevel";

    public static final String MAP_APP_DETAILS_KEY_APP_NAME = "appName";
    public static final String MAP_APP_DETAILS_KEY_APP_ID = "appId";
    public static final String MAP_APP_DETAILS_KEY_APP_AUDIENCE = "appAudience";
    public static final String MAP_APP_DETAILS_KEY_CLIENT_ID = "clientId";
    public static final String MAP_APP_DETAILS_KEY_CLIENT_SECRET = "clientSecret";
    public static final String MAP_APP_DETAILS_KEY_ROLE_NAMES = "roleNames";
    public static final String MAP_APP_DETAILS_KEY_ROLE_IDS_BY_NAME = "roleIdsByName";
    public static final String MAP_APP_DETAILS_KEY_APP_DETAILS_OF_SUB_ORGS = "appDetailsOfSubOrgs";
    public static final String MAP_APP_DETAILS_KEY_APP_SUB_ORG_NAME = "subOrgName";

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

    public static final String SCOPE_INTERNAL_USER_SHARE = "internal_user_share";
    public static final String SCOPE_INTERNAL_USER_UNSHARE = "internal_user_unshare";
    public static final String SCOPE_INTERNAL_USER_SHARED_ACCESS_VIEW = "internal_user_shared_access_view";
    public static final String SCOPE_INTERNAL_ORG_USER_SHARE = "internal_org_user_share";
    public static final String SCOPE_INTERNAL_ORG_USER_UNSHARE = "internal_org_user_unshare";
    public static final String SCOPE_INTERNAL_ORG_USER_SHARED_ACCESS_VIEW = "internal_org_user_shared_access_view";

    public static final String GRANT_AUTHORIZATION_CODE = "authorization_code";
    public static final String GRANT_IMPLICIT = "implicit";
    public static final String GRANT_PASSWORD = "password";
    public static final String GRANT_CLIENT_CREDENTIALS = "client_credentials";
    public static final String GRANT_REFRESH_TOKEN = "refresh_token";
    public static final String GRANT_ORGANIZATION_SWITCH = "organization_switch";

    public static final String CLAIM_EMAIL_URI = "http://wso2.org/claims/emailaddress";
    public static final String CLAIM_COUNTRY_URI = "http://wso2.org/claims/country";
    public static final String CLAIM_ROLES_URI = "http://wso2.org/claims/roles";
    public static final String CLAIM_GROUPS_URI = "http://wso2.org/claims/groups";

    public static final String ATTRIBUTE_USER_PASSWORD = "Admin123";
    public static final String ATTRIBUTE_USER_EMAIL_DOMAIN = "@gmail.com";
    public static final String ATTRIBUTE_USER_SCHEMA_SCIM2_USER = "urn:ietf:params:scim:schemas:core:2.0:User";

    // Response fields
    public static final String RESPONSE_STATUS = "status";
    public static final String RESPONSE_DETAILS = "details";
    public static final String RESPONSE_STATUS_VALUE = "Processing";
    public static final String RESPONSE_DETAIL_VALUE_SHARING = "User sharing process triggered successfully.";
    public static final String RESPONSE_DETAIL_VALUE_UNSHARING = "User unsharing process triggered successfully.";
    public static final String RESPONSE_DETAIL_VALUE_PATCH = "Role assignment patch operation processed successfully.";
    public static final String RESPONSE_DETAIL_VALUE_UPDATING = "Role assignment update process triggered successfully.";

    // GET Response pagination fields
    public static final String RESPONSE_TOTAL_RESULTS = "totalResults";
    public static final String RESPONSE_START_INDEX = "startIndex";
    public static final String RESPONSE_COUNT = "count";
    public static final String RESPONSE_NEXT_CURSOR = "nextCursor";
    public static final String RESPONSE_PREVIOUS_CURSOR = "previousCursor";

    // Default pagination values
    public static final int DEFAULT_LIMIT = 15;
    public static final int MAX_LIMIT = 100;

    // V2 Response JSON paths
    public static final String RESPONSE_LINKS = "links";
    public static final String RESPONSE_LINKS_SIZE = "links.size()";
    public static final String RESPONSE_SHARING_MODE = "sharingMode";
    public static final String RESPONSE_SHARING_MODE_POLICY = "sharingMode.policy";
    public static final String RESPONSE_SHARING_MODE_ROLE_ASSIGNMENT = "sharingMode.roleAssignment";
    public static final String RESPONSE_SHARING_MODE_ROLE_ASSIGNMENT_MODE = "sharingMode.roleAssignment.mode";
    public static final String RESPONSE_SHARING_MODE_ROLE_ASSIGNMENT_ROLES = "sharingMode.roleAssignment.roles";
    
    public static final String RESPONSE_ORGANIZATIONS = "organizations";
    public static final String RESPONSE_ORGANIZATIONS_SIZE = "organizations.size()";
    public static final String RESPONSE_ORGANIZATIONS_ORG_ID = "organizations.orgId";
    public static final String RESPONSE_ORGANIZATIONS_ORG_NAME = "organizations.orgName";
    public static final String RESPONSE_ORGANIZATIONS_ORG_HANDLE = "organizations.orgHandle";
    public static final String RESPONSE_ORGANIZATIONS_PARENT_ORG_ID = "organizations.parentOrgId";
    public static final String RESPONSE_ORGANIZATIONS_ORG_STATUS = "organizations.orgStatus";
    public static final String RESPONSE_ORGANIZATIONS_ORG_REF = "organizations.orgRef";
    public static final String RESPONSE_ORGANIZATIONS_HAS_CHILDREN = "organizations.hasChildren";
    public static final String RESPONSE_ORGANIZATIONS_DEPTH_FROM_ROOT = "organizations.depthFromRoot";
    public static final String RESPONSE_ORGANIZATIONS_SHARED_USER_ID = "organizations.sharedUserId";
    public static final String RESPONSE_ORGANIZATIONS_SHARED_TYPE = "organizations.sharedType";
    public static final String RESPONSE_ORGANIZATIONS_SHARING_MODE = "organizations.sharingMode";
    public static final String RESPONSE_ORGANIZATIONS_ROLES = "organizations.roles";

    // Per-organization response paths (for specific org validation)
    public static final String RESPONSE_ORG_ROLES = "roles";
    public static final String RESPONSE_ORG_ROLES_SIZE = "roles.size()";
    public static final String RESPONSE_ORG_ROLES_DISPLAY_NAME = "roles.displayName";
    public static final String RESPONSE_ORG_ROLES_AUDIENCE_DISPLAY = "roles.audience.display";
    public static final String RESPONSE_ORG_ROLES_AUDIENCE_TYPE = "roles.audience.type";

    public static final String ERROR_SETUP_SWAGGER_DEFINITION = "Unable to read the swagger definition %s from %s";
    public static final String SHARED_USER_ID_JSON_PATH = "organizations.find { it.orgName == '%s' }.sharedUserId";
    public static final String ORG_ROLES_JSON_PATH = "organizations.find { it.orgId == '%s' }.roles";
    public static final String ORG_SHARING_MODE_JSON_PATH = "organizations.find { it.orgId == '%s' }.sharingMode";
}
