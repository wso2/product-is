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

package org.wso2.identity.ui.integration.test.login;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.identity.integration.common.ui.page.LoginPage;
import org.wso2.identity.integration.common.ui.page.main.HomePage;
import org.wso2.identity.integration.common.utils.ISIntegrationUITest;

import java.util.ArrayList;
import java.util.List;

public class ISLoginTestCase extends ISIntegrationUITest {
    private WebDriver driver;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
    }

    @Test(groups = "wso2.identity", description = "verify login to IS Server")
    public void testLogin() throws Exception {
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL());
        LoginPage test = new LoginPage(driver);
        HomePage home = test.loginAs(userInfo.getUserName(), userInfo.getPassword());
        home.logout();
        driver.close();
    }

    @Test(groups = "wso2.identity", description = "verify XSS vulnerability fix")
    public void testXSSVulnerability() throws Exception {
        boolean isVulnerable = false;
        String authEndpoint = backendURL.substring(0, 22) + "/authenticationendpoint/login.do?";
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("client_id", "XXXXXXXX"));
        params.add(new BasicNameValuePair("commonAuthCallerPath", "/oauth2/authorize"));
        params.add(new BasicNameValuePair("forceAuth", "false"));
        params.add(new BasicNameValuePair("passiveAuth", "false"));
        params.add(new BasicNameValuePair("redirect_uri", "https://localhost/login"));
        params.add(new BasicNameValuePair("response_type", "code"));
        params.add(new BasicNameValuePair("scope", "openid"));
        params.add(new BasicNameValuePair("state", "XXXXXXXX"));
        params.add(new BasicNameValuePair("tenantDomain", "carbon.super"));
        params.add(new BasicNameValuePair("sessionDataKey", "XXXXXXXX"));
        params.add(new BasicNameValuePair("relyingParty", "XXXXXXXX"));
        params.add(new BasicNameValuePair("type", "oidc"));
        params.add(new BasicNameValuePair("sp", "mytestapp"));
        params.add(new BasicNameValuePair("isSaaSApp", "false"));
        params.add(new BasicNameValuePair("authenticators", "BasicAuthenticator:LOCAL\"></script>\"//'//" +
                "<svg onload=(function(){setTimeout(function(){$('body').prepend($('<div></div>')" +
                ".attr('id','vulnerable'))},2000)})()//"));
        driver = BrowserManager.getWebDriver();
        driver.get(authEndpoint + URLEncodedUtils.format(params, "UTF-8"));
        WebDriverWait webDriverWait = new WebDriverWait(driver, 5);
        try {
            webDriverWait.until(ExpectedConditions.presenceOfElementLocated(By.id("vulnerable")));
            // If not vulnerable, element with #vulnerable would not get injected
            // Therefore it'll throw an exception mentioning that.
            isVulnerable = true;
        } catch (Exception ignored) {
        }
        Assert.assertFalse(isVulnerable);
        driver.close();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }
}
