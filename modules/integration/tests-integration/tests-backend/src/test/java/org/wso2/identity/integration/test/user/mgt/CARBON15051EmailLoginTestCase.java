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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class CARBON15051EmailLoginTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(CARBON15051EmailLoginTestCase.class);
    private LoginLogoutClient loginLogoutClient;
    private UserManagementClient userManagementClient;
    private ServerConfigurationManager serverConfigurationManager;
    private String adminUsername;
    private String adminPassword;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String pathToCarbonXML = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "IS" + File.separator +
                                 "userMgt" + File.separator + "carbon15051" + File.separator + "carbon.xml";
        String targetCarbonXML = CarbonUtils.getCarbonHome() + "repository" + File.separator + "conf" + File.separator + "carbon.xml";
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfiguration(new File(pathToCarbonXML), new File(targetCarbonXML));


        String pathToUserMgtXML = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "IS" +
                File.separator +
                "userMgt" + File.separator + "carbon15051" + File.separator + "user-mgt.xml";
        String targetUserMgtXML = CarbonUtils.getCarbonHome() + "repository" + File.separator + "conf" + File.separator + "user-mgt.xml";
        serverConfigurationManager.applyConfiguration(new File(pathToUserMgtXML), new File(targetUserMgtXML));

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        loginLogoutClient = new LoginLogoutClient(isServer);

        adminUsername = userInfo.getUserName();
        adminPassword = userInfo.getPassword();

        userManagementClient = new UserManagementClient(backendURL, getSessionCookie());
        userManagementClient.addUser("user1@test.com", "passWord1@", new String[]{"admin"}, null);
    }

    @AfterClass(alwaysRun = true)
    public void endTest() throws Exception {

        serverConfigurationManager.restoreToLastConfiguration();
    }

    @Test(groups = "wso2.is", description = "Trying to log in with email as the username")
    public void testLoginWithEmail() throws Exception {
        this.loginLogoutClient.login();
        String backendURL = isServer.getContextUrls().getBackEndUrl();
        login("admin", "admin",  backendURL);
        login("admin@carbon.super", "admin", backendURL);
        login("user1@test.com", "passWord1@", backendURL);
        login("user1@test.com@carbon.super", "passWord1@", backendURL);
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