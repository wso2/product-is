/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.server.notification.sender.v2;

import io.restassured.RestAssured;
import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.wso2.identity.integration.test.rest.api.server.common.RESTAPIServerTestBase;

import java.io.IOException;

/**
 * Base test class for the Email Senders V2 Rest APIs.
 */
public class EmailSenderTestBase extends RESTAPIServerTestBase {

    public static final String API_DEFINITION_NAME = "notification-sender.yaml";
    public static final String API_VERSION = "v2";
    public static final String API_PACKAGE_NAME = "org.wso2.carbon.identity.api.server.notification.sender.v2";
    public static final String NOTIFICATION_SENDER_API_BASE_PATH = "/notification-senders";
    public static final String PATH_SEPARATOR = "/";
    public static final String EMAIL_SENDERS_PATH = "email";

    // Email sender name.
    public static final String EMAIL_SENDER_NAME = "EmailPublisher";

    // Auth type constants.
    public static final String AUTH_TYPE_BASIC = "BASIC";
    public static final String AUTH_TYPE_BEARER = "BEARER";
    public static final String AUTH_TYPE_CLIENT_CREDENTIAL = "CLIENT_CREDENTIAL";
    public static final String AUTH_TYPE_API_KEY = "API_KEY";
    public static final String AUTH_TYPE_NONE = "NONE";

    // Basic Auth constants
    public static final String BASIC_AUTH_USERNAME = "testuser";

    // Client Credential Auth constants
    public static final String CLIENT_CREDENTIAL_CLIENT_ID = "test-client-id";
    public static final String CLIENT_CREDENTIAL_TOKEN_ENDPOINT = "https://example.com/oauth2/token";
    public static final String CLIENT_CREDENTIAL_SCOPES =
            "internal_config_mgt_add internal_config_mgt_delete internal_config_mgt_list";


    // API Key Auth constants
    public static final String API_KEY_HEADER = "api_header";

    // HTTP-based email sender constants.
    public static final String HTTP_PROVIDER = "HTTP";
    public static final String HTTP_PROVIDER_URL = "https://example.email.sender";
    public static final String HTTP_PROVIDER_URL_UPDATED = "https://example.email.sender/updated";
    public static final String HTTP_CONTENT_TYPE = "JSON";
    public static final String HTTP_CLIENT_METHOD = "POST";
    public static final String HTTP_HEADERS = "X-Version: 1, Accept: application/json, Content-Type: application/json";
    public static final String HTTP_HEADERS_UPDATED =
            "X-Version: 2, Accept: application/json, Content-Type: application/json";

    // SMTP-based email sender constants.
    public static final String SMTP_SERVER_HOST = "smtp.gmail.com";
    public static final int SMTP_PORT = 587;
    public static final String SMTP_FROM_ADDRESS = "iam@gmail.com";
    public static final String SMTP_SERVER_HOST_UPDATED = "smtp.outlook.com";
    public static final String SMTP_FROM_ADDRESS_UPDATED = "updated@outlook.com";

    // GPath property expressions for asserting properties array values.
    public static final String PROPERTY_CONTENT_TYPE = "properties.find { it.key == 'contentType' }.value";
    public static final String PROPERTY_HTTP_METHOD = "properties.find { it.key == 'http.client.method' }.value";
    public static final String PROPERTY_HTTP_HEADERS = "properties.find { it.key == 'http.headers' }.value";
    public final String PROPERTY_USERNAME = "properties.find { it.key == 'userName' }.value";
    public final String PROPERTY_PASSWORD = "properties.find { it.key == 'password' }.value";
    public final String PROPERTY_CLIENT_ID = "properties.find { it.key == 'clientId' }.value";
    public final String PROPERTY_CLIENT_SECRET = "properties.find { it.key == 'clientSecret' }.value";
    public final String PROPERTY_TOKEN_ENDPOINT = "properties.find { it.key == 'tokenEndpoint' }.value";
    public final String PROPERTY_SCOPES = "properties.find { it.key == 'scopes' }.value";
    public final String PROPERTY_ACCESS_TOKEN = "properties.find { it.key == 'accessToken' }.value";
    public final String PROPERTY_API_KEY_HEADER = "properties.find { it.key == 'apiKeyHeader' }.value";
    public final String PROPERTY_API_KEY_VALUE = "properties.find { it.key == 'apiKeyValue' }.value";

    protected static String swaggerDefinition;

    static {
        try {
            swaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (Exception e) {
            Assert.fail(String.format("Unable to read the swagger definition %s from %s", API_DEFINITION_NAME,
                    API_PACKAGE_NAME), e);
        }
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() throws Exception {

        super.conclude();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        RestAssured.basePath = StringUtils.EMPTY;
    }
}
