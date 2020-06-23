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
import org.wso2.identity.integration.test.utils.DataExtractUtil;

import java.io.IOException;

public class Oauth2JWKSEndpointTestCase {

    private static final String JKWS_URI_SUPER_TENANT = "https://localhost:9853/oauth2/jwks";
    private static final String JKWS_URI_TENANT = "https://localhost:9853/t/wso2.com/oauth2/jwks";

    @Test(groups = "wso2.is", description = "This test JWKS endpoints.")
    public void testJWKSEndpoint() throws IOException, JSONException {

        getPublicKeySet(JKWS_URI_SUPER_TENANT);
        getPublicKeySet(JKWS_URI_TENANT);
    }

    private void getPublicKeySet(String jwksEndpoint) throws IOException, JSONException {

        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse httpResponse = sendGetRequest(client, jwksEndpoint);
        String content = DataExtractUtil.getContentData(httpResponse);
        Assert.assertNotNull(content, "Response content is not received");
        JSONObject publicKeySet = new JSONObject(content);
        Assert.assertNotNull(publicKeySet.getJSONArray("keys").getJSONObject(0).getString("kty"),
                "The public key type can not be null");
        Assert.assertNotNull(publicKeySet.getJSONArray("keys").getJSONObject(0).getString("e"),
                "The exponent value of the public key can not be null");
        Assert.assertNotNull(publicKeySet.getJSONArray("keys").getJSONObject(0).getString("use"),
                "Usage of the key can not be null");
        Assert.assertNotNull(publicKeySet.getJSONArray("keys").getJSONObject(0).getString("kid"),
                "The thumbprint of the certificate can not be null");
        Assert.assertNotNull(publicKeySet.getJSONArray("keys").getJSONObject(0).getString("alg"),
                "The algorithm can not be null");
        Assert.assertNotNull(publicKeySet.getJSONArray("keys").getJSONObject(0).getString("n"),
                "The modulus value of the public key can not be null");
    }

    private HttpResponse sendGetRequest(HttpClient client, String jwksEndpoint) throws IOException {

        HttpGet getRequest = new HttpGet(jwksEndpoint);
        HttpResponse response = client.execute(getRequest);
        return response;
    }
}
