/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.integration.test.oauth2;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.File;

/**
 * Oauth2 client id & client secret persistence processor test case.
 */
public class Oauth2PersistenceProcessorTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private ServerConfigurationManager serverConfigurationManager;
    ;
    private String consumerKey1;
    private String consumerKey2;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApplication(consumerKey1);
        deleteApplication(consumerKey2);
        resetISConfiguration();
    }

    @Test(groups = "wso2.is", description = "Test PlainTextPersistenceProcessor")
    public void testPlainTextPersistenceProcessor() throws Exception {

        OAuthConsumerAppDTO oAuthConsumerAppDTO = createApplication("app1");
        Assert.assertNotNull(oAuthConsumerAppDTO, "Application creation failed.");

        consumerKey1 = oAuthConsumerAppDTO.getOauthConsumerKey();
        Assert.assertNotNull(consumerKey1, "Application creation failed.");

        String consumerSecret1 = oAuthConsumerAppDTO.getOauthConsumerSecret();
        Assert.assertNotNull(consumerSecret1, "Application creation failed.");
    }

    @Test(groups = "wso2.is", description = "Test EncryptionDecryptionPersistenceProcessor",
            dependsOnMethods = "testPlainTextPersistenceProcessor")
    public void testEncryptionDecryptionPersistenceProcessor() throws Exception {

        changeISConfiguration();
        super.init(TestUserMode.SUPER_TENANT_USER);

        OAuthConsumerAppDTO oAuthConsumerAppDTO = createApplication("app2");
        Assert.assertNotNull(oAuthConsumerAppDTO, "Application creation failed.");

        consumerKey2 = oAuthConsumerAppDTO.getOauthConsumerKey();
        Assert.assertNotNull(consumerKey2, "Application creation failed.");

        String consumerSecret2 = oAuthConsumerAppDTO.getOauthConsumerSecret();
        Assert.assertNotNull(consumerSecret2, "Application creation failed.");
    }

    private OAuthConsumerAppDTO createApplication(String applicationName) throws Exception {

        OAuthConsumerAppDTO oAuthConsumerAppDTO = new OAuthConsumerAppDTO();
        oAuthConsumerAppDTO.setApplicationName(applicationName);
        oAuthConsumerAppDTO.setCallbackUrl(OAuth2Constant.CALLBACK_URL);
        oAuthConsumerAppDTO.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
        oAuthConsumerAppDTO.setGrantTypes("authorization_code implicit password client_credentials refresh_token "
                + "urn:ietf:params:oauth:grant-type:saml2-bearer iwa:ntlm");

        adminClient.registerOAuthApplicationData(oAuthConsumerAppDTO);
        OAuthConsumerAppDTO[] appDTOs = adminClient.getAllOAuthApplicationData();

        for (OAuthConsumerAppDTO appDTO : appDTOs) {
            if (appDTO.getApplicationName().equals(applicationName)) {
                return appDTO;
            }
        }
        return null;
    }

    private void deleteApplication(String consumerKey) throws Exception {

        adminClient.removeOAuthApplicationData(consumerKey);
    }

    private void changeISConfiguration() throws Exception {

        log.info("Enabling EncryptionDecryptionPersistenceProcessor.");

        String carbonHome = CarbonUtils.getCarbonHome();
        File identityXML = new File(carbonHome + File.separator + "repository" + File.separator + "conf" + File.separator
                + "identity" + File.separator + "identity.xml");
        File configuredIdentityXML = new File(getISResourceLocation() + File.separator + "oauth" + File.separator
                + "encrypt-decrypt-persistence-enabled-identity.xml");
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredIdentityXML, identityXML, true);
        serverConfigurationManager.restartGracefully();
    }

    private void resetISConfiguration() throws Exception {

        log.info("Replacing identity.xml with default configurations");
        String carbonHome = CarbonUtils.getCarbonHome();
        File identityXML = new File(carbonHome + File.separator + "repository" + File.separator + "conf" + File.separator
                + "identity" + File.separator + "identity.xml");
        File defaultIdentityXML = new File(getISResourceLocation() + File.separator + "default-identity.xml");

        serverConfigurationManager.applyConfigurationWithoutRestart(defaultIdentityXML, identityXML, true);
        serverConfigurationManager.restartGracefully();
    }

}
