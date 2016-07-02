/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.integration.test.claim.metadata.mgt;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.wso2.identity.integration.common.clients.claim.metadata.mgt.ClaimMetadataManagementServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

/**
 * Includes automated tests for operations in ClaimMetadataManagementService.
 */
public class ClaimMetadataManagementServiceTestCase extends ISIntegrationTest {

    private ClaimMetadataManagementServiceClient adminClient;

    private final static String CLAIM_URI = "http://wso2.com/testing";
    private final static String CLAIM_URI_NEW = "http://wso2.com/testing1";
    private final static String DISPLAY_NAME = "Test";
    private final static String DISPLAY_NAME_NEW = "New Display";
    private final static String DESCRIPTION = "Test";
    private final static String DESCRIPTION_NEW = "New Description";
    private final static String DIALECT = "http://wso2.com/testing";
    private final static String REGEX = "TestRegx";
    private final static String ATTRIBUTE = "attr1;attr2";
    private final static String STORE = "store";

    private final static int DISPLAY_ORDER = 0;
    private final static boolean REQUIRED = true;
    private final static boolean SUPPORTED = true;
    private final static boolean READONLY = false;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        adminClient = new ClaimMetadataManagementServiceClient(backendURL, sessionCookie);
        setSystemproperties();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        adminClient = null;
    }
}