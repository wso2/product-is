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

package org.wso2.identity.integration.common.clients.workflow.mgt;


import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.integration.common.admin.client.utils.AuthenticateStubUtil;
import org.wso2.carbon.service.mgt.stub.ServiceAdminStub;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaData;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaDataWrapper;

import java.rmi.RemoteException;

public class ServiceAdminClient {

    private static final Log log = LogFactory.getLog(ServiceAdminClient.class);
    private final String serviceName = "ServiceAdmin";
    private ServiceAdminStub serviceAdminStub;


    public ServiceAdminClient(String backEndUrl, String sessionCookie) throws AxisFault {

        String endPoint = backEndUrl + serviceName;
        serviceAdminStub = new ServiceAdminStub(endPoint);
        AuthenticateStubUtil.authenticateStub(sessionCookie, serviceAdminStub);
    }

    public ServiceAdminClient(String backEndUrl, String userName, String password)
            throws AxisFault {

        String endPoint = backEndUrl + serviceName;
        serviceAdminStub = new ServiceAdminStub(endPoint);
        AuthenticateStubUtil.authenticateStub(userName, password, serviceAdminStub);
    }

    public boolean isServiceExists(String serviceName)
            throws RemoteException {
        boolean serviceState = false;
        ServiceMetaDataWrapper serviceMetaDataWrapper;
        ServiceMetaData[] serviceMetaDataList;
        serviceMetaDataWrapper = listServices(serviceName);
        serviceMetaDataList = serviceMetaDataWrapper.getServices();
        if (serviceMetaDataList == null || serviceMetaDataList.length == 0) {
            serviceState = false;
        } else {
            for (ServiceMetaData serviceData : serviceMetaDataList) {
                if (serviceData != null && serviceData.getName().equalsIgnoreCase(serviceName)) {
                    return true;
                }
            }
        }
        return serviceState;
    }

    public ServiceMetaDataWrapper listServices(String serviceName)
            throws RemoteException {
        ServiceMetaDataWrapper serviceMetaDataWrapper;
        serviceMetaDataWrapper = serviceAdminStub.listServices("ALL", serviceName, 0);
        serviceAdminStub.getFaultyServiceArchives(0);
        return serviceMetaDataWrapper;
    }

    public ServiceMetaDataWrapper listServices(String serviceName, String filterType)
            throws RemoteException {
        ServiceMetaDataWrapper serviceMetaDataWrapper;
        serviceMetaDataWrapper = serviceAdminStub.listServices(filterType, serviceName, 0);
        serviceAdminStub.getFaultyServiceArchives(0);
        return serviceMetaDataWrapper;
    }

}
