/*
*  Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.identity.integration.test.dashboard;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

public class IDENTITY5811TestCase extends ISIntegrationTest {
    private static final String IMAGE_URL_1 = "https://localhost:9853/dashboard/img/designer-sprite.png";
    private static final String IMAGE_URL_2 = "https://localhost:9853/dashboard/img/dummy-grid.png";
    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    private static final int HTTP_OK_STATUS_CODE = 200;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

    }

    @Test(groups = "wso2.is", description = "Check wether image designer-sprite.png exist")
    public void testImageOneExist() throws Exception {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet request = new HttpGet(IMAGE_URL_1);
        request.addHeader("User-Agent", USER_AGENT);
        HttpResponse response = httpClient.execute(request);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HTTP_OK_STATUS_CODE);
    }

    @Test(groups = "wso2.is", description = "Check wether image dummy-grid.png exist")
    public void testImageTwoExist() throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(IMAGE_URL_2);
        request.addHeader("User-Agent", USER_AGENT);
        HttpResponse response = client.execute(request);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HTTP_OK_STATUS_CODE);

    }
}
