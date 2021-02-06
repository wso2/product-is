/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.integration.test.oidc;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.json.simple.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.dbutils.H2DataBaseManager;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.PropertyDTO;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.clients.user.store.config.UserStoreConfigAdminServiceClient;
import org.wso2.identity.integration.common.utils.UserStoreConfigUtils;
import org.wso2.identity.integration.test.oidc.bean.OIDCApplication;
import org.wso2.identity.integration.test.oidc.bean.OIDCUser;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;

public class OIDCPasswordGrantTest extends OIDCAbstractIntegrationTest {

    private static final String USER_STORE_DOMAIN = "WSO2TESTSTORE";
    private static final String OAUTH2_TOKEN_ENDPOINT_URI = "/oauth2/token";
    private static final String USER_INFO_ENDPOINT = "/oauth2/userinfo";
    private static final String USER_STORE_DB_NAME = "JDBC_USER_STORE_DB";
    private static final String DB_USER_NAME = "wso2automation";
    private static final String DB_USER_PASSWORD = "wso2automation";
    private UserStoreConfigAdminServiceClient userStoreConfigAdminServiceClient;
    private UserStoreConfigUtils userStoreConfigUtils = new UserStoreConfigUtils();


    protected OIDCUser user;
    private OIDCApplication application;

    protected String accessToken;
    protected String sessionDataKey;

    private static final String SERVICES = "/services";


    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        userStoreConfigAdminServiceClient = new UserStoreConfigAdminServiceClient(backendURL, sessionCookie);
        addSecondaryUserStore();
        // Wait till the user-store is deployed
        Thread.sleep(5000);

        RestAssured.baseURI = backendURL.replace(SERVICES, "");

        // Create a user in secondary user-store
        OIDCUtilTest.initUser();
        OIDCUtilTest.user.setUsername(USER_STORE_DOMAIN + "/" + OIDCUtilTest.user.getUsername());
        user = OIDCUtilTest.user;
        createUser(OIDCUtilTest.user);

        // Create application
        OIDCUtilTest.initApplications();
        application = OIDCUtilTest.applications.get(OIDCUtilTest.playgroundAppTwoAppName);
        createApplication(application);

    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {

        deleteUser(user);
        deleteApplication(application);
        clear();

    }

    @Test(groups = "wso2.is", description = "Get access token for playground.appone")
    public void testGetAccessTokenForPasswordGrant() throws Exception {

        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "password");
        params.put("scope", "openid");
        params.put("username", user.getUsername());
        params.put("password", user.getPassword());

        Response response = getResponseOfFormPostWithAuth(OAUTH2_TOKEN_ENDPOINT_URI, params, new HashMap<>(),
                application.getClientId(), application.getClientSecret());

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("access_token", notNullValue());

        accessToken = response.then().extract().path("access_token");
    }

    /**
     * Test /userinfo endpoint for GET method.
     */
    @Test(groups = "wso2.is", description = "Retrieve user claims from user-info endpoint",
            dependsOnMethods = "testGetAccessTokenForPasswordGrant")
    public void testHttpGetUserInfoEndpoint() {

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + accessToken);

        Response response = getResponseOfGet(USER_INFO_ENDPOINT, headers);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("email", is(user.getUserClaims().get(OIDCUtilTest.emailClaimUri)))
                .body("given_name", is(user.getUserClaims().get(OIDCUtilTest.firstNameClaimUri)))
                .body("last_name", is(user.getUserClaims().get(OIDCUtilTest.lastName)));
    }

    /**
     * Test /userinfo endpoint for POST method.
     *
     * @param headers Headers for http POST method.
     */
    @Test(groups = "wso2.is", description = "Retrieve user claims from user-info endpoint", dependsOnMethods =
            "testHttpGetUserInfoEndpoint", dataProvider = "userInfoEndpointRequestDataProvider")
    public void testHttpPostUserInfoEndpoint(Map<String, String> headers) {

        Map<String, String> formParams = new HashMap<>();
        formParams.put("access_token", accessToken);

        Response response = getResponseOfFormPost(USER_INFO_ENDPOINT, formParams, headers);

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("email", is(user.getUserClaims().get(OIDCUtilTest.emailClaimUri)))
                .body("given_name", is(user.getUserClaims().get(OIDCUtilTest.firstNameClaimUri)))
                .body("last_name", is(user.getUserClaims().get(OIDCUtilTest.lastName)));
    }

    @Test(groups = "wso2.is", description = "Get access token with a JSON request", dependsOnMethods =
            "testHttpPostUserInfoEndpoint")
    public void testGetAccessTokenForPasswordGrantJsonRequest() throws Exception {

        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "password");
        params.put("scope", "openid");
        params.put("username", user.getUsername());
        params.put("password", user.getPassword());

        JSONObject jsonObject = new JSONObject(params);
        String payload = jsonObject.toJSONString();

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json;charset=UTF-8");

        Response response = getResponseOfJsonPostWithAuth(OAUTH2_TOKEN_ENDPOINT_URI, payload, headers,
                                                          application.getClientId(), application.getClientSecret());

        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("access_token", notNullValue());
    }

    /**
     * Invoke given endpointUri for Form POST request with given body, headers and Basic authentication credentials.
     *
     * @param endpointUri endpoint to be invoked
     * @param params      map of parameters to be added to the request
     * @param headers     map of headers to be added to the request
     * @param username    basic auth username
     * @param password    basic auth password
     * @return response
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
     * Invoke given endpointUri for JSON POST request with given body, headers and Basic authentication credentials.
     *
     * @param endpointUri endpoint to be invoked
     * @param payload     json payload
     * @param headers     map of headers to be added to the request
     * @param username    basic auth username
     * @param password    basic auth password
     * @return response
     */
    protected Response getResponseOfJsonPostWithAuth(String endpointUri, String payload, Map<String, String>
            headers, String username, String password) {

        return given().auth().preemptive().basic(username, password)
                      .headers(headers)
                      .body(payload)
                      .when()
                      .post(endpointUri);
    }

    /**
     * Invoke given endpointUri for Form POST request with given formParams, headers.
     *
     * @param endpointUri Endpoint to be invoked.
     * @param formParams  Map of form body to be added to the request.
     * @param headers     Map of headers to be added to the request.
     * @return Http response from POST request.
     */
    protected Response getResponseOfFormPost(String endpointUri, Map<String, String> formParams, Map<String, String>
            headers) {

        return given()
                .headers(headers)
                .formParams(formParams)
                .when()
                .post(endpointUri);
    }

    /**
     * Invoke given endpointUri for GET request with given params, headers.
     *
     * @param endpointUri Endpoint to be invoked.
     * @param headers     Map of headers to be added to the request.
     * @return Http response from GET request.
     */
    protected Response getResponseOfGet(String endpointUri, Map<String, String> headers) {

        return given()
                .headers(headers)
                .when()
                .get(endpointUri);
    }

    /**
     * Provide request data to test userInfoEndpoint.
     *
     * @return Object with testUserInfoEndpoint method parameters.
     */
    @DataProvider(name = "userInfoEndpointRequestDataProvider")
    private Object[][] userInfoEndpointRequestDataProvider() {

        Map<String, String> contentTypeWithCharset = new HashMap<>();
        contentTypeWithCharset.put("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

        Map<String, String> contentTypeWithoutCharset = new HashMap<>();
        contentTypeWithoutCharset.put("Content-Type", "application/x-www-form-urlencoded");

        return new Object[][]{{contentTypeWithCharset}, {contentTypeWithoutCharset}};
    }

    private void addSecondaryUserStore() throws Exception {

        String jdbcClass = "org.wso2.carbon.user.core.jdbc.UniqueIDJDBCUserStoreManager";
        H2DataBaseManager dataBaseManager = new H2DataBaseManager(
                "jdbc:h2:" + ServerConfigurationManager.getCarbonHome() + "/repository/database/" + USER_STORE_DB_NAME,
                DB_USER_NAME, DB_USER_PASSWORD);
        dataBaseManager.executeUpdate(new File(ServerConfigurationManager.getCarbonHome() + "/dbscripts/h2.sql"));
        dataBaseManager.disconnect();

        PropertyDTO[] propertyDTOs = new PropertyDTO[10];
        for (int i = 0; i < 10; i++) {
            propertyDTOs[i] = new PropertyDTO();
        }

        propertyDTOs[0].setName("driverName");
        propertyDTOs[0].setValue("org.h2.Driver");

        propertyDTOs[1].setName("url");
        propertyDTOs[1].setValue(
                "jdbc:h2:" + ServerConfigurationManager.getCarbonHome() + "/repository/database/" + USER_STORE_DB_NAME);

        propertyDTOs[2].setName("userName");
        propertyDTOs[2].setValue(DB_USER_NAME);

        propertyDTOs[3].setName("password");
        propertyDTOs[3].setValue(DB_USER_PASSWORD);

        propertyDTOs[4].setName("UserIDEnabled");
        propertyDTOs[4].setValue("true");

        UserStoreDTO userStoreDTO = userStoreConfigAdminServiceClient
                .createUserStoreDTO(jdbcClass, USER_STORE_DOMAIN, propertyDTOs);
        userStoreConfigAdminServiceClient.addUserStore(userStoreDTO);
        Thread.sleep(5000);
        userStoreConfigUtils.waitForUserStoreDeployment(userStoreConfigAdminServiceClient, USER_STORE_DOMAIN);
    }
}
