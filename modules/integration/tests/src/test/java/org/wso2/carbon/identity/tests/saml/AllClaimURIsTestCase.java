/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.identity.tests.saml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.identity.sso.saml.SAMLSSOConfigServiceClient;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.carbon.identity.sso.saml.stub.IdentitySAMLSSOConfigServiceIdentityException;

import java.rmi.RemoteException;

public class AllClaimURIsTestCase {

//    private static final Log log = LogFactory.getLog(AllClaimURIsTestCase.class);
//    private EnvironmentVariables identityServer;

//    @BeforeTest(alwaysRun = true)
//    public void testInit() throws LoginAuthenticationExceptionException, RemoteException {
//        int userId = 2;
//        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
//        EnvironmentBuilder builder = new EnvironmentBuilder().is(userId);
//        identityServer = builder.build().getIs();
//    }
//
//    @Test(groups = "wso2.is", description = "Get all claims")
//    public void testAllClaims()
//            throws Exception {
//        SAMLSSOConfigServiceClient ssoConfigurationClient =
//                new SAMLSSOConfigServiceClient(identityServer.getBackEndUrl(),
//                                               identityServer.getSessionCookie());
//        ssoConfigurationClient.getClaimURIs();
//    }

}
