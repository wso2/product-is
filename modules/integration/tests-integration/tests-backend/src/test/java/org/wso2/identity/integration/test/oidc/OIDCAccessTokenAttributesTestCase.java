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

package org.wso2.identity.integration.test.oidc;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.test.oidc.bean.OIDCApplication;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AccessTokenConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;

public class OIDCAccessTokenAttributesTestCase extends OIDCAbstractIntegrationTest {

    private static final String OAUTH2_TOKEN_ENDPOINT_URI = "/oauth2/token";
    private static final String SERVICES = "/services";
    private OIDCApplication application;
    private OpenIDConnectConfiguration oidcInboundConfig;
    protected String refreshToken;
    protected String sessionDataKey;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();

        RestAssured.baseURI = backendURL.replace(SERVICES, "");

        // Create a user
        OIDCUtilTest.initUser();
        createUser(OIDCUtilTest.user);

        // Create application
        OIDCUtilTest.initApplications();
        application = OIDCUtilTest.applications.get(OIDCUtilTest.playgroundAppTwoAppName);
        createApplication(application);
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {

        deleteUser(OIDCUtilTest.user);
        deleteApplication(application);
        clear();
    }

    @Test(groups = "wso2.is", description = "Validate access token attributes with password grant")
    public void testValidateAccessTokenAttributesWithPasswordGrant() throws Exception {

        Map<String, String> params = new HashMap<>();
        params.put("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER);
        params.put("scope", "");
        params.put("username", OIDCUtilTest.user.getUserName());
        params.put("password", OIDCUtilTest.user.getPassword());

        Response response = getResponseOfFormPostWithAuth(OAUTH2_TOKEN_ENDPOINT_URI, params, new HashMap<>(),
                application.getClientId(), application.getClientSecret());

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("access_token", notNullValue())
                .body("refresh_token", notNullValue());

        String accessToken = response.then().extract().path("access_token");
        refreshToken = response.then().extract().path("refresh_token");
        Assert.assertNotNull(accessToken, "Access token is null");
        JWTClaimsSet jwtClaimsSet = SignedJWT.parse(accessToken).getJWTClaimsSet();
        Assert.assertNotNull(jwtClaimsSet.getClaim("username"), "Username is null.");

    }

    @Test(groups = "wso2.is", description = "Validate access token attributes with refresh grant",
            dependsOnMethods = "testValidateAccessTokenAttributesWithPasswordGrant")
    public void testValidateAccessTokenAttributesWithRefreshGrant() throws Exception {

        Map<String, String> params = new HashMap<>();
        params.put("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN);
        params.put(OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN, refreshToken);

        Response response = getResponseOfFormPostWithAuth(OAUTH2_TOKEN_ENDPOINT_URI, params, new HashMap<>(),
                application.getClientId(), application.getClientSecret());

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("access_token", notNullValue())
                .body("refresh_token", notNullValue());

        String accessToken = response.then().extract().path("access_token");
        refreshToken = response.then().extract().path("refresh_token");
        Assert.assertNotNull(accessToken, "Access token is null");
        JWTClaimsSet jwtClaimsSet = SignedJWT.parse(accessToken).getJWTClaimsSet();
        Assert.assertNotNull(jwtClaimsSet.getClaim("username"), "Username is null.");
    }

    @Test(groups = "wso2.is", description = "Update access token attributes of the application",
            dependsOnMethods = "testValidateAccessTokenAttributesWithRefreshGrant")
    public void testUpdateAccessTokenAttributes() throws Exception {

        AccessTokenConfiguration accessTokenConfig = new AccessTokenConfiguration().type("JWT");
        accessTokenConfig.setUserAccessTokenExpiryInSeconds(3600L);
        accessTokenConfig.setApplicationAccessTokenExpiryInSeconds(3600L);
        // Add access token attributes
        List<String> accessTokenAttributes = new ArrayList<>();
        accessTokenAttributes.add("username");
        accessTokenConfig.setAccessTokenAttributes(accessTokenAttributes);
        oidcInboundConfig.setAccessToken(accessTokenConfig);
        updateApplicationInboundConfig(application.getApplicationId(), oidcInboundConfig, OIDC);

        OpenIDConnectConfiguration updatedOidcInboundConfig =
                getOIDCInboundDetailsOfApplication(application.getApplicationId());
        Assert.assertEquals(updatedOidcInboundConfig.getAccessToken().getAccessTokenAttributes().size(),1,
                "Access token attribute should not be empty.");
    }

    @Test(groups = "wso2.is", description = "Validate access token attributes for empty allowed attributes",
            dependsOnMethods = "testUpdateAccessTokenAttributes")
    public void testValidateAccessTokenAttributesForEmptyAllowedAttributes() throws Exception {

        Map<String, String> params = new HashMap<>();
        params.put("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER);
        params.put("scope", "");
        params.put("username", OIDCUtilTest.user.getUserName());
        params.put("password", OIDCUtilTest.user.getPassword());

        Response response = getResponseOfFormPostWithAuth(OAUTH2_TOKEN_ENDPOINT_URI, params, new HashMap<>(),
                application.getClientId(), application.getClientSecret());

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("access_token", notNullValue())
                .body("refresh_token", notNullValue());

        String accessToken = response.then().extract().path("access_token");
        refreshToken = response.then().extract().path("refresh_token");
        Assert.assertNotNull(accessToken, "Access token is null");
        JWTClaimsSet jwtClaimsSet = SignedJWT.parse(accessToken).getJWTClaimsSet();
        Assert.assertNotNull(jwtClaimsSet.getClaim("username"), "Username is null.");
    }

    @Test(groups = "wso2.is", description = "Validate access token attributes for empty allowed attributes with " +
            "refresh grant", dependsOnMethods = "testValidateAccessTokenAttributesForEmptyAllowedAttributes")
    public void testValidateAccessTokenAttributesForEmptyAllowedAttributesWithRefreshGrant() throws Exception {

        Map<String, String> params = new HashMap<>();
        params.put("grant_type", OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN);
        params.put(OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN, refreshToken);

        Response response = getResponseOfFormPostWithAuth(OAUTH2_TOKEN_ENDPOINT_URI, params, new HashMap<>(),
                application.getClientId(), application.getClientSecret());

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("access_token", notNullValue())
                .body("refresh_token", notNullValue());

        String accessToken = response.then().extract().path("access_token");
        refreshToken = response.then().extract().path("refresh_token");
        Assert.assertNotNull(accessToken, "Access token is null");
        JWTClaimsSet jwtClaimsSet = SignedJWT.parse(accessToken).getJWTClaimsSet();
        Assert.assertNotNull(jwtClaimsSet.getClaim("username"), "Username is null.");
    }

    /**
     * Invoke given endpointUri for Form POST request with given body, headers and Basic authentication credentials.
     *
     * @param endpointUri endpoint to be invoked.
     * @param params      map of parameters to be added to the request.
     * @param headers     map of headers to be added to the request.
     * @param username    basic auth username.
     * @param password    basic auth password.
     * @return response.
     */
    protected Response getResponseOfFormPostWithAuth(String endpointUri, Map<String, String> params, Map<String, String>
            headers, String username, String password) {

        return given().auth().preemptive().basic(username, password)
                .headers(headers)
                .params(params)
                .when()
                .post(endpointUri);
    }

    /**
     * Create an OIDC application.
     *
     * @param application application instance.
     * @throws Exception If an error creating an application.
     */
    public void createApplication(OIDCApplication application) throws Exception {

        ApplicationModel applicationModel = new ApplicationModel();
        createAccessTokenAttributesEnabledApplication(applicationModel, application);
    }

    private void createAccessTokenAttributesEnabledApplication(ApplicationModel applicationModel,
                                                               OIDCApplication application) throws Exception {

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER,
                OAuth2Constant.OAUTH2_GRANT_TYPE_REFRESH_TOKEN);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.addCallbackURLsItem(application.getCallBackURL());

        AccessTokenConfiguration accessTokenConfig = new AccessTokenConfiguration().type("JWT");
        accessTokenConfig.setUserAccessTokenExpiryInSeconds(3600L);
        accessTokenConfig.setApplicationAccessTokenExpiryInSeconds(3600L);
        // Add access token attributes
        List<String> accessTokenAttributes = new ArrayList<>();
        Collections.addAll(accessTokenAttributes, "username", "email");
        accessTokenConfig.setAccessTokenAttributes(accessTokenAttributes);

        oidcConfig.setAccessToken(accessTokenConfig);

        applicationModel.setName(application.getApplicationName());
        applicationModel.setInboundProtocolConfiguration(new InboundProtocols().oidc(oidcConfig));

        String applicationId = addApplication(applicationModel);
        oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        oidcInboundConfig = oidcConfig;

        application.setApplicationId(applicationId);
        application.setClientId(oidcConfig.getClientId());
        application.setClientSecret(oidcConfig.getClientSecret());
    }
}
