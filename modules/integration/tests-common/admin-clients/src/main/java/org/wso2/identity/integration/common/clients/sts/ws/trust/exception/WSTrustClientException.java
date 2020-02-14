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

package org.wso2.identity.integration.common.clients.sts.ws.trust.exception;

/**
 * Exception class used to report exceptions caused on the org.wso2.samples.is.sts.wstrust.client.Client's side.
 */
public class WSTrustClientException extends Exception {

    /**
     * Non-Default constructor accepting an org.wso2.samples.is.sts.wstrust.client.exception message.
     *
     * @param exceptionMessage Description of the org.wso2.samples.is.sts.wstrust.client.exception.
     */
    public WSTrustClientException(String exceptionMessage) {

        super(exceptionMessage);
    }

    /**
     * Non-Default constructor accepting an org.wso2.samples.is.sts.wstrust.client.exception message and the cause.
     *
     * @param errorDescription Description of the org.wso2.samples.is.sts.wstrust.client.exception.
     * @param cause            Cause of the org.wso2.samples.is.sts.wstrust.client.exception.
     */
    public WSTrustClientException(String errorDescription, Throwable cause) {

        super(errorDescription, cause);
    }
}
