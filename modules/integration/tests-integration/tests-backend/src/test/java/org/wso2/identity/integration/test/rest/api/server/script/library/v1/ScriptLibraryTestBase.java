/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.rest.api.server.script.library.v1;

import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.wso2.identity.integration.test.rest.api.server.common.RESTAPIServerTestBase;

import java.io.IOException;

import static io.restassured.RestAssured.given;

/**
 * Base test class for Script library management REST APIs.
 */
public class ScriptLibraryTestBase extends RESTAPIServerTestBase {

    public static final String API_DEFINITION_NAME = "scriptLibrary.yaml";
    public static final String API_VERSION = "v1";
    public static final String API_PACKAGE_NAME = "org.wso2.carbon.identity.api.server.script.library.v1";
    public static final String SCRIPT_LIBRARY_API_BASE_PATH = "/script-libraries";
    public static final String PATH_SEPARATOR = "/";

    protected static String swaggerDefinition;

    static {
        try {
            swaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (IOException e) {
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

    /**
     * Multipart POST request to add the script library.
     *
     * @param endpointUri Endpoint to add script library
     * @param content     Script library content.
     * @param name        Script library name.
     * @param description Script library description
     * @return response
     */
    protected Response getResponseOfMultipartPost(String endpointUri, String content, String name, String description) {

        EncoderConfig encoderconfig = new EncoderConfig();
        return given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .config(RestAssured.config().encoderConfig(encoderconfig
                        .appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .multiPart("name", name)
                .multiPart("content", content)
                .multiPart("description", description)
                .log().ifValidationFails()
                .when()
                .log().ifValidationFails()
                .post(endpointUri);
    }

    /**
     * Multipart PUT request to update the script library..
     *
     * @param endpointUri Endpoint to add script library
     * @param content     Script library content.
     * @param description Script library description
     * @return response
     */
    protected Response getResponseOfMultipartPut(String endpointUri, String content, String description) {

        EncoderConfig encoderconfig = new EncoderConfig();
        return given().auth().preemptive().basic(authenticatingUserName, authenticatingCredential)
                .config(RestAssured.config().encoderConfig(encoderconfig
                        .appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .multiPart("content", content)
                .multiPart("description", description)
                .log().ifValidationFails()
                .when()
                .log().ifValidationFails()
                .put(endpointUri);
    }

}
