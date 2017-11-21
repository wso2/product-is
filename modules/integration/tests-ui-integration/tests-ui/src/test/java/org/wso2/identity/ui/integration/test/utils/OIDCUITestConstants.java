/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.ui.integration.test.utils;

/**
 * This class defines all the constants used by OIDC UI test cases
 */
public class OIDCUITestConstants {

    public static final String OAUTH_VERSION_2 = "OAuth-2.0";
    public static final String OAUTH_2 = "oauth2";
    public static final String OAUTH_CONSUMER_SECRET = "oauthConsumerSecret";

    public static final class OPEndpoints {

        public static final String authorizeEndpoint = "https://localhost:9853/oauth2/authorize";
        public static final String tokenEndpoint = "https://localhost:9853/oauth2/token";
        public static final String userInfoEndpoint = "https://localhost:9853/userinfo?schema=openid";
        public static final String logoutEndpoint = "https://localhost:9853/oidc/logout";
        public static final String checkSessionIframeEndpoint = "https://localhost:9853/oidc/checksession?client_id=%s";
    }

    public static final class PlaygroundAppElementIdentifiers {

        public static final String grantTypeElement = "grantType";
        public static final String clientIdElement = "consumerKey";
        public static final String scopeElement = "scope";
        public static final String callBackURLElement = "callbackurl";
        public static final String authorizeEndpointElement = "authorizeEndpoint";
        public static final String logoutEndpointElement = "logoutEndpoint";
        public static final String sessionIFrameEndpointElement = "sessionIFrameEndpoint";
        public static final String accessTokenEndpointElement = "accessEndpoint";
        public static final String clientSecretElement = "consumerSecret";
        public static final String accessTokenElement = "accessToken";
        public static final String loggedUserElement = "loggedUser";
        public static final String authorizeButtonElement = "authorize";
        public static final String logoutButtonElement = ".//*[@id='loginForm']/table/tbody/tr[4]/td[2]/button";
    }

    public static final class AuthEndpointElementIdentifiers {

        public static final String usernameElement = "username";
        public static final String passwordElement = "password";
        public static final String signInButtonElement = ".//*[@id='loginForm']/div[4]/div[2]/button";
        public static final String loginApproveButtonElement = "chkApprovedAlways";
        public static final String logoutApproveButtonElement = "approve";
    }

    public static final class PlaygroundAppPaths {

        public static final String callBackPath = "/oauth2client";
        public static final String homePagePath = "/index.jsp";
        public static final String appResetPath = "/oauth2.jsp?reset=true";
    }

    public static final class AuthEndpointPaths {

        public static final String loginPagePath = "authenticationendpoint/login.do";
        public static final String loginConsentPagePath = "authenticationendpoint/oauth2_consent.do";
        public static final String logoutConsentPagePath = "authenticationendpoint/oauth2_logout_consent.do";

    }

    private OIDCUITestConstants() {
    }
}
