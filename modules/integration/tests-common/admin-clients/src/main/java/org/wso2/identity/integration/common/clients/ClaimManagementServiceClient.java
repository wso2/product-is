/*
*  Licensed to the Apache Software Foundation (ASF) under one
*  or more contributor license agreements.  See the NOTICE file
*  distributed with this work for additional information
*  regarding copyright ownership.  The ASF licenses this file
*  to you under the Apache License, Version 2.0 (the
*  "License"); you may not use this file except in compliance
*  with the License.  You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.identity.integration.common.clients;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;
import org.wso2.carbon.claim.mgt.stub.ClaimManagementServiceException;
import org.wso2.carbon.claim.mgt.stub.ClaimManagementServiceStub;
import org.wso2.carbon.claim.mgt.stub.dto.ClaimDTO;
import org.wso2.carbon.claim.mgt.stub.dto.ClaimMappingDTO;

import java.rmi.RemoteException;

public class ClaimManagementServiceClient {

    private static Log log = LogFactory.getLog(ClaimManagementServiceClient.class);

    private final String serviceName = "ClaimManagementService";
    private ClaimManagementServiceStub claimManagementServiceStub;
    private String endPoint;

    public ClaimManagementServiceClient(String backEndUrl, String sessionCookie) throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        claimManagementServiceStub = new ClaimManagementServiceStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, claimManagementServiceStub);
    }

    public ClaimManagementServiceClient(String backEndUrl, String userName, String password)
            throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        claimManagementServiceStub = new ClaimManagementServiceStub(endPoint);
        AuthenticateStub.authenticateStub(userName, password, claimManagementServiceStub);
    }

    public void addNewClaimMapping(String dialectURI, String claimUri, String description, String mappedAttribute)
            throws ClaimManagementServiceException, RemoteException {
        ClaimMappingDTO claimMappingDTO = new ClaimMappingDTO();

        ClaimDTO claimDTO = new ClaimDTO();
        claimDTO.setDialectURI(dialectURI);
        claimDTO.setClaimUri(claimUri);
        claimDTO.setDescription(description);

        claimMappingDTO.setClaim(claimDTO);
        claimMappingDTO.setMappedAttribute(mappedAttribute);
        claimManagementServiceStub.addNewClaimMapping(claimMappingDTO);


    }

    //TODO Add other methods in this admin service

}
