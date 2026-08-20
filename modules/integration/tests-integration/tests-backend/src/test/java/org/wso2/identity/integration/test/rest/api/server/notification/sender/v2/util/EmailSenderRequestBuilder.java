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

package org.wso2.identity.integration.test.rest.api.server.notification.sender.v2.util;

import com.google.gson.Gson;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v2.model.EmailSender;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v2.model.Properties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.identity.integration.test.rest.api.common.RESTTestBase.readResource;

/**
 * Builder class for creating Email sender request JSON objects for V2 API.
 */
public class EmailSenderRequestBuilder {

    // Basic Auth constants
    public static final String BASIC_AUTH_USERNAME = "testuser";
    public static final String BASIC_AUTH_PASSWORD = "testpass";

    // Client Credential Auth constants
    public static final String CLIENT_CREDENTIAL_CLIENT_ID = "test-client-id";
    public static final String CLIENT_CREDENTIAL_CLIENT_SECRET = "test-client-secret";
    public static final String CLIENT_CREDENTIAL_TOKEN_ENDPOINT = "https://example.com/oauth2/token";
    public static final String CLIENT_CREDENTIAL_SCOPES =
            "internal_config_mgt_add internal_config_mgt_delete internal_config_mgt_list";

    // Bearer Auth constants
    public static final String BEARER_ACCESS_TOKEN = "test-bearer-token";

    // API Key Auth constants
    public static final String API_KEY_HEADER = "api_header";
    public static final String API_KEY_VALUE = "test_123";

    /**
     * Creates an add HTTP-based email sender JSON request with the specified authentication type.
     *
     * @param authType  the authentication type to use
     * @param testClass the test class to use for resource loading
     * @return EmailSender object for adding an HTTP-based email sender
     * @throws IOException if resource file cannot be read
     */
    public static EmailSender createAddHTTPEmailSenderJSON(String authType, Class<?> testClass)
            throws IOException {

        String basicJson = readResource("add-http-email-sender.json", testClass);
        EmailSender emailSender = new Gson().fromJson(basicJson, EmailSender.class);
        emailSender.setAuthType(authType);
        addAuthProperties(emailSender, authType);

        return emailSender;
    }

    /**
     * Creates an update HTTP-based email sender JSON request with the specified authentication type.
     *
     * @param authType  the authentication type to use
     * @param testClass the test class to use for resource loading
     * @return EmailSender object for updating an HTTP-based email sender
     * @throws IOException if resource file cannot be read
     */
    public static EmailSender createUpdateHTTPEmailSenderJSON(String authType, Class<?> testClass)
            throws IOException {

        String basicJson = readResource("update-http-email-sender.json", testClass);
        EmailSender emailSender = new Gson().fromJson(basicJson, EmailSender.class);
        emailSender.setAuthType(authType);
        addAuthProperties(emailSender, authType);

        return emailSender;
    }

    /**
     * Creates an add SMTP-based email sender JSON request with the specified authentication type.
     *
     * @param authType  the authentication type to use
     * @param testClass the test class to use for resource loading
     * @return EmailSender object for adding an SMTP-based email sender
     * @throws IOException if resource file cannot be read
     */
    public static EmailSender createAddSMTPEmailSenderJSON(String authType, Class<?> testClass)
            throws IOException {

        String basicJson = readResource("add-smtp-email-sender.json", testClass);
        EmailSender emailSender = new Gson().fromJson(basicJson, EmailSender.class);
        emailSender.setAuthType(authType);
        addAuthProperties(emailSender, authType);

        return emailSender;
    }

    /**
     * Creates an update SMTP-based email sender JSON request with the specified authentication type.
     *
     * @param authType  the authentication type to use
     * @param testClass the test class to use for resource loading
     * @return EmailSender object for updating an SMTP-based email sender
     * @throws IOException if resource file cannot be read
     */
    public static EmailSender createUpdateSMTPEmailSenderJSON(String authType, Class<?> testClass)
            throws IOException {

        String basicJson = readResource("update-smtp-email-sender.json", testClass);
        EmailSender emailSender = new Gson().fromJson(basicJson, EmailSender.class);
        emailSender.setAuthType(authType);
        addAuthProperties(emailSender, authType);

        return emailSender;
    }

    /**
     * Adds authentication-specific properties to the email sender based on the auth type.
     *
     * @param emailSender the email sender object to add properties to
     * @param authType    the authentication type
     */
    private static void addAuthProperties(EmailSender emailSender, String authType) {

        List<Properties> properties = emailSender.getProperties();
        if (properties == null) {
            properties = new ArrayList<>();
            emailSender.setProperties(properties);
        }

        switch (authType) {
            case "BASIC":
                properties.add(createProperty("userName", BASIC_AUTH_USERNAME));
                properties.add(createProperty("password", BASIC_AUTH_PASSWORD));
                break;
            case "CLIENT_CREDENTIAL":
                properties.add(createProperty("clientId", CLIENT_CREDENTIAL_CLIENT_ID));
                properties.add(createProperty("clientSecret", CLIENT_CREDENTIAL_CLIENT_SECRET));
                properties.add(createProperty("tokenEndpoint", CLIENT_CREDENTIAL_TOKEN_ENDPOINT));
                properties.add(createProperty("scopes", CLIENT_CREDENTIAL_SCOPES));
                break;
            case "BEARER":
                properties.add(createProperty("accessToken", BEARER_ACCESS_TOKEN));
                break;
            case "API_KEY":
                properties.add(createProperty("apiKeyHeader", API_KEY_HEADER));
                properties.add(createProperty("apiKeyValue", API_KEY_VALUE));
                break;
            case "NONE":
                // No additional properties needed
                break;
            default:
                throw new IllegalArgumentException("Unsupported authentication type: " + authType);
        }
    }

    /**
     * Creates a Properties object with the specified key and value.
     *
     * @param key   the property key
     * @param value the property value
     * @return the created Properties object
     */
    private static Properties createProperty(String key, String value) {

        Properties property = new Properties();
        property.setKey(key);
        property.setValue(value);
        return property;
    }
}
