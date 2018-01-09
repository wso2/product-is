/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.sample.extension.auth;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.common.model.Property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Extract attributes from the request and add them to the authentication context.
 * Does not do real authentication.
 * This Authenticator is just there to showcase how to get headers and make some intelligent decision.
 */
public class RequestAttributeExtractor implements ApplicationAuthenticator {

    private static final Log log = LogFactory.getLog(RequestAttributeExtractor.class);

    private static final String HEADERS = "Headers";

    @Override
    public boolean canHandle(HttpServletRequest request) {
        return true;
    }

    @Override
    public String getContextIdentifier(HttpServletRequest request) {
        return null;
    }

    @Override
    public String getName() {
        return RequestAttributeExtractor.class.getSimpleName();
    }

    @Override
    public String getFriendlyName() {
        return getName();
    }

    @Override
    public String getClaimDialectURI() {
        return null;
    }

    @Override
    public AuthenticatorFlowStatus process(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext context) throws AuthenticationFailedException, LogoutFailedException {
        Map<String, String> authenticatorProperties = context.getAuthenticatorProperties();
        String headersString = authenticatorProperties.get(HEADERS);
        if (log.isDebugEnabled()) {
            log.debug("Extracting request Attributes. List of Headers : " + headersString);
        }
        if (StringUtils.isNotBlank(headersString)) {
            Map<String, String> resultHeadersMap = new HashMap<>();
            String[] headers = headersString.split(",");
            for (String header : headers) {
                header = header.trim();
                String headerValue = request.getHeader(header);
                if (StringUtils.isNotEmpty(headerValue)) {
                    resultHeadersMap.put(header, headerValue);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Extracted  Headers : " + resultHeadersMap);
            }
            context.setProperty(HEADERS, resultHeadersMap);
        }
        return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
    }

    @Override
    public List<Property> getConfigurationProperties() {
        List<Property> configProperties = new ArrayList<>();

        Property headers = new Property();
        headers.setName(HEADERS);
        headers.setDisplayName("Extract Headers");
        headers.setRequired(false);
        headers.setDescription("List of headers to extract, separate with comma.");
        headers.setDisplayOrder(0);
        configProperties.add(headers);

        return configProperties;
    }
}
