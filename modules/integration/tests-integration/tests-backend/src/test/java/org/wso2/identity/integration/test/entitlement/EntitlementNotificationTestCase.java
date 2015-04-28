/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.identity.integration.common.clients.entitlement.EntitlementPolicyServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class EntitlementNotificationTestCase extends ISIntegrationTest {

    private EntitlementPolicyServiceClient entitlementPolicyClient;
    private static final Log log = LogFactory.getLog(EntitlementPolicyAdminServiceTestCase.class);

    private static final String POLICY_1_ID = "urn:sample:xacml:2.0:custompolicy";
    private static final String POLICY_1 = "<Policy xmlns='urn:oasis:names:tc:xacml:2.0:policy:schema:os' "
            + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
            + "xsi:schemaLocation='urn:oasis:names:tc:xacml:2.0:policy:schema:os   access_control-xacml-2.0-policy-schema-os.xsd' "
            + "PolicyId='" + POLICY_1_ID + "' "
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
    private ServerConfigurationManager serverConfigurationManager;
    private File entitlementProperties;
    private File notificationMgtProperties;
    private UserManagementClient userMgtServiceClient;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        changeISConfiguration();
        super.init();
        entitlementPolicyClient = new EntitlementPolicyServiceClient(backendURL, sessionCookie);
        userMgtServiceClient = new UserManagementClient(backendURL, sessionCookie);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        resetISConfiguration();
        super.init();
        userMgtServiceClient = new UserManagementClient(backendURL, sessionCookie);
        userMgtServiceClient.deleteUser("CREATE");
        userMgtServiceClient.deleteUser("UPDATE");
        userMgtServiceClient.deleteUser("DELETE");
    }

    @Test(groups = "wso2.is", description = "Check rest endpoint call on adding a policy")
    public void testAddPolicy() throws Exception {
        PolicyDTO policy = new PolicyDTO();
        policy.setPolicy(POLICY_1);
        entitlementPolicyClient.addPolicy(policy);
        // It takes some time to send http post and create user from scim
        Thread.sleep(5000);
        Assert.assertTrue(isUserExists("CREATE"));
    }

    @Test(groups = "wso2.is", description = "Check rest endpoint call on adding a policy",
            dependsOnMethods = "updatePolicy")
    public void deletePolicy() throws Exception {
        entitlementPolicyClient.removePolicy("urn:sample:xacml:2.0:custompolicy");
        // It takes some time to send http post and create user from scim
        Thread.sleep(5000);
        Assert.assertTrue(isUserExists("DELETE"));
    }

    @Test(groups = "wso2.is", description = "Check rest endpoint call on adding a policy",
            dependsOnMethods = "testAddPolicy")
    public void updatePolicy() throws Exception {
        PolicyDTO policy = new PolicyDTO();
        policy.setPolicy(POLICY_1);
        entitlementPolicyClient.updatePolicy(policy);
        // It takes some time to send http post and create user from scim
        Thread.sleep(5000);
        Assert.assertTrue(isUserExists("UPDATE"));
    }

    private void changeISConfiguration() throws Exception {
        changeEntitlementPropertyConfig();
        changeNotificationMgtPropertyConfig();
        serverConfigurationManager.restartForcefully();
    }

    private void changeEntitlementPropertyConfig() throws Exception {
        log.info("Changing entitlement.properties to add EntitlementNotificationExtension");

        String carbonHome = CarbonUtils.getCarbonHome();
        entitlementProperties = new File(carbonHome + File.separator
                + "repository" + File.separator + "conf" + File.separator + "security" + File.separator +
                "entitlement.properties");
        File configuredEntitlementProperties = new File(getISResourceLocation()
                + File.separator + "entitlement" + File.separator + "config" + File.separator
                + "entitlementNotificationEnabled.properties");

        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredEntitlementProperties,
                entitlementProperties, true);
    }

    private void changeNotificationMgtPropertyConfig() throws Exception {
        log.info("Changing msg-mgt.properties to add EntitlementNotificationExtension");

        String carbonHome = CarbonUtils.getCarbonHome();
        String templateLocation = getISResourceLocation()
                + File.separator + "notification-mgt" + File.separator + "templates" + File.separator
                + "entitlement";
        String msgMgtPropertiesFileLocation = getISResourceLocation()
                + File.separator + "notification-mgt" + File.separator + "config" + File.separator
                + "entitlementNotificationMgt.properties";

        HashMap<String, String> newProperties = new HashMap<String, String>();
        newProperties.put("json.subscription.policyUpdate.jsonContentTemplate", templateLocation);
        replaceProperties(newProperties, msgMgtPropertiesFileLocation);
        notificationMgtProperties = new File(carbonHome + File.separator
                + "repository" + File.separator + "conf" + File.separator + "msg-mgt.properties");

        File configuredNotificationProperties = new File(msgMgtPropertiesFileLocation);

        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredNotificationProperties,
                notificationMgtProperties, true);
    }

    private boolean isUserExists(String userName) throws Exception {
        FlaggedName[] nameList = userMgtServiceClient.listAllUsers(userName, 100);
        for (FlaggedName name : nameList) {
            if (name.getItemName().contains(userName)) {
                return true;
            }
        }
        return false;
    }

    public void replaceProperties(Map<String, String> properties, String filePath) throws IOException {

        Properties prop = new Properties();
        FileInputStream input = null;
        FileOutputStream outputStream = null;
        input = new FileInputStream(filePath);

        prop.load(input);

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            prop.put(entry.getKey(), entry.getValue());
        }

        outputStream = new FileOutputStream(filePath);
        prop.store(outputStream, null);
    }

    private void resetISConfiguration() throws Exception {
        resetEntitlementProperties();
        resetMsgMgtProperties();
        serverConfigurationManager.restartGracefully();
    }

    private void resetEntitlementProperties() throws Exception {
        log.info("Replacing entitlement.properties with default configurations");

        File defaultEntitlementProperties = new File(getISResourceLocation()
                + File.separator + "entitlement" + File.separator + "config" + File.separator
                + "entitlement_default.properties");

        serverConfigurationManager.applyConfigurationWithoutRestart(defaultEntitlementProperties, entitlementProperties, true);
    }

    private void resetMsgMgtProperties() throws Exception {
        log.info("Replacing msg-mgt.properties with default configurations");

        File defaultMsgMgtPropertiesFile = new File(getISResourceLocation()
                + File.separator + "notification-mgt" + File.separator + "config" + File.separator
                + "msg-mgt-default.properties");

        serverConfigurationManager.applyConfigurationWithoutRestart(defaultMsgMgtPropertiesFile, notificationMgtProperties, true);

    }

}