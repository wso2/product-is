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

package org.wso2.identity.integration.test.rest.api.server.configs.v1;

import org.testng.annotations.DataProvider;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

/**
 * Base class for /configs/consent/purposes REST API integration tests.
 */
public class ConsentPurposesTestBase extends ConfigTestBase {

    protected static final String CONSENT_PURPOSES_API_PATH = "/configs/consent/purposes";
    protected static final String APPLICATIONS_SUFFIX = "/applications";
    protected static final String CONSENT_MGT_API_BASE_PATH = "/api/identity/consent-mgt/v2.0";
    protected static final String CONSENT_MGT_PURPOSES_PATH = "/purposes";
    protected static final String CONSENT_MGT_ELEMENTS_PATH = "/elements";

    protected String createdPurposeId;
    protected String createdElementId;
    protected String createdAppId;

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    protected String getConsentMgtBasePath(String tenantDomain) {

        return ISIntegrationTest.getTenantedRelativePath(CONSENT_MGT_API_BASE_PATH, tenantDomain);
    }
}
