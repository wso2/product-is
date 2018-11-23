/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.common.clients.functions.library.mgt;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.identity.functions.library.mgt.model.xsd.FunctionLibrary;
import org.wso2.carbon.identity.functions.library.mgt.stub.FunctionLibraryManagementAdminServiceFunctionLibraryManagementException;
import org.wso2.carbon.identity.functions.library.mgt.stub.FunctionLibraryManagementAdminServiceStub;

import java.rmi.RemoteException;

/**
 * Function library management service client for integration tests.
 */
public class FunctionLibraryManagementServiceClient {

    FunctionLibraryManagementAdminServiceStub stub;

    /**
     * Instantiates FunctionLibraryManagementServiceClient.
     *
     * @param cookie           For session management
     * @param backendServerURL URL of the back end server where FunctionLibraryManagementAdminServiceStub is running
     * @param configCtx        ConfigurationContext
     * @throws AxisFault
     */
    public FunctionLibraryManagementServiceClient(String cookie, String backendServerURL,
                                                  ConfigurationContext configCtx) throws AxisFault {

        String serviceURL = backendServerURL + "FunctionLibraryManagementAdminService";
        stub = new FunctionLibraryManagementAdminServiceStub(configCtx, serviceURL);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(HTTPConstants.COOKIE_STRING, cookie);
    }

    /**
     * Create a new function library.
     *
     * @param functionLibrary Function library
     * @throws AxisFault
     */
    public void createFunctionLibrary(FunctionLibrary functionLibrary) throws AxisFault {

        try {
            stub.createFunctionLibrary(functionLibrary);
        } catch (RemoteException | FunctionLibraryManagementAdminServiceFunctionLibraryManagementException e) {
            handleException(e);
        }
    }

    /**
     * Retrieve a list of function libraries.
     *
     * @return A list of function library
     * @throws AxisFault
     */
    public FunctionLibrary[] listFunctionLibraries() throws AxisFault {

        try {
            return stub.listFunctionLibraries();
        } catch (RemoteException | FunctionLibraryManagementAdminServiceFunctionLibraryManagementException e) {
            handleException(e);
        }
        return new FunctionLibrary[0];
    }

    /**
     * Retrieve a function library using the given name.
     *
     * @param functionLibraryName Function library name
     * @return A function library
     * @throws AxisFault
     */
    public FunctionLibrary getFunctionLibrary(String functionLibraryName) throws AxisFault {

        try {
            return stub.getFunctionLibrary(functionLibraryName);
        } catch (RemoteException | FunctionLibraryManagementAdminServiceFunctionLibraryManagementException e) {
            handleException(e);
        }
        return null;
    }

    /**
     * Delete an existing function library.
     *
     * @param functionLibraryName Function library name
     * @throws AxisFault
     */
    public void deleteFunctionLibrary(String functionLibraryName) throws AxisFault {

        try {
            stub.deleteFunctionLibrary(functionLibraryName);
        } catch (RemoteException | FunctionLibraryManagementAdminServiceFunctionLibraryManagementException e) {
            handleException(e);
        }
    }

    /**
     * Update an existing function library.
     *
     * @param functionLibrary        Function library
     * @param oldFunctionLibraryName Previous name of function library
     * @throws AxisFault
     */
    public void updateFunctionLibrary(FunctionLibrary functionLibrary, String oldFunctionLibraryName)
            throws AxisFault {
        try {
            stub.updateFunctionLibrary(functionLibrary, oldFunctionLibraryName);
        } catch (RemoteException | FunctionLibraryManagementAdminServiceFunctionLibraryManagementException e) {
            handleException(e);
        }
    }

    /**
     * Handles the exception.
     *
     * @param e Exception
     * @throws AxisFault
     */
    private void handleException(Exception e) throws AxisFault {
        String errorMessage = "Unknown error occurred.";

        if (e instanceof FunctionLibraryManagementAdminServiceFunctionLibraryManagementException) {
            FunctionLibraryManagementAdminServiceFunctionLibraryManagementException exception =
                    (FunctionLibraryManagementAdminServiceFunctionLibraryManagementException) e;
            if (exception.getFaultMessage().getFunctionLibraryManagementException() != null) {
                errorMessage = exception.getFaultMessage().getFunctionLibraryManagementException().getMessage();
            }
        } else {
            errorMessage = e.getMessage();
        }
        throw new AxisFault(errorMessage, e);
    }
}
