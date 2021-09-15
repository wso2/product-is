/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.ID_ATTRIBUTE;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SCIM2_USERS_ENDPOINT;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.SERVER_URL;

public class UserUtil {

    public static String getUserId(String username, String tenantDomain, String tenantAdminUsername,
                                   String tenantAdminPassword) throws IOException {

        String userResourcePath = getPath(tenantDomain) + "?filter=username+EQ+" + username;
        HttpGet request = new HttpGet(userResourcePath);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader(tenantAdminUsername, tenantAdminPassword));
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        request.addHeader(HttpHeaders.ACCEPT, "application/scim+json");

        try (CloseableHttpClient client = HttpClients.createDefault();) {
            HttpResponse response = client.execute(request);
            assertEquals(response.getStatusLine().getStatusCode(), 200, "User " +
                    "has not been retrieved successfully");

            String responseString = EntityUtils.toString(response.getEntity());
            Object responseObj = JSONValue.parse(responseString);
            EntityUtils.consume(response.getEntity());

            return ((JSONObject) ((JSONArray) ((JSONObject) responseObj).get("Resources")).get(0)).get
                    (ID_ATTRIBUTE).toString();
        }
    }

    public static String getUserId(String username, Tenant contextTenant) throws IOException {

        return getUserId(username, contextTenant.getDomain(), contextTenant.getTenantAdmin().getUserName(),
                contextTenant.getTenantAdmin().getPassword());
    }

    private static String getPath(String tenantDomain) {

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return SERVER_URL + SCIM2_USERS_ENDPOINT;
        } else {
            return SERVER_URL + "/t/" + tenantDomain + SCIM2_USERS_ENDPOINT;
        }
    }

    private static String getAuthzHeader(String adminUsername, String adminPassword) {

        return "Basic " + Base64.encodeBase64String((adminUsername + ":" + adminPassword).getBytes()).trim();
    }
}
