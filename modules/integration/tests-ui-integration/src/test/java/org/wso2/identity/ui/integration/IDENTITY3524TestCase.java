/*
*Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.ui.integration.test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.BrowserManager;
import org.wso2.carbon.automation.core.utils.serverutils.ServerConfigurationManager;
import org.wso2.carbon.identity.ui.test.ISIntegrationUITest;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class IDENTITY3524TestCase extends ISIntegrationUITest {

    private WebDriver driver;
    private ServerConfigurationManager scm;
    private File carbonXML;
    private File catalinaXML;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {

        super.init();
        changeISConfiguration();
    }

    @Test(groups = "wso2.is", description = "Check adding policies")
    public void testIDENTITY3524() throws Exception {

        String baseUrl =
                "https://" + isServer.getProductVariables().getHostName() + ":" + isServer.getProductVariables()
                        .getHttpsPort();
        driver = BrowserManager.getWebDriver();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

        driver.get(baseUrl + "/carbon/admin/login.jsp");
        driver.findElement(By.id("txtUserName")).clear();
        driver.findElement(By.id("txtUserName")).sendKeys("admin");
        driver.findElement(By.id("txtPassword")).clear();
        driver.findElement(By.id("txtPassword")).sendKeys("admin");
        driver.findElement(By.cssSelector("input.button")).click();
        driver.findElement(By.cssSelector("#menu-panel-button4 > span")).click();
        driver.findElement(By.linkText("TryIt")).click();
        driver.findElement(By.id("resourceNames")).clear();
        driver.findElement(By.id("resourceNames")).sendKeys("http://localhost:8280/services/echo/");
        driver.findElement(By.id("subjectNames")).clear();
        driver.findElement(By.id("subjectNames")).sendKeys("admin");
        driver.findElement(By.id("actionNames")).clear();
        driver.findElement(By.id("actionNames")).sendKeys("read");
        driver.findElement(By.xpath("//input[@value='Create Request']")).click();
        driver.findElement(By.cssSelector("input.button")).click();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        Assert.assertTrue(!driver.findElement(By.id("txtRequestTemp")).getAttribute("value")
                        .contains("DOM of request element can not be created from String"),
                "Entitlement UI get decision failed.");
    }

    private void changeISConfiguration() throws Exception {

        String carbonHome = CarbonUtils.getCarbonHome();
        carbonXML = new File(
                carbonHome + File.separator + "repository" + File.separator + "conf" + File.separator + "carbon.xml");

        File configuredCarbonXML = new File(
                getISResourceLocation() + File.separator + "IDENTITY3524" + File.separator + File.separator
                        + "carbon-security.xml");

        catalinaXML = new File(
                carbonHome + File.separator + "repository" + File.separator + "conf" + File.separator + "tomcat"
                        + File.separator + "catalina-server.xml");

        File configuredCatalinaXML = new File(
                getISResourceLocation() + File.separator + "IDENTITY3524" + File.separator + File.separator
                        + "catalina-server-security.xml");

        scm = new ServerConfigurationManager(isServer.getBackEndUrl());
        scm.applyConfigurationWithoutRestart(configuredCarbonXML, carbonXML, true);
        scm.applyConfigurationWithoutRestart(configuredCatalinaXML, catalinaXML, true);
        scm.restartGracefully();
        super.init(0);
    }

    private void resetISConfiguration() throws Exception {

        String carbonHome = CarbonUtils.getCarbonHome();
        carbonXML = new File(
                carbonHome + File.separator + "repository" + File.separator + "conf" + File.separator + "carbon.xml");
        File configuredCarbonXML = new File(
                getISResourceLocation() + File.separator + "IDENTITY3524" + File.separator + File.separator
                        + "carbon-default.xml");

        catalinaXML = new File(
                carbonHome + File.separator + "repository" + File.separator + "conf" + File.separator + "tomcat"
                        + File.separator + "catalina-server.xml");
        File configuredCatalinaXML = new File(
                getISResourceLocation() + File.separator + "IDENTITY3524" + File.separator + File.separator
                        + "catalina-server-default.xml");

        scm = new ServerConfigurationManager(isServer.getBackEndUrl());
        scm.applyConfigurationWithoutRestart(configuredCarbonXML, carbonXML, true);
        scm.applyConfigurationWithoutRestart(configuredCatalinaXML, catalinaXML, true);
        scm.restartGracefully();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
        resetISConfiguration();
    }
}
