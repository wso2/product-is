/*
 * Copyright (c) 2016-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.claim.metadata.mgt;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.AttributeMappingDTO;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.LabelValueDTO;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.LocalClaimReq;
import org.wso2.identity.integration.test.restclients.ClaimManagementRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

import java.util.Collections;

/**
 * Includes automated tests for operations in ClaimMetadataManagementService.
 */
public class ClaimMetadataManagementServiceTestCase extends ISIntegrationTest {

    private static final String LOCAL_CLAIM_URI_PREFIX = "http://wso2.org/claims/";
    private final static String DOMAIN = "PRIMARY";

    private final static int DISPLAY_ORDER = 0;
    private final static boolean REQUIRED = true;
    private final static boolean SUPPORTED = true;
    private final static boolean READONLY = false;

    private ClaimManagementRestClient claimManagementRestClient;
    private SCIM2RestClient scim2RestClient;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        claimManagementRestClient = new ClaimManagementRestClient(serverURL, tenantInfo);
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        claimManagementRestClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
    }

    @Test
    public void testAddClaimComplexClaimWithCanonicalValues() throws Exception {

        // Account ID claim.
        String ACCOUNT_ID_CLAIM = "account_id";
        LocalClaimReq accountIDClaimReq = new LocalClaimReq();
        accountIDClaimReq.setClaimURI(LOCAL_CLAIM_URI_PREFIX + ACCOUNT_ID_CLAIM);
        accountIDClaimReq.setDisplayName(ACCOUNT_ID_CLAIM);
        accountIDClaimReq.setDescription(ACCOUNT_ID_CLAIM);
        accountIDClaimReq.setSupportedByDefault(SUPPORTED);
        accountIDClaimReq.setRequired(false);
        accountIDClaimReq.setReadOnly(READONLY);
        accountIDClaimReq.setDataType("integer");

        AttributeMappingDTO accountIdMapping = new AttributeMappingDTO();
        accountIdMapping.setMappedAttribute(ACCOUNT_ID_CLAIM);
        accountIdMapping.setUserstore(DOMAIN);
        accountIDClaimReq.setAttributeMapping(Collections.singletonList(accountIdMapping));

        claimManagementRestClient.addLocalClaim(accountIDClaimReq);

        // Account type claim.
        String ACCOUNT_TYPE_CLAIM = "account_type";
        LocalClaimReq accountTypeClaimReq = new LocalClaimReq();
        accountTypeClaimReq.setClaimURI(LOCAL_CLAIM_URI_PREFIX + ACCOUNT_TYPE_CLAIM);
        accountTypeClaimReq.setDisplayName(ACCOUNT_TYPE_CLAIM);
        accountTypeClaimReq.setDescription(ACCOUNT_TYPE_CLAIM);
        accountTypeClaimReq.setSupportedByDefault(SUPPORTED);
        accountTypeClaimReq.setRequired(REQUIRED);
        accountTypeClaimReq.setReadOnly(READONLY);
        accountTypeClaimReq.setDisplayOrder(DISPLAY_ORDER);

        accountTypeClaimReq.setDataType("complex");
        accountTypeClaimReq.setSubAttributes(new String[] { LOCAL_CLAIM_URI_PREFIX + ACCOUNT_ID_CLAIM });

        AttributeMappingDTO accountTypeMapping = new AttributeMappingDTO();
        accountTypeMapping.setMappedAttribute(ACCOUNT_TYPE_CLAIM);
        accountTypeMapping.setUserstore(DOMAIN);
        accountTypeClaimReq.setAttributeMapping(Collections.singletonList(accountTypeMapping));

        LabelValueDTO[] accountTypes = new LabelValueDTO[2];
        LabelValueDTO labelValueDTO1 = new LabelValueDTO();
        labelValueDTO1.setLabel("Personal");
        labelValueDTO1.setValue("personal");
        accountTypes[0] = labelValueDTO1;
        LabelValueDTO labelValueDTO2 = new LabelValueDTO();
        labelValueDTO2.setLabel("Work");
        labelValueDTO2.setValue("work");
        accountTypes[1] = labelValueDTO2;
        accountTypeClaimReq.setCanonicalValues(accountTypes);
        String accountTypeClaimId = claimManagementRestClient.addLocalClaim(accountTypeClaimReq);

        JSONObject claim = claimManagementRestClient.getLocalClaim(accountTypeClaimId);
        JSONArray subAttributes = (JSONArray) claim.get("subAttributes");
        assert (subAttributes != null && subAttributes.size() == 1 &&
                subAttributes.get(0).equals(LOCAL_CLAIM_URI_PREFIX + ACCOUNT_ID_CLAIM));

        JSONArray canonicalValues = (JSONArray) claim.get("canonicalValues");
        assert (canonicalValues != null && canonicalValues.size() == 2
                && ((JSONObject) canonicalValues.get(0)).get("label").equals("Personal")
                && ((JSONObject) canonicalValues.get(0)).get("value").equals("personal")
                && ((JSONObject) canonicalValues.get(1)).get("label").equals("Work")
                && ((JSONObject) canonicalValues.get(1)).get("value").equals("work"));
    }
}
