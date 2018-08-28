/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.ui.integration.test.oidc;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.ui.integration.test.utils.OIDCUITestConstants;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This test class tests OIDC session management functionality by registering to RPs
 */
public class OIDCAuthCodeSessionTestCase extends OIDCAbstractUIIntegrationTest {

    private static final String username = "oidcsessiontestuser";
    private static final String password = "oidcsessiontestuser";
    private static final String email = "oidcsessiontestuser@wso2.com";
    private static final String firstName = "oidcsessiontestuser-first";
    private static final String lastName = "oidcsessiontestuser-last";
    private static final String role = "internal/everyone";
    private static final String profile = "default";

    private static final String playground2AppName = "playground2";
    private static final String playground2AppCallBackUri = "http://localhost:8490/playground2/oauth2client";
    private static final String playground2AppContext = "/playground2";

    private static final String playground3AppName = "playground3";
    private static final String playground3AppCallBackUri = "http://localhost:8490/playground3/oauth2client";
    private static final String playground3AppContext = "/playground3";

    private static final String targetApplicationUrl = "http://localhost:8490%s";

    private static final String emailClaimUri = "http://wso2.org/claims/emailaddress";
    private static final String firstNameClaimUri = "http://wso2.org/claims/givenname";
    private static final String lastNameClaimUri = "http://wso2.org/claims/lastname";

    private OIDCUser user;
    private Map<String, OIDCApplication> applications = new HashMap<>(2);

    private String playground2AppWindow;
    private String playground3AppWindow;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();

        createUser();
        createApplications();

        startTomcat();
        deployApplications();

        loadWindows();
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {

        driver.quit();

        deleteUser(user);
        deleteApplications();

        stopTomcat();

        applicationManagementServiceClient = null;
        remoteUserStoreManagerServiceClient = null;
        oauthAdminClient = null;

    }

    @Test(groups = "wso2.identity", description = "Initiate authentication request from playground2")
    public void testSendAuthenticationRequestFromRP1() throws Exception {

        driver.switchTo().window(playground2AppWindow);

        sendAuthenticationRequest(driver, applications.get(playground2AppName));
        Assert.assertTrue(driver.getCurrentUrl().contains(OIDCUITestConstants.AuthEndpointPaths.loginPagePath),
                          "Authentication request failed. Was expecting a redirection to the login page");
    }

    @Test(groups = "wso2.identity", description = "Authenticate end user to playground2",
            dependsOnMethods = "testSendAuthenticationRequestFromRP1")
    public void testAuthenticateForRP1() {

        // Set username
        driver.findElement(By.id(OIDCUITestConstants.AuthEndpointElementIdentifiers.usernameElement))
              .sendKeys(user.getUsername());

        // Set password
        driver.findElement(By.id(OIDCUITestConstants.AuthEndpointElementIdentifiers.passwordElement))
              .sendKeys(user.getPassword());

        // Perform form submit
        driver.findElement(By.xpath(OIDCUITestConstants.AuthEndpointElementIdentifiers.signInButtonElement)).click();

        Assert.assertTrue(driver.getCurrentUrl().contains(OIDCUITestConstants.AuthEndpointPaths.loginConsentPagePath),
                          "User authentication failed. Was expecting a redirection to the login consent page");
    }

    @Test(groups = "wso2.identity", description = "Authorize end user to playground2",
            dependsOnMethods = "testAuthenticateForRP1")
    public void testApproveLoginConsentForRP1() throws MalformedURLException {

        performLoginConsentApproval(driver);
    }

    @Test(groups = "wso2.identity", description = "Send token request from playground2",
            dependsOnMethods = "testApproveLoginConsentForRP1")
    public void testSendTokenRequestFromRP1() {

        sendTokenRequest(driver, applications.get(playground2AppName));

        Assert.assertTrue(
                !driver.findElement(By.id(OIDCUITestConstants.PlaygroundAppElementIdentifiers.accessTokenElement))
                       .getAttribute("value").isEmpty(), "Access Token not received");
        Assert.assertTrue(
                driver.findElement(By.id(OIDCUITestConstants.PlaygroundAppElementIdentifiers.loggedUserElement))
                      .getText().contains(user.getUsername()), "Id Token not received");
    }

    @Test(groups = "wso2.identity", description = "Initiate authentication request from playground3",
            dependsOnMethods = "testSendTokenRequestFromRP1")
    public void testSendAuthenticationRequestFromRP2() throws Exception {

        driver.switchTo().window(playground3AppWindow);

        sendAuthenticationRequest(driver, applications.get(playground3AppName));

        Assert.assertTrue(driver.getCurrentUrl().contains(OIDCUITestConstants.AuthEndpointPaths.loginConsentPagePath),
                          "User authentication failed. Was expecting a redirection to the login consent page");
    }

    @Test(groups = "wso2.identity", description = "Authorize end user to playground3",
            dependsOnMethods = "testSendAuthenticationRequestFromRP2")
    public void testApproveLoginConsentForRP2() throws MalformedURLException, InterruptedException {

        performLoginConsentApproval(driver);
    }

    @Test(groups = "wso2.identity", description = "Initiate passive request and update session state from playground2",
            dependsOnMethods = "testApproveLoginConsentForRP2")
    public void testSessionStateRequestFromRP1() throws MalformedURLException, InterruptedException {

        driver.switchTo().window(playground2AppWindow);

        boolean passiveResponseReceived = false;
        for (int i = 0; i < 6; i++) {
            if (driver.getCurrentUrl().contains(OIDCUITestConstants.PlaygroundAppPaths.callBackPath)) {
                passiveResponseReceived = true;
                break;
            }
            Thread.sleep(1000);
        }

        if (passiveResponseReceived) {
            validateAuthenticationResponse(driver);
        } else {
            Assert.fail("Session State has changed. But no passive request is initiated from playground2 app.");
        }
    }

    @Test(groups = "wso2.identity", description = "Send token request from playground3",
            dependsOnMethods = "testSessionStateRequestFromRP1")
    public void testSendTokenRequestFromRP2() {

        driver.switchTo().window(playground3AppWindow);

        sendTokenRequest(driver, applications.get(playground3AppName));

        Assert.assertTrue(
                !driver.findElement(By.id(OIDCUITestConstants.PlaygroundAppElementIdentifiers.accessTokenElement))
                       .getAttribute("value").isEmpty(), "Access Token not received");
        Assert.assertTrue(
                driver.findElement(By.id(OIDCUITestConstants.PlaygroundAppElementIdentifiers.loggedUserElement))
                      .getText().contains(user.getUsername()), "Id Token not received");
    }

    @Test(groups = "wso2.identity", description = "Send logout request from playground3",
            dependsOnMethods = "testSendTokenRequestFromRP2")
    public void testLogoutFromRP2() {

        driver.findElement(By.xpath(OIDCUITestConstants.PlaygroundAppElementIdentifiers.logoutButtonElement)).click();

        Assert.assertTrue(driver.getCurrentUrl().contains(OIDCUITestConstants.AuthEndpointPaths.logoutConsentPagePath),
                          "User logout failed. Was expecting a redirection to the logout consent page");
    }

    @Test(groups = "wso2.identity", description = "Approve logout request from playground3",
            dependsOnMethods = "testLogoutFromRP2")
    public void testApproveLogoutConsentForRP2() {

        driver.findElement(By.id(OIDCUITestConstants.AuthEndpointElementIdentifiers.logoutApproveButtonElement))
              .click();
        Assert.assertTrue(driver.getPageSource().contains("You have successfully logged out."),
                          "User logout has failed");
    }

    @Test(groups = "wso2.identity", description = "Send passive request and logout from playground2",
            dependsOnMethods = "testApproveLogoutConsentForRP2")
    public void testLogoutOfRP1() throws InterruptedException {

        driver.switchTo().window(playground2AppWindow);

        boolean isLogout = false;
        for (int i = 0; i < 6; i++) {
            if (driver.getCurrentUrl().contains(OIDCUITestConstants.PlaygroundAppPaths.homePagePath)) {
                isLogout = true;
                break;
            }
            Thread.sleep(1000);
        }

        if (!isLogout) {
            Assert.fail("User logged out and Session State has changed. Playground2 app could not logout.");
        }
    }

    private void createUser() throws Exception {

        user = new OIDCUser(username, password);
        user.setProfile(profile);
        user.addUserClaim(emailClaimUri, email);
        user.addUserClaim(firstNameClaimUri, firstName);
        user.addUserClaim(lastNameClaimUri, lastName);
        user.addRole(role);

        createUser(user);
    }

    private void createApplications() throws Exception {

        OIDCApplication playgroundApp = new OIDCApplication(playground2AppName, playground2AppContext,
                                                            playground2AppCallBackUri);
        playgroundApp.addRequiredClaim(emailClaimUri);
        playgroundApp.addRequiredClaim(firstNameClaimUri);
        playgroundApp.addRequiredClaim(lastNameClaimUri);
        applications.put(playground2AppName, playgroundApp);

        playgroundApp = new OIDCApplication(playground3AppName, playground3AppContext, playground3AppCallBackUri);
        playgroundApp.addRequiredClaim(emailClaimUri);
        playgroundApp.addRequiredClaim(firstNameClaimUri);
        playgroundApp.addRequiredClaim(lastNameClaimUri);
        applications.put(playground3AppName, playgroundApp);

        for (Map.Entry<String, OIDCApplication> entry : applications.entrySet()) {
            createApplication(entry.getValue());
        }
    }

    private void deleteApplications() throws Exception {

        for (Map.Entry<String, OIDCApplication> entry : applications.entrySet()) {
            deleteApplication(entry.getValue());
        }
    }

    private void deployApplications() {

        for (Map.Entry<String, OIDCApplication> entry : applications.entrySet()) {
            URL resourceUrl =
                    getClass().getResource(URL_SEPARATOR + "samples" + URL_SEPARATOR + entry.getKey() + ".war");
            tomcat.addWebapp(tomcat.getHost(), entry.getValue().getApplicationContext(), resourceUrl.getPath());
        }
    }

    private Map<String, String> getQueryParameters(String query) {

        Map<String, String> queryParamsMap = new HashMap<>();

        String[] queryParams = query.split("&");
        for (int i = 0; i < queryParams.length; i++) {
            String[] keyValuePair = queryParams[i].split("=", 2);
            queryParamsMap.put(keyValuePair[0], keyValuePair[1]);
        }

        return queryParamsMap;
    }

    private void loadWindows() {

        Set<String> windowsBefore = driver.getWindowHandles();
        playground2AppWindow = driver.getWindowHandle();

        // Open a new window
        ((JavascriptExecutor) driver).executeScript("window.open();");

        Set<String> windowsAfter = driver.getWindowHandles();
        windowsAfter.removeAll(windowsBefore);
        playground3AppWindow = ((String) windowsAfter.toArray()[0]);
    }

    private void sendAuthenticationRequest(WebDriver driver, OIDCApplication application) {

        driver.get(String.format(targetApplicationUrl, application.getApplicationContext() +
                                                       OIDCUITestConstants.PlaygroundAppPaths.appResetPath));

        // Select 'code' response type
        Select grantTypeSelect = new Select(
                driver.findElement(By.id(OIDCUITestConstants.PlaygroundAppElementIdentifiers.grantTypeElement)));
        grantTypeSelect.selectByValue("code");

        // Set client id
        driver.findElement(By.id(OIDCUITestConstants.PlaygroundAppElementIdentifiers.clientIdElement))
              .sendKeys(application.getClientId());

        // Set openid scope
        driver.findElement(By.id(OIDCUITestConstants.PlaygroundAppElementIdentifiers.scopeElement)).sendKeys("openid");

        // Set callback url
        driver.findElement(By.id(OIDCUITestConstants.PlaygroundAppElementIdentifiers.callBackURLElement))
              .sendKeys(application.getCallBackURL());

        // Set OP authorization endpoint
        driver.findElement(By.id(OIDCUITestConstants.PlaygroundAppElementIdentifiers.authorizeEndpointElement))
              .sendKeys(OIDCUITestConstants.OPEndpoints.authorizeEndpoint);

        /**
         * In debug mode where the browser is not an active window 'onchange' events are not fired.
         * Thus, below two elements remains invisible.
         */

        // Set OP logout endpoint
        driver.findElement(By.id(OIDCUITestConstants.PlaygroundAppElementIdentifiers.logoutEndpointElement))
              .sendKeys(OIDCUITestConstants.OPEndpoints.logoutEndpoint);

        // Set OP check session iframe endpoint
        driver.findElement(By.id(OIDCUITestConstants.PlaygroundAppElementIdentifiers.sessionIFrameEndpointElement))
              .sendKeys(String.format(OIDCUITestConstants.OPEndpoints.checkSessionIframeEndpoint,
                                      application.getClientId()));
        // Perform form submit
        driver.findElement(By.name(OIDCUITestConstants.PlaygroundAppElementIdentifiers.authorizeButtonElement)).click();
    }

    private void performLoginConsentApproval(WebDriver driver) throws MalformedURLException {
        // Perform consent approval
        driver.findElement(By.id(OIDCUITestConstants.AuthEndpointElementIdentifiers.loginApproveButtonElement)).click();
        validateAuthenticationResponse(driver);
    }

    private void validateAuthenticationResponse(WebDriver driver) throws MalformedURLException {

        String query = new URL(driver.getCurrentUrl()).getQuery();

        if (StringUtils.isNotBlank(query)) {
            Map<String, String> queryParameters = getQueryParameters(query);
            Assert.assertNotNull(queryParameters.get("code"), "Authorization Code not received.");
            Assert.assertNotNull(queryParameters.get("session_state"), "Session State not reeived");
        } else {
            Assert.fail("Authorization Code and Session State not received");
        }
    }

    private void sendTokenRequest(WebDriver driver, OIDCApplication application) {
        // Set callback url
        driver.findElement(By.id(OIDCUITestConstants.PlaygroundAppElementIdentifiers.callBackURLElement))
              .sendKeys(application.getCallBackURL());

        // Set OP token endpoint
        driver.findElement(By.id(OIDCUITestConstants.PlaygroundAppElementIdentifiers.accessTokenEndpointElement))
              .sendKeys(OIDCUITestConstants.OPEndpoints.tokenEndpoint);

        // Set client secret
        driver.findElement(By.id(OIDCUITestConstants.PlaygroundAppElementIdentifiers.clientSecretElement))
              .sendKeys(application.getClientSecret());

        // Perform form submit
        driver.findElement(By.name(OIDCUITestConstants.PlaygroundAppElementIdentifiers.authorizeButtonElement)).click();
    }
}
