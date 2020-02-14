/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.common.clients.sts.ws.trust.constants;

/**
 * Class contains the constant values used across the module.
 */
public class Constants {

    // Security token service endpoint of WSO2 Identity Server.
    public static final String STS_ENDPOINT_URL = "https://localhost:9853/services/wso2carbon-sts";

    // Identifiers used to validate the action performed.
    public static final String ACTION_REQUEST = "Request";
    public static final String ACTION_RENEW = "Renew";
    public static final String ACTION_VALIDATE = "Validate";

    // Attributes for templating.
    public static final String CREATION_TIME = "creationTime";
    public static final String EXPIRY_TIME = "expiryTime";
    public static final String URI = "uri";

    // Location of template files.
    public static final String REQUEST_ST_TEMPLATE = "ws-trust-templates/request_security_token_RST.xml";
    public static final String RENEW_ST_TEMPLATE = "ws-trust-templates/renew_security_token_RST.xml";
    public static final String VALIDATE_ST_TEMPLATE = "ws-trust-templates/validate_security_token_RST.xml";
}
