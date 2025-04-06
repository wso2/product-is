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

package org.wso2.identity.integration.test.extensions.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This class models the common properties of the Request that is sent in the request payload
 * to the API endpoint of a particular action.
 */
public class Request {

    protected Map<String, String[]> additionalHeaders = new HashMap<>();
    protected Map<String, String[]> additionalParams = new HashMap<>();

    public Map<String, String[]> getAdditionalHeaders() {

        return additionalHeaders != null ? Collections.unmodifiableMap(additionalHeaders) : Collections.emptyMap();
    }

    public void setAdditionalHeaders(Map<String, String[]> additionalHeaders) {

        this.additionalHeaders = additionalHeaders;
    }

    public Map<String, String[]> getAdditionalParams() {

        return additionalParams != null ? Collections.unmodifiableMap(additionalParams) : Collections.emptyMap();
    }

    public void setAdditionalParams(Map<String, String[]> additionalParams) {

        this.additionalParams = additionalParams;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return Objects.equals(additionalHeaders, request.additionalHeaders) &&
                Objects.equals(additionalParams, request.additionalParams);
    }

    @Override
    public int hashCode() {

        return Objects.hash(additionalHeaders, additionalParams);
    }
}
