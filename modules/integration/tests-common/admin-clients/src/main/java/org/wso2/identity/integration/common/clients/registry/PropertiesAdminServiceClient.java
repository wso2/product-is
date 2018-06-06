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

package org.wso2.identity.integration.common.clients.registry;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceStub;
import org.wso2.carbon.registry.properties.stub.beans.xsd.PropertiesBean;
import org.wso2.identity.integration.common.clients.AuthenticateStub;

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

    public void setProperty(String path, String key, String value) throws
            PropertiesAdminServiceRegistryExceptionException, RemoteException {
        stub.setProperty(path, key, value);
    }

    public void updateProperty(String path, String key, String value, String oldKey) throws
            PropertiesAdminServiceRegistryExceptionException, RemoteException {
        stub.updateProperty(path, key, value, oldKey);
    }

    /**
     * To get all the properties related with particular resource exist in the path.
     *
     * @param path      Relevant registry path.
     * @param viewProps Whether to view properties.
     * @return all the properties of the resource.
     * @throws RemoteException                                  Remote Exception.
     * @throws PropertiesAdminServiceRegistryExceptionException PropertiesAdminService Registry Exception.
     */
    public PropertiesBean getProperties(String path, String viewProps)
            throws RemoteException, PropertiesAdminServiceRegistryExceptionException {
        return stub.getProperties(path, viewProps);
    }

    /**
     * To remove a property from a registry resource.
     *
     * @param path     Path of registry resource.
     * @param propName Relevant property name
     * @throws RemoteException                                  Remote exception.
     * @throws PropertiesAdminServiceRegistryExceptionException Properties Admin Service Registry Exception.
     */
    public void removeProperty(String path, String propName)
            throws RemoteException, PropertiesAdminServiceRegistryExceptionException {
        stub.removeProperty(path, propName);
    }

}
