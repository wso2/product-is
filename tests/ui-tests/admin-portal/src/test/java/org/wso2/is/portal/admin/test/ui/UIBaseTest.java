/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.wso2.is.portal.admin.test.ui;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to select the web driver.
 */
public class UIBaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(UIBaseTest.class);
    private static WebDriver driver;

    public static WebDriver selectDriver(String driverType) {


        try {
            if (driverType.equalsIgnoreCase("chrome")) {
                System.setProperty("webdriver.chrome.driver", System.getProperty("pathToChromeDriver"));
                DesiredCapabilities capability = DesiredCapabilities.chrome();
                capability.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
                driver = new ChromeDriver(capability);
                driver.manage().window().maximize();
            } else {
                driver = new HtmlUnitDriver();
            }
        } catch (WebDriverException wb) {
            LOGGER.info("The relavent driver is not found. Please read the README.txt in the uer-portal tests");
        } catch (Exception e) {
            LOGGER.info(e.toString());
        }
        return driver;
    }
}
