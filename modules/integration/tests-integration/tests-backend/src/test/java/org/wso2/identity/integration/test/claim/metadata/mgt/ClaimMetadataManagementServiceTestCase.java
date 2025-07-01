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
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.AttributeMappingDTO;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.LabelValueDTO;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.LocalClaimReq;
import org.wso2.identity.integration.test.restclients.ClaimManagementRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

import java.util.Collections;
import java.util.HashMap;

/**
 * Includes automated tests for operations in ClaimMetadataManagementService.
 */
public class ClaimMetadataManagementServiceTestCase extends ISIntegrationTest {

    private static final String LOCAL_CLAIM_URI_PREFIX = "http://wso2.org/claims/";
    private final static String DOMAIN = "PRIMARY";

    private final static boolean REQUIRED = true;
    private final static boolean SUPPORTED = true;
    private final static boolean READONLY = false;
    private final static String ACCOUNT_ID_CLAIM = "account_id";
    private final static String ACCOUNT_TYPE_CLAIM = "account_type";
    private final static String INPUT_TYPE = "inputType";
    private final static String INPUT_TYPE_CHECKBOX_GROUP = "checkbox_group";
    private final static String ERROR_CODE_LOCAL_CLAIM_NOT_FOUND = "CMT-50019";

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

        LocalClaimReq accountIDClaimReq = buildLocalClaimReq(ACCOUNT_ID_CLAIM, "integer");
        claimManagementRestClient.addLocalClaim(accountIDClaimReq);

        LocalClaimReq accountTypeClaimReq = buildLocalClaimReq(ACCOUNT_TYPE_CLAIM, "complex");
        accountTypeClaimReq.setSubAttributes(new String[] { LOCAL_CLAIM_URI_PREFIX + ACCOUNT_ID_CLAIM });

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
        HashMap<String, String> inputFormat = new HashMap<>();
        inputFormat.put(INPUT_TYPE, INPUT_TYPE_CHECKBOX_GROUP);
        accountTypeClaimReq.setInputFormat(inputFormat);
        String accountTypeClaimId = claimManagementRestClient.addLocalClaim(accountTypeClaimReq);

        JSONObject claim = claimManagementRestClient.getLocalClaim(accountTypeClaimId);
        JSONArray subAttributes = (JSONArray) claim.get(ClaimConstants.SUB_ATTRIBUTES_PROPERTY);
        assert (subAttributes != null && subAttributes.size() == 1 &&
                subAttributes.get(0).equals(LOCAL_CLAIM_URI_PREFIX + ACCOUNT_ID_CLAIM));

        JSONArray canonicalValues = (JSONArray) claim.get(ClaimConstants.CANONICAL_VALUES_PROPERTY);
        assert (canonicalValues != null && canonicalValues.size() == 2
                && ((JSONObject) canonicalValues.get(0)).get("label").equals("Personal")
                && ((JSONObject) canonicalValues.get(0)).get("value").equals("personal")
                && ((JSONObject) canonicalValues.get(1)).get("label").equals("Work")
                && ((JSONObject) canonicalValues.get(1)).get("value").equals("work"));

        assert ((JSONObject) claim.get(ClaimConstants.INPUT_FORMAT_PROPERTY)).get(INPUT_TYPE)
                .equals(INPUT_TYPE_CHECKBOX_GROUP);
    }


    @Test(dependsOnMethods = {"testAddClaimComplexClaimWithCanonicalValues"})
    public void testDeleteClaim() throws Exception {

        claimManagementRestClient.deleteLocalClaim(ACCOUNT_ID_CLAIM);
        JSONObject response = claimManagementRestClient.getLocalClaim(ACCOUNT_ID_CLAIM);
        assert ERROR_CODE_LOCAL_CLAIM_NOT_FOUND.equals(response.get("code"));

        claimManagementRestClient.deleteLocalClaim(ACCOUNT_TYPE_CLAIM);
        response = claimManagementRestClient.getLocalClaim(ACCOUNT_TYPE_CLAIM);
        assert ERROR_CODE_LOCAL_CLAIM_NOT_FOUND.equals(response.get("code"));
    }

    @Test(dependsOnMethods = {"testDeleteClaim"})
    public void testAddClaimWithInvalidDataType() throws Exception {

        LocalClaimReq accountIDClaimReq = buildLocalClaimReq(ACCOUNT_ID_CLAIM, "invalid-data-type");
        claimManagementRestClient.addInvalidLocalClaim(accountIDClaimReq, 400);
    }

    private LocalClaimReq buildLocalClaimReq(String claim, String dataType) {

        LocalClaimReq localClaimReq = new LocalClaimReq();
        localClaimReq.setClaimURI(LOCAL_CLAIM_URI_PREFIX + claim);
        localClaimReq.setDisplayName(claim);
        localClaimReq.setDescription(claim);
        localClaimReq.setSupportedByDefault(SUPPORTED);
        localClaimReq.setRequired(REQUIRED);
        localClaimReq.setReadOnly(READONLY);
        localClaimReq.setDataType(dataType);

        AttributeMappingDTO mapping = new AttributeMappingDTO();
        mapping.setMappedAttribute(claim);
        mapping.setUserstore(DOMAIN);
        localClaimReq.setAttributeMapping(Collections.singletonList(mapping));
        return localClaimReq;
    }
}
