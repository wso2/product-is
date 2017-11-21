/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.user.mgt;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserMgtUISecurityTestCase extends ISIntegrationTest {

    private String SERVER_URL;
    private static final String LOGIN_PAGE_CONTEXT = "/carbon/admin/login_action.jsp";
    private static final String CHANGE_PASSWORD_CONTEXT = "/carbon/user/change-passwd-finish.jsp";
    private static final String USER_NAME = "admin";
    private static final String OLD_PASSWORD = "admin";
    private static final String NEW_PASSWORD = "admin";
    private static final String PW_REGEX = "^[\\S]{5,30}$";
    private static final String RETURN_PATH = "../../carbon/admin/index.jsp";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        SERVER_URL = backendURL.split("/services")[0];
    }

    /**
     * Tests the open redirect vulnerability in change-passwd-finish.jsp.
     * @throws IOException
     */
    @Test(alwaysRun = true, description = "Testing open redirect vulnerability")
    public void openRedirectTest() throws IOException {

        HttpClient httpClient = new DefaultHttpClient();

        HttpPost loginPost = new HttpPost(SERVER_URL + LOGIN_PAGE_CONTEXT);

        List<NameValuePair> loginParameters = new ArrayList<>();

        loginParameters.add(new BasicNameValuePair("username", USER_NAME));
        loginParameters.add(new BasicNameValuePair("password", OLD_PASSWORD));

        loginPost.setEntity(new UrlEncodedFormEntity(loginParameters));

        HttpResponse loginResponse = httpClient.execute(loginPost);
        String cookie = loginResponse.getHeaders("Set-Cookie")[0].getValue();

        EntityUtils.consume(loginResponse.getEntity());

        HttpPost post = new HttpPost(SERVER_URL + CHANGE_PASSWORD_CONTEXT);

        post.setHeader("Cookie", cookie);

        List<NameValuePair> urlParameters = new ArrayList<>();

        urlParameters.add(new BasicNameValuePair("pwd_regex", PW_REGEX));
        urlParameters.add(new BasicNameValuePair("username", USER_NAME));
        urlParameters.add(new BasicNameValuePair("isUserChange", "true"));
        urlParameters.add(new BasicNameValuePair("returnPath", RETURN_PATH));
        urlParameters.add(new BasicNameValuePair("currentPassword", NEW_PASSWORD));
        urlParameters.add(new BasicNameValuePair("checkPassword", OLD_PASSWORD));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = httpClient.execute(post);

        String repString = EntityUtils.toString(response.getEntity());

        if (repString != null) {
            Assert.assertFalse(repString.contains(RETURN_PATH), "Possible open redirect vulnerability.");
        } else {
            Assert.assertTrue(false, "Invalid response for password update.");
        }

    }
}
