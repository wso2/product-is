/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.identity.integration.test.utils;

import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ClientCredentialsGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;

import org.json.JSONException;
import org.json.JSONObject;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import org.wso2.identity.integration.test.rest.api.common.RESTTestBase;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationListItem;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Optional;

/**
 * Utility class for creating OIDC applications and obtaining M2M access tokens via OAuth2.
 */
public final class OAuth2Util {

    public static String createOIDCApplication(OAuth2RestClient oAuth2RestClient, String apiAuthorizations,
                                               String authenticatingUserName, String authenticatingCredential)
            throws IOException, JSONException {

        String endpointURL = "applications";
        String body = readResource("create-oidc-app-request-body.json");

        Response response = given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .contentType(ContentType.JSON)
                .body(body).when().post(endpointURL);
        response.then()
                .log().ifValidationFails().assertThat().statusCode(HttpStatus.SC_CREATED);

        Optional<ApplicationListItem> application = oAuth2RestClient.getAllApplications().getApplications().stream()
                                                .filter(app -> app.getName().equals("oidc-app")).findAny();
        Assert.assertTrue(application.isPresent(), "OIDC Application is not created");
        String applicationId = application.get().getId();

        JSONObject jsonObject = new JSONObject(apiAuthorizations);

        for (Iterator<String> apiNameIterator = jsonObject.keys(); apiNameIterator.hasNext(); ) {
            String apiName = apiNameIterator.next();
            Object requiredScopes = jsonObject.get(apiName);
            Response apiResource = given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                                .when().queryParam("filter", "identifier eq " + apiName)
                                .get("api-resources");
            apiResource.then().log().ifValidationFails().assertThat().statusCode(HttpStatus.SC_OK);
            String apiUUID = apiResource.getBody().jsonPath().getString("apiResources[0].id");

            JSONObject authorizedAPIRequestBody = new JSONObject();
            authorizedAPIRequestBody.put("id", apiUUID);
            authorizedAPIRequestBody.put("policyIdentifier", "RBAC");
            authorizedAPIRequestBody.put("scopes", requiredScopes);

            Response authorizedAPIResponse = given().auth().preemptive()
                                        .basic(authenticatingUserName, authenticatingCredential)
                                        .contentType(ContentType.JSON).body(authorizedAPIRequestBody.toString()).when()
                                        .post("applications/" + applicationId + "/authorized-apis");
            authorizedAPIResponse.then().log().ifValidationFails().assertThat().statusCode(HttpStatus.SC_OK);
        }
        return applicationId;
    }

    public static String getM2MAccessToken(OAuth2RestClient oAuth2RestClient, String applicationId, URI tokenEndpoint)
            throws Exception {

        OpenIDConnectConfiguration openIDConnectConfiguration = oAuth2RestClient.getOIDCInboundDetails(applicationId);
        TokenRequest request = getTokenRequest(tokenEndpoint, openIDConnectConfiguration);
        HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();
        Assert.assertNotNull(tokenHTTPResp, "Access token http response is null.");

        TokenResponse tokenResponse = TokenResponse.parse(tokenHTTPResp);
        AccessTokenResponse accessTokenResponse = tokenResponse.toSuccessResponse();
        String m2mToken = accessTokenResponse.getTokens().getAccessToken().getValue();
        Assert.assertNotNull(m2mToken, "The retrieved M2M Token is null in the token response.");

        Scope scopesInResponse = accessTokenResponse.getTokens().getAccessToken().getScope();
        Assert.assertTrue(scopesInResponse.contains("internal_organization_create"),
                "Requested scope is missing in the token response");
        return m2mToken;
    }

    private static TokenRequest getTokenRequest(URI tokenEndpoint,
                                                OpenIDConnectConfiguration openIDConnectConfiguration) {

        String selfServiceAppClientId = openIDConnectConfiguration.getClientId();
        String selfServiceAppClientSecret = openIDConnectConfiguration.getClientSecret();
        AuthorizationGrant clientCredentialsGrant = new ClientCredentialsGrant();
        ClientID clientID = new ClientID(selfServiceAppClientId);
        Secret clientSecret = new Secret(selfServiceAppClientSecret);
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);
        Scope scope = new Scope("SYSTEM");
        return new TokenRequest(tokenEndpoint, clientAuth, clientCredentialsGrant, scope);
    }

    public static void deleteApplication(OAuth2RestClient oAuth2RestClient, String applicationId) throws Exception {
        
        oAuth2RestClient.deleteApplication(applicationId);
    }

    private static String readResource(String filename) throws IOException {

        return RESTTestBase.readResource(filename, org.wso2.identity.integration.test.utils.OAuth2Util.class);
    }
}
