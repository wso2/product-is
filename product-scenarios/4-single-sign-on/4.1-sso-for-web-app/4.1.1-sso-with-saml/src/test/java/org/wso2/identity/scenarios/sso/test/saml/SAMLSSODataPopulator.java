/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.scenarios.sso.test.saml;

import org.apache.axis2.AxisFault;
import org.apache.xml.security.signature.XMLSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IExecutionListener;
import org.testng.annotations.Test;
import org.wso2.identity.scenarios.commons.SAMLConfig;
import org.wso2.identity.scenarios.commons.SAMLSSOExternalAppTestClient;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;
import org.wso2.identity.scenarios.commons.TestConfig;
import org.wso2.identity.scenarios.commons.TestUserMode;
import org.wso2.identity.scenarios.commons.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;

import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.identity.scenarios.commons.util.Constants.DEFAULT_PROFILE_NAME;
import static org.wso2.identity.scenarios.commons.util.Constants.HttpBinding.HTTP_REDIRECT;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.getTestUser;

public class SAMLSSODataPopulator extends ScenarioTestBase implements IExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(SAMLSSODataPopulator.class);

    private static final String APPLICATION_NAME = "SAML-SSO-TestApplication";
    private static final String ISSUER_NAME = "travelocity.com";

    private SAMLSSOExternalAppTestClient samlssoExternalAppClient;
    private RemoteUserStoreManagerServiceClient remoteUSMServiceClient;
    private SAMLConfig config;

    @Override
    @Test(description = "4.1.1.1")
    public void onExecutionStart() {
        try {
            super.init();
            loginAndObtainSessionCookie();
            this.remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendServiceURL, sessionCookie);
            this.config = new SAMLConfig(TestUserMode.SUPER_TENANT_ADMIN, new TestConfig.User(getTestUser("super-tenant-user" +
                    ".json"), SUPER_TENANT_DOMAIN_NAME), TestConfig.ClaimType.NONE,
                    HTTP_REDIRECT, null, ISSUER_NAME, "", XMLSignature.ALGO_ID_SIGNATURE_RSA, "", true);
            this.samlssoExternalAppClient = new SAMLSSOExternalAppTestClient(backendURL, sessionCookie,
                    backendServiceURL,
                    webAppHost, configContext, config);

            super.createUser(config, remoteUSMServiceClient, DEFAULT_PROFILE_NAME);
            samlssoExternalAppClient.createApplication(config, APPLICATION_NAME);
            Boolean isAddSuccess = samlssoExternalAppClient.createSAMLconfigForServiceProvider();
            assertTrue(isAddSuccess, "Adding a service provider has failed for " + config);

        } catch (AxisFault axisFault) {
            log.error("Unable to create RemoteUserStoreManagerServiceClient", axisFault);
        } catch (Exception e) {
            log.error("Unable to populate test data", e);
        }
    }

    @Override
    @Test(description = "4.1.1.2")
    public void onExecutionFinish() {
        super.deleteUser(config, remoteUSMServiceClient);
        try {
            Boolean isAddSuccess = samlssoExternalAppClient.removeServiceProvider(config);
            assertTrue(isAddSuccess, "Removing a service provider has failed for " + config);
            samlssoExternalAppClient.deleteApplication(APPLICATION_NAME);
        } catch (Exception e) {
            log.error("Unable to delete test data.", e);
        }
    }
}
