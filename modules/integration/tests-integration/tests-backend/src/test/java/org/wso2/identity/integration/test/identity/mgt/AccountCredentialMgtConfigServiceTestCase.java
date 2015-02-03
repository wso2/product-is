/*
* Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.identity.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.common.clients.mgt.AccountCredentialMgtConfigServiceClient;
import org.wso2.carbon.identity.mgt.stub.AccountCredentialMgtConfigServiceIdentityMgtServiceExceptionException;
import org.wso2.carbon.identity.mgt.stub.dto.EmailTemplateDTO;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.rmi.RemoteException;

/**
 * Test cases to test AccountCredentialMgtConfigService
 */
public class AccountCredentialMgtConfigServiceTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(AccountCredentialMgtConfigServiceTestCase.class.getName());
    private AccountCredentialMgtConfigServiceClient accCredentialMgtConfigServiceClient;
    private String testConfigName = "passwordReset";
    private String testConfigBody = "This is test email config body";
    private String testDisplayName = "Password Reset";
    private String testFooter = "Test Footer";
    private String testSubject = "Test email subject";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        accCredentialMgtConfigServiceClient = new AccountCredentialMgtConfigServiceClient(backendURL, sessionCookie);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() {
    }

    @Test(groups = "wso2.is", description = "Test saving the Email template configurations")
    public void testSaveEmailConfigs() throws RemoteException,
                                              AccountCredentialMgtConfigServiceIdentityMgtServiceExceptionException {
        //retrieve email templates
        EmailTemplateDTO[] currentEmailTemplates = accCredentialMgtConfigServiceClient.getEmailConfig();

        Assert.assertNotNull(currentEmailTemplates, "Retrieving current email templates failed");

        //add update {testConfigName} email template with test values
        for (EmailTemplateDTO emailTemplate : currentEmailTemplates) {
            if (emailTemplate.getName().equals(testConfigName)) {
                emailTemplate.setName(testConfigName);
                emailTemplate.setBody(testConfigBody);
                emailTemplate.setDisplayName(testDisplayName);
                emailTemplate.setFooter(testFooter);
                emailTemplate.setSubject(testSubject);
                break;
            }
        }
        //save new email template list
        accCredentialMgtConfigServiceClient.saveEmailConfigs(currentEmailTemplates);

        //retrieve email templates to verify
        EmailTemplateDTO[] updatedEmailTemplates = accCredentialMgtConfigServiceClient.getEmailConfig();
        Assert.assertNotNull(updatedEmailTemplates, "Retrieving updated email templates failed");

        log.info("Received email templates : " + updatedEmailTemplates.length);
        EmailTemplateDTO emailTemplate = null;
        for (EmailTemplateDTO emailTemplateDTO : updatedEmailTemplates) {
            if (emailTemplateDTO.getName().equals(testConfigName)) {
                emailTemplate = emailTemplateDTO;
                break;
            }
        }

        Assert.assertNotNull(emailTemplate, "Email template '"+testConfigName+"' is not found");

        if (emailTemplate != null) {
            Assert.assertEquals(emailTemplate.getName(), testConfigName, "Email template name mismatch");
            Assert.assertEquals(emailTemplate.getSubject(), testSubject, "Email template Subject update failed");
            Assert.assertEquals(emailTemplate.getBody(), testConfigBody, "Email template Body update failed");
            Assert.assertEquals(emailTemplate.getDisplayName(), testDisplayName, "Email template display name mismatch");
            Assert.assertEquals(emailTemplate.getFooter(), testFooter, "Email template footer update failed");
        }

    }

}
