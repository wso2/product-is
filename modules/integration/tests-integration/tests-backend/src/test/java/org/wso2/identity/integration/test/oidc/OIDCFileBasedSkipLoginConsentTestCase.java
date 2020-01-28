/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.integration.test.oidc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.test.oidc.bean.OIDCApplication;
import org.wso2.identity.integration.test.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.xml.xpath.XPathExpressionException;

/**
 * This class is to handle the test cases to skip the login consent based on the file and per service provider.
 */
public class OIDCFileBasedSkipLoginConsentTestCase extends OIDCAbstractIntegrationTest {

    private static final String SKIP_CONSENT_ENABLED_TOML = "skip_consent_enabled.toml";
    protected Log log = LogFactory.getLog(getClass());
    protected HttpClient client;
    private CookieStore cookieStore = new BasicCookieStore();
    protected String sessionDataKey;
    protected String sessionDataKeyConsent;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        changeISConfiguration(SKIP_CONSENT_ENABLED_TOML);
        // Re-initiating after the restart.
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        OIDCUtilTest.initUser();
        createUser(OIDCUtilTest.user);
        OIDCUtilTest.initApplications();
        createApplications();
        client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
    }

    @AfterClass(alwaysRun = true)
    public void clearObjects() throws Exception {

        deleteObjects();
        clear();
    }

    private void changeISConfiguration(String fileName) throws IOException,
            XPathExpressionException, AutomationUtilException {

        log.info("Replacing identity.xml to skip the user consent in OIDC flows.");
        String carbonHome = Utils.getResidentCarbonHome();
        File defaultTomlFile = getDeploymentTomlFile(carbonHome);
        File configuredTomlFile = new File
                (getISResourceLocation() + File.separator + "oauth" + File.separator + fileName);
        ServerConfigurationManager serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredTomlFile, defaultTomlFile, true);
        serverConfigurationManager.restartForcefully();
    }

    private void deleteObjects() throws Exception {

        deleteUser(OIDCUtilTest.user);
        deleteApplications();
    }

    @Test(groups = "wso2.is", description = "Test authz endpoint before creating a valid session")
    public void testCreateUserSession() throws Exception {

        testSendAuthenticationRequest(OIDCUtilTest.applications.get(OIDCUtilTest.playgroundAppOneAppName), true, client,
                cookieStore);
        testAuthentication(OIDCUtilTest.applications.get(OIDCUtilTest.playgroundAppOneAppName));
    }

    @Test(groups = "wso2.is", description = "Initiate authentication request from playground.apptwo")
    public void testIntiateLoginRequestForAlreadyLoggedUser() throws Exception {

        testSendAuthenticationRequest(OIDCUtilTest.applications.get(OIDCUtilTest.playgroundAppTwoAppName), false, client,
                cookieStore);
    }

    private void testAuthentication(OIDCApplication application) throws Exception {

        HttpResponse response = sendLoginPost(client, sessionDataKey);
        EntityUtils.consume(response.getEntity());
    }

    private void createApplications() throws Exception {

        for (Map.Entry<String, OIDCApplication> entry : OIDCUtilTest.applications.entrySet()) {
            createApplication(entry.getValue());
        }
    }

    private void deleteApplications() throws Exception {

        for (Map.Entry<String, OIDCApplication> entry : OIDCUtilTest.applications.entrySet()) {
            deleteApplication(entry.getValue());
        }
    }
}
