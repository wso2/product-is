/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.hamcrest.Matcher;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;

import java.rmi.RemoteException;
import java.util.ResourceBundle;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;

/**
 * Base test class for B2B organization management REST API tests.
 */
public class B2BRESTTestBase extends RESTTestBase {

    protected static final String TENANT_CONTEXT_IN_ORG_URL = "/t/%s/o";
    protected static final String SERVICES = "/services";
    private static ResourceBundle errorProperties = ResourceBundle.getBundle("RESTAPIErrors");

    protected String authenticatingUserName;
    protected String authenticatingCredential;
    protected String tenant;
    protected AutomationContext context;

    protected RemoteUserStoreManagerServiceClient remoteUSMServiceClient;
    protected OAuth2RestClient oAuth2RestClient;
    protected String swaggerDefinition;

    protected String basePath = StringUtils.EMPTY;

    /**
     * Initialize the REST API validation requirements configuring the OpenApiValidationFilter
     *
     * @param swaggerDefinition Swagger definition name.
     * @param basePathInSwagger Basepath that is defined in the swagger definition (ex: /api/users/v1).
     * @param basePath Basepath of the current test run (ex: /o/{organization-domain}/api/users/v1).
     * @throws RemoteException Throws this exception.
     */
    protected void init(String swaggerDefinition, String basePathInSwagger, String basePath) throws RemoteException {

        super.init(swaggerDefinition, basePathInSwagger, basePath);
        this.basePath = basePath;
    }

    /**
     * Invoke given endpointUri for GET with Basic authentication, authentication credential being the
     * authenticatingUserName and authenticatingCredential.
     *
     * @param endpointUri Endpoint to be invoked.
     * @return response of get request.
     */
    protected Response getResponseOfGet(String endpointUri) {

        return given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .log().ifValidationFails()
                .when()
                .get(endpointUri);
    }

    /**
     * Invoke given endpointUri for POST with given body and Basic authentication, authentication credential being the
     * authenticatingUserName and authenticatingCredential.
     *
     * @param endpointUri Endpoint to be invoked.
     * @param body        Payload.
     * @return Response of post request.
     */
    protected Response getResponseOfPost(String endpointUri, String body) {

        return given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .body(body)
                .log().ifValidationFails()
                .log().ifValidationFails()
                .when()
                .log().ifValidationFails()
                .post(endpointUri);
    }

    /**
     * Invoke given endpointUri for DELETE with given body and Basic authentication, authentication credential being
     * the authenticatingUserName and authenticatingCredential.
     *
     * @param endpointURI Endpoint of the request.
     * @return Response of delete request.
     */
    protected Response getResponseOfDelete(String endpointURI) {

        return given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .log().ifValidationFails()
                .log().ifValidationFails()
                .when()
                .log().ifValidationFails()
                .delete(endpointURI);
    }

    /**
     * Validate the error response of a request.
     *
     * @param response Response of the request.
     * @param httpStatusCode Status code of the response.
     * @param errorCode Error code of the response.
     * @param errorDescriptionArgs Error msg and the description of the response.
     */
    protected void validateErrorResponse(Response response, int httpStatusCode, String errorCode, String...
            errorDescriptionArgs) {

        validateHttpStatusCode(response, httpStatusCode);
        validateResponseElement(response, "code", is(errorCode));
        validateErrorMessage(response, errorCode);
        validateErrorDescription(response, errorCode, errorDescriptionArgs);
    }

    /**
     * Validate http status code of the response.
     *
     * @param response       Response.
     * @param httpStatusCode Expected status code.
     */
    protected void validateHttpStatusCode(Response response, int httpStatusCode) {

        response
                .then()
                .assertThat()
                .log().ifValidationFails()
                .statusCode(httpStatusCode);
    }

    /**
     * Validate error description of the response, if an entry is available in RESTAPIErrors.properties.
     *
     * @param response     Response.
     * @param errorCode    Error code.
     * @param placeHolders Values to be replaced in the error description in the corresponding entry in
     *                     RESTAPIError.properties.
     */
    private void validateErrorDescription(Response response, String errorCode, String... placeHolders) {

        validateElementAgainstErrorProperties(response, errorCode, "description", placeHolders);
    }

    /**
     * Validate error message of the response, if an entry is available in RESTAPIErrors.properties.
     *
     * @param response  Response.
     * @param errorCode Error code.
     */
    private void validateErrorMessage(Response response, String errorCode) {

        validateElementAgainstErrorProperties(response, errorCode, "message");
    }

    /**
     * Validate elements in error response against entries in RESTAPIErrors.properties.
     *
     * @param response          Response.
     * @param errorCode         API error code.
     * @param element           Element.
     * @param placeHolderValues Placeholder values.
     *                          arg[0], key element in the RESTAPIErrors.properties (error-code.arg[0]).
     *                          arg[1-n] place holder values to replace in value in the RESTAPIErrors.properties.
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
     * Validate a response element against a matcher.
     *
     * @param response             Response.
     * @param element              JSON path element to match.
     * @param responseAwareMatcher Expected matcher.
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
