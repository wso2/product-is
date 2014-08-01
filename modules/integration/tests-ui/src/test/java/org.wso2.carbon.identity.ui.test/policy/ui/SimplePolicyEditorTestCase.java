package org.wso2.carbon.identity.ui.test.policy.ui;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.selenium.configuretab.RoleHomePage;
import org.wso2.carbon.automation.api.selenium.home.HomePage;
import org.wso2.carbon.automation.api.selenium.login.LoginPage;
import org.wso2.carbon.automation.api.selenium.util.UIElementMapper;
import org.wso2.carbon.automation.core.BrowserManager;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.carbon.identity.ui.test.ISIntegrationUITest;

/**
 * 
 */

/**
 * @author wso2
 * 
 */
public class SimplePolicyEditorTestCase extends ISIntegrationUITest {

	private WebDriver driver;
	private UIElementMapper uiElementMapper;

	@BeforeClass(alwaysRun = true)
	public void setUp() throws Exception {
		super.init();
		System.out.println(" ************* setup *************** ");
		driver = BrowserManager.getWebDriver();
		
		driver.get(getLoginURL(ProductConstant.IS_SERVER_NAME));
        EnvironmentBuilder builder = new EnvironmentBuilder().is(5);
        EnvironmentVariables environment =builder.build().getIs();
        System.out.printf(environment.getBackEndUrl());
		
	}

	@Test(groups = "wso2.is", description = "verify last deny rule in simple policy editor")
	public void testPolicyCreate() throws Exception {
		
		LoginPage test = new LoginPage(driver);
        test.loginAs("admin", "admin");
        
		System.out.println(" *********** Running test policy create ********** ");
		this.uiElementMapper = UIElementMapper.getInstance();
				
		driver.findElement(By.linkText("Policy Administration")).click();
		driver.findElement(By.linkText("Add New Entitlement Policy")).click();
		driver.findElement(By.linkText("Simple Policy Editor")).click();

		driver.findElement(By.id("policyId")).sendKeys("policy1");
		driver.findElement(By.id("policyDescription")).sendKeys("Test Description");
		
		WebElement dropDownListBox = driver.findElement(By.id("policyApplied"));
		Select clickThis = new Select(dropDownListBox);
		clickThis.selectByVisibleText("Subject");
		
		WebElement dropDownListBox2 = driver.findElement(By.id("userAttributeId"));
		Select clickThis2 = new Select(dropDownListBox2);
		clickThis2.selectByVisibleText("Role");

		driver.findElement(By.id("userAttributeValue")).sendKeys("testRole");
		driver.findElement(By.id("actionRuleValue_0")).sendKeys("read");
		driver.findElement(By.id("resourceRuleValue_0")).sendKeys("Res1");
		
		driver.findElement(By.xpath("//*[@id=\"mainTable\"]/tbody/tr[6]/td/input[1]")).click();
		
		// click ok button of message box
		driver.findElement(By.xpath("/html/body/div[3]/div[2]/button")).click();
		
		// View policies... 
		driver.findElement(By.linkText("Policy Administration")).click();
		driver.findElement(By.linkText("policy1")).click();
		Thread.sleep(3000);
		
		// Check for Deny-Rule
		WebElement frame = driver.findElement(By.xpath("//*[@id=\"frame_raw-policy\"]"));
		driver.switchTo().frame(frame);
		String contentText = driver.findElement(By.id("content_highlight")).getText();
		Assert.assertNotNull(contentText);
		Assert.assertTrue(contentText.contains("<Rule Effect=\"Deny\" RuleId=\"Deny-Rule\"/>"));
		driver.close();

	}

	@AfterClass(alwaysRun = true)
	public void tearDown() throws Exception {
		driver.quit();
	}
}
