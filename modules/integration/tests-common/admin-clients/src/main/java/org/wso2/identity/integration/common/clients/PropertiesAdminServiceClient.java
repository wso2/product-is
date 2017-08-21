/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.common.clients;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceStub;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;

import java.rmi.RemoteException;

/**
 * Client for the properties admin service .
 */
public class PropertiesAdminServiceClient {

    private final String SERVICE_NAME = "PropertiesAdminService";
    private PropertiesAdminServiceStub stub;

    public PropertiesAdminServiceClient(String backendURL, String sessionCookie) throws AxisFault {
        String serviceURL = backendURL + SERVICE_NAME;
        stub = new PropertiesAdminServiceStub(serviceURL);
        AuthenticateStub.authenticateStub(sessionCookie, stub);
    }

    public void setProperty(String path, String key, String value) throws RegistryException, PropertiesAdminServiceRegistryExceptionException  {
        try {
            stub.setProperty(path, key, value);
        } catch (RemoteException e) {
            throw new RegistryException("Failed to add property " + key + "to resource at path " + path, e);
        }
    }

}
