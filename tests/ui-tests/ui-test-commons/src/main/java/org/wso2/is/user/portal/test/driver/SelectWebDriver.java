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

package org.wso2.is.user.portal.test.driver;

import com.opera.core.systems.OperaDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

/**
 * Select the driver to run tests.
 */
public class SelectWebDriver {

    public static WebDriver selectDriver(String driverType) {

        WebDriver driver = null;

        if (driverType.equalsIgnoreCase("headless")) {
            driver = new HtmlUnitDriver();
        } else if (driverType.equalsIgnoreCase("firefox")) {
            driver = new FirefoxDriver();
        } else if (driverType.equalsIgnoreCase("chrome")) {
            driver = new ChromeDriver();
        } else if (driverType.equalsIgnoreCase("opera")) {
            driver = new OperaDriver();
        } else {
            driver = new InternetExplorerDriver();
        }
        return driver;
    }
}
