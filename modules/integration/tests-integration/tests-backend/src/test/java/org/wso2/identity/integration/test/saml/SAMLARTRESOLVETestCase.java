/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.identity.integration.test.saml;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.CommonConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SAMLARTRESOLVETestCase extends AbstractSAMLSSOTestCase {

    private static final Log log = LogFactory.getLog(SAMLSSOTestCase.class);

    private SAMLConfig config;

    @Factory(dataProvider = "samlConfigProvider")
    public SAMLARTRESOLVETestCase(SAMLConfig config) {

        if (log.isDebugEnabled()) {
            log.info("SAML Artifact Binding Test initialized for " + config);
        }
        this.config = config;
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(config.getUserMode());
        super.testInit();
        super.createUser(config);
        super.createApplicationForSAMLArtResolve(config, config.getApp().getArtifact());
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {

        super.deleteUser(config);
        super.deleteApplication(config.getApp().getArtifact());
        super.testClear();
    }

    @Test(alwaysRun = true, description = "Testing SAML SSO login", groups = "wso2.is")
    public void testSAMLSSOLogin() {
        try {
            HttpResponse response;
            Boolean isAddSuccess = ssoConfigServiceClient.
                    addServiceProvider(super.createSsoSPDTOForSAMLartifactBinding(config));
            Assert.assertTrue(isAddSuccess, "Adding a service provider has failed for " + config);

            SAMLSSOServiceProviderDTO[] samlssoServiceProviderDTOs = ssoConfigServiceClient
                    .getServiceProviders().getServiceProviders();
            Assert.assertEquals(samlssoServiceProviderDTOs[0].getIssuer(), config.getApp().getArtifact(),
                    "Adding a service provider has failed for " + config);

            response = Utils.sendGetRequest(String.format(SAML_SSO_LOGIN_URL, config.getApp().getArtifact(), config
                    .getHttpBinding().binding), USER_AGENT, httpClient);

            String sessionKey = Utils.extractDataFromResponse(response, CommonConstants.SESSION_DATA_KEY, 1);
            response = Utils.sendPOSTMessage(sessionKey, SAML_SSO_URL, USER_AGENT, ACS_URL, config.getApp()
                    .getArtifact(), config.getUser().getUsername(), config.getUser().getPassword(), httpClient);

            String samlRedirectUrl = Utils.getRedirectUrl(response);
            Assert.assertTrue(samlRedirectUrl.contains("SAMLart"), "SAML artifact binding failed for " + config);
            EntityUtils.consume(response.getEntity());
            response = Utils.sendGetRequest(samlRedirectUrl, USER_AGENT, httpClient);
            String samlRedirectPage = extractDataFromResponse(response);
            EntityUtils.consume(response.getEntity());

            Assert.assertTrue(samlRedirectPage.contains("You are logged in as " +
                            config.getUser().getTenantAwareUsername()),
                    "SAML artifact binding failed for " + config);

            Boolean isRemoveSuccess = ssoConfigServiceClient.removeServiceProvider(config.getApp().getArtifact());
            Assert.assertTrue(isRemoveSuccess, "Removing a service provider has failed for " + config);

        } catch (Exception e) {
            Assert.fail("SAML SSO Login test failed for " + config, e);
        }
    }

    @DataProvider(name = "samlConfigProvider")
    public static Object[][] samlConfigProvider() {
        return new SAMLConfig[][]{
                {new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, User.SUPER_TENANT_USER, HttpBinding.HTTP_REDIRECT,
                        ClaimType.NONE, App.SUPER_TENANT_APP_WITH_SIGNING)},
                {new SAMLConfig(TestUserMode.TENANT_ADMIN, User.TENANT_USER, HttpBinding.HTTP_REDIRECT,
                        ClaimType.NONE, App.TENANT_APP_WITHOUT_SIGNING)},
        };
    }

    private String extractDataFromResponse(HttpResponse response) throws IOException {
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }
}
