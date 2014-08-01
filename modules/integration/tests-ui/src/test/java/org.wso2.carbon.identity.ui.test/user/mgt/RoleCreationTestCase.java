package org.wso2.carbon.identity.ui.test.user.mgt;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.selenium.configuretab.RoleHomePage;
import org.wso2.carbon.automation.api.selenium.login.LoginPage;
import org.wso2.carbon.automation.core.BrowserManager;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.carbon.identity.ui.test.ISIntegrationUITest;

public class RoleCreationTestCase extends ISIntegrationUITest {

    private WebDriver driver;


    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL(ProductConstant.IS_SERVER_NAME));
        EnvironmentBuilder builder = new EnvironmentBuilder().is(5);
        EnvironmentVariables environment =builder.build().getIs();
        System.out.printf(environment.getBackEndUrl());
    }

    @Test(groups = "wso2.is", description = "verify adding a role is successful")
    public void testLogin() throws Exception {

        LoginPage test = new LoginPage(driver);
        test.loginAs(userInfo.getUserName(), userInfo.getPassword());
        RoleHomePage roleHomePage = new RoleHomePage(driver);
        String roleName = "SeleniumRole";
        roleHomePage.addRole(roleName);
        roleHomePage.checkOnUploadRole(roleName);
        driver.close();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }

}