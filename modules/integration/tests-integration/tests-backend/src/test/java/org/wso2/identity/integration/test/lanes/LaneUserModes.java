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

package org.wso2.identity.integration.test.lanes;

import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Lets a test lane run only some user modes, e.g. only tenant-mode instances, so that the super-tenant and tenant
 * runs of the same test classes can run in parallel lanes against one IS server.
 *
 * The modes come from the {@code lane.userModes} system property, e.g. {@code TENANT_ADMIN} or
 * {@code SUPER_TENANT_ADMIN,TENANT_ADMIN}. Without it every mode is allowed and nothing changes. Classes that run in
 * none of a lane's modes are left out of the lane's suite by TestLaneRunner, so a provider never ends up empty.
 */
public final class LaneUserModes {

    private static final Set<String> ALLOWED = parse(System.getProperty("lane.userModes", ""));

    private LaneUserModes() {

    }

    /**
     * Filters the rows of a {@code @Factory} data provider: a row is dropped if one of its values is, or carries (via
     * a {@code getUserMode()} method, like SAMLConfig), a {@link TestUserMode} this lane doesn't run. Rows without a
     * user mode are kept.
     */
    public static <T> T[][] filter(T[][] rows) {

        if (ALLOWED.isEmpty() || rows == null) {
            return rows;
        }
        List<T[]> kept = new ArrayList<>();
        for (T[] row : rows) {
            boolean keep = true;
            for (Object value : row) {
                TestUserMode mode = userModeOf(value);
                if (mode != null && !isAllowed(mode)) {
                    keep = false;
                }
            }
            if (keep) {
                kept.add(row);
            }
        }
        return kept.toArray(Arrays.copyOf(rows, 0));
    }

    private static TestUserMode userModeOf(Object value) {

        if (value instanceof TestUserMode) {
            return (TestUserMode) value;
        }
        if (value == null) {
            return null;
        }
        try {
            Object mode = value.getClass().getMethod("getUserMode").invoke(value);
            return mode instanceof TestUserMode ? (TestUserMode) mode : null;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    /** Whether this lane runs the given user mode. */
    public static boolean isAllowed(TestUserMode mode) {

        return ALLOWED.isEmpty() || ALLOWED.contains(mode.name());
    }

    /** Whether this lane restricts user modes at all. */
    public static boolean isRestricted() {

        return !ALLOWED.isEmpty();
    }

    private static Set<String> parse(String value) {

        Set<String> modes = new HashSet<>();
        for (String mode : Arrays.asList(value.split(","))) {
            if (!mode.trim().isEmpty()) {
                modes.add(TestUserMode.valueOf(mode.trim()).name());
            }
        }
        return modes;
    }
}
