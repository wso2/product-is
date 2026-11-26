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

import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.annotations.Factory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.automation.engine.context.TestUserMode.SUPER_TENANT_ADMIN;

/**
 * Drops test classes without a {@code @Factory} constructor from lanes that don't run the super tenant: such classes
 * always run as the super tenant admin. Factory classes are filtered at their data providers instead (see
 * {@link LaneUserModes#filter}), because TestNG 6.1.1 can't drop single factory instances here.
 * Does nothing unless {@code lane.userModes} is set.
 */
public class LaneUserModeInterceptor implements IMethodInterceptor {

    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {

        if (!LaneUserModes.isRestricted() || LaneUserModes.isAllowed(SUPER_TENANT_ADMIN)) {
            return methods;
        }
        List<IMethodInstance> kept = new ArrayList<>();
        for (IMethodInstance method : methods) {
            if (hasFactory(method.getMethod().getRealClass())) {
                kept.add(method);
            }
        }
        return kept;
    }

    private static boolean hasFactory(Class<?> testClass) {

        for (Constructor<?> constructor : testClass.getConstructors()) {
            if (constructor.isAnnotationPresent(Factory.class)) {
                return true;
            }
        }
        return false;
    }
}
