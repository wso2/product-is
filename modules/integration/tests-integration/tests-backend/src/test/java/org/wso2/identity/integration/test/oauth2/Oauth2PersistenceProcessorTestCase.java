/*
 * Copyright (c) 2017, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.oauth2;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Oauth2 client id & client secret persistence processor test case.
 */
public class Oauth2PersistenceProcessorTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private ServerConfigurationManager serverConfigurationManager;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        resetISConfiguration();
        restClient.closeHttpClient();
    }

    @Test(groups = "wso2.is", description = "Test PlainTextPersistenceProcessor")
    public void testPlainTextPersistenceProcessor() throws Exception {

        ApplicationResponseModel application = createApplication("app1");
        Assert.assertNotNull(application, "Application creation failed.");
        String applicationId1 = application.getId();

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId1);
        String consumerKey1 = oidcConfig.getClientId();
        Assert.assertNotNull(consumerKey1, "Application creation failed.");

        String consumerSecret1 = oidcConfig.getClientSecret();
        Assert.assertNotNull(consumerSecret1, "Application creation failed.");

        deleteApp(applicationId1);
    }

    @Test(groups = "wso2.is", description = "Test EncryptionDecryptionPersistenceProcessor",
            dependsOnMethods = "testPlainTextPersistenceProcessor")
    public void testEncryptionDecryptionPersistenceProcessor() throws Exception {

        changeISConfiguration();
        super.init(TestUserMode.SUPER_TENANT_USER);

        ApplicationResponseModel application = createApplication("app2");
        Assert.assertNotNull(application, "Application creation failed.");
        String applicationId2 = application.getId();

        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId2);
        String consumerKey2 = oidcConfig.getClientId();
        Assert.assertNotNull(consumerKey2, "Application creation failed.");

        String consumerSecret2 = oidcConfig.getClientSecret();
        Assert.assertNotNull(consumerSecret2, "Application creation failed.");

        deleteApp(applicationId2);
    }

    private ApplicationResponseModel createApplication(String applicationName) throws Exception {

        ApplicationModel application = new ApplicationModel();

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, "authorization_code", "implicit", "password", "client_credentials",
                "refresh_token", "urn:ietf:params:oauth:grant-type:saml2-bearer", "iwa:ntlm");

        List<String> callBackUrls = new ArrayList<>();
        Collections.addAll(callBackUrls, OAuth2Constant.CALLBACK_URL);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setCallbackURLs(callBackUrls);

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setName(applicationName);
        application.setIsManagementApp(true);

        String appId = addApplication(application);

        return getApplication(appId);
    }

    private void changeISConfiguration() throws Exception {

        log.info("Enabling EncryptionDecryptionPersistenceProcessor.");

        String carbonHome = Utils.getResidentCarbonHome();
        File defaultTomlFile = getDeploymentTomlFile(carbonHome);
        File configuredTomlFile = new File(getISResourceLocation() + File.separator
                + "identity_encryption_enabled.toml");
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredTomlFile, defaultTomlFile, true);
        serverConfigurationManager.restartGracefully();
    }

    private void resetISConfiguration() throws Exception {

        serverConfigurationManager.restoreToLastConfiguration(false);
    }
}
