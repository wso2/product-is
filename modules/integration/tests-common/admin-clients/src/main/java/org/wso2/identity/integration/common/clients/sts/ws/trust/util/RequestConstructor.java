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

package org.wso2.identity.integration.common.clients.sts.ws.trust.util;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import java.io.StringWriter;
import java.util.Properties;

import static org.wso2.identity.integration.common.clients.sts.ws.trust.constants.Constants.CREATION_TIME;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.constants.Constants.EXPIRY_TIME;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.constants.Constants.RENEW_ST_TEMPLATE;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.constants.Constants.REQUEST_ST_TEMPLATE;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.constants.Constants.URI;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.constants.Constants.VALIDATE_ST_TEMPLATE;

/**
 * RequestConstructor class builds the RequestSecurityTokens for each action performed.
 */
public class RequestConstructor {

    private static VelocityEngine velocityEngine = new VelocityEngine();
    private static boolean isVEInitialized = false;
    private static Template requestSTTemplate;
    private static Template renewSTTemplate;
    private static Template validateSTTemplate;
    private static StringWriter stringWriter = new StringWriter();

    /**
     * Build a RST which is required for requesting a security token
     * from the Security Token Service.
     *
     * @param creationTime Creation time of the Request Security Token.
     * @param expiryTime   Expiry time of the Request Security Token.
     * @return A soap request containing the Request Security Token
     * used to request a security token.
     */
    public static String buildRSTToRequestSecurityToken(String creationTime, String expiryTime) {

        initVelocityEngine();
        clearStringWriter();

        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put(CREATION_TIME, creationTime);
        velocityContext.put(EXPIRY_TIME, expiryTime);

        requestSTTemplate.merge(velocityContext, stringWriter);

        return stringWriter.toString();
    }

    /**
     * Build a RST which is required for renewing a security token from
     * the Security Token Service.
     *
     * @param creationTime Creation time of the Request Security Token.
     * @param expiryTime   Expiry time of the Request Security Token.
     * @param uri          Identifier for the Security Token  to be validated.
     * @return A soap request containing the Request Security Token used
     * to renew a security token.
     */
    public static String buildRSTToRenewSecurityToken(String creationTime, String expiryTime, String uri) {

        initVelocityEngine();
        clearStringWriter();

        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put(CREATION_TIME, creationTime);
        velocityContext.put(EXPIRY_TIME, expiryTime);
        velocityContext.put(URI, uri);

        renewSTTemplate.merge(velocityContext, stringWriter);

        return stringWriter.toString();
    }

    /**
     * Build a RST which is required for validating a security token
     * from the Security Token Service.
     *
     * @param creationTime Creation time of the Request Security Token.
     * @param expiryTime   Expiry time of the Request Security Token.
     * @param uri          Identifier for the Security Token to be validated.
     * @return A soap request containing the Request Security Token used
     * to validate a security token.
     */
    public static String buildRSTToValidateSecurityToken(String creationTime, String expiryTime, String uri) {

        initVelocityEngine();
        clearStringWriter();

        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put(CREATION_TIME, creationTime);
        velocityContext.put(EXPIRY_TIME, expiryTime);
        velocityContext.put(URI, uri);

        validateSTTemplate.merge(velocityContext, stringWriter);

        return stringWriter.toString();
    }

    /**
     * Initialize the velocity engine if it is not initialized yet to
     * use functions based on templating.
     */
    private static void initVelocityEngine() {

        if (!isVEInitialized) {

            Properties properties = new Properties();
            properties.setProperty("resource.loader", "class");
            properties.setProperty("class.resource.loader.class",
                    "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            properties.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                    "org.apache.velocity.runtime.log.NullLogChute" );

            velocityEngine.init(properties);

            requestSTTemplate = velocityEngine.getTemplate(REQUEST_ST_TEMPLATE);
            renewSTTemplate = velocityEngine.getTemplate(RENEW_ST_TEMPLATE);
            validateSTTemplate = velocityEngine.getTemplate(VALIDATE_ST_TEMPLATE);

            isVEInitialized = true;
        }
    }

    /**
     * Clear the string writer after it has been used previously.
     */
    private static void clearStringWriter() {

        if (stringWriter.getBuffer().length() > 0) {
            stringWriter.getBuffer().setLength(0);
        }
    }
}
