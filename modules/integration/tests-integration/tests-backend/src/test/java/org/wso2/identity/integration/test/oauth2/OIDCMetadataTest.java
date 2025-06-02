/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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

package org.wso2.identity.integration.test.oauth2;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.utils.DataExtractUtil;

import java.io.IOException;

public class OIDCMetadataTest extends ISIntegrationTest {

    private static final String TOKEN_ENDPOINT_SUPER_TENANT =
            "https://localhost:9853/oauth2/token/.well-known/openid-configuration";
    private static final String TOKEN_ENDPOINT_TENANT =
            "https://localhost:9853/t/wso2.com/oauth2/token/.well-known/openid-configuration";
    private static final String TOKEN_ENDPOINT_WITH_SUPER_TENANT_AS_PATH_PARAM =
            "https://localhost:9853/oauth2/token/.well-known/openid-configuration";
    private static final String OIDCDISCOVERY_ENDPOINT_SUPER_TENANT =
            "https://localhost:9853/oauth2/oidcdiscovery/.well-known/openid-configuration";
    private static final String OIDCDISCOVERY_ENDPOINT_TENANT =
            "https://localhost:9853/t/wso2.com/oauth2/oidcdiscovery/.well-known/openid-configuration";
    private static final String OIDCDISCOVERY_ENDPOINT_WITH_SUPER_TENANT_AS_PATH_PARAM =
            "https://localhost:9853/oauth2/oidcdiscovery/.well-known/openid-configuration";
    private static final String INTROSPECTION_ENDPOINT_SUPER_TENANT = "https://localhost:9853/oauth2/introspect";
    private static final String INTROSPECTION_ENDPOINT_TENANT = "https://localhost:9853/t/wso2.com/oauth2/introspect";
    private static final String CHECK_SESSION_IFRAME = "/oidc/checksession";
    private static final String ISSUER = "/oauth2/token";
    private static final String AUTHORIZATION_ENDPOINT = "/oauth2/authorize";
    private static final String TOKEN_ENDPOINT = "/oauth2/token";
    private static final String END_SESSION_ENDPOINT = "/oidc/logout";
    private static final String REVOCATION_ENDPOINT = "/oauth2/revoke";
    private static final String USERINFO_ENDPOINT =	"/oauth2/userinfo";
    private static final String JKWS_URI_SUPER_TENANT =	"https://localhost:9853/oauth2/jwks";
    private static final String JKWS_URI_TENANT = "https://localhost:9853/t/wso2.com/oauth2/jwks";
    private static final String REGISTRATION_ENDPOINT_SUPER_TENANT =
            "https://localhost:9853/api/identity/oauth2/dcr/v1.1/register";
    private static final String REGISTRATION_ENDPOINT_TENANT =
            "https://localhost:9853/t/wso2.com/api/identity/oauth2/dcr/v1.1/register";
    private static final String BASE_IS_URL = "https://localhost:9853";
    private static final String TENANTED_BASE_IS_URL = "https://localhost:9853/t/wso2.com";

    @Test(groups = "wso2.is", description = "This test method will test OIDC Metadata endpoints.")
    public void getOIDCMetadata() throws Exception {

        testResponseContent(TOKEN_ENDPOINT_SUPER_TENANT, BASE_IS_URL);
        testResponseContent(TOKEN_ENDPOINT_TENANT, TENANTED_BASE_IS_URL);
        testResponseContent(TOKEN_ENDPOINT_WITH_SUPER_TENANT_AS_PATH_PARAM, BASE_IS_URL);
        testResponseContent(OIDCDISCOVERY_ENDPOINT_SUPER_TENANT, BASE_IS_URL);
        testResponseContent(OIDCDISCOVERY_ENDPOINT_TENANT, TENANTED_BASE_IS_URL);
        testResponseContent(OIDCDISCOVERY_ENDPOINT_WITH_SUPER_TENANT_AS_PATH_PARAM, BASE_IS_URL);
    }

    private void testResponseContent(String oidcMetadataEndpoint, String baseUrl) throws IOException, JSONException {

        Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, new RFC6265CookieSpecProvider())
                .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();
        HttpClient client = HttpClientBuilder.create()
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .setDefaultRequestConfig(requestConfig)
                .build();
        HttpResponse httpResponse = sendGetRequest(client, oidcMetadataEndpoint);
        String content = DataExtractUtil.getContentData(httpResponse);
        Assert.assertNotNull(content, "Response content is not received");

        JSONObject oidcMetadataEndpoints = new JSONObject(content);
        Assert.assertEquals(oidcMetadataEndpoints.getString("check_session_iframe"),
                baseUrl + CHECK_SESSION_IFRAME, "Incorrect session iframe");
        Assert.assertEquals(oidcMetadataEndpoints.getString("issuer"),
                baseUrl + ISSUER, "Incorrect issuer");
        Assert.assertEquals(oidcMetadataEndpoints.getString("authorization_endpoint"),
                baseUrl + AUTHORIZATION_ENDPOINT, "Incorrect authorization endpoint");
        Assert.assertEquals(oidcMetadataEndpoints.getString("token_endpoint"),
                baseUrl + TOKEN_ENDPOINT, "Incorrect token_endpoint");
        Assert.assertEquals(oidcMetadataEndpoints.getString("end_session_endpoint"),
                baseUrl + END_SESSION_ENDPOINT, "Incorrect end session endpoint");
        Assert.assertEquals(oidcMetadataEndpoints.getString("revocation_endpoint"),
                baseUrl + REVOCATION_ENDPOINT, "Incorrect revocation endpoint");
        Assert.assertEquals(oidcMetadataEndpoints.getString("userinfo_endpoint"),
                baseUrl + USERINFO_ENDPOINT, "Incorrect userinfo endpoint");

        if (oidcMetadataEndpoint.equals(TOKEN_ENDPOINT_SUPER_TENANT) ||
                oidcMetadataEndpoint.equals(OIDCDISCOVERY_ENDPOINT_SUPER_TENANT) ||
                oidcMetadataEndpoint.equals(TOKEN_ENDPOINT_WITH_SUPER_TENANT_AS_PATH_PARAM) ||
                oidcMetadataEndpoint.equals(OIDCDISCOVERY_ENDPOINT_WITH_SUPER_TENANT_AS_PATH_PARAM)) {
            Assert.assertEquals(oidcMetadataEndpoints.getString("jwks_uri"),
                    JKWS_URI_SUPER_TENANT, "Incorrect jwks uri");
            Assert.assertEquals(oidcMetadataEndpoints.getString("registration_endpoint"),
                    REGISTRATION_ENDPOINT_SUPER_TENANT, "Incorrect flow endpoint");
            Assert.assertEquals(oidcMetadataEndpoints.getString("introspection_endpoint"),
                    INTROSPECTION_ENDPOINT_SUPER_TENANT, "Incorrect introspection endpoint");
        }

        if (oidcMetadataEndpoint.equals(TOKEN_ENDPOINT_TENANT) ||
                oidcMetadataEndpoint.equals(OIDCDISCOVERY_ENDPOINT_TENANT)) {
            Assert.assertEquals(oidcMetadataEndpoints.getString("jwks_uri"),
                    JKWS_URI_TENANT, "Incorrect jwks uri");
            Assert.assertEquals(oidcMetadataEndpoints.getString("registration_endpoint"),
                    REGISTRATION_ENDPOINT_TENANT, "Incorrect flow endpoint");
            Assert.assertEquals(oidcMetadataEndpoints.getString("introspection_endpoint"),
                    INTROSPECTION_ENDPOINT_TENANT, "Incorrect introspection endpoint");
        }
    }

    private HttpResponse sendGetRequest(HttpClient client, String oidcMetadataEndpoint) throws IOException {

        HttpGet getRequest = new HttpGet(oidcMetadataEndpoint);
        HttpResponse response = client.execute(getRequest);
        return response;
    }

}
