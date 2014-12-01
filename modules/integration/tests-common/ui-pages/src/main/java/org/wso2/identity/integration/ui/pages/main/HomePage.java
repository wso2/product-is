package org.wso2.identity.integration.ui.pages.main;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.wso2.identity.integration.ui.pages.UIElementMapper;
import org.wso2.identity.integration.ui.pages.login.LoginPage;

import java.io.IOException;

/**
 * Home page class holds the information of product page you got once login
 * NOTE: To navigate to a page Don't use direct links to pages. To ensure there is a UI element to navigate to
 * that page.
 */
public class HomePage {

    private static final Log log = LogFactory.getLog(HomePage.class);
    private WebDriver driver;

    public HomePage(WebDriver driver) throws IOException {
        this.driver = driver;
        // Check that we're on the right page.
        if (!driver.findElement(By.id(UIElementMapper.getInstance()
                .getElement("home.dashboard.middle.text"))).getText().contains("Home")) {
            throw new IllegalStateException("This is not the home page");
        }
    }

    public LoginPage logout() throws IOException {
        driver.findElement(By.xpath(UIElementMapper.getInstance().getElement("home.sign.out.xpath"))).click();
        return new LoginPage(driver);
    }
}
