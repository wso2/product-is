package org.wso2.identity.integration.ui.pages;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.wso2.carbon.automation.engine.configurations.UrlGenerationUtil;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.identity.integration.ui.pages.login.LoginPage;

public class ISIntegrationUiBaseTest {
    private static final Log log = LogFactory.getLog(ISIntegrationUiBaseTest.class);
    protected AutomationContext isServer;
    protected String sessionCookie;
    protected String backendURL;
    protected LoginLogoutClient loginLogoutClient;
    protected WebDriver driver;
    private static final String PRODUCT_GROUP_NAME = "IS";

    protected void init() throws Exception {
        isServer = new AutomationContext(PRODUCT_GROUP_NAME, TestUserMode.SUPER_TENANT_ADMIN);
        loginLogoutClient = new LoginLogoutClient(isServer);
        sessionCookie = loginLogoutClient.login();
        backendURL = isServer.getContextUrls().getBackEndUrl();
        this.driver = BrowserManager.getWebDriver();
    }

    protected void init(TestUserMode testUserMode) throws Exception {
        isServer = new AutomationContext(PRODUCT_GROUP_NAME, testUserMode);
        loginLogoutClient = new LoginLogoutClient(isServer);
        sessionCookie = loginLogoutClient.login();
        backendURL = isServer.getContextUrls().getBackEndUrl();
        this.driver = BrowserManager.getWebDriver();
    }

    protected String getLoginURL() throws Exception{
        return UrlGenerationUtil.getLoginURL(isServer.getInstance());
    }

    protected LoginPage logout() throws Exception {
        driver.findElement(By.xpath(UIElementMapper.getInstance().getElement("home.sign.out.xpath"))).click();
        return new LoginPage(driver);
    }
}
