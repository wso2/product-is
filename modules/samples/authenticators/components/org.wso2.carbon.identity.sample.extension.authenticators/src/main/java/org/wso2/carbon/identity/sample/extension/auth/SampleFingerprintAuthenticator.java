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
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Sample Fingerprint Authenticator.
 */
public class SampleFingerprintAuthenticator extends AbstractSampleAuthenticator {

    private static final long serialVersionUID = 6439291340285653402L;
    private static final String FINGERPRINT_APP_URL = "FingerprintAppUrl";

    @Override
    public boolean canHandle(HttpServletRequest request) {

        String authenticatorName = request.getParameter("authenticatorName");
        if (authenticatorName != null && StringUtils.equals(authenticatorName, getName())) {
            return true;
        }
        return false;
    }

    @Override
    protected String getPageUrlProperty() {

        return FINGERPRINT_APP_URL;
    }

    @Override
    public String getContextIdentifier(HttpServletRequest request) {

        String identifier = request.getParameter("sessionDataKey");
        return identifier;
    }

    @Override
    public String getName() {

        return "SampleFingerprintAuthenticator";
    }

    @Override
    public String getFriendlyName() {

        return "Sample Fingerprint Authenticator";
    }

    @Override
    public String getClaimDialectURI() {

        return null;
    }

    @Override
    public List<Property> getConfigurationProperties() {

        List<Property> configProperties = new ArrayList<>();

        Property appUrl = new Property();
        appUrl.setName(FINGERPRINT_APP_URL);
        appUrl.setValue(IdentityUtil.
                getServerURL("sample-auth", true, true) + "/fpt.jsp");
        appUrl.setDisplayName("Fingerprint URL");
        appUrl.setRequired(true);
        appUrl.setDescription("Enter sample Fingerprint URL value.");
        appUrl.setDisplayOrder(0);
        configProperties.add(appUrl);
        return configProperties;
    }

    @Override
    protected void initiateAuthenticationRequest(HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 AuthenticationContext context)
            throws AuthenticationFailedException {

        String loginPage = IdentityUtil
                .getServerURL("sample-auth", true, true) + "/fpt.jsp";
        String queryParams =
                FrameworkUtils.getQueryStringWithFrameworkContextId(context.getQueryParams(),
                        context.getCallerSessionKey(),
                        context.getContextIdentifier());
        try {
            String retryParam = "";

            if (context.isRetrying()) {
                retryParam = "&authFailure=true&authFailureMsg=login.fail.message";
            }
            String callbackUrl = IdentityUtil
                    .getServerURL(FrameworkConstants.COMMONAUTH, true, true);
            callbackUrl = callbackUrl + "?sessionDataKey=" + context.getContextIdentifier() + "&authenticatorName="
                    + getName();
            String encodedUrl = URLEncoder.encode(callbackUrl, StandardCharsets.UTF_8.name());
            response.sendRedirect(response.encodeRedirectURL(loginPage + ("?" + queryParams)) +
                    "&callbackUrl=" + encodedUrl + "&authenticators=SampleFingerprintAuthenticator:" + "LOCAL" +
                    retryParam);
        } catch (IOException e) {
            throw new AuthenticationFailedException("Authentication failed for the Sample Fingerprint Authenticator.",
                    e);
        }
    }
}
