/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.rest.api.common;

import com.atlassian.oai.validator.SwaggerRequestResponseValidator;
import com.atlassian.oai.validator.restassured.SwaggerValidationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.axis2.AxisFault;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.hamcrest.Matcher;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase;
import org.wso2.identity.integration.test.scim2.rest.api.SCIM2BaseTest;
import org.wso2.identity.integration.test.util.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import javax.xml.xpath.XPathExpressionException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.StringContains.containsString;

/**
 * Base test class for REST API tests
 */
public class RESTTestBase extends ISIntegrationTest {

    protected static final Log log = LogFactory.getLog(RESTTestBase.class);

    private static final String API_WEB_APP_ROOT = File.separator + "repository"
            + File.separator + "deployment" + File.separator + "server" + File.separator + "webapps" + File
            .separator + "api" + File.separator + "WEB-INF" + File.separator
            + "lib" + File.separator;
    protected static final String TENANT_CONTEXT_IN_URL = "/t/%s";
    private static final String JAR_EXTENSION = ".jar";
    private static final String SERVICES = "/services";

    static final String BUNDLE = "RESTAPIErrors";
    private static ResourceBundle errorProperties = ResourceBundle.getBundle(BUNDLE);

    protected String authenticatingUserName;
    protected String authenticatingCredential;
    protected String tenant;
    protected AutomationContext context;

    protected RemoteUserStoreManagerServiceClient remoteUSMServiceClient;
    protected String swaggerDefinition;

    protected String basePath = StringUtils.EMPTY;

    private SwaggerRequestResponseValidator swaggerRequestResponseValidator;
    protected SwaggerValidationFilter validationFilter;
    EncoderConfig encoderconfig = new EncoderConfig();

    /**
     * Initialize the RestAssured environment and create SwaggerRequestResponseValidator with the swagger definition
     *
     * @param swaggerDefinition swagger definition name
     * @param basePathInSwagger basepath that is defined in the swagger definition (ex: /api/users/v1)
     * @param basePath          basepath of the current test run (ex: /t/carbon.super/api/users/v1)
     * @throws IOException
     * @throws XPathExpressionException
     */
    protected void init(String swaggerDefinition, String basePathInSwagger, String basePath)
            throws AxisFault {

        this.basePath = basePath;
        this.swaggerDefinition = swaggerDefinition;
        RestAssured.baseURI = backendURL.replace(SERVICES, "");
        String swagger = replaceInSwaggerDefinition(swaggerDefinition, basePathInSwagger, basePath);
        swaggerRequestResponseValidator = SwaggerRequestResponseValidator.createFor(swagger).build();
        validationFilter = new SwaggerValidationFilter(swaggerRequestResponseValidator);
        remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
    }

    protected void conclude() {
        RestAssured.basePath = StringUtils.EMPTY;
    }

    /**
     * Read the Swagger Definition from the .jar file in the "api" webapp
     *
     * @param jarName         .jar name
     * @param swaggerYamlName .yaml name
     * @return content of the specified swagger definition
     * @throws IOException
     */
    protected static String getAPISwaggerDefinition(String jarName, String swaggerYamlName) throws IOException {

        File dir = new File(Utils.getResidentCarbonHome() + API_WEB_APP_ROOT);
        File[] files = dir.listFiles((dir1, name) -> name.startsWith(jarName) && name.endsWith(JAR_EXTENSION));
        JarFile jarFile = new JarFile(files[0]);
        JarEntry entry = jarFile.getJarEntry(swaggerYamlName);
        InputStream input = jarFile.getInputStream(entry);
        String content = getString(input);
        jarFile.close();
        return convertYamlToJson(content);
    }

    /**
     * Read the Swagger Definition from the filePath.
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    protected static String getAPISwaggerDefinition(String filePath) throws IOException {

        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        return convertYamlToJson(content);
    }


    /**
     * Override all the occurrences of "find" values in content with "replace"
     *
     * @param find    existing base path
     * @param replace actual base path for tests run
     * @return content of the specified swagger definition
     * @throws IOException
     */
    private String replaceInSwaggerDefinition(String content, String find, String replace) {

        content = content.replaceAll(find, replace);
        return content;
    }

    /**
     * Build an String from InputStream
     *
     * @param inputStream input stream
     * @return
     * @throws IOException
     */
    private static String getString(InputStream inputStream) throws IOException {

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    /**
     * Convert swagger definition from .yaml to .json
     *
     * @param yaml swagger definition as string
     * @return json converted swagger definition as string
     * @throws IOException
     */
    private static String convertYamlToJson(String yaml) throws IOException {

        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Object obj = yamlReader.readValue(yaml, Object.class);
        ObjectMapper jsonWriter = new ObjectMapper();
        return jsonWriter.writeValueAsString(obj);
    }

    /**
     * Read a resource in class path
     *
     * @param filename file name to be read
     * @return content of the file
     * @throws IOException
     */
    protected String readResource(String filename) throws IOException {

        return readResource(filename, this.getClass());
    }

    /**
     * Read a resource in class path
     *
     * @param filename file name to be read
     * @return content of the file
     * @throws IOException
     */
    public static String readResource(String filename, Class cClass) throws IOException {

        try (InputStream resourceAsStream = cClass.getResourceAsStream(filename);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(resourceAsStream)) {
            StringBuilder resourceFile = new StringBuilder();

            int character;
            while ((character = bufferedInputStream.read()) != -1) {
                char value = (char) character;
                resourceFile.append(value);
            }

            return resourceFile.toString();
        }
    }

    /**
     * Invoke given endpointUri for GET with Basic authentication, authentication credential being the
     * authenticatingUserName and authenticatingCredential
     *
     * @param endpointUri endpoint to be invoked
     * @return response
     */
    protected Response getResponseOfGet(String endpointUri) {
        return given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .log().ifValidationFails()
                .filter(validationFilter)
                .when()
                .get(endpointUri);
    }

    /**
     *
     * Invoke given endpointUri for GET with Basic authentication, authentication credential being the
     * authenticatingUserName and authenticatingCredential
     * @param endpointUri endpoint to be invoked
     * @param contentType content type to be passed
     * @return
     */
    protected Response getResponseOfGet(String endpointUri, String contentType) {

        return given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .config(RestAssured.config().encoderConfig(encoderconfig
                        .appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .contentType(contentType)
                .header(HttpHeaders.ACCEPT, contentType)
                .log().ifValidationFails()
                .filter(validationFilter)
                .when()
                .get(endpointUri);
    }

    /**
     * Invoke given endpointUri for GET with Basic authentication, authentication credential being the
     * authenticatingUserName and authenticatingCredential
     *
     * @param endpointUri endpoint to be invoked
     * @param params request Parameters
     * @return response
     */
    protected Response getResponseOfGet(String endpointUri, Map<String,Object> params) {
        return given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .params(params)
                .log().ifValidationFails()
                .filter(validationFilter)
                .when()
                .get(endpointUri);
    }

    /**
     * Invoke given endpointUri for POST with given body and Basic authentication, authentication credential being the
     * authenticatingUserName and authenticatingCredential
     *
     * @param endpointUri endpoint to be invoked
     * @param body        payload
     * @return response
     */
    protected Response getResponseOfPost(String endpointUri, String body) {

        return given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .body(body)
                .log().ifValidationFails()
                .filter(validationFilter)
                .log().ifValidationFails()
                .when()
                .log().ifValidationFails()
                .post(endpointUri);
    }

    /**
     * Invoke given endpointUri for POST with given body and Basic authentication, authentication credential being the
     * authenticatingUserName and authenticatingCredential
     *
     * @param endpointUri endpoint to be invoked
     * @param body        payload
     * @param contentType content Type
     * @return response
     */
    protected Response getResponseOfPost(String endpointUri, String body, String contentType) {

        return given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .config(RestAssured.config().encoderConfig(encoderconfig
                        .appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .contentType(contentType)
                .header(HttpHeaders.ACCEPT, contentType)
                .body(body)
                .log().ifValidationFails()
                .filter(validationFilter)
                .log().ifValidationFails()
                .when()
                .log().ifValidationFails()
                .post(endpointUri);
    }

    /**
     * Invoke given endpointUri for JSON POST request with given body, headers and Basic authentication, authentication
     * credential being the authenticatingUserName and authenticatingCredential.
     *
     * @param endpointUri endpoint to be invoked
     * @param body        payload
     * @param headers     list of headers to be added to the request
     * @return response
     */
    protected Response getResponseOfJSONPost(String endpointUri, String body, Map<String, String> headers) {

        return given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                      .contentType(ContentType.JSON)
                      .headers(headers)
                      .body(body)
                      .when()
                      .post(endpointUri);
    }


    /**
     * Invoke given endpointUri for Form POST request with given body, headers and Basic authentication credentials
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
     * Invoke given endpointUri for PUT with given body and Basic authentication, authentication credential being the
     * authenticatingUserName and authenticatingCredential
     *
     * @param endpointUri endpoint to be invoked
     * @param body        payload
     * @return response
     */
    protected Response getResponseOfPut(String endpointUri, String body) {

        return given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .body(body)
                .log().ifValidationFails()
                .filter(validationFilter)
                .log().ifValidationFails()
                .when()
                .log().ifValidationFails()
                .put(endpointUri);
    }


    /**
     * Invoke given endpointUri for PUT with given body and Basic authentication, authentication credential being the
     * authenticatingUserName and authenticatingCredential
     *
     * @param endpointUri ndpoint to be invoked
     * @param body payload
     * @param contentType content type
     * @return response
     */
    protected Response getResponseOfPut(String endpointUri, String body, String contentType) {

        return given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .config(RestAssured.config().encoderConfig(encoderconfig
                        .appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .contentType(contentType)
                .header(HttpHeaders.ACCEPT, contentType)
                .body(body)
                .log().ifValidationFails()
                .filter(validationFilter)
                .log().ifValidationFails()
                .when()
                .log().ifValidationFails()
                .put(endpointUri);
    }

    /**
     * Invoke given endpointUri for  PATCH request with given body, headers and Basic authentication, authentication
     * credential being the authenticatingUserName and authenticatingCredential.
     *
     * @param endpointURI endpoint to be invoked
     * @param body payload
     * @param contentType content Type
     * @return reponse
     */
    protected Response getResponseOfPatch(String endpointURI, String body, String contentType) {

        return given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .config(RestAssured.config().encoderConfig(encoderconfig
                        .appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .contentType(contentType)
                .header(HttpHeaders.ACCEPT, contentType)
                .body(body)
                .log().ifValidationFails()
                .filter(validationFilter)
                .log().ifValidationFails()
                .when()
                .log().ifValidationFails()
                .patch(endpointURI);

    }

    /**
     * Invoke given endpointUri for DELETE with given body and Basic authentication, authentication credential being
     * the authenticatingUserName and authenticatingCredential
     *
     * @return response
     */
    protected Response getResponseOfDelete(String endpointURI) {

        return given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .log().ifValidationFails()
                .filter(validationFilter)
                .log().ifValidationFails()
                .when()
                .log().ifValidationFails()
                .delete(endpointURI);
    }

    /**
     *
     * Invoke given endpointUri for DELETE with given body and Basic authentication, authentication credential being
     * the authenticatingUserName and authenticatingCredential
     *
     * @param endpointURI
     * @param headers
     * @return response
     */
    protected Response getResponseOfDelete(String endpointURI, Map<String, String> headers) {

        return given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                      .contentType(ContentType.JSON)
                      .headers(headers)
                      .when()
                      .delete(endpointURI);
    }


    /**
     * Validate the response to be in the following desired format of Error Response
     *  {
     *      "code": "some_error_code",
     *      "message": "Some Error Message",
     *      "description": "Some Error Description",
     *      "traceId"" : "corelation-id",
     *  }
     *
     * @param response             API error response
     * @param httpStatusCode       http status code
     * @param errorCode            error code
     * @param errorDescriptionArgs placeholder values if, error decryption in RESTAPIErrors.properties contains,
     *                             dynamic values
     */
    protected void validateErrorResponse(Response response, int httpStatusCode, String errorCode, String...
            errorDescriptionArgs) {

        validateHttpStatusCode(response, httpStatusCode);
        validateResponseElement(response, "code", is(errorCode));
        validateResponseElement(response, "traceId", notNullValue());
        validateErrorMessage(response, errorCode);
        validateErrorDescription(response, errorCode, errorDescriptionArgs);
    }

    /** Validate http status code of the response
     * @param response response
     * @param httpStatusCode expected status code
     */
    private void validateHttpStatusCode(Response response, int httpStatusCode) {

        response
                .then()
                .assertThat()
                .log().ifValidationFails()
                .statusCode(httpStatusCode);
    }

    /** Validate error description of the response, if an entry is available in RESTAPIErrors.properties
     * @param response response
     * @param errorCode error code
     * @param placeHolders values to be replaced in the error description in the corresponding entry in RESTAPIErrors
     *                     .properties
     */
    private void validateErrorDescription(Response response, String errorCode, String... placeHolders) {

        validateElementAgainstErrorProperties(response, errorCode, "description", placeHolders);
    }

    /** Validate error message of the response, if an entry is available in RESTAPIErrors.properties
     * @param response response
     * @param errorCode error code
     */
    private void validateErrorMessage(Response response, String errorCode) {

        validateElementAgainstErrorProperties(response, errorCode, "message");
    }

    /**
     * Validate elements in error response against entries in RESTAPIErrors.properties
     * @param response response
     * @param errorCode API error code
     * @param element element
     * @param placeHolderValues placeholder values
     *                          arg[0], key element in the RESTAPIErrors.properties (error-code.arg[0])
     *                          arg[1-n] place holder values to replace in value in the RESTAPIErrors.properties
     */
    private void validateElementAgainstErrorProperties(Response response, String errorCode, String element, String...
            placeHolderValues) {

        String expected = StringUtils.EMPTY;
        try {
            expected = errorProperties.getString(String.format("%s.%s", errorCode, element));
        } catch (Throwable e) {
            //Ignore if error properties are not defined as keys in RESTAPIErrors.properties
        }
        if (StringUtils.isNotEmpty(expected)) {
            expected = String.format(expected, placeHolderValues);
            validateResponseElement(response, element, containsString(expected));
        }
    }

    /**
     * Validate a response element against a matcher
     * @param response response
     * @param element JSON path element to match
     * @param responseAwareMatcher expected matcher
     */
    protected void validateResponseElement(Response response, String element, Matcher
            responseAwareMatcher) {

        response
                .then()
                .assertThat()
                .log().ifValidationFails()
                .body(element, responseAwareMatcher);
    }

}
