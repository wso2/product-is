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

import java.rmi.RemoteException;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.common.clients.entitlement.EntitlementPolicyServiceClient;
import org.wso2.carbon.identity.entitlement.stub.dto.PaginatedPolicySetDTO;
import org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.stub.dto.PublisherDataHolder;
import org.wso2.carbon.identity.entitlement.stub.dto.PublisherPropertyDTO;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

public class EntitlementPolicyAdminServiceTestCase extends ISIntegrationTest {

	private EntitlementPolicyServiceClient entitlementPolicyClient;
	private static final Log log = LogFactory.getLog(EntitlementPolicyAdminServiceTestCase.class);
	
	private static final String POLICY_1_ID = "urn:sample:xacml:2.0:custompolicy";
    private static final String POLICY_2_ID = "urn:sample:xacml:2.0:custompolicy2";
    private static final String POLICY_1_VERSION = "1";
	private static final String POLICY_1 = "<Policy xmlns='urn:oasis:names:tc:xacml:2.0:policy:schema:os' "
			+ "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
			+ "xsi:schemaLocation='urn:oasis:names:tc:xacml:2.0:policy:schema:os   access_control-xacml-2.0-policy-schema-os.xsd' "
			+ "PolicyId='"+ POLICY_1_ID +"' "
			+ "RuleCombiningAlgId='urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:deny-overrides'> "
			+ "<Description>   Custom policy </Description> "
			+ "<Target/> <Rule RuleId='urn:oasis:names:tc:xacml:2.0:conformance-test:IIA1:rule' Effect='Permit'>   "
			+ "<Description> admin can read or write echo service   </Description>   "
			+ "<Target> <Subjects> <Subject>   "
			+ "<SubjectMatch   MatchId='urn:oasis:names:tc:xacml:1.0:function:string-equal'> "
			+ "<AttributeValue DataType='http://www.w3.org/2001/XMLSchema#string'>admin</AttributeValue> "
			+ "<SubjectAttributeDesignator AttributeId='urn:oasis:names:tc:xacml:1.0:subject:subject-id' "
			+ "DataType='http://www.w3.org/2001/XMLSchema#string'/>   </SubjectMatch> </Subject> </Subjects> "
			+ "<Resources> <Resource>   <ResourceMatch   MatchId='urn:oasis:names:tc:xacml:1.0:function:string-equal'> "
			+ "<AttributeValue DataType='http://www.w3.org/2001/XMLSchema#string'>http://localhost:8280/services/echo</AttributeValue> "
			+ "<ResourceAttributeDesignator AttributeId='urn:oasis:names:tc:xacml:1.0:resource:resource-id' "
			+ "DataType='http://www.w3.org/2001/XMLSchema#string'/>   </ResourceMatch> </Resource> </Resources> "
			+ "<Actions> <Action>   <ActionMatch   MatchId='urn:oasis:names:tc:xacml:1.0:function:string-equal'> "
			+ "<AttributeValue DataType='http://www.w3.org/2001/XMLSchema#string'>read</AttributeValue> "
			+ "<ActionAttributeDesignator AttributeId='urn:oasis:names:tc:xacml:1.0:action:action-id' "
			+ "DataType='http://www.w3.org/2001/XMLSchema#string'/>   </ActionMatch> </Action> <Action>   "
			+ "<ActionMatch   MatchId='urn:oasis:names:tc:xacml:1.0:function:string-equal'> "
			+ "<AttributeValue DataType='http://www.w3.org/2001/XMLSchema#string'>write</AttributeValue> "
			+ "<ActionAttributeDesignator AttributeId='urn:oasis:names:tc:xacml:1.0:action:action-id' "
			+ "DataType='http://www.w3.org/2001/XMLSchema#string'/>   </ActionMatch> </Action> </Actions>   "
			+ "</Target> </Rule> </Policy>";

    private static final String POLICY_2 = "<Policy xmlns='urn:oasis:names:tc:xacml:2.0:policy:schema:os' "
            + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
            + "xsi:schemaLocation='urn:oasis:names:tc:xacml:2.0:policy:schema:os   access_control-xacml-2.0-policy-schema-os.xsd' "
            + "PolicyId='"+ POLICY_2_ID +"' "
            + "RuleCombiningAlgId='urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:deny-overrides'> "
            + "<Description>   Custom policy </Description> "
            + "<Target/> <Rule RuleId='urn:oasis:names:tc:xacml:2.0:conformance-test:IIA1:rule' Effect='Permit'>   "
            + "<Description> admin can read or write echo service   </Description>   "
            + "<Target> <Subjects> <Subject>   "
            + "<SubjectMatch   MatchId='urn:oasis:names:tc:xacml:1.0:function:string-equal'> "
            + "<AttributeValue DataType='http://www.w3.org/2001/XMLSchema#string'>admin</AttributeValue> "
            + "<SubjectAttributeDesignator AttributeId='urn:oasis:names:tc:xacml:1.0:subject:subject-id' "
            + "DataType='http://www.w3.org/2001/XMLSchema#string'/>   </SubjectMatch> </Subject> </Subjects> "
            + "<Resources> <Resource>   <ResourceMatch   MatchId='urn:oasis:names:tc:xacml:1.0:function:string-equal'> "
            + "<AttributeValue DataType='http://www.w3.org/2001/XMLSchema#string'>http://localhost:8280/services/echo</AttributeValue> "
            + "<ResourceAttributeDesignator AttributeId='urn:oasis:names:tc:xacml:1.0:resource:resource-id' "
            + "DataType='http://www.w3.org/2001/XMLSchema#string'/>   </ResourceMatch> </Resource> </Resources> "
            + "<Actions> <Action>   <ActionMatch   MatchId='urn:oasis:names:tc:xacml:1.0:function:string-equal'> "
            + "<AttributeValue DataType='http://www.w3.org/2001/XMLSchema#string'>read</AttributeValue> "
            + "<ActionAttributeDesignator AttributeId='urn:oasis:names:tc:xacml:1.0:action:action-id' "
            + "DataType='http://www.w3.org/2001/XMLSchema#string'/>   </ActionMatch> </Action> <Action>   "
            + "<ActionMatch   MatchId='urn:oasis:names:tc:xacml:1.0:function:string-equal'> "
            + "<AttributeValue DataType='http://www.w3.org/2001/XMLSchema#string'>write</AttributeValue> "
            + "<ActionAttributeDesignator AttributeId='urn:oasis:names:tc:xacml:1.0:action:action-id' "
            + "DataType='http://www.w3.org/2001/XMLSchema#string'/>   </ActionMatch> </Action> </Actions>   "
            + "</Target> </Rule> </Policy>";
	
	@BeforeClass(alwaysRun = true)
	public void testInit() throws Exception {
		super.init();
		entitlementPolicyClient = new EntitlementPolicyServiceClient(backendURL,sessionCookie);
	}

	@AfterClass(alwaysRun = true)
	public void atEnd() throws Exception {

//		TODO - Null pointer exception when no policies are available.
//		if(Arrays.asList(entitlementPolicyClient.getAllPolicyIds("*")).contains(POLICY_1_ID)) {
//			entitlementPolicyClient.removePolicy(POLICY_1_ID, false);
//		}

	}

	@Test(groups = "wso2.is", description = "Check adding a policy")
	public void testAddPolicy() throws Exception {
		PolicyDTO policy = new PolicyDTO();
		policy.setPolicy(POLICY_1);
		entitlementPolicyClient.addPolicy(policy);
	}

	@Test(groups = "wso2.is", description = "Check get specific policy", dependsOnMethods="testAddPolicy")
	public void testGetPolicy() throws Exception {
		PolicyDTO policyDTO = entitlementPolicyClient.getPolicy(POLICY_1_ID, false);
		Assert.assertEquals(POLICY_1_ID, policyDTO.getPolicyId(), "Getting the policy with specified Id has failed.");
	}
	
	@Test(groups = "wso2.is", description = "Check get all policy ids", dependsOnMethods="testGetPolicy")
	public void testGetAllPolicyIds() throws Exception {		
		Assert.assertTrue(Arrays.asList(entitlementPolicyClient.getAllPolicyIds("*")).contains(POLICY_1_ID), "Getting all policies has failed.");
	}
	
	@Test(groups = "wso2.is", description = "Check get policy versions", dependsOnMethods="testGetAllPolicyIds")
	public void testGetPolicyVersions() throws Exception {
		Assert.assertTrue(Arrays.asList(entitlementPolicyClient.getPolicyVersions(POLICY_1_ID)).contains(POLICY_1_VERSION), "Getting policy versions has failed.");
	}
	
	@Test(groups = "wso2.is", description = "Check get policy by version", dependsOnMethods="testGetPolicyVersions")
	public void testGetPolicyByVersion() throws Exception {
		PolicyDTO policyDTO = entitlementPolicyClient.getPolicyByVersion(POLICY_1_ID, POLICY_1_VERSION);
		Assert.assertTrue(POLICY_1_ID.equals(policyDTO.getPolicyId()), "Getting policy by version has failed.");
	}
	
	@Test(groups = "wso2.is", description = "Check get policy with params", dependsOnMethods="testGetPolicyByVersion")
	public void testGetAllPoliciesWithParam() throws Exception {
		PaginatedPolicySetDTO policyDTOPaginated = entitlementPolicyClient.getAllPolicies("ALL", "*", 0, false);
		PolicyDTO[] policyDTOs = policyDTOPaginated.getPolicySet();
		boolean exists = false;
		
		for (PolicyDTO policy : policyDTOs) {
			if(POLICY_1_ID.equals(policy.getPolicyId())) {
				exists = true;
				break;
			}
		}
		Assert.assertTrue(exists, "Getting the policy with params has failed.");
	}

    @Test(groups = "wso2.is", description = "Check Getting status data", dependsOnMethods="testGetAllPoliciesWithParam")
    public void testGetStatusData() throws Exception{
        Assert.assertNotNull(entitlementPolicyClient.getStatusData("*", "*", "urn", "*", 1));
    }

    @Test(groups = "wso2.is", description = "Check getting subscriber", dependsOnMethods="testGetStatusData")
    public void testAddSubscriber() throws Exception{
        PublisherDataHolder holder = new PublisherDataHolder();
        holder.setModuleName("test");

        PublisherPropertyDTO propertyDto = new PublisherPropertyDTO();
        propertyDto.setId("subscriberId");
        propertyDto.setValue("1001");
        PublisherPropertyDTO[] propertyDTOs = new PublisherPropertyDTO[1];
        propertyDTOs[0]= propertyDto;
        holder.setPropertyDTOs(propertyDTOs);
        entitlementPolicyClient.addSubscriber(holder);
    }

    @Test(groups = "wso2.is", description = "Check getting subscriber", dependsOnMethods="testAddSubscriber")
    public void testGetSubscriber() throws Exception{
//        Assert.assertNotNull(entitlementPolicyClient.getSubscriber("test"));
        entitlementPolicyClient.getSubscriber("1001");
    }

    @Test(groups = "wso2.is", description = "Check getting subscriber Ids", dependsOnMethods="testGetSubscriber")
    public void testGetSubscriberIds() throws Exception{
        Assert.assertNotNull(entitlementPolicyClient.getSubscriberIds("*"));
    }

    @Test(groups = "wso2.is", description = "Check getting subscriber Ids", dependsOnMethods="testGetSubscriberIds")
    public void testPublish() throws Exception{
        entitlementPolicyClient.publish(POLICY_1_ID);
    }

    @Test(groups = "wso2.is", description = "Check getting subscriber Ids", dependsOnMethods="testPublish")
    public void testOrderPolicy() throws Exception{
        entitlementPolicyClient.orderPolicy(POLICY_1_ID, 1);
    }

//    @Test(groups = "wso2.is", description = "Check getting subscriber Ids", dependsOnMethods="testOrderPolicy")
//    public void testEnableDisablePolicy() throws Exception{
//        entitlementPolicyClient.enableDisablePolicy(POLICY_1_ID, false);
//        entitlementPolicyClient.enableDisablePolicy(POLICY_1_ID, true);
//    }

    @Test(groups = "wso2.is", description = "Check getting subscriber", dependsOnMethods="testOrderPolicy")
    public void testUpdateSubscriber() throws Exception{
        PublisherDataHolder holder = new PublisherDataHolder();
        holder.setModuleName("test2");
        PublisherPropertyDTO propertyDto = new PublisherPropertyDTO();
        propertyDto.setId("subscriberId");
        propertyDto.setValue("1002");
        PublisherPropertyDTO[] propertyDTOs = new PublisherPropertyDTO[1];
        propertyDTOs[0]= propertyDto;
        holder.setPropertyDTOs(propertyDTOs);

        entitlementPolicyClient.updateSubscriber(holder);
    }

    @Test(groups = "wso2.is", description = "Check getting subscriber", dependsOnMethods="testUpdateSubscriber")
    public void testDeleteSubscriber() throws Exception{
        entitlementPolicyClient.deleteSubscriber("1002");
        entitlementPolicyClient.deleteSubscriber("1001");
    }

	@Test(groups = "wso2.is", description = "Check import policy from registry", dependsOnMethods="testDeleteSubscriber", expectedExceptions=RemoteException.class)
	public void testImportPolicyFromRegistry() throws Exception {
//		TODO - need to modify this to have success import without the exception below.
		entitlementPolicyClient.importPolicyFromRegistry("InvalidRegistry:Path/test");
	}
	
	@Test(groups = "wso2.is", description = "Check remove policy by id", dependsOnMethods="testImportPolicyFromRegistry")
	public void testRemovePolicy() throws Exception {
		entitlementPolicyClient.removePolicy(POLICY_1_ID, false);
//		TODO - Null pointer when no policies exists by getAllPolicyIds.
//		Assert.assertNotNull(entitlementPolicyClient.getAllPolicyIds("*"), "Possible null pointer exception. Instead return empty array");
//		Assert.assertFalse(Arrays.asList(entitlementPolicyClient.getAllPolicyIds("*")).contains(POLICY_1_ID), "Remove policy after adding has failed.");
	}

    @Test(groups = "wso2.is", description = "Check adding policies", dependsOnMethods = "testRemovePolicy")
    public void testAddPolicies() throws Exception {
        PolicyDTO policy = new PolicyDTO();
        policy.setPolicy(POLICY_2);
        PolicyDTO[] policies = new PolicyDTO[1];
        policies[0] = policy;
        entitlementPolicyClient.addPolicies(policies);

        PolicyDTO returnPolicies = entitlementPolicyClient.getPolicy(POLICY_2_ID, false);
        Assert.assertNotNull(returnPolicies, "Addling policy list has failed with null return.");
        Assert.assertEquals(POLICY_2_ID, returnPolicies.getPolicyId(), "Adding policy list has failed.");


    }

    @Test(groups = "wso2.is", description = "Check remove policies", dependsOnMethods="testAddPolicies")
    public void testRemovePolicies() throws Exception {
        entitlementPolicyClient.removePolicies(new String[]{POLICY_2_ID}, false);
//		TODO - Null pointer when no policies exists by getAllPolicyIds.
//		Assert.assertNotNull(entitlementPolicyClient.getAllPolicyIds("*"), "Possible null pointer exception. Instead return empty array");
//		Assert.assertFalse(Arrays.asList(entitlementPolicyClient.getAllPolicyIds("*")).contains(POLICY_2_ID), "Remove policy after adding has failed.");
    }
}
