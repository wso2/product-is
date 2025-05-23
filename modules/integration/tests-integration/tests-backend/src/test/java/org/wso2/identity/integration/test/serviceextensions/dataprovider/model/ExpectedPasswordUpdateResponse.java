/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.serviceextensions.dataprovider.model;

public class ExpectedPasswordUpdateResponse {

    private final int statusCode;
    private final String errorMessage;
    private final String errorDetail;

    public ExpectedPasswordUpdateResponse(int statusCode, String errorMessage, String errorDetail) {

        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
        this.errorDetail = errorDetail;
    }

    public int getStatusCode() {

        return statusCode;
    }

    public String getErrorDetail() {

        return errorDetail;
    }

    public String getErrorMessage() {

        return errorMessage;
    }
}
