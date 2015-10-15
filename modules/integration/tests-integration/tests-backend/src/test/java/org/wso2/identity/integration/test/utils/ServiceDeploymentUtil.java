/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.utils;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.identity.integration.common.clients.workflow.mgt.ServiceAdminClient;

import java.rmi.RemoteException;
import java.util.Calendar;

public class ServiceDeploymentUtil {

    public static int SERVICE_DEPLOYMENT_DELAY = 240 * 1000;
    private static Log log = LogFactory.getLog(ServiceDeploymentUtil.class);


    /**
     * This method checks the deployment state of the Axis2 service.
     *
     *
     * @param backEndUrl Service backend URL of the service.
     * @param sessionCookie Session cookie to authorize the stub.
     * @param serviceName Corresponding service name.
     * @return
     * @throws RemoteException
     */

    public static boolean isServiceDeployed(String backEndUrl, String sessionCookie,
                                            String serviceName)
            throws RemoteException {
        log.info("waiting " + SERVICE_DEPLOYMENT_DELAY + " millis for Service deployment " + serviceName);
        boolean isServiceDeployed = false;
        ServiceAdminClient adminServiceService = new ServiceAdminClient(backEndUrl, sessionCookie);
        Calendar startTime = Calendar.getInstance();
        long time;
        while ((time = (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())) <
                SERVICE_DEPLOYMENT_DELAY) {
            if (adminServiceService.isServiceExists(serviceName)) {
                isServiceDeployed = true;
                log.info(serviceName + " Service Deployed in " + time + " millis");
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
        }
        return isServiceDeployed;
    }

}
