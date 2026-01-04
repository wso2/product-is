/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.applicationNativeAuthentication;

/**
 * Constants for Application Native Authentication.
 */
public class Constants {

    // Common Constants
    public static final String UTF_8 = "UTF-8";
    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";

    // User Credentials
    public static final String TEST_USER_NAME = "attestationTestUser";
    public static final String TEST_APP_NAME = "oauthTestANAApplication";

    public static final String TEST_PASSWORD = "passWord1@";
    public static final String TEST_PROFILE = "default";

    // Constants related to Client Native Authentication Schema
    public static final String RESPONSE_MODE = "direct";
    public static final String ERROR_CODE_CLIENT_NATIVE_AUTHENTICATION_DISABLED = "ABA-60007";
    public static final String FLOW_STATUS = "flowStatus";
    public static final String FLOW_ID = "flowId";
    public static final String FLOW_TYPE = "flowType";
    public static final String NEXT_STEP = "nextStep";
    public static final String LINKS = "links";
    public static final String STEP_TYPE = "stepType";
    public static final String AUTHENTICATORS = "authenticators";
    public static final String AUTHENTICATOR_ID = "authenticatorId";
    public static final String AUTHENTICATOR = "authenticator";
    public static final String IDP = "idp";
    public static final String METADATA = "metadata";
    public static final String REQUIRED_PARAMS = "requiredParams";
    public static final String PROMPT_TYPE = "promptType";
    public static final String PARAMS = "params";
    public static final String ADDITIONAL_DATA = "additionalData";
    public static final String REDIRECT_URL = "redirectUrl";
    public static final String STATE ="state";
    public static final String HREF = "href";
    public static final String TRACE_ID = "traceId";
    public static final String CODE = "code";
    public static final String SUCCESS_COMPLETED = "SUCCESS_COMPLETED";
    public static final String AUTH_DATA_CODE = "authData.code";
    public static final String AUTH_DATA_APP_NAME = "authData.app_name";
    public static final String AUTH_DATA_SESSION_STATE = "authData.session_state";
    public static final String PARAM = "param";
    public static final String TYPE = "type";
    public static final String ORDER = "order";
    public static final String I18N_KEY = "i18nKey";
    public static final String DISPLAY_NAME = "displayName";
    public static final String CONFIDENTIAL = "confidential";
    public static final String MESSAGE = "message";
    public static final String DESCRIPTION = "description";
    public static final String MESSAGE_ID = "messageId";
    public static final String MESSAGES = "messages";
    public static final String FAIL_INCOMPLETE = "FAIL_INCOMPLETE";
}
