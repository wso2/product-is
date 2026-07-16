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

package org.wso2.identity.integration.test.rest.api.server.roles.v2;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationListItem;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.Audience;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.Permission;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.RoleV2;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Integration tests for Console role permission handling with granular application feature scopes.
 */
public class ConsoleGranularScopeTestCase extends ISIntegrationTest {

    // Test groups marking the two server-state phases. Membership (not a suite-level filter) drives
    // the @BeforeGroups knob-enable hook; priority keeps the disabled phase strictly ahead.
    private static final String GRANULAR_DISABLED = "granular.console.disabled";
    private static final String GRANULAR_ENABLED = "granular.console.enabled";

    private static final String CONSOLE_APP_NAME = "Console";
    private static final String APPLICATION_AUDIENCE = "APPLICATION";

    // Feature scope (page-level UI rendering).
    private static final String FEATURE = "console:applications";
    // Feature-action scopes (assigned on a role).
    private static final String VIEW = "console:applications_view";
    private static final String EDIT = "console:applications_edit";       // legacy combined.
    private static final String CREATE = "console:applications_create";   // new granular.
    private static final String UPDATE = "console:applications_update";
    private static final String DELETE = "console:applications_delete";
    // Effective internal scopes (the actual authorization effect surfaced in the role response).
    private static final String I_VIEW = "internal_application_mgt_view";
    private static final String I_CREATE = "internal_application_mgt_create";
    private static final String I_UPDATE = "internal_application_mgt_update";
    private static final String I_DELETE = "internal_application_mgt_delete";
    // The write-internal closure that console:applications_edit resolves to.
    private static final List<String> EDIT_INTERNALS = Arrays.asList(I_CREATE, I_UPDATE, I_DELETE);

    private SCIM2RestClient scim2RestClient;
    private OAuth2RestClient oAuth2RestClient;
    private ServerConfigurationManager serverConfigurationManager;
    private String consoleAppId;
    private final List<String> createdRoleIds = new ArrayList<>();

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        // Knob stays OFF here: the disabled phase runs first against the pristine default-off pack.
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        oAuth2RestClient = new OAuth2RestClient(serverURL, tenantInfo);
        consoleAppId = resolveConsoleAppId();
        assertNotNull(consoleAppId, "Console application could not be resolved.");
    }

    /**
     * Turns on {@code [console.console_settings] use_granular_console_permissions} (default off) by appending
     * the knob to the resident deployment.toml and restarting the server.
     */
    @BeforeGroups(groups = GRANULAR_ENABLED, alwaysRun = true)
    public void enableGranularConsolePermissions() throws Exception {

        File defaultTomlFile = getDeploymentTomlFile(Utils.getResidentCarbonHome());
        String content = new String(Files.readAllBytes(defaultTomlFile.toPath()), StandardCharsets.UTF_8);
        String enabledContent = content.replaceFirst(
                "(?m)^(\\s*use_granular_console_permissions\\s*=\\s*)(true|false)(\\s*(#.*)?)$",
                "$1true$3");
        if (enabledContent.equals(content)) {
            content += System.lineSeparator() + "[console.console_settings]" + System.lineSeparator()
                    + "use_granular_console_permissions = true" + System.lineSeparator();
        } else {
            content = enabledContent;
        }
        File enabledTomlFile = File.createTempFile("console-granular-enabled", ".toml");
        Files.write(enabledTomlFile.toPath(), content.getBytes(StandardCharsets.UTF_8));

        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(enabledTomlFile, defaultTomlFile, true);
        serverConfigurationManager.restartGracefully();

        scim2RestClient.closeHttpClient();
        oAuth2RestClient.closeHttpClient();
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        oAuth2RestClient = new OAuth2RestClient(serverURL, tenantInfo);
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUpRoles() throws Exception {

        for (String roleId : createdRoleIds) {
            scim2RestClient.deleteV2Role(roleId);
        }
        createdRoleIds.clear();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        if (scim2RestClient != null) {
            scim2RestClient.closeHttpClient();
        }
        if (oAuth2RestClient != null) {
            oAuth2RestClient.closeHttpClient();
        }
        if (serverConfigurationManager != null) {
            serverConfigurationManager.restoreToLastConfiguration();
        }
    }

    @Test(groups = {"wso2.is", GRANULAR_DISABLED}, priority = 1,
            description = "OFF.1: with the knob off, all three granular scopes do NOT resolve to the "
                    + "internal write scopes (the granular feature is inert).")
    public void granularScopesDoNotResolveWhenDisabled() throws Exception {

        String roleId = createConsoleRole("console-off-granular-all", FEATURE, CREATE, UPDATE, DELETE);

        Set<String> resolved = scim2RestClient.getV2RolePermissions(roleId);
        assertFalse(resolved.contains(I_CREATE), "create must not resolve to internal create when disabled.");
        assertFalse(resolved.contains(I_UPDATE), "update must not resolve to internal update when disabled.");
        assertFalse(resolved.contains(I_DELETE), "delete must not resolve to internal delete when disabled.");
        assertFalse(resolved.contains(EDIT), "granular scopes must not derive edit when disabled.");
    }

    @DataProvider(name = "singleGranularScopeDisabled")
    public Object[][] singleGranularScopeDisabled() {

        return new Object[][] {
                {"console-off-create", CREATE, I_CREATE},
                {"console-off-update", UPDATE, I_UPDATE},
                {"console-off-delete", DELETE, I_DELETE},
        };
    }

    @Test(groups = {"wso2.is", GRANULAR_DISABLED}, priority = 2, dataProvider = "singleGranularScopeDisabled",
            description = "OFF.2: with the knob off, a granular scope does NOT resolve to its internal "
                    + "action scope.")
    public void singleGranularDoesNotResolveWhenDisabled(String roleName, String granularScope, String internalScope)
            throws Exception {

        String roleId = createConsoleRole(roleName, FEATURE, granularScope);

        Set<String> resolved = scim2RestClient.getV2RolePermissions(roleId);
        assertFalse(resolved.contains(internalScope),
                granularScope + " must not resolve to " + internalScope + " when disabled.");
    }

    @Test(groups = {"wso2.is", GRANULAR_DISABLED}, priority = 3,
            description = "OFF.3: console:applications_edit still resolves to the internal write scopes "
                    + "when the knob is off (legacy behaviour is not gated).")
    public void editStillResolvesWhenDisabled() throws Exception {

        String roleId = createConsoleRole("console-off-edit", FEATURE, VIEW, EDIT);

        Set<String> resolved = scim2RestClient.getV2RolePermissions(roleId);
        assertTrue(resolved.contains(EDIT), "Assigned edit feature scope must be retained.");
        assertTrue(resolved.containsAll(EDIT_INTERNALS),
                "Edit must resolve to internal create + update + delete even when the granular knob is off.");
    }

    @Test(groups = {"wso2.is", GRANULAR_DISABLED}, priority = 4,
            description = "OFF.4: directly-assigned internal scopes are retained when the knob is off "
                    + "(legacy roles unaffected).")
    public void legacyInternalScopesPreservedWhenDisabled() throws Exception {

        String roleId = createConsoleRole("console-off-legacy-internal", FEATURE, I_VIEW, I_UPDATE);

        Set<String> resolved = scim2RestClient.getV2RolePermissions(roleId);
        assertTrue(resolved.contains(I_VIEW), "Directly-assigned internal view scope must be retained.");
        assertTrue(resolved.contains(I_UPDATE), "Directly-assigned internal update scope must be retained.");
    }

    @Test(groups = {"wso2.is", GRANULAR_ENABLED}, priority = 10,
            description = "M1.1: a legacy role assigned internal scopes directly retains those internal "
                    + "scopes after retrieval.")
    public void legacyInternalScopesPreserved() throws Exception {

        String roleId = createConsoleRole("console-legacy-internal", FEATURE, I_VIEW, I_UPDATE);

        Set<String> resolved = scim2RestClient.getV2RolePermissions(roleId);
        assertTrue(resolved.contains(I_VIEW), "Directly-assigned internal view scope must be retained.");
        assertTrue(resolved.contains(I_UPDATE), "Directly-assigned internal update scope must be retained.");
        assertTrue(resolved.contains(VIEW), "View feature scope must resolve to the internal view scope.");
    }

    @Test(groups = {"wso2.is", GRANULAR_ENABLED}, priority = 11,
            description = "M1.2: a legacy role assigned the full set of internal application action "
                    + "scopes retains all of them.")
    public void legacyAllInternalActionsPreserved() throws Exception {

        String roleId = createConsoleRole("console-legacy-internal-all", FEATURE, I_VIEW, I_CREATE, I_UPDATE, I_DELETE);

        Set<String> resolved = scim2RestClient.getV2RolePermissions(roleId);
        assertTrue(resolved.containsAll(Arrays.asList(I_VIEW, I_CREATE, I_UPDATE, I_DELETE)),
                "All directly-assigned internal action scopes must be retained.");
        assertTrue(resolved.contains(VIEW), "View feature scope must resolve to the internal view scope.");
        assertTrue(resolved.contains(EDIT), "Edit feature scope must resolve to the internal edit scopes.");
    }

    @Test(groups = {"wso2.is", GRANULAR_ENABLED}, priority = 12,
            description = "M2.1: console:applications_edit resolves to the internal create/update/delete "
                    + "application-management scopes (the backward-compat reference).")
    public void editResolvesToInternalWriteScopes() throws Exception {

        String roleId = createConsoleRole("console-edit", FEATURE, VIEW, EDIT);

        Set<String> resolved = scim2RestClient.getV2RolePermissions(roleId);
        assertTrue(resolved.contains(EDIT), "Assigned edit feature scope must be retained.");
        assertTrue(resolved.containsAll(EDIT_INTERNALS),
                "Edit must resolve to internal create + update + delete scopes.");
        assertTrue(resolved.contains(VIEW), "View feature scope must resolve to the internal view scope.");
        assertTrue(resolved.contains(I_VIEW), "View internal scope must resolve to the internal view scope.");
    }

    @DataProvider(name = "singleGranularScopeEnabled")
    public Object[][] singleGranularScopeEnabled() {

        return new Object[][] {
                // role-name, granular feature scope, expected internal, two internals that must be absent.
                {"console-granular-create", CREATE, I_CREATE, I_UPDATE, I_DELETE},
                {"console-granular-update", UPDATE, I_UPDATE, I_CREATE, I_DELETE},
                {"console-granular-delete", DELETE, I_DELETE, I_CREATE, I_UPDATE},
        };
    }

    @Test(groups = {"wso2.is", GRANULAR_ENABLED}, priority = 13, dataProvider = "singleGranularScopeEnabled",
            description = "M3.2-M3.4: a single granular scope resolves to exactly its matching internal "
                    + "action scope and must not grant the other write actions.")
    public void singleGranularResolvesToMatchingInternal(String roleName, String featureScope, String expectedInternalScope,
                                                          String absentInternalScopeA, String absentInternalScopeB)
            throws Exception {

        String roleId = createConsoleRole(roleName, FEATURE, featureScope);

        Set<String> resolved = scim2RestClient.getV2RolePermissions(roleId);
        assertTrue(resolved.contains(expectedInternalScope),
                featureScope + " must resolve to " + expectedInternalScope + ".");
        assertFalse(resolved.contains(absentInternalScopeA),
                featureScope + " must not grant " + absentInternalScopeA + ".");
        assertFalse(resolved.contains(absentInternalScopeB),
                featureScope + " must not grant " + absentInternalScopeB + ".");
    }

    @Test(groups = {"wso2.is", GRANULAR_ENABLED}, priority = 14,
            description = "M3.1: all three granular scopes resolve to the same internal write scopes as "
                    + "console:applications_edit (effective backward compatibility).")
    public void allThreeGranularResolveToEditInternals() throws Exception {

        String roleId = createConsoleRole("console-granular-all", FEATURE, CREATE, UPDATE, DELETE);

        Set<String> resolved = scim2RestClient.getV2RolePermissions(roleId);
        assertTrue(resolved.containsAll(EDIT_INTERNALS),
                "create + update + delete must resolve to internal create + update + delete (edit-equivalent).");
        assertTrue(resolved.contains(EDIT), "edit must resolve to internal create.");
    }

    @Test(groups = {"wso2.is", GRANULAR_ENABLED}, priority = 15,
            description = "M3.5: two granular scopes resolve to exactly their two internal actions, not "
                    + "the third.")
    public void twoGranularResolveToPartialInternals() throws Exception {

        String roleId = createConsoleRole("console-granular-two", FEATURE, CREATE, UPDATE);

        Set<String> resolved = scim2RestClient.getV2RolePermissions(roleId);
        assertTrue(resolved.contains(I_CREATE), "create must resolve to internal create.");
        assertTrue(resolved.contains(I_UPDATE), "update must resolve to internal update.");
        assertFalse(resolved.contains(I_DELETE), "delete action must not be granted from create + update.");
    }

    @Test(groups = {"wso2.is", GRANULAR_ENABLED}, priority = 16,
            description = "E2: edit together with all three granular scopes resolves to the write "
                    + "internals with no duplicate feature-action entries.")
    public void editPlusAllGranularIsIdempotent() throws Exception {

        String roleId = createConsoleRole("console-edit-and-granular", FEATURE, EDIT, CREATE, UPDATE, DELETE);

        List<String> resolved = rolePermissionList(roleId);
        assertTrue(resolved.containsAll(EDIT_INTERNALS),
                "Combined edit + granular must resolve to internal create + update + delete.");
        assertEquals(Collections.frequency(resolved, EDIT), 1, "Edit feature scope must not be duplicated.");
        assertEquals(Collections.frequency(resolved, CREATE), 1, "Create feature scope must not be duplicated.");
    }

    @Test(groups = {"wso2.is", GRANULAR_ENABLED}, priority = 17,
            description = "E7: retrieving a resolved role twice returns an identical, stable scope set "
                    + "(no read-time write amplification).")
    public void retrievalIsIdempotent() throws Exception {

        String roleId = createConsoleRole("console-granular-idempotent", FEATURE, CREATE, UPDATE, DELETE);

        Set<String> first = scim2RestClient.getV2RolePermissions(roleId);
        Set<String> second = scim2RestClient.getV2RolePermissions(roleId);
        assertEquals(second, first, "Repeated retrieval must return an identical resolved scope set.");
        assertTrue(second.containsAll(EDIT_INTERNALS),
                "Resolved write internals must be stable across retrievals.");
    }

    private String resolveConsoleAppId() throws Exception {

        List<ApplicationListItem> applications = oAuth2RestClient.getAllApplications().getApplications();
        for (ApplicationListItem application : applications) {
            if (CONSOLE_APP_NAME.equals(application.getName())) {
                return application.getId();
            }
        }
        return null;
    }

    private String createConsoleRole(String name, String... permissionValues) throws Exception {

        List<Permission> permissions = Arrays.stream(permissionValues)
                .map(Permission::new)
                .collect(Collectors.toList());
        RoleV2 role = new RoleV2(new Audience(APPLICATION_AUDIENCE, consoleAppId), name, permissions,
                Collections.emptyList());
        String roleId = scim2RestClient.addV2Role(role);
        createdRoleIds.add(roleId);
        return roleId;
    }

    /**
     * Returns the raw resolved permission values for a role, preserving any duplicates, so that
     * de-duplication can be asserted at the API-response level.
     */
    private List<String> rolePermissionList(String roleId) throws Exception {

        org.json.simple.JSONObject role = scim2RestClient.getV2Role(roleId);
        List<String> permissions = new ArrayList<>();
        org.json.simple.JSONArray permissionArray = (org.json.simple.JSONArray) role.get("permissions");
        if (permissionArray != null) {
            for (Object permission : permissionArray) {
                permissions.add(((org.json.simple.JSONObject) permission).get("value").toString());
            }
        }
        return permissions;
    }
}
