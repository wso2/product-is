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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Abstract Sample Authenticator.
 */
public abstract class AbstractSampleAuthenticator extends AbstractApplicationAuthenticator implements
        LocalApplicationAuthenticator {

    private static final Log log = LogFactory.getLog(AbstractSampleAuthenticator.class);

    /**
     * Return the page URL.
     *
     * @return
     */
    protected abstract String getPageUrlProperty();


    @Override
    public AuthenticatorFlowStatus process(HttpServletRequest request, HttpServletResponse response,
                                           AuthenticationContext context)
            throws AuthenticationFailedException, LogoutFailedException {

        log.info("Sample Authenticator: \"" + getFriendlyName() + "\" called");
        if (context.isLogoutRequest()) {
            return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
        } else {
            return super.process(request, response, context);
        }
    }

    @Override
    public void processAuthenticationResponse(HttpServletRequest request,
                                              HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException {

        AuthenticatorFlowStatus status = AuthenticatorFlowStatus.FAIL_COMPLETED;
        AuthenticatedUser lastUser = context.getLastAuthenticatedUser();
        String successParam = request.getParameter("success");
        boolean isSuccess = Boolean.parseBoolean(successParam);
        if (isSuccess) {
            String subject = lastUser.getAuthenticatedSubjectIdentifier();
            AuthenticatedUser authenticatedUser =
                    AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier(subject);
            authenticatedUser.setTenantDomain(lastUser.getTenantDomain());
            context.setSubject(authenticatedUser);
            log.info(getFriendlyName() + " successful, User : " + subject);
            status = AuthenticatorFlowStatus.SUCCESS_COMPLETED;
        }
        if (status == AuthenticatorFlowStatus.FAIL_COMPLETED) {
            log.error("user authentication failed.");
        }
    }
}
