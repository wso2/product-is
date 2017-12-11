/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.user.mgt;


import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class CARBON15051EmailLoginTestCase extends ISIntegrationTest {

    private LoginLogoutClient loginLogoutClient;
    private UserManagementClient userManagementClient;
    private String emailUser = "user1@test.com";
    private String emailUserPassword = "passWord1@";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        loginLogoutClient = new LoginLogoutClient(isServer);

        userManagementClient = new UserManagementClient(backendURL, getSessionCookie());
        userManagementClient.addUser(emailUser, emailUserPassword, new String[]{"admin"}, null);
    }

    @Test(groups = "wso2.is", description = "Trying to log in with email as the username")
    public void testLoginWithEmail() throws Exception {
        this.loginLogoutClient.login();
        String backendURL = isServer.getContextUrls().getBackEndUrl();
        login("admin", "admin",  backendURL);
        login("admin@carbon.super", "admin", backendURL);
        login(emailUser, emailUserPassword, backendURL);
        login(emailUser + "@carbon.super", emailUserPassword, backendURL);
        this.loginLogoutClient.logout();
    }

    /**
     * Log in to a Carbon server
     *
     * @return The session cookie on successful login
     */
    private String login(String username, String password, String backendUrl) throws LoginAuthenticationExceptionException, IOException, XMLStreamException,
                                                                                     URISyntaxException, SAXException, XPathExpressionException {
        AuthenticatorClient authenticatorClient = new AuthenticatorClient(backendUrl);
        return authenticatorClient.login(username, password, new URL(backendUrl).getHost());
    }
}