/**
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.identity.integration.test.entitlement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.common.clients.UserProfileMgtServiceClient;
import org.wso2.identity.integration.common.clients.entitlement.EntitlementPolicyServiceClient;
import org.wso2.identity.integration.common.clients.entitlement.EntitlementServiceClient;
import org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;
import org.wso2.carbon.identity.entitlement.stub.EntitlementServiceException;
import org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException;
import org.wso2.carbon.identity.user.profile.stub.types.UserFieldDTO;
import org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO;

import java.rmi.RemoteException;

public class EntitlementServiceTestCase extends ISIntegrationTest {
    private static final Log log = LogFactory.getLog(EntitlementServiceTestCase.class);
    private EntitlementServiceClient entitlementServiceClient;
    private EntitlementPolicyServiceClient entitlementPolicyClient;
    private UserProfileMgtServiceClient userProfileMgtClient;

    private static final String REQUEST = "<Request xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" " +
            "CombinedDecision=\"false\" ReturnPolicyIdList=\"false\">" +
            "<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\">" +
            "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" IncludeInResult=\"false\">" +
            "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">read</AttributeValue></Attribute></Attributes>" +
            "<Attributes Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\">" +
            "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject:subject-id\" IncludeInResult=\"false\">" +
            "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">admin</AttributeValue></Attribute></Attributes>" +
            "<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\">" +
            "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\" IncludeInResult=\"false\">" +
            "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">http://localhost:8280/services/echo/</AttributeValue>" +
            "</Attribute></Attributes></Request>";
    private static final String POLICY_ID = "urn:oasis:names:tc:xacml:3.0:custompolicy";
    private static final String POLICY = "<Policy xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\"  " +
            "PolicyId=\"urn:oasis:names:tc:xacml:3.0:custompolicy\" " +
            "RuleCombiningAlgId=\"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable\" Version=\"1.0\">" +
            "<Description>sample policy</Description><Target></Target><Rule Effect=\"Permit\" RuleId=\"primary-group-customer-rule\">" +
            "<Target><AnyOf><AllOf><Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-regexp-match\">" +
            "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">http://localhost:8280/services/echo/</AttributeValue>" +
            "<AttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\" " +
            "Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" " +
            "MustBePresent=\"true\"></AttributeDesignator></Match><Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\">" +
            "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">read</AttributeValue><AttributeDesignator " +
            "AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\" " +
            "DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"true\"></AttributeDesignator></Match></AllOf></AnyOf></Target>" +
            "<Condition><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-subset\">" +
            "<Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-bag\">" +
            "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">SL</AttributeValue></Apply>" +
            "<AttributeDesignator AttributeId=\"http://wso2.org/claims/country\" " +
            "Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" " +
            "MustBePresent=\"true\"></AttributeDesignator></Apply></Condition></Rule><Rule Effect=\"Deny\" RuleId=\"deny-rule\"></Rule></Policy>";


    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        entitlementPolicyClient = new EntitlementPolicyServiceClient(backendURL, sessionCookie);
        entitlementServiceClient = new EntitlementServiceClient(backendURL, sessionCookie);
        userProfileMgtClient = new UserProfileMgtServiceClient(backendURL, sessionCookie);
    }

    @Test(groups = "wso2.is", description = "Check set user profile")
    public void testSetUserProfile() throws RemoteException, UserProfileMgtServiceUserProfileExceptionException {
        UserProfileDTO profile = userProfileMgtClient.getUserProfile("admin", "default");
        UserFieldDTO country = new UserFieldDTO();
        country.setClaimUri("http://wso2.org/claims/country");
        country.setFieldValue("SL");
        UserFieldDTO[] fields = profile.getFieldValues();
        UserFieldDTO[] newfields = new UserFieldDTO[fields.length];
        for (int i=0; i<fields.length; i++){
            if(fields[i].getDisplayName().toString().equals("Country")){
                newfields[i] = country;
            }else{
                newfields[i] = fields[i];
            }
        }
        profile.setFieldValues(newfields);
        userProfileMgtClient.setUserProfile("admin", profile);
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testSetUserProfile"}, description = "Check publish policy")
    public void testPublishPolicy() throws Exception {
        PolicyDTO policy = new PolicyDTO();
        policy.setPolicy(POLICY);
        policy.setPolicy(policy.getPolicy().replaceAll(">\\s+<", "><").trim());
        policy.setVersion("3.0");
        policy.setPolicyId(POLICY_ID);
        Thread.sleep(10000); //waiting till server gets ready
        entitlementPolicyClient.addPolicy(policy);
        Thread.sleep(5000); // waiting for the policy to deploy
        entitlementPolicyClient.publishPolicies(new String[]{POLICY_ID}, new String[]{"PDP Subscriber"}, "CREATE", true, null, 1);
        Assert.assertNotNull(entitlementPolicyClient.getPolicy(POLICY_ID, true), "Entitlement service publish policy failed.");
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testPublishPolicy"}, description = "Check get decision")
    public void testGetDecision() throws EntitlementServiceException, RemoteException,
            EntitlementPolicyAdminServiceEntitlementException {
        String decision = entitlementServiceClient.getDecision(REQUEST);
        log.info(decision);
        Assert.assertTrue(decision.contains("Permit"), "Entitlement service get decision failed.");
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testPublishPolicy"}, description = "Check get decision by attributes")
    public void testGetDecisionByAttributes() throws EntitlementServiceException, RemoteException {
        String decision = entitlementServiceClient.getDecisionByAttributes("admin", "http://localhost:8280/services/echo/", "read", null);
        log.info(decision);
        Assert.assertTrue(decision.contains("Permit"), "Entitlement service get decision failed.");
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testGetDecisionByAttributes"}, description = "Check get decision deny state")
    public void testGetDecisionDenyState() throws Exception {
        UserProfileDTO profile = userProfileMgtClient.getUserProfile("admin", "default");
        UserFieldDTO country = new UserFieldDTO();
        country.setClaimUri("http://wso2.org/claims/country");
        country.setFieldValue("USA");
        UserFieldDTO[] fields = profile.getFieldValues();
        UserFieldDTO[] newfields = new UserFieldDTO[fields.length];
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getDisplayName().toString().equals("Country")) {
                newfields[i] = country;
            } else {
                newfields[i] = fields[i];
            }
        }
        profile.setFieldValues(newfields);
        userProfileMgtClient.setUserProfile("admin", profile);
        Thread.sleep(5000);
        String decision = entitlementServiceClient.getDecisionByAttributes("admin", "http://localhost:8280/services/echo/", "read", null);
        log.info(decision);
        Assert.assertTrue(decision.contains("Deny"), "Entitlement service get decision failed.");
    }
}
