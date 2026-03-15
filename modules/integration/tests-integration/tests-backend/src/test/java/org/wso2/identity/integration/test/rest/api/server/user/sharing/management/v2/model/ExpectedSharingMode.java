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

package org.wso2.identity.integration.test.rest.api.server.user.sharing.management.v2.model;

import java.util.List;
import java.util.Objects;

/**
 * Test-only model describing the expected content of a {@code sharingMode} field in the
 * GET /shared-organizations V2 response.
 * <p>
 * Used as the value type in the {@code expectedResults} map under the keys:
 * <ul>
 *   <li>{@code MAP_KEY_EXPECTED_TOP_LEVEL_SHARING_MODE} – describes the top-level
 *       {@code sharingMode} present for general share policies.</li>
 *   <li>{@code MAP_KEY_EXPECTED_PER_ORG_SHARING_MODE} (map value per orgId) – describes the
 *       per-org {@code sharingMode} present on the policy-holding org of a
 *       {@code SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN} share.</li>
 * </ul>
 * Pass {@code null} for the map value (or for the top-level key itself) to assert that the
 * {@code sharingMode} field is absent in the response.
 * <p>
 * <b>Key rule:</b> when {@link #getRoleAssignmentMode()} is {@code "NONE"},
 * the GET response returns an empty list for {@code roleAssignment.roles}.
 * Pass {@code null} for {@link #getRoleAssignmentRoles()} in the constructor — the
 * assertion helper handles NONE mode by checking {@code equalTo(Collections.emptyList())}
 * regardless of the stored value.
 */
public class ExpectedSharingMode {

    private final String policy;
    private final String roleAssignmentMode;

    /**
     * Expected roles in {@code sharingMode.roleAssignment.roles}.
     * Pass {@code null} when {@code roleAssignmentMode} is {@code "NONE"} — the assertion
     * helper ignores this value for NONE mode and always checks for an empty list.
     */
    private final List<RoleWithAudience> roleAssignmentRoles;

    /**
     * Constructs an expected sharing mode descriptor.
     *
     * @param policy              The expected policy string, e.g.
     *                            {@code "ALL_EXISTING_AND_FUTURE_ORGS"} or
     *                            {@code "SELECTED_ORG_WITH_ALL_EXISTING_AND_FUTURE_CHILDREN"}.
     * @param roleAssignmentMode  Either {@code "SELECTED"} or {@code "NONE"}.
     * @param roleAssignmentRoles The expected roles list. Pass {@code null} when
     *                            {@code roleAssignmentMode} is {@code "NONE"} so that the
     *                            validator asserts the field is absent.
     */
    public ExpectedSharingMode(String policy, String roleAssignmentMode,
                               List<RoleWithAudience> roleAssignmentRoles) {

        this.policy = policy;
        this.roleAssignmentMode = roleAssignmentMode;
        this.roleAssignmentRoles = roleAssignmentRoles;
    }

    /**
     * Returns the expected value of {@code sharingMode.policy}.
     */
    public String getPolicy() {
        return policy;
    }

    /**
     * Returns the expected value of {@code sharingMode.roleAssignment.mode}.
     * Either {@code "SELECTED"} or {@code "NONE"}.
     */
    public String getRoleAssignmentMode() {
        return roleAssignmentMode;
    }

    /**
     * Returns the expected roles for {@code sharingMode.roleAssignment.roles}.
     * {@code null} when mode is {@code "NONE"} — the assertion helper uses
     * {@code equalTo(Collections.emptyList())} for NONE mode regardless of this value.
     */
    public List<RoleWithAudience> getRoleAssignmentRoles() {
        return roleAssignmentRoles;
    }

    /**
     * Convenience check: returns {@code true} when mode is NONE, i.e., when roles are expected
     * to be an empty list in the response.
     */
    public boolean isNoneMode() {
        return "NONE".equalsIgnoreCase(roleAssignmentMode);
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExpectedSharingMode that = (ExpectedSharingMode) o;
        return Objects.equals(this.policy, that.policy) &&
                Objects.equals(this.roleAssignmentMode, that.roleAssignmentMode) &&
                Objects.equals(this.roleAssignmentRoles, that.roleAssignmentRoles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(policy, roleAssignmentMode, roleAssignmentRoles);
    }

    @Override
    public String toString() {

        return "ExpectedSharingMode{" +
                "policy='" + policy + '\'' +
                ", roleAssignmentMode='" + roleAssignmentMode + '\'' +
                ", roleAssignmentRoles=" + roleAssignmentRoles +
                '}';
    }
}
