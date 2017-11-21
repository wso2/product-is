package org.wso2.identity.ui.integration.test.user.mgt;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.identity.integration.common.ui.page.LoginPage;
import org.wso2.identity.integration.common.ui.page.main.HomePage;
import org.wso2.identity.integration.common.utils.ISIntegrationUITest;

public class RoleCreationTestCase extends ISIntegrationUITest {

    private WebDriver driver;


    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL());
        //ToDO migrate to new test environment
//        EnvironmentBuilder builder = new EnvironmentBuilder().is(5);
//        EnvironmentVariables environment =builder.build().getIs();
    }

    @Test(groups = "wso2.is", description = "verify adding a role is successful")
    public void testLogin() throws Exception {

        LoginPage test = new LoginPage(driver);
        test.loginAs(userInfo.getUserName(), userInfo.getPassword());
        HomePage home = test.loginAs(isServer.getContextTenant().getContextUser().getUserName()
                , isServer.getContextTenant().getContextUser().getPassword());
        //ToDO migrate to new test environment
//        RoleHomePage roleHomePage = new RoleHomePage(driver);
//        String roleName = "SeleniumRole";
//        roleHomePage.addRole(roleName);
//        roleHomePage.checkOnUploadRole(roleName);
        driver.close();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }

}