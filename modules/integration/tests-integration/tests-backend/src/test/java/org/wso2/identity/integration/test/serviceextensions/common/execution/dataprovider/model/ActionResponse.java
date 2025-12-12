/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.serviceextensions.common.execution.dataprovider.model;

/**
 * This class is used to represent the response from the extension, when an action is invoked.
 */
public class ActionResponse {

    private final int statusCode;
    private final String responseBody;

    public ActionResponse(int statusCode, String responseBody) {

        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {

        return statusCode;
    }

    public String getResponseBody() {

        return responseBody;
    }
}
