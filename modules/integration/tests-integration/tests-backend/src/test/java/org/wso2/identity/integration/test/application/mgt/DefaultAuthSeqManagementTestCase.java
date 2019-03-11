/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.identity.integration.test.application.mgt;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.lang.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.script.xsd.AuthenticationScriptConfig;
import org.wso2.carbon.identity.application.common.model.xsd.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.xsd.DefaultAuthenticationSequence;
import org.wso2.carbon.identity.application.common.model.xsd.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.defaultsequence.stub.IdentityDefaultSeqManagementServiceDefaultAuthSeqMgtException;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.application.mgt.DefaultAuthSeqMgtServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

public class DefaultAuthSeqManagementTestCase extends ISIntegrationTest {

    private DefaultAuthSeqMgtServiceClient defaultAuthSeqMgtServiceClient;
    private ApplicationManagementServiceClient applicationManagementServiceClient;
    private final String applicationName1 = "TestServiceProvider1";
    private final String seqName = "default_sequence";
    private final String seqDesc1 = "This is a seq with basic and totp with role based adaptive authentication";
    private final String seqDesc1_updated = "This is a seq with basic and totp with role based adaptive authentication" +
            " for admin role";
    private final String seqDesc1_updated2 = "This is a seq with basic authentication";
    private final String seqFileName1 = "valid_default_seq.xml";
    private final String seqFileName2 = "invalid_content_default_seq.xml";
    private final String seqFileName3 = "incorrect_default_seq.xml";
    private final String seqFileName4 = "invalid_xml_element_default_seq.xml";
    private String seqContent1;
    private String seqContent2;
    private String seqContent3;
    private String seqContent4;
    private final String applicationName = "TestServiceProvider";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null
                , null);
        defaultAuthSeqMgtServiceClient = new DefaultAuthSeqMgtServiceClient(sessionCookie, backendURL, configContext);
        applicationManagementServiceClient = new ApplicationManagementServiceClient(sessionCookie, backendURL,
                configContext);
        seqContent1 = readResource("defaultAuthSeq/" + seqFileName1);
        seqContent2 = readResource("defaultAuthSeq/" + seqFileName2);
        seqContent3 = readResource("defaultAuthSeq/" + seqFileName3);
        seqContent4 = readResource("defaultAuthSeq/" + seqFileName4);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() {

        defaultAuthSeqMgtServiceClient = null;
    }

    @Test(alwaysRun = true, description = "Testing create default authentication sequence with invalid content")
    public void testCreateDefaultAuthSeqWithInvalidContent() {

        try {
            DefaultAuthenticationSequence sequence = getDefaultAuthSeq(seqName, null, seqContent2, null);
            defaultAuthSeqMgtServiceClient.createDefaultAuthenticationSeq(sequence);

            DefaultAuthenticationSequence receivedSequence = defaultAuthSeqMgtServiceClient
                    .getDefaultAuthenticationSeqInXML();
            assertNull(receivedSequence, "Successfully retrieve the tenant default auth sequence with " +
                    "invalid content");
        } catch (IdentityDefaultSeqManagementServiceDefaultAuthSeqMgtException e) {
            if (ArrayUtils.isEmpty(e.getFaultMessage().getDefaultAuthSeqMgtException().getMessages())) {
                fail("Error while trying to create default authentication sequence with invalid content.", e);
            }
        }
    }

    @Test(alwaysRun = true, description = "Testing create default authentication sequence with invalid content")
    public void testCreateDefaultAuthSeqWithInvalidXMLElement() {

        try {
            DefaultAuthenticationSequence sequence = getDefaultAuthSeq(seqName, null, seqContent4, null);
            defaultAuthSeqMgtServiceClient.createDefaultAuthenticationSeq(sequence);

            DefaultAuthenticationSequence receivedSequence = defaultAuthSeqMgtServiceClient
                    .getDefaultAuthenticationSeqInXML();
            assertNull(receivedSequence, "Successfully retrieve the tenant default auth sequence with " +
                    "invalid XML element");
        } catch (IdentityDefaultSeqManagementServiceDefaultAuthSeqMgtException e) {
            if (e.getFaultMessage() != null && e.getFaultMessage().getDefaultAuthSeqMgtException() != null
                    && ArrayUtils.isEmpty(e.getFaultMessage().getDefaultAuthSeqMgtException().getMessages())) {
                fail("Error while trying to create default authentication sequence with " +
                        "invalid XML element.", e);
            }
        }
    }

    @Test(alwaysRun = true, description = "Testing create default authentication sequence with invalid content")
    public void testCreateDefaultAuthSeqWithIncorrectContent() {

        try {
            DefaultAuthenticationSequence sequence = getDefaultAuthSeq(seqName, null, seqContent3, null);
            defaultAuthSeqMgtServiceClient.createDefaultAuthenticationSeq(sequence);

            DefaultAuthenticationSequence receivedSequence = defaultAuthSeqMgtServiceClient
                    .getDefaultAuthenticationSeqInXML();
            assertNull(receivedSequence, "Successfully retrieve the tenant default auth sequence with " +
                    "incorrect content");
        } catch (IdentityDefaultSeqManagementServiceDefaultAuthSeqMgtException e) {
            if (ArrayUtils.isEmpty(e.getFaultMessage().getDefaultAuthSeqMgtException().getMessages())) {
                fail("Error while trying to create default authentication sequence with incorrect content.", e);
            }
        }
    }

    @Test(alwaysRun = true, description = "Testing create default authentication sequence",
            dependsOnMethods = {"testCreateDefaultAuthSeqWithInvalidContent",
                    "testCreateDefaultAuthSeqWithIncorrectContent"})
    public void testCreateDefaultAuthSeq() {

        try {
            DefaultAuthenticationSequence sequence = getDefaultAuthSeq(seqName, seqDesc1, seqContent1, null);
            defaultAuthSeqMgtServiceClient.createDefaultAuthenticationSeq(sequence);

            DefaultAuthenticationSequence receivedSequence = defaultAuthSeqMgtServiceClient
                    .getDefaultAuthenticationSeqInXML();
            assertNotNull(receivedSequence, "Failed to create the tenant default auth sequence");
            assertEquals(receivedSequence.getName(), seqName,
                    "Failed to create the tenant default authn sequence");
            assertEquals(receivedSequence.getDescription(), seqDesc1, "Failed to create the tenant default " +
                    "auth sequence");
        } catch (Exception e) {
            fail("Error while trying to create default authentication sequence.", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing get default authentication sequence",
            dependsOnMethods = "testCreateDefaultAuthSeq")
    public void testGetDefaultAuthSeq() {

        try {
            DefaultAuthenticationSequence sequence = defaultAuthSeqMgtServiceClient.getDefaultAuthenticationSeqInXML();

            assertNotNull(sequence, "Failed to retrieve the tenant defualt auth sequence");
            assertEquals(sequence.getName(), seqName,
                    "Failed to retrieve the tenant default authn sequence");
            assertEquals(sequence.getDescription(), seqDesc1, "Failed to retrieve the tenant default " +
                    "auth sequence");
        } catch (Exception e) {
            fail("Error while trying to get default authentication sequence.", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing update default authentication sequence",
            dependsOnMethods = "testGetDefaultAuthSeq")
    public void testUpdateDefaultAuthSeq() {

        try {
            DefaultAuthenticationSequence sequence = getDefaultAuthSeq(seqName, seqDesc1_updated, seqContent1, null);
            defaultAuthSeqMgtServiceClient.updateDefaultAuthenticationSeq(sequence);

            DefaultAuthenticationSequence receivedSequence = defaultAuthSeqMgtServiceClient
                    .getDefaultAuthenticationSeqInXML();
            assertNotNull(receivedSequence, "Failed to update the tenant default auth sequence");
            assertEquals(receivedSequence.getName(), seqName,
                    "Failed to update the tenant default authn sequence");
            assertEquals(receivedSequence.getDescription(), seqDesc1_updated, "Failed to update the " +
                    "tenant default auth sequence");
        } catch (Exception e) {
            fail("Error while trying to update default authentication sequence.", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing create default auth sequence from existing Service Provider config",
            dependsOnMethods = "testUpdateDefaultAuthSeq")
    public void createDefaultAuthSeqFromSP() {

        try {
            createApplication(applicationName, "This is a test Service Provider");
            updateApplicationData(applicationName);

            ServiceProvider serviceProvider = getApplication(applicationName);
            assertNotNull(serviceProvider, "Failed to create Service Provider.");

            DefaultAuthenticationSequence sequence = getDefaultAuthSeq(seqName, seqDesc1_updated2, null,
                    serviceProvider.getLocalAndOutBoundAuthenticationConfig());
            defaultAuthSeqMgtServiceClient.updateDefaultAuthenticationSeq(sequence);

            DefaultAuthenticationSequence receivedSequence = defaultAuthSeqMgtServiceClient
                    .getDefaultAuthenticationSeq();
            assertNotNull(receivedSequence, "Failed to create default auth sequence from existing Service " +
                    "Provider config");
            assertEquals(receivedSequence.getName(), seqName,
                    "Failed to create default auth sequence from existing Service Provider config");
            assertEquals(receivedSequence.getDescription(), seqDesc1_updated2, "Failed to create default auth " +
                    "sequence from existing Service Provider config");
        } catch (Exception e) {
            fail("Error while trying to create default auth sequence from existing Service Provider " +
                    "config", e);
        }
    }

    @Test(alwaysRun = true, description = "Testing delete default authentication sequence",
            dependsOnMethods = "createDefaultAuthSeqFromSP")
    public void testDeleteDefaultAuthSeq() {

        try {
            defaultAuthSeqMgtServiceClient.deleteDefaultAuthenticationSeq();

            DefaultAuthenticationSequence receivedSequence = defaultAuthSeqMgtServiceClient
                    .getDefaultAuthenticationSeqInXML();
            assertNull(receivedSequence, "Failed to delete the tenant default auth sequence");
        } catch (Exception e) {
            fail("Error while trying to delete default authentication sequence.", e);
        }
    }

    private void createApplication(String name, String description) {

        try {
            ServiceProvider serviceProvider = new ServiceProvider();
            serviceProvider.setApplicationName(name);
            serviceProvider.setDescription(description);
            applicationManagementServiceClient.createApplication(serviceProvider);
        } catch (Exception e) {
            fail("Error while trying to create Service Provider", e);
        }
    }

    private void updateApplicationData(String name) {
        try {
            ServiceProvider serviceProvider = applicationManagementServiceClient.getApplication(name);

            serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationType
                    ("flow");
            AuthenticationStep authStep = new AuthenticationStep();
            authStep.setStepOrder(1);
            authStep.setAttributeStep(true);
            authStep.setSubjectStep(true);
            LocalAuthenticatorConfig localAuthenticatorConfig = new LocalAuthenticatorConfig();
            localAuthenticatorConfig.setName("BasicAuthenticator");
            localAuthenticatorConfig.setDisplayName("basic");
            localAuthenticatorConfig.setEnabled(true);
            authStep.setLocalAuthenticatorConfigs(new LocalAuthenticatorConfig[]{localAuthenticatorConfig});
            serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationSteps(
                    new AuthenticationStep[]{authStep});

            AuthenticationScriptConfig scriptConfig = new AuthenticationScriptConfig();
            scriptConfig.setLanguage("application/javascript");
            scriptConfig.setContent("function onLoginRequest(context) {\r\n  executeStep(1);\r\n}\r\n");
            serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationScriptConfig(scriptConfig);

            applicationManagementServiceClient.updateApplicationData(serviceProvider);
            ServiceProvider updatedServiceProvider = applicationManagementServiceClient
                    .getApplication(applicationName);

            Assert.assertEquals(updatedServiceProvider.getLocalAndOutBoundAuthenticationConfig()
                            .getAuthenticationSteps()[0].getLocalAuthenticatorConfigs()[0].getDisplayName(), "basic",
                    "Failed update Authentication step");
        } catch (Exception e) {
            fail("Error while trying to update Service Provider", e);
        }
    }

    private ServiceProvider getApplication(String name) {

        try {
            return applicationManagementServiceClient.getApplication(name);
        } catch (Exception e) {
            fail("Error while trying to retrieve Service Provider", e);
        }
        return null;
    }

    private DefaultAuthenticationSequence getDefaultAuthSeq(String seqName, String seqDesc, String seqContentXml,
                                                            LocalAndOutboundAuthenticationConfig seqContent) {

        DefaultAuthenticationSequence sequence = new DefaultAuthenticationSequence();
        sequence.setName(seqName);
        sequence.setDescription(seqDesc);
        sequence.setContentXml(seqContentXml);
        sequence.setContent(seqContent);
        return sequence;
    }

    private String readResource(String path) {

        StringBuilder result = new StringBuilder();
        Scanner scanner = null;
        try {
            //Get file from resources folder
            ClassLoader classLoader = getClass().getClassLoader();
            if (classLoader != null && path != null && classLoader.getResource(path) != null) {
                URI filepath = new URI(classLoader.getResource(path).toString());
                File file = new File(filepath);
                scanner = new Scanner(file);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    result.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            log.error("Error occurred when reading the file.", e);
        } catch (URISyntaxException e) {
            log.error("URI syntax error.", e);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return result.toString().replaceAll("\\n\\r|\\n|\\r|\\t|\\s{2,}", "").replaceAll(": ", ":");
    }
}
