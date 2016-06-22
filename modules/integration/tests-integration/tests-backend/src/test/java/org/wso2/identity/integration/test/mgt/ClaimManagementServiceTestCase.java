/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.identity.integration.test.mgt;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.wso2.carbon.identity.claim.mgt.stub.dto.ClaimDTO;
import org.wso2.carbon.identity.claim.mgt.stub.dto.ClaimDialectDTO;
import org.wso2.carbon.identity.claim.mgt.stub.dto.ClaimMappingDTO;
import org.wso2.identity.integration.common.clients.ClaimManagementServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

public class ClaimManagementServiceTestCase extends ISIntegrationTest {

	private ClaimManagementServiceClient adminClient;

	private final static String CLAIM_URI = "http://wso2.com/testing";
	private final static String CLAIM_URI_NEW = "http://wso2.com/testing1";
	private final static String DISPLAY_NAME = "Test";
	private final static String DISPLAY_NAME_NEW = "New Display";
	private final static String DESCRIPTION = "Test";
	private final static String DESCRIPTION_NEW = "New Description";
	private final static String DIALECT = "http://wso2.com/testing";
	private final static String REGEX = "TestRegx";
	private final static String ATTRIBUTE = "http://wso2.org/claims/country";
    private final static String NEW_ATTRIBUTE = "http://wso2.org/claims/emailaddress";
	private final static String STORE = "store";

	private final static int DISPLAY_ORDER = 0;
	private final static boolean REQUIRED = true;
	private final static boolean SUPPORTED = true;
	private final static boolean READONLY = false;

	@BeforeClass(alwaysRun = true)
	public void testInit() throws Exception {
		super.init();
		adminClient = new ClaimManagementServiceClient(backendURL, sessionCookie);
		setSystemproperties();
	}

	@AfterClass(alwaysRun = true)
	public void atEnd() throws Exception {
		adminClient = null;
	}

	@Test(alwaysRun = true, description = "Add new claim dialect")
	public void testAddNewClaimDialect() {
		ClaimMappingDTO mapping = new ClaimMappingDTO();
		ClaimDTO claim = new ClaimDTO();

		claim.setClaimUri(CLAIM_URI);
		claim.setDisplayTag(DISPLAY_NAME);
		claim.setDescription(DESCRIPTION);
		claim.setDialectURI(DIALECT);
		claim.setRegEx(REGEX);
		claim.setDisplayOrder(DISPLAY_ORDER);
		claim.setRequired(REQUIRED);
		claim.setSupportedByDefault(SUPPORTED);
		claim.setReadOnly(READONLY);

		mapping.setClaim(claim);

		if (ATTRIBUTE != null) {
			mapping.setMappedAttribute(ATTRIBUTE);
		}
		try {
			ClaimDialectDTO dialectDTO = new ClaimDialectDTO();
			dialectDTO.addClaimMappings(mapping);
			dialectDTO.setDialectURI(DIALECT);
			dialectDTO.setUserStore(STORE);
			adminClient.addNewClaimDialect(dialectDTO);

			dialectDTO = adminClient.getClaimMappingByDialect(DIALECT);
			Assert.assertNotNull(dialectDTO, "Error occured while adding claim dialect.");

		} catch (Exception e) {
			Assert.fail("Error while trying to add new claim dialect", e);
		}
	}

	@Test(alwaysRun = true, description = "Add new claim mapping dialect", dependsOnMethods = "testAddNewClaimDialect")
	public void testAddNewClaimMapping() {
		ClaimMappingDTO mapping = new ClaimMappingDTO();
		ClaimDTO claim = new ClaimDTO();

		claim.setClaimUri(CLAIM_URI_NEW);
		claim.setDisplayTag("Test1");
		claim.setDescription("Test1");
		claim.setDialectURI(DIALECT);
		claim.setRegEx(REGEX);
		claim.setDisplayOrder(DISPLAY_ORDER);
		claim.setRequired(REQUIRED);
		claim.setSupportedByDefault(SUPPORTED);
		claim.setReadOnly(READONLY);

		mapping.setClaim(claim);

		if (NEW_ATTRIBUTE != null) {
            mapping.setMappedAttribute(NEW_ATTRIBUTE);
		}
		try {
			adminClient.addNewClaimMapping(mapping);
			ClaimDialectDTO dialectDTO = adminClient.getClaimMappingByDialect(DIALECT);
			Assert.assertNotNull(dialectDTO, "Claim dialect adding failed.");

			String mappedAttribute = null;
			for (ClaimMappingDTO mappingDTO : dialectDTO.getClaimMappings()) {
				if (CLAIM_URI_NEW.equals(mappingDTO.getClaim().getClaimUri())) {
					mappedAttribute = mappingDTO.getMappedAttribute();
                    break;
				}
			}
			Assert.assertNotNull(mappedAttribute, "Error occured while adding claim mapping.");
		} catch (Exception e) {
			Assert.fail("Error while trying to add new claim dialect", e);
		}
	}

	@Test(alwaysRun = true, description = "Update claim mapping", dependsOnMethods = "testAddNewClaimMapping")
	public void testUpdateClaimMapping() {
		ClaimMappingDTO mapping = new ClaimMappingDTO();
		ClaimDTO claim = new ClaimDTO();
		claim.setClaimUri(CLAIM_URI_NEW);
		claim.setDisplayTag(DISPLAY_NAME_NEW);
		claim.setDescription(DESCRIPTION_NEW);
		claim.setDialectURI(DIALECT);
		claim.setRegEx(REGEX);
		claim.setDisplayOrder(DISPLAY_ORDER);
		claim.setRequired(REQUIRED);
		claim.setSupportedByDefault(SUPPORTED);
		claim.setReadOnly(READONLY);

		mapping.setClaim(claim);

		if (ATTRIBUTE != null) {
            mapping.setMappedAttribute(NEW_ATTRIBUTE);
		}
		try {
			adminClient.updateClaimMapping(mapping);
			ClaimDialectDTO dialectDTO = adminClient.getClaimMappingByDialect(DIALECT);
			Assert.assertNotNull(dialectDTO, "Claim mapping adding failed.");

			for (ClaimMappingDTO mappingDTO : dialectDTO.getClaimMappings()) {
				if (CLAIM_URI_NEW.equals(mappingDTO.getClaim().getClaimUri())) {
					Assert.assertEquals(DESCRIPTION_NEW, mappingDTO.getClaim().getDescription(),
					                    "Claim mapping update failed.");
					Assert.assertEquals(DISPLAY_NAME_NEW, mappingDTO.getClaim().getDisplayTag(),
					                    "Claim mapping update failed.");
                    Assert.assertEquals(NEW_ATTRIBUTE, mappingDTO.getMappedAttribute(), "Claim mapping update failed" +
                            ".");
					break;
				}
			}
		} catch (Exception e) {
			Assert.fail("Error while trying to update claim mapping", e);
		}
	}

	@Test(alwaysRun = true, description = "Remove claim mapping", dependsOnMethods = "testUpdateClaimMapping")
	public void testRemoveClaimMapping() {
		try {
			adminClient.removeClaimMapping(DIALECT, CLAIM_URI);
			ClaimDialectDTO dialectDTO = adminClient.getClaimMappingByDialect(DIALECT);

			String dialectURI = null;
			for (ClaimMappingDTO mappintDTO : dialectDTO.getClaimMappings()) {
				if (mappintDTO.getClaim().getClaimUri().equals(CLAIM_URI)) {
					dialectURI = mappintDTO.getClaim().getDialectURI();
				}
			}
			Assert.assertNull(dialectURI, "Error occured while removing claim mapping.");
		} catch (Exception e) {
			Assert.fail("Error while trying to remove claim dialect", e);
		}
	}

	@Test(alwaysRun = true, description = "Remove claim dialect", dependsOnMethods = "testRemoveClaimMapping")
	public void testRemoveClaimDialect() {
		try {
			adminClient.removeClaimDialect(DIALECT);
			ClaimDialectDTO dialectDTO = adminClient.getClaimMappingByDialect(DIALECT);
			Assert.assertNull(dialectDTO, "Error occured while removing claim dialect.");
		} catch (Exception e) {
			Assert.fail("Error while trying to remove claim dialect", e);
		}
	}

}