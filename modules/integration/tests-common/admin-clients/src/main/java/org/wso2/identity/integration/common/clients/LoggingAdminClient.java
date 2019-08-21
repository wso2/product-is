/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.common.clients;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LoggingAdminClient {

    private static final Log log = LogFactory.getLog(LoggingAdminClient.class);
    private final String serviceName = "LoggingAdmin";
    private String endpoint = null;

    public static enum LogLevel {OFF, TRACE, DEBUG, INFO, WARN, ERROR, FATAL}

    public LoggingAdminClient(String backEndUrl, String sessionCookie) throws AxisFault {

        this.endpoint = backEndUrl + serviceName;
    }

    public LoggingAdminClient(String backEndURL, String userName, String password) throws AxisFault {

        this.endpoint = backEndURL + serviceName;
    }

    public boolean updateLoggerData(String loggerName, String logLevel, boolean additivity, boolean persist)
            throws Exception {

        return true;
    }
}
