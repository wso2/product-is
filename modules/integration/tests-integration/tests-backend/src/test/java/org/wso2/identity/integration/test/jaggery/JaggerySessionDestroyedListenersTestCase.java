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
package org.wso2.identity.integration.test.jaggery;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.servers.utils.FileManipulator;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.File;

public class JaggerySessionDestroyedListenersTestCase extends ISIntegrationTest {
    private static final String APP_URL = "https://localhost:9853/read-only-test-app?change";
    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    private static final int HTTP_OK_STATUS_CODE = 200;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();

        File src = new File(getTestArtifactLocation() + File.separator + "artifacts" + File.separator + "IS"
                + File.separator + "jaggery" + File.separator + "read-only-test-app");
        File dest = new File(System.getProperty(ServerConstants.CARBON_HOME) + File.separator + "repository"
                + File.separator + "deployment" + File.separator + "server" + File.separator +
                "jaggeryapps" + File.separator + "read-only-test-app");
        FileManipulator.copyDir(src, dest);
        super.init();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

    }

    @Test(groups = "wso2.is", description = "Check whether ReadOnlyTestApp returns 200 after session.put")
    public void testSessionNonReadOnlyBehaviour() throws Exception {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet request = new HttpGet(APP_URL);
        request.addHeader("User-Agent", USER_AGENT);

        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");

        int counter = 0;
        while (responseString != null && !responseString.contains("ReadOnlyTestApp updated session successfully")) {
            counter++;
            response = httpClient.execute(request);
            entity = response.getEntity();
            responseString = EntityUtils.toString(entity, "UTF-8");
            Thread.sleep(1000);

            if (counter > 30) {
                Assert.fail("ReadOnlyTestApp did't respond properly.");
                return;
            }
        }
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HTTP_OK_STATUS_CODE);
    }
}
