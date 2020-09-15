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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
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
            "https://localhost:9853/t/carbon.super/oauth2/token/.well-known/openid-configuration";
    private static final String OIDCDISCOVERY_ENDPOINT_SUPER_TENANT =
            "https://localhost:9853/oauth2/oidcdiscovery/.well-known/openid-configuration";
    private static final String OIDCDISCOVERY_ENDPOINT_TENANT =
            "https://localhost:9853/t/wso2.com/oauth2/oidcdiscovery/.well-known/openid-configuration";
    private static final String OIDCDISCOVERY_ENDPOINT_WITH_SUPER_TENANT_AS_PATH_PARAM =
            "https://localhost:9853/t/carbon.super/oauth2/oidcdiscovery/.well-known/openid-configuration";
    private static final String INTROSPECTION_ENDPOINT_SUPER_TENANT = "https://localhost:9853/oauth2/introspect";
    private static final String INTROSPECTION_ENDPOINT_TENANT = "https://localhost:9853/t/wso2.com/oauth2/introspect";
    private static final String CHECK_SESSION_IFRAME = "https://localhost:9853/oidc/checksession";
    private static final String ISSUER = "https://localhost:9853/oauth2/token";
    private static final String AUTHORIZATION_ENDPOINT = "https://localhost:9853/oauth2/authorize";
    private static final String TOKEN_ENDPOINT = "https://localhost:9853/oauth2/token";
    private static final String END_SESSION_ENDPOINT = "https://localhost:9853/oidc/logout";
    private static final String REVOCATION_ENDPOINT = "https://localhost:9853/oauth2/revoke";
    private static final String USERINFO_ENDPOINT =	"https://localhost:9853/oauth2/userinfo";
    private static final String JKWS_URI_SUPER_TENANT =	"https://localhost:9853/oauth2/jwks";
    private static final String JKWS_URI_TENANT = "https://localhost:9853/t/wso2.com/oauth2/jwks";
    private static final String REGISTRATION_ENDPOINT_SUPER_TENANT =
            "https://localhost:9853/api/identity/oauth2/dcr/v1.1/register";
    private static final String REGISTRATION_ENDPOINT_TENANT =
            "https://localhost:9853/t/wso2.com/api/identity/oauth2/dcr/v1.1/register";

    @Test(groups = "wso2.is", description = "This test method will test OIDC Metadata endpoints.")
    public void getOIDCMetadata() throws Exception {

        testResponseContent(TOKEN_ENDPOINT_SUPER_TENANT);
        testResponseContent(TOKEN_ENDPOINT_TENANT);
        testResponseContent(TOKEN_ENDPOINT_WITH_SUPER_TENANT_AS_PATH_PARAM);
        testResponseContent(OIDCDISCOVERY_ENDPOINT_SUPER_TENANT);
        testResponseContent(OIDCDISCOVERY_ENDPOINT_TENANT);
        testResponseContent(OIDCDISCOVERY_ENDPOINT_WITH_SUPER_TENANT_AS_PATH_PARAM);
    }

    private void testResponseContent(String oidcMetadataEndpoint) throws IOException, JSONException {

        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse httpResponse = sendGetRequest(client, oidcMetadataEndpoint);
        String content = DataExtractUtil.getContentData(httpResponse);
        Assert.assertNotNull(content, "Response content is not received");

        JSONObject oidcMetadataEndpoints = new JSONObject(content);
        Assert.assertEquals(oidcMetadataEndpoints.getString("check_session_iframe"),
                CHECK_SESSION_IFRAME, "Incorrect session iframe");
        Assert.assertEquals(oidcMetadataEndpoints.getString("issuer"),
                ISSUER, "Incorrect issuer");
        Assert.assertEquals(oidcMetadataEndpoints.getString("authorization_endpoint"),
                AUTHORIZATION_ENDPOINT, "Incorrect authorization endpoint");
        Assert.assertEquals(oidcMetadataEndpoints.getString("token_endpoint"),
                TOKEN_ENDPOINT, "Incorrect token_endpoint");
        Assert.assertEquals(oidcMetadataEndpoints.getString("end_session_endpoint"),
                END_SESSION_ENDPOINT, "Incorrect end session endpoint");
        Assert.assertEquals(oidcMetadataEndpoints.getString("revocation_endpoint"),
                REVOCATION_ENDPOINT, "Incorrect revocation endpoint");
        Assert.assertEquals(oidcMetadataEndpoints.getString("userinfo_endpoint"),
                USERINFO_ENDPOINT, "Incorrect userinfo endpoint");

        if (oidcMetadataEndpoint.equals(TOKEN_ENDPOINT_SUPER_TENANT) ||
                oidcMetadataEndpoint.equals(OIDCDISCOVERY_ENDPOINT_SUPER_TENANT) ||
                oidcMetadataEndpoint.equals(TOKEN_ENDPOINT_WITH_SUPER_TENANT_AS_PATH_PARAM) ||
                oidcMetadataEndpoint.equals(OIDCDISCOVERY_ENDPOINT_WITH_SUPER_TENANT_AS_PATH_PARAM)) {
            Assert.assertEquals(oidcMetadataEndpoints.getString("jwks_uri"),
                    JKWS_URI_SUPER_TENANT, "Incorrect jwks uri");
            Assert.assertEquals(oidcMetadataEndpoints.getString("registration_endpoint"),
                    REGISTRATION_ENDPOINT_SUPER_TENANT, "Incorrect registration endpoint");
            Assert.assertEquals(oidcMetadataEndpoints.getString("introspection_endpoint"),
                    INTROSPECTION_ENDPOINT_SUPER_TENANT, "Incorrect introspection endpoint");
        }

        if (oidcMetadataEndpoint.equals(TOKEN_ENDPOINT_TENANT) ||
                oidcMetadataEndpoint.equals(OIDCDISCOVERY_ENDPOINT_TENANT)) {
            Assert.assertEquals(oidcMetadataEndpoints.getString("jwks_uri"),
                    JKWS_URI_TENANT, "Incorrect jwks uri");
            Assert.assertEquals(oidcMetadataEndpoints.getString("registration_endpoint"),
                    REGISTRATION_ENDPOINT_TENANT, "Incorrect registration endpoint");
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
