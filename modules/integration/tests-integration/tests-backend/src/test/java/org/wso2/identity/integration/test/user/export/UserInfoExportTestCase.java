/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.identity.integration.test.user.export;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.testng.Assert.assertEquals;

/**
 * OAuth2 DCRM API Create process test case
 */
public class UserInfoExportTestCase extends ISIntegrationTest {

    private static final String HOST_PART = "https://localhost:9853";
    private static final String PI_INFO = "pi-info/";
    private static final String ME = "me";
    private static final String RESOURCE_PATH = "/api/identity/user/v1.0/";
    private static final String USERNAME_CLAIM_URI = "http://wso2.org/claims/username";
    private HttpClient client;

    private String username;
    private String tenantAwareUsername;
    private String password;
    private String tenant;

    @Factory(dataProvider = "userInfoExportConfigProvider")
    public UserInfoExportTestCase(TestUserMode userMode) throws Exception {

        AutomationContext context = new AutomationContext("IDENTITY", userMode);
        this.username = context.getContextTenant().getTenantAdmin().getUserName();
        this.tenantAwareUsername = context.getContextTenant().getTenantAdmin().getUserNameWithoutDomain();
        this.password = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @DataProvider(name = "userInfoExportConfigProvider")
    public static Object[][] userInfoExportConfigProvider() {
        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        client = HttpClients.createDefault();

    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Export user details")
    public void testExportUserInfo() throws IOException {

        HttpGet request = new HttpGet(getPiInfoPath());
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 200);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        Object responseObj = JSONValue.parse(rd);
        EntityUtils.consume(response.getEntity());
        Object basicObj = ((JSONObject) responseObj).get("basic");
        if (basicObj == null) {
            Assert.fail();
        } else {
            JSONObject basic = (JSONObject) basicObj;
            String username = basic.get(USERNAME_CLAIM_URI).toString();
            //TODO tenant aware username is coming. is this okay?
            Assert.assertEquals(username, this.tenantAwareUsername);
        }
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Export user details")
    public void testExportUserInfoMe() throws IOException {

        HttpGet request = new HttpGet(getMePath());
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 200);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        Object responseObj = JSONValue.parse(rd);
        EntityUtils.consume(response.getEntity());
        JSONObject basic = (JSONObject)((JSONObject) responseObj).get("basic");
        String username = basic.get(USERNAME_CLAIM_URI).toString();
        //TODO tenant aware username is coming. is this okay?
        Assert.assertEquals(username, this.tenantAwareUsername);
    }

    private String getPiInfoPath() {
        if (tenant.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return HOST_PART + RESOURCE_PATH + PI_INFO + getResourceId();
        } else {
            return HOST_PART + "/t/" + tenant + RESOURCE_PATH + PI_INFO + getResourceId();
        }
    }

    private String getMePath() {

        if (tenant.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return HOST_PART + RESOURCE_PATH + ME;
        } else {
            return HOST_PART + "/t/" + tenant + RESOURCE_PATH + ME;
        }
    }

    private String getAuthzHeader() {
        return "Basic " + Base64.encodeBase64String((username + ":" + password).getBytes()).trim();
    }

    private String getResourceId(){
        return Base64.encodeBase64URLSafeString(username.getBytes()).trim();
    }

}
