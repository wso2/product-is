/*
 * Copyright (c) 2020, WSO2 LLC. (http://www.wso2.com).
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
import org.wso2.identity.integration.test.base.MockClientCallback;
import org.wso2.identity.integration.test.oidc.bean.OIDCApplication;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdvancedApplicationConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationPatchModel;

import java.util.Map;

/**
 * This class is to handle the test cases to skip the login consent based on the file and per service provider.
 */
public class OIDCSPWiseSkipLoginConsentTestCase extends OIDCAbstractIntegrationTest {

    protected Log log = LogFactory.getLog(getClass());
    protected HttpClient client;
    private CookieStore cookieStore = new BasicCookieStore();
    protected String sessionDataKey;
    protected String sessionDataKeyConsent;
    private MockClientCallback mockClientCallback;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        OIDCUtilTest.initUser();
        createUser(OIDCUtilTest.user);
        OIDCUtilTest.initApplications();
        createApplications();
        configureSPToSkipConsent();
        client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();

        mockClientCallback = new MockClientCallback();
        mockClientCallback.start();
    }

    @AfterClass(alwaysRun = true)
    public void clearObjects() throws Exception {

        deleteObjects();
        clear();
        mockClientCallback.stop();
    }

    private void deleteObjects() throws Exception {

        deleteUser(OIDCUtilTest.user);
        deleteApplications();
    }

    private void configureSPToSkipConsent() throws Exception {

        OIDCApplication oidcApplication = OIDCUtilTest.applications.get(OIDCUtilTest.playgroundAppTwoAppName);
        ApplicationPatchModel applicationPatch = new ApplicationPatchModel();
        applicationPatch.setAdvancedConfigurations(new AdvancedApplicationConfiguration().skipLoginConsent(true));
        updateApplication(oidcApplication.getApplicationId(), applicationPatch);
    }

    @Test(groups = "wso2.is", description = "Test authz endpoint before creating a valid session")
    public void testCreateUserSession() throws Exception {

        testSendAuthenticationRequest(OIDCUtilTest.applications.get(OIDCUtilTest.playgroundAppOneAppName), true,
                client, cookieStore);
        testAuthentication();
    }

    @Test(groups = "wso2.is", description = "Initiate authentication request from playground.apptwo")
    public void testInitiateLoginRequestForAlreadyLoggedUser() throws Exception {

        testSendAuthenticationRequest(OIDCUtilTest.applications.get(OIDCUtilTest.playgroundAppTwoAppName), false,
                client, cookieStore);
    }

    private void testAuthentication() throws Exception {

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
