/*
 * Copyright (c) 2019-2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.rest.api.common;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.report.LevelResolverFactory;
import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.helpers.HttpHeaderHelper;
import org.apache.http.HttpHeaders;
import org.apache.http.ParseException;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.hamcrest.Matcher;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.clients.UserProfileMgtServiceClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.Base64;
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
    private static final String JAR_EXTENSION = ".jar";
    protected static final String SERVICES = "/services";

    private static final String BUNDLE = "RESTAPIErrors";
    private static ResourceBundle errorProperties = ResourceBundle.getBundle(BUNDLE);

    protected String authenticatingUserName;
    protected String authenticatingCredential;
    protected String tenant;
    protected AutomationContext context;

    protected RemoteUserStoreManagerServiceClient remoteUSMServiceClient;
    protected UserProfileMgtServiceClient userProfileMgtServiceClient;
    protected IdentityProviderMgtServiceClient identityProviderMgtServiceClient;
    protected String swaggerDefinition;

    protected String basePath = StringUtils.EMPTY;

    private OpenApiValidationFilter validationFilter;
    private EncoderConfig encoderconfig = new EncoderConfig();

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
            throws RemoteException {

        this.basePath = basePath;
        this.swaggerDefinition = swaggerDefinition;
        RestAssured.baseURI = backendURL.replace(SERVICES, "");
        String swagger = replaceInSwaggerDefinition(swaggerDefinition, basePathInSwagger, basePath);
        OpenApiInteractionValidator openAPIValidator = OpenApiInteractionValidator
                .createForInlineApiSpecification(swagger)
                .withLevelResolver(LevelResolverFactory.withAdditionalPropertiesIgnored())
                .build();
        validationFilter = new OpenApiValidationFilter(openAPIValidator);
        remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
        userProfileMgtServiceClient = new UserProfileMgtServiceClient(backendURL, sessionCookie);
        identityProviderMgtServiceClient = new IdentityProviderMgtServiceClient(sessionCookie, backendURL);
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
     * Invoke given endpointUri for GET with Basic authentication, authentication credential being the
     * authenticatingUserName and authenticatingCredential with no filter.
     *
     * @param endpointUri endpoint to be invoked
     * @return response
     */
    protected Response getResponseOfGetNoFilter(String endpointUri) {

        return given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .log().ifValidationFails()
                .when()
                .get(endpointUri);
    }

    /**
     * Invoke given endpointUri for GET with Basic authentication, authentication credential being the
     * authenticatingUserName and authenticatingCredential
     *
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
                .log().ifValidationFails(LogDetail.ALL)
                .filter(validationFilter)
                .when()
                .get(endpointUri);
    }

    /**
     * Invoke given endpointUri for GET with Basic authentication, authentication credential being the
     * authenticatingUserName and authenticatingCredential
     *
     * @param endpointUri endpoint to be invoked
     * @param params      request Parameters
     * @return response
     */
    protected Response getResponseOfGet(String endpointUri, Map<String, Object> params) {

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
     * Invoke given endpointUri for GET with Basic authentication, authentication credential being the
     * authenticatingUserName and authenticatingCredential.
     *
     * @param endpointUri endpoint to be invoked
     * @param queryParams request query parameters
     * @return response
     */
    protected Response getResponseOfGetWithQueryParams(String endpointUri, Map<String, Object> queryParams) {

        return given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .queryParams(queryParams)
                .log().ifValidationFails()
                .filter(validationFilter)
                .when()
                .get(endpointUri);
    }

    /**
     * Invoke given endpointURL for GET with OAuth2 authentication, using the provided token.
     *
     * @param endpointURL Endpoint to be invoked.
     * @param accessToken OAuth2 access token.
     * @return Response.
     */
    protected Response getResponseOfGetWithOAuth2(String endpointURL, String accessToken) {

        return given().auth().preemptive().oauth2(accessToken)
                .contentType(ContentType.JSON)
                .when()
                .get(endpointURL);
    }

    /**
     * Invoke given endpointUri for GET without authentication.
     *
     * @param endpointUri endpoint to be invoked
     * @param contentType request Parameters
     * @return response
     */
    protected Response getResponseOfGetWithoutAuthentication(String endpointUri, String contentType) {

        return given()
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
     * authenticatingUserName and authenticatingCredential.
     * This implementation does not incorporate any additional filters.
     *
     * @param endpointUri endpoint to be invoked
     * @param body        payload
     * @return response
     */
    protected Response getResponseOfPostNoFilter(String endpointUri, String body) {

        return given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .body(body)
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
     * Invoke the given endpointURL for POST with the provided body and OAuth2 authentication, using the provided
     * access token.
     *
     * @param endpointURL Endpoint to be invoked.
     * @param body Payload.
     * @param accessToken OAuth2 access token.
     * @return Response.
     */
    protected Response getResponseOfPostWithOAuth2(String endpointURL, String body, String accessToken) {

        return given().auth().preemptive().oauth2(accessToken)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(endpointURL);
    }

    /**
     * Uploads a file to a given endpointUri with a POST request.
     *
     * @param endpointUri endpoint to upload the file
     * @param fileField   control name to be used in form
     * @param filePath    file path of the file
     * @return response
     */
    protected Response getResponseOfMultipartFilePost(String endpointUri, String fileField, String filePath) {

        return given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .config(RestAssured.config().encoderConfig(encoderconfig
                        .appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .multiPart(fileField, new File(filePath))
                .filter(validationFilter)
                .log().ifValidationFails()
                .when()
                .log().ifValidationFails()
                .post(endpointUri);
    }

    /**
     * Uploads a file to a given endpointUri with a POST request.
     *
     * @param endpointUri endpoint to upload the file
     * @param filePath    file path of the file
     * @return response
     */
    protected Response getResponseOfMultipartFilePost(String endpointUri, String filePath) {

        return given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .config(RestAssured.config().encoderConfig(encoderconfig
                        .appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .multiPart(new File(filePath))
                .filter(validationFilter)
                .log().ifValidationFails()
                .when()
                .log().ifValidationFails()
                .post(endpointUri);
    }

    /**
     * Uploads a file to a given endpointUri with a PUT request.
     *
     * @param endpointUri endpoint to upload the file
     * @param filePath    file path of the file
     * @return response
     */
    protected Response getResponseOfMultipartFilePut(String endpointUri, String filePath) {

        return given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .config(RestAssured.config().encoderConfig(encoderconfig
                        .appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .multiPart(new File(filePath))
                .when()
                .put(endpointUri);
    }

    /**
     * Uploads a file to a given endpointUri with a PUT request.
     *
     * @param endpointUri endpoint to upload the file
     * @param fileField   control name to be used in form
     * @param filePath    file path of the file
     * @return response
     */
    protected Response getResponseOfMultipartFilePut(String endpointUri, String fileField, String filePath) {

        return given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .config(RestAssured.config().encoderConfig(encoderconfig
                        .appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .multiPart(fileField, new File(filePath))
                .when()
                .put(endpointUri);
    }

    /**
     * Invoke an Http POST request to the given endpoint with a file.
     *
     * @param endpointUri URI to invoke.
     * @param filePath    Path of the file to upload.
     * @param fileField   Field of the file should be uploaded with.
     * @return Http Response body as a string.
     */
    protected String getResponseOfPostWithFile(String endpointUri, String filePath, String fileField)
            throws ParseException, IOException {

        String twoHyphens = "--";
        String boundary = "*****" + System.currentTimeMillis() + "*****";
        String lineEnd = "\r\n";

        String result = "";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;

        String[] q = filePath.split("/");
        int idx = q.length - 1;

        File file = new File(filePath);
        FileInputStream fileInputStream = new FileInputStream(file);

        String completeUri = RestAssured.baseURI.substring(0, RestAssured.baseURI.length() - 1) + this.basePath +
                endpointUri;
        URL url = new URL(completeUri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);

        connection.setRequestMethod(HTTPConstants.POST);
        connection.setRequestProperty(HTTPConstants.HEADER_CONNECTION, HTTPConstants.KEEP_ALIVE);
        connection.setRequestProperty(HTTPConstants.HEADER_CONTENT_TYPE, HTTPConstants.MULTIPART_FORM_DATA +
                "; boundary=" + boundary);

        String userCredentials = authenticatingUserName + ":" + authenticatingCredential;
        String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));
        connection.setRequestProperty( HttpHeaders.AUTHORIZATION, basicAuth);

        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.writeBytes(twoHyphens + boundary + lineEnd);
        outputStream.writeBytes(HTTPConstants.HEADER_CONTENT_DISPOSITION + ": form-data; name=\"" +
                fileField + "\"; filename=\"" + q[idx] + "\"" + lineEnd);
        outputStream.writeBytes(HTTPConstants.HEADER_CONTENT_TYPE + ": image/jpeg" + lineEnd);
        outputStream.writeBytes(HttpHeaderHelper.CONTENT_TRANSFER_ENCODING + ": binary" + lineEnd);
        outputStream.writeBytes(lineEnd);

        bytesAvailable = fileInputStream.available();
        bufferSize = Math.min(bytesAvailable, maxBufferSize);
        buffer = new byte[bufferSize];

        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        while (bytesRead > 0) {
            outputStream.write(buffer, 0, bufferSize);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        }

        outputStream.writeBytes(lineEnd);
        outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

        InputStream inputStream = connection.getInputStream();
        result = this.convertStreamToString(inputStream);

        fileInputStream.close();
        inputStream.close();
        outputStream.flush();
        outputStream.close();

        return result;
    }

    private String convertStreamToString(InputStream inputStream) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        inputStream.close();

        return stringBuilder.toString();
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
     * This implementation does not incorporate any additional filters.
     *
     * @param endpointUri endpoint to be invoked
     * @param body        payload
     * @return response
     */
    protected Response getResponseOfPutWithNoFilter(String endpointUri, String body) {

        return given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .body(body)
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
     * @param body        payload
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
     * Invoke the given endpointURL for PUT with the provided body and OAuth2 authentication, using the provided
     * access token.
     *
     * @param endpointURL Endpoint to be invoked.
     * @param body Payload.
     * @param accessToken OAuth2 access token.
     * @return Response.
     */
    protected Response getResponseOfPutWithOAuth2(String endpointURL, String body, String accessToken) {

        return given().auth().preemptive().oauth2(accessToken)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .put(endpointURL);
    }

    /**
     * Invoke given endpointUri for  PATCH request with given body, headers and Basic authentication, authentication
     * credential being the authenticatingUserName and authenticatingCredential.
     *
     * @param endpointURI endpoint to be invoked
     * @param body        payload
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
     * Invoke given endpointUri for  PATCH request with given body, headers and Basic authentication, authentication
     * credential being the authenticatingUserName and authenticatingCredential.
     *
     * @param endpointURI endpoint to be invoked
     * @param body        payload
     * @return reponse
     */
    protected Response getResponseOfPatch(String endpointURI, String body) {

        return given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .config(RestAssured.config().encoderConfig(encoderconfig
                        .appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
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
     * Invoke given endpointUri for DELETE with given body and Basic authentication, authentication credential being
     * the authenticatingUserName and authenticatingCredential.
     *
     * @return response
     */
    protected Response getResponseOfDelete(String endpointURI, String contentType) {

        return given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, contentType)
                .log().ifValidationFails()
                .filter(validationFilter)
                .log().ifValidationFails()
                .when()
                .log().ifValidationFails()
                .delete(endpointURI);
    }

    /**
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
     * Invoke the given endpointURL for DELETE with OAuth2 authentication, using the provided access token.
     *
     * @param endpointURL Endpoint to be invoked.
     * @param accessToken OAuth2 access token.
     * @return Response.
     */
    protected Response getResponseOfDeleteWithOAuth2(String endpointURL, String accessToken) {

        return given().auth().preemptive().oauth2(accessToken)
                .contentType(ContentType.JSON)
                .when()
                .delete(endpointURL);
    }

    /**
     * Validate the response to be in the following desired format of Error Response
     * {
     * "code": "some_error_code",
     * "message": "Some Error Message",
     * "description": "Some Error Description",
     * "traceId"" : "correlation-id",
     * }
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

    /**
     * Validate the response to be in the following desired format of Error Response.
     * {
     * "code": "some_error_code",
     * "message": "Some Error Message",
     * "description": "Some Error Description"
     * }
     *
     * @param response API error response.
     * @param httpStatusCode HTTP status code.
     * @param errorCode Error code.
     * @param errorDescriptionArgs Placeholder values if error description in RESTAPIErrors.properties contains
     *                             dynamic values.
     */
    protected void validateErrorResponseWithoutTraceId(Response response, int httpStatusCode, String errorCode,
                                                       String... errorDescriptionArgs) {

        validateHttpStatusCode(response, httpStatusCode);
        validateResponseElement(response, "code", is(errorCode));
        validateErrorMessage(response, errorCode);
        validateErrorDescription(response, errorCode, errorDescriptionArgs);
    }

    /**
     * Validate http status code of the response
     *
     * @param response       response
     * @param httpStatusCode expected status code
     */
    protected void validateHttpStatusCode(Response response, int httpStatusCode) {

        response
                .then()
                .assertThat()
                .log().ifValidationFails()
                .statusCode(httpStatusCode);
    }

    /**
     * Validate error description of the response, if an entry is available in RESTAPIErrors.properties
     *
     * @param response     response
     * @param errorCode    error code
     * @param placeHolders values to be replaced in the error description in the corresponding entry in RESTAPIErrors
     *                     .properties
     */
    private void validateErrorDescription(Response response, String errorCode, String... placeHolders) {

        validateElementAgainstErrorProperties(response, errorCode, "description", placeHolders);
    }

    /**
     * Validate error message of the response, if an entry is available in RESTAPIErrors.properties
     *
     * @param response  response
     * @param errorCode error code
     */
    private void validateErrorMessage(Response response, String errorCode) {

        validateElementAgainstErrorProperties(response, errorCode, "message");
    }

    /**
     * Validate elements in error response against entries in RESTAPIErrors.properties
     *
     * @param response          response
     * @param errorCode         API error code
     * @param element           element
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
     *
     * @param response             response
     * @param element              JSON path element to match
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
