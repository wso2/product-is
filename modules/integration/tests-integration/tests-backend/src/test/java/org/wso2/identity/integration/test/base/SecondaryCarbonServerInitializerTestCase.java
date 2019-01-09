/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.identity.integration.test.base;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.application.mgt.AbstractIdentityFederationTestCase;
import org.wso2.identity.integration.test.utils.CommonConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Test class that will start and stop secondary carbon server for the tests in the test suite.
 * This can be used once per test as belows.
 *
 *     <test name="is-test-xxxxx" preserve-order="true" parallel="false">
 *         <classes>
 *             <class name="org.wso2.identity.integration.test.base.SecondaryCarbonServerInitializerTestCase"/>
 *             <class name="testClassX1"/>
 *             <class name="testClassX2"/>
 *             <class name="testClassX3"/>
 *         </classes>
 *     </test>
 *     <test name="is-test-yyyyy" preserve-order="true" parallel="false">
 *         <classes>
 *             <class name="org.wso2.identity.integration.test.base.SecondaryCarbonServerInitializerTestCase"/>
 *             <class name="testClassY1"/>
 *             <class name="testClassY2"/>
 *             <class name="testClassY3"/>
 *         </classes>
 *     </test>
 */
public class SecondaryCarbonServerInitializerTestCase extends AbstractIdentityFederationTestCase {

    private static final Log LOG = LogFactory.getLog(SecondaryCarbonServerInitializerTestCase.class);
    private static final int PORT_OFFSET_1 = 1;

    @BeforeTest(alwaysRun = true)
    public void initTest() throws Exception {

        super.initTest();
        LOG.info("Starting secondary carbon server...");
        TestDataHolder testDataHolder = TestDataHolder.getInstance();
        Map<String, String> startupParameters = new HashMap<>();
        startupParameters.put("-DportOffset", String.valueOf(PORT_OFFSET_1 + CommonConstants.IS_DEFAULT_OFFSET));
        testDataHolder.setAutomationContext(new AutomationContext("IDENTITY", "identity002", TestUserMode
                .SUPER_TENANT_ADMIN));
        startCarbonServer(PORT_OFFSET_1, testDataHolder.getAutomationContext(), startupParameters);
        LOG.info("Secondary carbon server started.");
    }

    @AfterTest(alwaysRun = true)
    public void tearDownTest() throws Exception {

        LOG.info("Stopping secondary carbon server...");
        super.stopCarbonServer(PORT_OFFSET_1);
        LOG.info("Secondary carbon server stopped.");
    }
}
