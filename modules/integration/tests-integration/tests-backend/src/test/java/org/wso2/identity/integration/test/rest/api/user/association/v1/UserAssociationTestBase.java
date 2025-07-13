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

package org.wso2.identity.integration.test.rest.api.user.association.v1;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.wso2.identity.integration.test.rest.api.user.common.RESTAPIUserTestBase;

import java.io.IOException;

import static io.restassured.RestAssured.given;

public class UserAssociationTestBase extends RESTAPIUserTestBase {

    static final String API_DEFINITION_NAME = "association.yaml";
    static final String API_VERSION = "v1";
    static String API_PACKAGE_NAME = "org.wso2.carbon.identity.rest.api.user.association.v1";

    public static final String ASSOCIATION_ENDPOINT_URI = "/%s/associations";
    public static final String FEDERATED_ASSOCIATION_ENDPOINT_URI = "/%s/federated-associations";
    public static final String BULK_FEDERATED_ASSOCIATION_ENDPOINT_URI = "federated-associations/bulk";

    protected String userAssociationEndpointURI;
    protected String federatedUserAssociationEndpointURI;

    protected static String swaggerDefinition;

    static {
        try {
            swaggerDefinition = getAPISwaggerDefinition(API_PACKAGE_NAME, API_DEFINITION_NAME);
        } catch (IOException e) {
            Assert.fail(String.format("Unable to read the swagger definition %s from %s", API_DEFINITION_NAME,
                    API_PACKAGE_NAME), e);
        }
    }

    void initUrls(String pathParam) {

        this.userAssociationEndpointURI = String.format(ASSOCIATION_ENDPOINT_URI, pathParam);
        this.federatedUserAssociationEndpointURI = String.format(FEDERATED_ASSOCIATION_ENDPOINT_URI, pathParam);
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
