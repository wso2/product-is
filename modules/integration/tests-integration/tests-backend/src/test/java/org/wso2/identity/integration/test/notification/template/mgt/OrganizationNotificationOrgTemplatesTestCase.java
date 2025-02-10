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

package org.wso2.identity.integration.test.notification.template.mgt;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.common.RESTTestBase;
import org.wso2.identity.integration.test.rest.api.server.notification.template.v1.model.EmailTemplateWithID;
import org.wso2.identity.integration.test.restclients.NotificationTemplatesRestClient;
import org.wso2.identity.integration.test.restclients.OrgMgtRestClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Test class to test the organization notification org templates for override and inheritance capabilities.
 */
public class OrganizationNotificationOrgTemplatesTestCase extends ISIntegrationTest {

    private static final String AUTHORIZED_APIS_JSON_FILE = "authorized-apis.json";
    private static final String NOTIFICATION_TEMPLATE_MANAGEMENT_API = "Notification Template Management API";
    private static final String API_RESOURCE_TENANT_TYPE = "TENANT";
    private static final List<String> NOTIFICATION_TEMPLATE_MANAGEMENT_API_TENANT_SCOPES =
            new ArrayList<String>() {{
                add("internal_template_mgt_view");
                add("internal_template_mgt_create");
                add("internal_template_mgt_update");
                add("internal_template_mgt_delete");
            }};
    private static final String API_RESOURCE_ORGANIZATION_TYPE = "ORGANIZATION";
    private static final List<String> NOTIFICATION_TEMPLATE_MANAGEMENT_API_ORG_SCOPES =
            new ArrayList<String>() {{
                add("internal_org_template_mgt_view");
                add("internal_org_template_mgt_create");
                add("internal_org_template_mgt_update");
                add("internal_org_template_mgt_delete");
            }};
    private static final String SUBJECT = "subject";
    private static final String BODY = "body";
    private static final String FOOTER = "footer";
    private static final String CODE = "code";
    private static final String NOT_EXIST_ERROR_CODE = "NTM-65004";

    private static final String L1_SUB_ORG1_NAME = "L1_Sub_Org1";
    private static final String L2_SUB_ORG1_NAME = "L2_Sub_Org1";
    private static final String TEST_LOCALE = "fr_FR";
    private static final String TEST_CONTENT_TYPE = "text/html";
    private static final String TEST_DATA_SUBJECT_1 = "Test email subject 1";
    private static final String TEST_DATA_BODY_1 = "Test email body 1";
    private static final String TEST_DATA_FOOTER_1 = "Test email footer 1";
    private static final String TEST_DATA_SUBJECT_2 = "Test email subject 2";
    private static final String TEST_DATA_BODY_2 = "Test email body 2";
    private static final String TEST_DATA_FOOTER_2 = "Test email footer 2";
    private static final String TEST_DATA_SUBJECT_3 = "Test email subject 3";
    private static final String TEST_DATA_BODY_3 = "Test email body 3";
    private static final String TEST_DATA_FOOTER_3 = "Test email footer 3";
    private static final String TEST_DATA_SUBJECT_4 = "Test email subject 4";
    private static final String TEST_DATA_BODY_4 = "Test email body 4";
    private static final String TEST_DATA_FOOTER_4 = "Test email footer 4";
    private static final String TEST_DATA_SUBJECT_5 = "Test email subject 5";
    private static final String TEST_DATA_BODY_5 = "Test email body 5";
    private static final String TEST_DATA_FOOTER_5 = "Test email footer 5";
    private static final String TEST_DATA_SUBJECT_6 = "Test email subject 6";
    private static final String TEST_DATA_BODY_6 = "Test email body 6";
    private static final String TEST_DATA_FOOTER_6 = "Test email footer 6";
    private static final String TEST_EMAIL_TEMPLATE_ID = "aWRsZUFjY291bnRSZW1pbmRlcg";

    private final TestUserMode userMode;

    private OrgMgtRestClient orgMgtRestClient;
    private NotificationTemplatesRestClient notificationTemplatesRestClient;
    private IdentityProviderMgtServiceClient identityProviderMgtServiceClient;

    private String level1Org1Id;
    private String level2Org1Id;
    private String switchedM2MTokenLevel1Org1;
    private String switchedM2MTokenLevel2Org1;

    @DataProvider(name = "testExecutionContextProvider")
    public static Object[][] getTestExecutionContext() throws Exception {

        return new Object[][] {
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @Factory(dataProvider = "testExecutionContextProvider")
    public OrganizationNotificationOrgTemplatesTestCase(TestUserMode userMode) {

        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init(userMode);

        orgMgtRestClient = new OrgMgtRestClient(isServer, tenantInfo, serverURL,
                new JSONObject(RESTTestBase.readResource(AUTHORIZED_APIS_JSON_FILE, this.getClass())));
        orgMgtRestClient.authorizeAPIForB2BApp(NOTIFICATION_TEMPLATE_MANAGEMENT_API, API_RESOURCE_TENANT_TYPE,
                NOTIFICATION_TEMPLATE_MANAGEMENT_API_TENANT_SCOPES);
        orgMgtRestClient.authorizeAPIForB2BApp(NOTIFICATION_TEMPLATE_MANAGEMENT_API, API_RESOURCE_ORGANIZATION_TYPE,
                NOTIFICATION_TEMPLATE_MANAGEMENT_API_ORG_SCOPES);

        notificationTemplatesRestClient = new NotificationTemplatesRestClient(serverURL, tenantInfo);

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        identityProviderMgtServiceClient =
                new IdentityProviderMgtServiceClient(sessionCookie, backendURL, configContext);

        level1Org1Id = orgMgtRestClient.addOrganization(L1_SUB_ORG1_NAME);
        level2Org1Id = orgMgtRestClient.addSubOrganization(L2_SUB_ORG1_NAME, level1Org1Id);

        switchedM2MTokenLevel1Org1 = orgMgtRestClient.switchM2MToken(level1Org1Id);
        switchedM2MTokenLevel2Org1 = orgMgtRestClient.switchM2MToken(level2Org1Id);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        notificationTemplatesRestClient.deleteEmailOrgTemplate(TEST_EMAIL_TEMPLATE_ID, TEST_LOCALE);
        notificationTemplatesRestClient.closeHttpClient();

        orgMgtRestClient.deleteSubOrganization(level2Org1Id, level1Org1Id);
        orgMgtRestClient.deleteOrganization(level1Org1Id);
        orgMgtRestClient.closeHttpClient();

        identityProviderMgtServiceClient.deleteIdP("SSO");
    }

    @Test(description = "Add email org template in root organization.")
    public void testAddEmailOrgTemplateInRootOrg() throws Exception {

        EmailTemplateWithID emailTemplateWithID = new EmailTemplateWithID();
        emailTemplateWithID.setLocale(TEST_LOCALE);
        emailTemplateWithID.setContentType(TEST_CONTENT_TYPE);
        emailTemplateWithID.setSubject(TEST_DATA_SUBJECT_1);
        emailTemplateWithID.setBody(TEST_DATA_BODY_1);
        emailTemplateWithID.setFooter(TEST_DATA_FOOTER_1);
        String templateKey =
                notificationTemplatesRestClient.addEmailOrgTemplate(TEST_EMAIL_TEMPLATE_ID, emailTemplateWithID);
        Assert.assertNotNull(templateKey, "Failed to add the email org template in root organization.");
    }

    @Test(dependsOnMethods = "testAddEmailOrgTemplateInRootOrg",
            description = "Get the email org template in first level sub organization.")
    public void testGetEmailOrgTemplateInL1Org() throws Exception {

        org.json.simple.JSONObject emailTemplate = notificationTemplatesRestClient.getSubOrgEmailOrgTemplate(
                TEST_EMAIL_TEMPLATE_ID, TEST_LOCALE, switchedM2MTokenLevel1Org1, false);
        Object code = emailTemplate.get(CODE);
        Assert.assertNotNull(code, "Error code is null.");
        Assert.assertEquals(code, NOT_EXIST_ERROR_CODE, "Error code is incorrect.");
    }

    @Test(dependsOnMethods = "testGetEmailOrgTemplateInL1Org",
            description = "Resolve the email org template in first level sub organization.")
    public void testResolveEmailOrgTemplateInL1Org() throws Exception {

        org.json.simple.JSONObject emailTemplate = notificationTemplatesRestClient.getSubOrgEmailOrgTemplate(
                TEST_EMAIL_TEMPLATE_ID, TEST_LOCALE, switchedM2MTokenLevel1Org1, true);
        Object subject = emailTemplate.get(SUBJECT);
        Assert.assertNotNull(subject, "Email subject is null.");
        Assert.assertEquals(subject, TEST_DATA_SUBJECT_1, "Email subject is incorrect.");
        Object body = emailTemplate.get(BODY);
        Assert.assertNotNull(body, "Email body is null.");
        Assert.assertEquals(body, TEST_DATA_BODY_1, "Email body is incorrect.");
        Object footer = emailTemplate.get(FOOTER);
        Assert.assertNotNull(footer, "Email footer is null.");
        Assert.assertEquals(footer, TEST_DATA_FOOTER_1, "Email footer is incorrect.");
    }

    @Test(dependsOnMethods = "testResolveEmailOrgTemplateInL1Org",
            description = "Get the email org template in second level sub organization.")
    public void testGetEmailOrgTemplateInL2Org() throws Exception {

        org.json.simple.JSONObject emailTemplate = notificationTemplatesRestClient.getSubOrgEmailOrgTemplate(
                TEST_EMAIL_TEMPLATE_ID, TEST_LOCALE, switchedM2MTokenLevel2Org1, false);
        Object code = emailTemplate.get(CODE);
        Assert.assertNotNull(code, "Error code is null.");
        Assert.assertEquals(code, NOT_EXIST_ERROR_CODE, "Error code is incorrect.");
    }

    @Test(dependsOnMethods = "testGetEmailOrgTemplateInL2Org",
            description = "Resolve the email org template in second level sub organization.")
    public void testResolveEmailOrgTemplateInL2Org() throws Exception {

        org.json.simple.JSONObject emailTemplate = notificationTemplatesRestClient.getSubOrgEmailOrgTemplate(
                TEST_EMAIL_TEMPLATE_ID, TEST_LOCALE, switchedM2MTokenLevel2Org1, true);
        Object subject = emailTemplate.get(SUBJECT);
        Assert.assertNotNull(subject, "Email subject is null.");
        Assert.assertEquals(subject, TEST_DATA_SUBJECT_1, "Email subject is incorrect.");
        Object body = emailTemplate.get(BODY);
        Assert.assertNotNull(body, "Email body is null.");
        Assert.assertEquals(body, TEST_DATA_BODY_1, "Email body is incorrect.");
        Object footer = emailTemplate.get(FOOTER);
        Assert.assertNotNull(footer, "Email footer is null.");
        Assert.assertEquals(footer, TEST_DATA_FOOTER_1, "Email footer is incorrect.");
    }

    @Test(dependsOnMethods = "testResolveEmailOrgTemplateInL2Org",
            description = "Update the email org template in root organization.")
    public void testUpdateEmailOrgTemplateInRootOrg() throws Exception {

        EmailTemplateWithID emailTemplateWithID = new EmailTemplateWithID();
        emailTemplateWithID.setContentType(TEST_CONTENT_TYPE);
        emailTemplateWithID.setSubject(TEST_DATA_SUBJECT_2);
        emailTemplateWithID.setBody(TEST_DATA_BODY_2);
        emailTemplateWithID.setFooter(TEST_DATA_FOOTER_2);
        notificationTemplatesRestClient.updateEmailOrgTemplate(
                TEST_EMAIL_TEMPLATE_ID, TEST_LOCALE, emailTemplateWithID);
    }

    @Test(dependsOnMethods = "testUpdateEmailOrgTemplateInRootOrg",
            description = "Resolve the updated email org template in first level sub organization.")
    public void testResolveUpdatedRootEmailOrgTemplateInL1Org() throws Exception {

        org.json.simple.JSONObject emailTemplate = notificationTemplatesRestClient.getSubOrgEmailOrgTemplate(
                TEST_EMAIL_TEMPLATE_ID, TEST_LOCALE, switchedM2MTokenLevel1Org1, true);
        Object subject = emailTemplate.get(SUBJECT);
        Assert.assertNotNull(subject, "Email subject is null.");
        Assert.assertEquals(subject, TEST_DATA_SUBJECT_2, "Email subject is incorrect.");
        Object body = emailTemplate.get(BODY);
        Assert.assertNotNull(body, "Email body is null.");
        Assert.assertEquals(body, TEST_DATA_BODY_2, "Email body is incorrect.");
        Object footer = emailTemplate.get(FOOTER);
        Assert.assertNotNull(footer, "Email footer is null.");
        Assert.assertEquals(footer, TEST_DATA_FOOTER_2, "Email footer is incorrect.");
    }

    @Test(dependsOnMethods = "testResolveUpdatedRootEmailOrgTemplateInL1Org",
            description = "Resolve the updated email org template in second level sub organization.")
    public void testResolveUpdatedRootEmailOrgTemplateInL2Org() throws Exception {

        org.json.simple.JSONObject emailTemplate = notificationTemplatesRestClient.getSubOrgEmailOrgTemplate(
                TEST_EMAIL_TEMPLATE_ID, TEST_LOCALE, switchedM2MTokenLevel2Org1, true);
        Object subject = emailTemplate.get(SUBJECT);
        Assert.assertNotNull(subject, "Email subject is null.");
        Assert.assertEquals(subject, TEST_DATA_SUBJECT_2, "Email subject is incorrect.");
        Object body = emailTemplate.get(BODY);
        Assert.assertNotNull(body, "Email body is null.");
        Assert.assertEquals(body, TEST_DATA_BODY_2, "Email body is incorrect.");
        Object footer = emailTemplate.get(FOOTER);
        Assert.assertNotNull(footer, "Email footer is null.");
        Assert.assertEquals(footer, TEST_DATA_FOOTER_2, "Email footer is incorrect.");
    }

    @Test(dependsOnMethods = "testResolveUpdatedRootEmailOrgTemplateInL2Org",
            description = "Add the email org template in first level sub organization.")
    public void testAddEmailOrgTemplateInL1Org() throws Exception {

        EmailTemplateWithID emailTemplateWithID = new EmailTemplateWithID();
        emailTemplateWithID.setLocale(TEST_LOCALE);
        emailTemplateWithID.setContentType(TEST_CONTENT_TYPE);
        emailTemplateWithID.setSubject(TEST_DATA_SUBJECT_3);
        emailTemplateWithID.setBody(TEST_DATA_BODY_3);
        emailTemplateWithID.setFooter(TEST_DATA_FOOTER_3);
        String templateKey = notificationTemplatesRestClient.addSubOrgEmailOrgTemplate(
                TEST_EMAIL_TEMPLATE_ID, emailTemplateWithID, switchedM2MTokenLevel1Org1);
        Assert.assertNotNull(templateKey,
                "Failed to add the email org template in first level sub organization.");
    }

    @Test(dependsOnMethods = "testAddEmailOrgTemplateInL1Org",
            description = "Get the added email org template in first level sub organization.")
    public void testGetAddedL1OrgEmailOrgTemplateInL1Org() throws Exception {

        org.json.simple.JSONObject emailTemplate = notificationTemplatesRestClient.getSubOrgEmailOrgTemplate(
                TEST_EMAIL_TEMPLATE_ID, TEST_LOCALE, switchedM2MTokenLevel1Org1, false);
        Object subject = emailTemplate.get(SUBJECT);
        Assert.assertNotNull(subject, "Email subject is null.");
        Assert.assertEquals(subject, TEST_DATA_SUBJECT_3, "Email subject is incorrect.");
        Object body = emailTemplate.get(BODY);
        Assert.assertNotNull(body, "Email body is null.");
        Assert.assertEquals(body, TEST_DATA_BODY_3, "Email body is incorrect.");
        Object footer = emailTemplate.get(FOOTER);
        Assert.assertNotNull(footer, "Email footer is null.");
        Assert.assertEquals(footer, TEST_DATA_FOOTER_3, "Email footer is incorrect.");
    }

    @Test(dependsOnMethods = "testGetAddedL1OrgEmailOrgTemplateInL1Org",
            description = "Resolve the updated email org template in first level sub organization.")
    public void testResolveAddedL1OrgEmailOrgTemplateInL1Org() throws Exception {

        org.json.simple.JSONObject emailTemplate = notificationTemplatesRestClient.getSubOrgEmailOrgTemplate(
                TEST_EMAIL_TEMPLATE_ID, TEST_LOCALE, switchedM2MTokenLevel1Org1, true);
        Object subject = emailTemplate.get(SUBJECT);
        Assert.assertNotNull(subject, "Email subject is null.");
        Assert.assertEquals(subject, TEST_DATA_SUBJECT_3, "Email subject is incorrect.");
        Object body = emailTemplate.get(BODY);
        Assert.assertNotNull(body, "Email body is null.");
        Assert.assertEquals(body, TEST_DATA_BODY_3, "Email body is incorrect.");
        Object footer = emailTemplate.get(FOOTER);
        Assert.assertNotNull(footer, "Email footer is null.");
        Assert.assertEquals(footer, TEST_DATA_FOOTER_3, "Email footer is incorrect.");
    }

    @Test(dependsOnMethods = "testResolveAddedL1OrgEmailOrgTemplateInL1Org",
            description = "Resolve the updated email org template in second level sub organization.")
    public void testResolveAddedL1OrgEmailOrgTemplateInL2Org() throws Exception {

        org.json.simple.JSONObject emailTemplate = notificationTemplatesRestClient.getSubOrgEmailOrgTemplate(
                TEST_EMAIL_TEMPLATE_ID, TEST_LOCALE, switchedM2MTokenLevel2Org1, true);
        Object subject = emailTemplate.get(SUBJECT);
        Assert.assertNotNull(subject, "Email subject is null.");
        Assert.assertEquals(subject, TEST_DATA_SUBJECT_3, "Email subject is incorrect.");
        Object body = emailTemplate.get(BODY);
        Assert.assertNotNull(body, "Email body is null.");
        Assert.assertEquals(body, TEST_DATA_BODY_3, "Email body is incorrect.");
        Object footer = emailTemplate.get(FOOTER);
        Assert.assertNotNull(footer, "Email footer is null.");
        Assert.assertEquals(footer, TEST_DATA_FOOTER_3, "Email footer is incorrect.");
    }

    @Test(dependsOnMethods = "testResolveAddedL1OrgEmailOrgTemplateInL2Org",
            description = "Update the email org template in first level sub organization.")
    public void testUpdateEmailOrgTemplateInL1Org() throws Exception {

        EmailTemplateWithID emailTemplateWithID = new EmailTemplateWithID();
        emailTemplateWithID.setContentType(TEST_CONTENT_TYPE);
        emailTemplateWithID.setSubject(TEST_DATA_SUBJECT_4);
        emailTemplateWithID.setBody(TEST_DATA_BODY_4);
        emailTemplateWithID.setFooter(TEST_DATA_FOOTER_4);
        notificationTemplatesRestClient.updateSubOrgEmailOrgTemplate(
                TEST_EMAIL_TEMPLATE_ID, TEST_LOCALE, emailTemplateWithID, switchedM2MTokenLevel1Org1);
    }

    @Test(dependsOnMethods = "testUpdateEmailOrgTemplateInL1Org",
            description = "Resolve the updated email org template in first level sub organization.")
    public void testResolveUpdatedL1OrgEmailOrgTemplateInL1Org() throws Exception {

        org.json.simple.JSONObject emailTemplate = notificationTemplatesRestClient.getSubOrgEmailOrgTemplate(
                TEST_EMAIL_TEMPLATE_ID, TEST_LOCALE, switchedM2MTokenLevel1Org1, true);
        Object subject = emailTemplate.get(SUBJECT);
        Assert.assertNotNull(subject, "Email subject is null.");
        Assert.assertEquals(subject, TEST_DATA_SUBJECT_4, "Email subject is incorrect.");
        Object body = emailTemplate.get(BODY);
        Assert.assertNotNull(body, "Email body is null.");
        Assert.assertEquals(body, TEST_DATA_BODY_4, "Email body is incorrect.");
        Object footer = emailTemplate.get(FOOTER);
        Assert.assertNotNull(footer, "Email footer is null.");
        Assert.assertEquals(footer, TEST_DATA_FOOTER_4, "Email footer is incorrect.");
    }

    @Test(dependsOnMethods = "testResolveUpdatedL1OrgEmailOrgTemplateInL1Org",
            description = "Resolve the updated email org template in second level sub organization.")
    public void testResolveUpdatedL1OrgEmailOrgTemplateInL2Org() throws Exception {

        org.json.simple.JSONObject emailTemplate = notificationTemplatesRestClient.getSubOrgEmailOrgTemplate(
                TEST_EMAIL_TEMPLATE_ID, TEST_LOCALE, switchedM2MTokenLevel2Org1, true);
        Object subject = emailTemplate.get(SUBJECT);
        Assert.assertNotNull(subject, "Email subject is null.");
        Assert.assertEquals(subject, TEST_DATA_SUBJECT_4, "Email subject is incorrect.");
        Object body = emailTemplate.get(BODY);
        Assert.assertNotNull(body, "Email body is null.");
        Assert.assertEquals(body, TEST_DATA_BODY_4, "Email body is incorrect.");
        Object footer = emailTemplate.get(FOOTER);
        Assert.assertNotNull(footer, "Email footer is null.");
        Assert.assertEquals(footer, TEST_DATA_FOOTER_4, "Email footer is incorrect.");
    }

    @Test(dependsOnMethods = "testResolveUpdatedL1OrgEmailOrgTemplateInL2Org",
            description = "Delete the email org template in first level sub organization.")
    public void testDeleteEmailOrgTemplateInL1Org() throws Exception {

        notificationTemplatesRestClient.deleteSubOrgEmailOrgTemplate(TEST_EMAIL_TEMPLATE_ID, TEST_LOCALE,
                switchedM2MTokenLevel1Org1);
    }

    @Test(dependsOnMethods = "testDeleteEmailOrgTemplateInL1Org",
            description = "Get the deleted email org template in first level sub organization.")
    public void testGetDeletedL1OrgEmailOrgTemplateInL1Org() throws Exception {

        org.json.simple.JSONObject emailTemplate = notificationTemplatesRestClient.getSubOrgEmailOrgTemplate(
                TEST_EMAIL_TEMPLATE_ID, TEST_LOCALE, switchedM2MTokenLevel1Org1, false);
        Object code = emailTemplate.get(CODE);
        Assert.assertNotNull(code, "Error code is null.");
        Assert.assertEquals(code, NOT_EXIST_ERROR_CODE, "Error code is incorrect.");
    }

    @Test(dependsOnMethods = "testGetDeletedL1OrgEmailOrgTemplateInL1Org",
            description = "Resolve the deleted email org template in first level sub organization.")
    public void testResolveDeletedL1OrgEmailOrgTemplateInL1Org() throws Exception {

        org.json.simple.JSONObject emailTemplate = notificationTemplatesRestClient.getSubOrgEmailOrgTemplate(
                TEST_EMAIL_TEMPLATE_ID, TEST_LOCALE, switchedM2MTokenLevel1Org1, true);
        Object subject = emailTemplate.get(SUBJECT);
        Assert.assertNotNull(subject, "Email subject is null.");
        Assert.assertEquals(subject, TEST_DATA_SUBJECT_2, "Email subject is incorrect.");
        Object body = emailTemplate.get(BODY);
        Assert.assertNotNull(body, "Email body is null.");
        Assert.assertEquals(body, TEST_DATA_BODY_2, "Email body is incorrect.");
        Object footer = emailTemplate.get(FOOTER);
        Assert.assertNotNull(footer, "Email footer is null.");
        Assert.assertEquals(footer, TEST_DATA_FOOTER_2, "Email footer is incorrect.");
    }

    @Test(dependsOnMethods = "testResolveDeletedL1OrgEmailOrgTemplateInL1Org",
            description = "Resolve the deleted email org template in second level sub organization.")
    public void testResolveDeletedL1OrgEmailOrgTemplateInL2Org() throws Exception {

        org.json.simple.JSONObject emailTemplate = notificationTemplatesRestClient.getSubOrgEmailOrgTemplate(
                TEST_EMAIL_TEMPLATE_ID, TEST_LOCALE, switchedM2MTokenLevel2Org1, true);
        Object subject = emailTemplate.get(SUBJECT);
        Assert.assertNotNull(subject, "Email subject is null.");
        Assert.assertEquals(subject, TEST_DATA_SUBJECT_2, "Email subject is incorrect.");
        Object body = emailTemplate.get(BODY);
        Assert.assertNotNull(body, "Email body is null.");
        Assert.assertEquals(body, TEST_DATA_BODY_2, "Email body is incorrect.");
        Object footer = emailTemplate.get(FOOTER);
        Assert.assertNotNull(footer, "Email footer is null.");
        Assert.assertEquals(footer, TEST_DATA_FOOTER_2, "Email footer is incorrect.");
    }

    @Test(dependsOnMethods = "testResolveDeletedL1OrgEmailOrgTemplateInL2Org",
            description = "Add the email org template in second level sub organization.")
    public void testAddEmailOrgTemplateInL2Org() throws Exception {

        EmailTemplateWithID emailTemplateWithID = new EmailTemplateWithID();
        emailTemplateWithID.setLocale(TEST_LOCALE);
        emailTemplateWithID.setContentType(TEST_CONTENT_TYPE);
        emailTemplateWithID.setSubject(TEST_DATA_SUBJECT_5);
        emailTemplateWithID.setBody(TEST_DATA_BODY_5);
        emailTemplateWithID.setFooter(TEST_DATA_FOOTER_5);
        String templateKey = notificationTemplatesRestClient.addSubOrgEmailOrgTemplate(
                TEST_EMAIL_TEMPLATE_ID, emailTemplateWithID, switchedM2MTokenLevel2Org1);
        Assert.assertNotNull(templateKey,
                "Failed to add the email org template in second level sub organization.");
    }

    @Test(dependsOnMethods = "testAddEmailOrgTemplateInL2Org",
            description = "Get the added email org template in second level sub organization.")
    public void testGetAddedL2OrgEmailOrgTemplateInL2Org() throws Exception {

        org.json.simple.JSONObject emailTemplate = notificationTemplatesRestClient.getSubOrgEmailOrgTemplate(
                TEST_EMAIL_TEMPLATE_ID, TEST_LOCALE, switchedM2MTokenLevel2Org1, false);
        Object subject = emailTemplate.get(SUBJECT);
        Assert.assertNotNull(subject, "Email subject is null.");
        Assert.assertEquals(subject, TEST_DATA_SUBJECT_5, "Email subject is incorrect.");
        Object body = emailTemplate.get(BODY);
        Assert.assertNotNull(body, "Email body is null.");
        Assert.assertEquals(body, TEST_DATA_BODY_5, "Email body is incorrect.");
        Object footer = emailTemplate.get(FOOTER);
        Assert.assertNotNull(footer, "Email footer is null.");
        Assert.assertEquals(footer, TEST_DATA_FOOTER_5, "Email footer is incorrect.");
    }

    @Test(dependsOnMethods = "testGetAddedL2OrgEmailOrgTemplateInL2Org",
            description = "Resolve the added email org template in second level sub organization.")
    public void testResolveAddedL2OrgEmailOrgTemplateInL2Org() throws Exception {

        org.json.simple.JSONObject emailTemplate = notificationTemplatesRestClient.getSubOrgEmailOrgTemplate(
                TEST_EMAIL_TEMPLATE_ID, TEST_LOCALE, switchedM2MTokenLevel2Org1, true);
        Object subject = emailTemplate.get(SUBJECT);
        Assert.assertNotNull(subject, "Email subject is null.");
        Assert.assertEquals(subject, TEST_DATA_SUBJECT_5, "Email subject is incorrect.");
        Object body = emailTemplate.get(BODY);
        Assert.assertNotNull(body, "Email body is null.");
        Assert.assertEquals(body, TEST_DATA_BODY_5, "Email body is incorrect.");
        Object footer = emailTemplate.get(FOOTER);
        Assert.assertNotNull(footer, "Email footer is null.");
        Assert.assertEquals(footer, TEST_DATA_FOOTER_5, "Email footer is incorrect.");
    }

    @Test(dependsOnMethods = "testResolveAddedL2OrgEmailOrgTemplateInL2Org",
            description = "Update the email org template in second level sub organization.")
    public void testUpdateEmailOrgTemplateInL2Org() throws Exception {

        EmailTemplateWithID emailTemplateWithID = new EmailTemplateWithID();
        emailTemplateWithID.setContentType(TEST_CONTENT_TYPE);
        emailTemplateWithID.setSubject(TEST_DATA_SUBJECT_6);
        emailTemplateWithID.setBody(TEST_DATA_BODY_6);
        emailTemplateWithID.setFooter(TEST_DATA_FOOTER_6);
        notificationTemplatesRestClient.updateSubOrgEmailOrgTemplate(
                TEST_EMAIL_TEMPLATE_ID, TEST_LOCALE, emailTemplateWithID, switchedM2MTokenLevel2Org1);
    }

    @Test(dependsOnMethods = "testUpdateEmailOrgTemplateInL2Org",
            description = "Resolve the updated email org template in second level sub organization.")
    public void testResolveUpdatedL2OrgEmailOrgTemplateInL2Org() throws Exception {

        org.json.simple.JSONObject emailTemplate = notificationTemplatesRestClient.getSubOrgEmailOrgTemplate(
                TEST_EMAIL_TEMPLATE_ID, TEST_LOCALE, switchedM2MTokenLevel2Org1, true);
        Object subject = emailTemplate.get(SUBJECT);
        Assert.assertNotNull(subject, "Email subject is null.");
        Assert.assertEquals(subject, TEST_DATA_SUBJECT_6, "Email subject is incorrect.");
        Object body = emailTemplate.get(BODY);
        Assert.assertNotNull(body, "Email body is null.");
        Assert.assertEquals(body, TEST_DATA_BODY_6, "Email body is incorrect.");
        Object footer = emailTemplate.get(FOOTER);
        Assert.assertNotNull(footer, "Email footer is null.");
        Assert.assertEquals(footer, TEST_DATA_FOOTER_6, "Email footer is incorrect.");
    }

    @Test(dependsOnMethods = "testResolveUpdatedL2OrgEmailOrgTemplateInL2Org",
            description = "Delete the email org template in second level sub organization.")
    public void testDeleteEmailOrgTemplateInL2Org() throws Exception {

        notificationTemplatesRestClient.deleteSubOrgEmailOrgTemplate(TEST_EMAIL_TEMPLATE_ID, TEST_LOCALE,
                switchedM2MTokenLevel2Org1);
    }

    @Test(dependsOnMethods = "testDeleteEmailOrgTemplateInL2Org",
            description = "Get the deleted email org template in second level sub organization.")
    public void testGetDeletedL2OrgEmailOrgTemplateInL2Org() throws Exception {

        org.json.simple.JSONObject emailTemplate = notificationTemplatesRestClient.getSubOrgEmailOrgTemplate(
                TEST_EMAIL_TEMPLATE_ID, TEST_LOCALE, switchedM2MTokenLevel2Org1, false);
        Object code = emailTemplate.get(CODE);
        Assert.assertNotNull(code, "Error code is null.");
        Assert.assertEquals(code, NOT_EXIST_ERROR_CODE, "Error code is incorrect.");
    }

    @Test(dependsOnMethods = "testGetDeletedL2OrgEmailOrgTemplateInL2Org",
            description = "Resolve the deleted email org template in second level sub organization.")
    public void testResolveDeletedL2OrgEmailOrgTemplateInL2Org() throws Exception {

        org.json.simple.JSONObject emailTemplate = notificationTemplatesRestClient.getSubOrgEmailOrgTemplate(
                TEST_EMAIL_TEMPLATE_ID, TEST_LOCALE, switchedM2MTokenLevel2Org1, true);
        Object subject = emailTemplate.get(SUBJECT);
        Assert.assertNotNull(subject, "Email subject is null.");
        Assert.assertEquals(subject, TEST_DATA_SUBJECT_2, "Email subject is incorrect.");
        Object body = emailTemplate.get(BODY);
        Assert.assertNotNull(body, "Email body is null.");
        Assert.assertEquals(body, TEST_DATA_BODY_2, "Email body is incorrect.");
        Object footer = emailTemplate.get(FOOTER);
        Assert.assertNotNull(footer, "Email footer is null.");
        Assert.assertEquals(footer, TEST_DATA_FOOTER_2, "Email footer is incorrect.");
    }
}
