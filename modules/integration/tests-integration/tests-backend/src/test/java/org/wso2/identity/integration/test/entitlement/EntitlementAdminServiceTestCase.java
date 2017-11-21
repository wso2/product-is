/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.identity.integration.test.entitlement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.Assert;
import org.wso2.identity.integration.common.clients.entitlement.EntitlementAdminServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

public class EntitlementAdminServiceTestCase extends ISIntegrationTest{
	
	private EntitlementAdminServiceClient entitlementAdminClient;
	private static final Log log = LogFactory.getLog(EntitlementAdminServiceTestCase.class);
	private static final String REQUEST_1 = "<Request xmlns='urn:oasis:names:tc:xacml:2.0:context:schema:os' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='urn:oasis:names:tc:xacml:2.0:context:schema:os access_control-xacml-2.0-context-schema-os.xsd'> <Subject> <Attribute AttributeId='urn:oasis:names:tc:xacml:1.0:subject:subject-id' DataType='http://www.w3.org/2001/XMLSchema#string'> <AttributeValue>admin</AttributeValue> </Attribute> </Subject> <Resource> <Attribute AttributeId='urn:oasis:names:tc:xacml:1.0:resource:resource-id' DataType='http://www.w3.org/2001/XMLSchema#string'> <AttributeValue>http://localhost:8280/services/echo</AttributeValue> </Attribute> </Resource> <Action> <Attribute AttributeId='urn:oasis:names:tc:xacml:1.0:action:action-id' DataType='http://www.w3.org/2001/XMLSchema#string'> <AttributeValue>read</AttributeValue> </Attribute> </Action> <Environment/> </Request>";

	@BeforeClass(alwaysRun = true)
	public void testInit() throws Exception {
		super.init();
		entitlementAdminClient = new EntitlementAdminServiceClient(backendURL, sessionCookie);
	}

	@AfterClass(alwaysRun = true)
	public void atEnd() throws Exception {

	}

	@Test(groups = "wso2.is", description = "Check get policy algorithm")
	public void testGetGlobalPolicyAlgorithm() throws Exception {
		Assert.assertNotNull(entitlementAdminClient.getGlobalPolicyAlgorithm(), "Getting the policy algorithm has failed with null return.");
	}
	
	@Test(groups = "wso2.is", description = "Check get pdp data", dependsOnMethods="testGetGlobalPolicyAlgorithm")
	public void testGetPDPData() throws Exception {
		entitlementAdminClient.getPDPData();
	}
	
	@Test(groups = "wso2.is", description = "Check policy request", dependsOnMethods="testGetPDPData")
	public void testDoTestRequest() throws Exception {
		Assert.assertNotNull(entitlementAdminClient.doTestRequest(REQUEST_1), "Testing the policy request has failed with null return.");
	}
	
	@Test(groups = "wso2.is", description = "Check get PIP attribute finder data", dependsOnMethods="testDoTestRequest")
	public void testGetPIPAttributeFinderData() throws Exception {
		entitlementAdminClient.getPIPAttributeFinderData("urn:oasis:names:tc:xacml:1.0:subject:subject-id");
//		TODO - fix request	
//		Assert.assertNotNull(entitlementAdminClient.getPIPAttributeFinderData("urn:oasis:names:tc:xacml:1.0:subject:subject-id"), "Getting PIP attribute finder data has failed with null return.");
	}
	
	@Test(groups = "wso2.is", description = "Check get PIP resource finder data", dependsOnMethods="testGetPIPAttributeFinderData")
	public void testGetPIPResourceFinderData() throws Exception {
		Assert.assertNotNull(entitlementAdminClient.getPIPResourceFinderData("urn:oasis:names:tc:xacml:1.0:resource:resource-id"), "Getting PIP resouce finder data has failed with null return.");
	}
	
	@Test(groups = "wso2.is", description = "Check get policy finder data", dependsOnMethods="testGetPIPResourceFinderData")
	public void testGetPolicyFinderData() throws Exception {
		entitlementAdminClient.getPolicyFinderData("");
//		TODO - fix request		
//		Assert.assertNotNull(entitlementAdminClient.getPolicyFinderData(""), "Getting policy finder data has failed with null return.");
	}
	
	@Test(groups = "wso2.is", description = "Check refresh attribute finder", dependsOnMethods="testGetPolicyFinderData")
	public void testRefreshAttributeFinder() throws Exception {
		entitlementAdminClient.refreshAttributeFinder("*");
	}
	
	@Test(groups = "wso2.is", description = "Check refresh policy finder", dependsOnMethods="testRefreshAttributeFinder")
	public void testRefreshPolicyFinders() throws Exception {
		entitlementAdminClient.refreshPolicyFinders("*");
	}
	
	@Test(groups = "wso2.is", description = "Check refresh resouce finder", dependsOnMethods="testRefreshPolicyFinders")
	public void testRefreshResourceFinder() throws Exception {
		entitlementAdminClient.refreshResourceFinder("*");
	}
	
	@Test(groups = "wso2.is", description = "Check set global policy algorithm", dependsOnMethods="testRefreshResourceFinder")
	public void testSetGlobalPolicyAlgorithm() throws Exception {
		entitlementAdminClient.setGlobalPolicyAlgorithm("Algo");
	}
}
