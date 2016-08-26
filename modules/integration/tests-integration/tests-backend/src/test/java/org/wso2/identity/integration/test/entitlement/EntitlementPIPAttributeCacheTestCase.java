/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;
import org.wso2.carbon.identity.entitlement.stub.EntitlementServiceException;
import org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.common.clients.entitlement.EntitlementPolicyServiceClient;
import org.wso2.identity.integration.common.clients.entitlement.EntitlementServiceClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;

import java.io.File;
import java.io.FileFilter;
import java.rmi.RemoteException;

/**
 * This class contains test case to test the use case described in https://wso2.org/jira/browse/IDENTITY-4740
 */
public class EntitlementPIPAttributeCacheTestCase extends ISIntegrationTest {
    private EntitlementServiceClient entitlementServiceClient;
    private EntitlementPolicyServiceClient entitlementPolicyClient;
    private ServerConfigurationManager scm;

    private static final String POLICY_ID = "urn:oasis:names:tc:xacml:3.0:IDENTITY4740";
    private static final String POLICY = "<Policy xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" " +
            "PolicyId=\"urn:oasis:names:tc:xacml:3.0:IDENTITY4740\" " +
            "RuleCombiningAlgId=\"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable\" " +
            "Version=\"1.0\">\n" +
            "   <Target>\n" +
            "      <AnyOf>\n" +
            "         <AllOf>\n" +
            "            <Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\">\n" +
            "               <AttributeValue DataType=\"http://www.w3" +
            ".org/2001/XMLSchema#string\">fooIDENTITY4740</AttributeValue>\n" +
            "               <AttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\" " +
            "Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\" DataType=\"http://www.w3" +
            ".org/2001/XMLSchema#string\" MustBePresent=\"true\"/>\n" +
            "            </Match>\n" +
            "         </AllOf>\n" +
            "      </AnyOf>\n" +
            "   </Target>\n" +
            "   <Rule Effect=\"Permit\" RuleId=\"Rule-1\">\n" +
            "      <Target>\n" +
            "         <AnyOf>\n" +
            "            <AllOf>\n" +
            "               <Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\">\n" +
            "                  <AttributeValue DataType=\"http://www.w3" +
            ".org/2001/XMLSchema#string\">readIDENTITY4740</AttributeValue>\n" +
            "                  <AttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" " +
            "Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\" DataType=\"http://www.w3" +
            ".org/2001/XMLSchema#string\" MustBePresent=\"true\"/>\n" +
            "               </Match>\n" +
            "            </AllOf>\n" +
            "         </AnyOf>\n" +
            "      </Target>\n" +
            "      <Condition>\n" +
            "         <Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:any-of\">\n" +
            "            <Function FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"/>\n" +
            "            <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">admin@wso2" +
            ".com</AttributeValue>\n" +
            "            <AttributeDesignator AttributeId=\"http://wso2.org/claims/emailaddress\" " +
            "Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\" DataType=\"http://www.w3" +
            ".org/2001/XMLSchema#string\" MustBePresent=\"true\"/>\n" +
            "         </Apply>\n" +
            "      </Condition>\n" +
            "   </Rule>\n" +
            "   <Rule Effect=\"Deny\" RuleId=\"Deny-Rule\"/>\n" +
            "</Policy>        " +
            "";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        scm = new ServerConfigurationManager(isServer);
        entitlementPolicyClient = new EntitlementPolicyServiceClient(backendURL, sessionCookie);
        addPolicy();
        addCustomAttributeFinder();
        //call super init since server restart happens in previous step
        super.init();
        entitlementServiceClient = new EntitlementServiceClient(backendURL, sessionCookie);
    }

    @Test(groups = "wso2.is", description = "Check get decision")
    public void testGetPermitDecision() throws EntitlementServiceException, RemoteException,
                                               EntitlementPolicyAdminServiceEntitlementException {
        String decision = entitlementServiceClient.getDecision(buildRequest("admin@wso2.com"));
        Assert.assertTrue(decision.contains("Permit"), "Entitlement service get decision failed.");
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testGetPermitDecision"}, description = "Check get decision")
    public void testGetNotPermitDecision() throws EntitlementServiceException, RemoteException,
                                                  EntitlementPolicyAdminServiceEntitlementException {
        String decision = entitlementServiceClient.getDecision(buildRequest("non.existing.email@wso2.com"));
        Assert.assertTrue(!decision.contains("Permit"), "Entitlement service get decision failed.");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        //Remove added policy
        entitlementPolicyClient = new EntitlementPolicyServiceClient(backendURL, sessionCookie);
        entitlementPolicyClient.enableDisablePolicy(POLICY_ID, false);
        entitlementPolicyClient.removePolicy(POLICY_ID);
        //Copy default config file
        File srcConfigFile = new File(getISResourceLocation()
                                              + File.separator + "entitlement" + File.separator
                                               + "config" + File.separator
                                              + "entitlement_default.properties");
        File targetConfigFile = new File(
                System.getProperty(ServerConstants.CARBON_HOME) + File.separator + "repository"
                        + File.separator + "conf" + File.separator + "identity" + File.separator +
                        "entitlement.properties");

        scm.applyConfigurationWithoutRestart(srcConfigFile, targetConfigFile, true);
        //remove custom attribute finder from lib
        scm.removeFromComponentLib("org.wso2.carbon.identity.custom.pip-4.2.2.jar");
        scm.restartGracefully();
    }

    private void addPolicy() throws Exception {
        PolicyDTO policy = new PolicyDTO();
        policy.setPolicy(POLICY);
        policy.setPolicy(policy.getPolicy().replaceAll(">\\s+<", "><").trim());
        policy.setVersion("3.0");
        policy.setPolicyId(POLICY_ID);
        entitlementPolicyClient.addPolicy(policy);
        entitlementPolicyClient.publishPolicies(new String[]{POLICY_ID}, new String[]{"PDP Subscriber"}, "CREATE",
                                                true, null, 1);
    }

    private void addCustomAttributeFinder() throws Exception {
        File jarFile = getCustomAttributeFinder();
        scm.copyToComponentLib(jarFile);

        //Copy entitlement.properties
        File srcConfigFile = new File(getISResourceLocation()
                                              + File.separator + "entitlement" + File.separator
                                              +  "config" +  File.separator +
                                              "entitlement_custom_attribute_finder.properties");
        File targetConfigFile = new File(
                System.getProperty(ServerConstants.CARBON_HOME) + File.separator + "repository"
                        + File.separator + "conf" + File.separator + "identity" + File.separator +
                        "entitlement.properties");

        scm.applyConfigurationWithoutRestart(srcConfigFile, targetConfigFile, true);
        scm.restartGracefully();
    }

    private File getCustomAttributeFinder() throws Exception {

        File targetDir = new File(
                getISResourceLocation() + File.separator + ".." + File.separator + ".." + File.separator + ".." +
                        File.separator + ".." + File.separator + ".." + File.separator + ".." +
                        File.separator + ".." + File.separator + "tests-common" + File.separator + "extensions" +
                        File.separator + "target");
        if (!targetDir.isDirectory()) {
            throw new Exception(targetDir + " is not a directory.");
        }

        File[] files = targetDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                String fileName = file.getName();
                if (fileName.indexOf("org.wso2.carbon.identity.custom.pip") >= 0 && !(fileName.indexOf("test") >= 0)) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        if (files != null && files.length == 1) {
            return files[0];
        } else {
            throw new Exception("Could not found custom attribute finder jar");
        }
    }
    private String buildRequest(String subject) {
        String request = "<Request xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" " +
                "CombinedDecision=\"false\" ReturnPolicyIdList=\"false\">\n" +
                "<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\">\n" +
                "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" " +
                "IncludeInResult=\"false\">\n" +
                "<AttributeValue DataType=\"http://www.w3" +
                ".org/2001/XMLSchema#string\">readIDENTITY4740</AttributeValue>\n" +
                "</Attribute>\n" +
                "</Attributes>\n" +
                "<Attributes Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\">\n" +
                "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject:subject-id\" " +
                "IncludeInResult=\"false\">\n" +
                "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + subject +
                "</AttributeValue>\n" +
                "</Attribute>\n" +
                "</Attributes>\n" +
                "<Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\">\n" +
                "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\" " +
                "IncludeInResult=\"false\">\n" +
                "<AttributeValue DataType=\"http://www.w3" +
                ".org/2001/XMLSchema#string\">fooIDENTITY4740</AttributeValue>\n" +
                "</Attribute>\n" +
                "</Attributes>\n" +
                "</Request>" +
                "";
        return request;
    }
}
