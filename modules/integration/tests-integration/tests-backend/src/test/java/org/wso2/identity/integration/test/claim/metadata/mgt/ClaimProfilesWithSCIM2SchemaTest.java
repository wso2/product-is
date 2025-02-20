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

package org.wso2.identity.integration.test.claim.metadata.mgt;

import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.AttributeMappingDTO;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.AttributeProfileDTO;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.ClaimDialectReqDTO;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.ExternalClaimReq;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.LocalClaimReq;
import org.wso2.identity.integration.test.restclients.ClaimManagementRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

import java.util.Collections;
import java.util.HashMap;

/**
 * Test class to verify the claim profiles with SCIM2 schemas endpoint.
 */
public class ClaimProfilesWithSCIM2SchemaTest extends ISIntegrationTest {

    private static final String LOCAL_CLAIM_URI_PREFIX = "http://wso2.org/claims/";
    private static final String SCIM2_CUSTOM_SCHEMA_DIALECT_URI = "urn:scim:schemas:extension:custom:User";
    private static final String ENCODED_SCIM2_CUSTOM_SCHEMA_DIALECT_URI =
            "dXJuOnNjaW06c2NoZW1hczpleHRlbnNpb246Y3VzdG9tOlVzZXI";
    private static final String USERSTORE_DOMAIN = "PRIMARY";

    private static final String ID_PROPERTY = "id";
    private static final String ATTRIBUTES_PROPERTY = "attributes";
    private static final String NAME_PROPERTY = "name";
    private static final String PROFILES_PROPERTY = "profiles";
    private static final String SUPPORTED_BY_DEFAULT_PROPERTY = "supportedByDefault";
    private static final String REQUIRED_PROPERTY = "required";
    private static final String READ_ONLY_PROPERTY = "readOnly";
    private static final String MUTABILITY_PROPERTY = "mutability";
    private static final String READ_ONLY_VALUE = "READ_ONLY";
    private static final String READ_WRITE_VALUE = "READ_WRITE";

    ClaimManagementRestClient claimManagementRestClient;
    SCIM2RestClient scim2RestClient;

    private String consoleLocalClaimId;
    private String consoleExternalClaimId;
    private String endUserLocalClaimId;
    private String endUserExternalClaimId;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init();
        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        claimManagementRestClient = new ClaimManagementRestClient(serverURL, tenantInfo);

        // Add custom scim2 claim dialect.
        ClaimDialectReqDTO claimDialectReqDTO = new ClaimDialectReqDTO();
        claimDialectReqDTO.setDialectURI(SCIM2_CUSTOM_SCHEMA_DIALECT_URI);
        org.json.simple.JSONObject externalDialect =
                claimManagementRestClient.getExternalDialect(ENCODED_SCIM2_CUSTOM_SCHEMA_DIALECT_URI);
        boolean isExistingDialect = isExistingDialect(externalDialect);
        if (!isExistingDialect) {
            claimManagementRestClient.addExternalDialect(claimDialectReqDTO);
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanUpClass() throws Exception {

        if (StringUtils.isNotBlank(consoleExternalClaimId)) {
            claimManagementRestClient.deleteExternalClaim(ENCODED_SCIM2_CUSTOM_SCHEMA_DIALECT_URI,
                    consoleExternalClaimId);
        }
        if (StringUtils.isNotBlank(endUserExternalClaimId)) {
            claimManagementRestClient.deleteExternalClaim(ENCODED_SCIM2_CUSTOM_SCHEMA_DIALECT_URI,
                    endUserExternalClaimId);
        }
        claimManagementRestClient.deleteExternalDialect(ENCODED_SCIM2_CUSTOM_SCHEMA_DIALECT_URI);
        if (StringUtils.isNotBlank(consoleLocalClaimId)) {
            claimManagementRestClient.deleteLocalClaim(consoleLocalClaimId);
        }
        if (StringUtils.isNotBlank(endUserLocalClaimId)) {
            claimManagementRestClient.deleteLocalClaim(endUserLocalClaimId);
        }
    }

    @Test
    public void testSupportedByConsoleClaim() throws Exception {

        String claimName = "consoletest1";
        String localClaimURI = LOCAL_CLAIM_URI_PREFIX + claimName;
        String externalClaimURI = SCIM2_CUSTOM_SCHEMA_DIALECT_URI + ":" + claimName;
        String profileName = "console";

        HashMap<String, AttributeProfileDTO> attributeProfiles = new HashMap<>();
        AttributeProfileDTO attributeProfile = new AttributeProfileDTO();
        attributeProfile.setSupportedByDefault(true);
        attributeProfile.setRequired(true);
        attributeProfile.setReadOnly(false);
        attributeProfiles.put(profileName, attributeProfile);

        LocalClaimReq localClaimReq = createLocalClaim(claimName, false, false,
                true, attributeProfiles);

        consoleLocalClaimId = claimManagementRestClient.addLocalClaim(localClaimReq);
        Assert.assertNotNull(consoleLocalClaimId, "Claim addition failed");

        // Assert local claim response.
        JSONObject localClaimResponse = claimManagementRestClient.getExternalClaim("local", consoleLocalClaimId);
        JSONObject localClaimProfile =
                (JSONObject) ((JSONObject) localClaimResponse.get(PROFILES_PROPERTY)).get(profileName);
        Assert.assertNotNull(localClaimProfile);
        Assert.assertTrue((Boolean) localClaimProfile.get(SUPPORTED_BY_DEFAULT_PROPERTY));
        Assert.assertTrue((Boolean) localClaimProfile.get(REQUIRED_PROPERTY));
        Assert.assertFalse((Boolean) localClaimProfile.get(READ_ONLY_PROPERTY));

        // Add external claim.
        ExternalClaimReq externalClaimReq = new ExternalClaimReq();
        externalClaimReq.setClaimURI(externalClaimURI);
        externalClaimReq.setMappedLocalClaimURI(localClaimURI);

        consoleExternalClaimId =
                claimManagementRestClient.addExternalClaim(ENCODED_SCIM2_CUSTOM_SCHEMA_DIALECT_URI, externalClaimReq);
        Assert.assertNotNull(consoleExternalClaimId, "External claim addition failed");

        // Assert SCIM2/Schemas response.
        JSONArray scim2Schemas = scim2RestClient.getScim2Schemas();

        Assert.assertNotNull(scim2Schemas, "SCIM2 schemas retrieval failed");
        Assert.assertFalse(scim2Schemas.isEmpty(), "SCIM2 schemas array is empty");

        JSONObject attributeSchema = getSchemaProfileForAttribute(scim2Schemas, claimName);
        Assert.assertNotNull(attributeSchema, "Attribute schema not found");

        JSONObject attributeSchemaProfile = getAttributeProfile(attributeSchema, profileName);
        Assert.assertNotNull(attributeSchemaProfile, "Attribute profile not found");

        Assert.assertTrue((Boolean) attributeSchemaProfile.get(SUPPORTED_BY_DEFAULT_PROPERTY));
        Assert.assertTrue((Boolean) attributeSchemaProfile.get(REQUIRED_PROPERTY));
        Assert.assertEquals(attributeSchemaProfile.get(MUTABILITY_PROPERTY), READ_WRITE_VALUE);
    }

    @Test
    public void testSupportedByEndUserClaim() throws Exception {

        String claimName = "endusertest1";
        String localClaimURI = LOCAL_CLAIM_URI_PREFIX + claimName;
        String externalClaimURI = SCIM2_CUSTOM_SCHEMA_DIALECT_URI + ":" + claimName;
        String profileName = "endUser";

        HashMap<String, AttributeProfileDTO> attributeProfiles = new HashMap<>();
        AttributeProfileDTO attributeProfile = new AttributeProfileDTO();
        attributeProfile.setSupportedByDefault(true);
        attributeProfile.setRequired(false);
        attributeProfile.setReadOnly(true);
        attributeProfiles.put(profileName, attributeProfile);

        LocalClaimReq localClaimReq = createLocalClaim(claimName, false, false,
                true, attributeProfiles);

        endUserLocalClaimId = claimManagementRestClient.addLocalClaim(localClaimReq);
        Assert.assertNotNull(endUserLocalClaimId, "Claim addition failed");

        // Assert local claim response.
        JSONObject localClaimResponse =
                claimManagementRestClient.getExternalClaim("local", endUserLocalClaimId);
        JSONObject localClaimProfile =
                (JSONObject) ((JSONObject) localClaimResponse.get(PROFILES_PROPERTY)).get(profileName);
        Assert.assertNotNull(localClaimProfile);
        Assert.assertTrue((Boolean) localClaimProfile.get(SUPPORTED_BY_DEFAULT_PROPERTY));
        Assert.assertNull(localClaimProfile.get(REQUIRED_PROPERTY));
        Assert.assertNull(localClaimProfile.get(READ_ONLY_PROPERTY));

        // Add external claim.
        ExternalClaimReq externalClaimReq = new ExternalClaimReq();
        externalClaimReq.setClaimURI(externalClaimURI);
        externalClaimReq.setMappedLocalClaimURI(localClaimURI);

        endUserExternalClaimId =
                claimManagementRestClient.addExternalClaim(ENCODED_SCIM2_CUSTOM_SCHEMA_DIALECT_URI, externalClaimReq);
        Assert.assertNotNull(endUserExternalClaimId, "External claim addition failed");

        // Assert SCIM2/Schemas response.
        JSONArray scim2Schemas = scim2RestClient.getScim2Schemas();

        Assert.assertNotNull(scim2Schemas, "SCIM2 schemas retrieval failed");
        Assert.assertFalse(scim2Schemas.isEmpty(), "SCIM2 schemas array is empty");

        JSONObject attributeSchema = getSchemaProfileForAttribute(scim2Schemas, claimName);
        Assert.assertNotNull(attributeSchema, "Attribute schema not found");

        JSONObject attributeSchemaProfile = getAttributeProfile(attributeSchema, profileName);
        Assert.assertNotNull(attributeSchemaProfile, "Attribute profile not found");

        Assert.assertTrue((Boolean) attributeSchemaProfile.get(SUPPORTED_BY_DEFAULT_PROPERTY));
        Assert.assertNull(attributeSchemaProfile.get(REQUIRED_PROPERTY));
        Assert.assertNull(attributeSchemaProfile.get(MUTABILITY_PROPERTY));
    }

    private JSONObject getSchemaProfileForAttribute(JSONArray scim2Schemas, String claimName) {

        JSONObject targetSchema = null;
        for (Object schemaObject : scim2Schemas) {
            JSONObject schema = (JSONObject) schemaObject;
            if (SCIM2_CUSTOM_SCHEMA_DIALECT_URI.equals(schema.get(ID_PROPERTY))) {
                targetSchema = schema;
                break;
            }
        }
        Assert.assertNotNull(targetSchema,
                String.format("Schema with SCIM URI '%s' not found", SCIM2_CUSTOM_SCHEMA_DIALECT_URI));

        JSONArray attributes = (JSONArray) targetSchema.get(ATTRIBUTES_PROPERTY);
        Assert.assertNotNull(attributes, "Attributes array not found in the schema");

        // Locate the target attribute by its name.
        JSONObject targetAttribute = null;
        for (Object attributeObject : attributes) {
            JSONObject attribute = (JSONObject) attributeObject;
            if (claimName.equals(attribute.get(NAME_PROPERTY))) {
                targetAttribute = attribute;
                break;
            }
        }
        Assert.assertNotNull(targetAttribute, String.format("Attribute with claim name '%s' not found", claimName));
        return targetAttribute;
    }

    private JSONObject getAttributeProfile(JSONObject attribute, String profileName) {

        JSONObject profiles = (JSONObject) attribute.get(PROFILES_PROPERTY);
        Assert.assertNotNull(profiles, "Profiles object not found in the attribute");
        JSONObject profile = (JSONObject) profiles.get(profileName);
        Assert.assertNotNull(profile, String.format("Profile with name '%s' not found in profiles", profileName));
        return profile;
    }

    private LocalClaimReq createLocalClaim(String name, boolean supportedByDefault, boolean required, boolean readOnly,
                                           HashMap<String, AttributeProfileDTO> attributeProfiles) {

        LocalClaimReq localClaimReq = new LocalClaimReq();
        AttributeMappingDTO attributeMappingDTO = new AttributeMappingDTO();
        attributeMappingDTO.setMappedAttribute(name);
        attributeMappingDTO.setUserstore(USERSTORE_DOMAIN);

        localClaimReq.setClaimURI(LOCAL_CLAIM_URI_PREFIX + name);
        localClaimReq.setDisplayName(name);
        localClaimReq.setDescription(name);
        localClaimReq.setSupportedByDefault(supportedByDefault);
        localClaimReq.setRequired(required);
        localClaimReq.setReadOnly(readOnly);
        localClaimReq.setAttributeMapping(Collections.singletonList(attributeMappingDTO));
        localClaimReq.setProfiles(attributeProfiles);
        return localClaimReq;
    }

    private boolean isExistingDialect(org.json.simple.JSONObject externalDialectGetResponse) {

        if (externalDialectGetResponse.get("code") != null &&
                externalDialectGetResponse.get("code").equals("CMT-50016")) {
            return false;
        }
        return externalDialectGetResponse.get("id") != null;
    }
}
