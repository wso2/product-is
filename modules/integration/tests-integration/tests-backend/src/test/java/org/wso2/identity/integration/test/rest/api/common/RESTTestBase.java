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
import io.restassured.response.Response;
import org.apache.axis2.AxisFault;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hamcrest.Matcher;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import javax.xml.xpath.XPathExpressionException;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.StringContains.containsString;

/**
 * Base test class for REST API tests
 */
public class RESTTestBase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(RESTTestBase.class);

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

    protected String basePath = StringUtils.EMPTY;

    private SwaggerRequestResponseValidator swaggerRequestResponseValidator;
    protected SwaggerValidationFilter validationFilter;

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

    protected void validateErrorResponse(Response response, int httpStatusCode, String errorCode, String...
            errorDescriptionArgs) {

        validateHttpStatusCode(response, httpStatusCode);
        validateResponseElement(response, "code", is(errorCode));
        validateResponseElement(response, "traceId", notNullValue());
        validateErrorMessage(response, errorCode);
        validateErrorDescription(response, errorCode, errorDescriptionArgs);
    }

    private void validateHttpStatusCode(Response response, int httpStatusCode) {

        response
                .then()
                .assertThat()
                .log().ifValidationFails()
                .statusCode(httpStatusCode);
    }

    private void validateErrorDescription(Response response, String errorCode, String... placeHolders) {

        validateElementAgainstErrorProperties(response, errorCode, "description", placeHolders);
    }

    private void validateErrorMessage(Response response, String errorCode) {

        validateElementAgainstErrorProperties(response, errorCode, "message");
    }

    private void validateElementAgainstErrorProperties(Response response, String errorCode, String element, String...
            placeHolderValues) {

        String expected = StringUtils.EMPTY;
        try {
            expected = errorProperties.getString(String.format("%s.%s", errorCode, element));
        } catch (Throwable e) {
            //Ignore if error properties are not defined
        }
        if (StringUtils.isNotEmpty(expected)) {
            expected = String.format(expected, placeHolderValues);
            validateResponseElement(response, element, containsString(expected));
        }
    }

    protected void validateResponseElement(Response response, String element, Matcher
            responseAwareMatcher) {

        response
                .then()
                .assertThat()
                .log().ifValidationFails()
                .body(element, responseAwareMatcher);
    }

}
