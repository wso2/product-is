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

package org.wso2.identity.integration.test.rest.api.user.challenge.v1;

import io.restassured.RestAssured;
import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.wso2.identity.integration.test.rest.api.user.common.RESTAPIUserTestBase;

import java.io.IOException;

public class UserChallengeTestBase extends RESTAPIUserTestBase {

    static final String API_DEFINITION_NAME = "challenge.yaml";
    static final String API_VERSION = "v1";
    static String API_PACKAGE_NAME = "org.wso2.carbon.identity.rest.api.user.challenge.v1";

    public static final String CHALLENGES_ENDPOINT_URI = "/%s/challenges";
    public static final String CHALLENGE_ANSWERS_ENDPOINT_URI = "/%s/challenge-answers";

    protected String userChallengesEndpointURI;
    protected String userChallengeAnswerEndpointURI;
    protected String userChallengeAnswersEndpointURI;

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

        this.userChallengesEndpointURI = String.format(CHALLENGES_ENDPOINT_URI, pathParam);
        this.userChallengeAnswersEndpointURI = String.format(CHALLENGE_ANSWERS_ENDPOINT_URI, pathParam);
        this.userChallengeAnswerEndpointURI = this.userChallengeAnswersEndpointURI + "/%s";
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
