/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.integration.common.clients.claim.metadata.mgt;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.ClaimMetadataManagementServiceClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.ClaimMetadataManagementServiceStub;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.ClaimDialectDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.ExternalClaimDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.LocalClaimDTO;
import org.wso2.identity.integration.common.clients.AuthenticateStub;
import org.wso2.identity.integration.common.clients.ClaimManagementServiceClient;

import java.rmi.RemoteException;

/**
 * This class invokes the operations of ClaimMetadataManagementService.
 */
public class ClaimMetadataManagementServiceClient {

    private static Log log = LogFactory.getLog(ClaimManagementServiceClient.class);

    private final String serviceName = "ClaimMetadataManagementService";
    private ClaimMetadataManagementServiceStub claimMetadataManagementServiceStub;
    private String endPoint;

    public ClaimMetadataManagementServiceClient(String backEndUrl, String sessionCookie) throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        claimMetadataManagementServiceStub = new ClaimMetadataManagementServiceStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, claimMetadataManagementServiceStub);
    }

    public ClaimMetadataManagementServiceClient(String backEndUrl, String userName, String password)
            throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        claimMetadataManagementServiceStub = new ClaimMetadataManagementServiceStub(endPoint);
        AuthenticateStub.authenticateStub(userName, password, claimMetadataManagementServiceStub);
    }


    public ClaimDialectDTO[] getClaimDialects() throws RemoteException,
            ClaimMetadataManagementServiceClaimMetadataException {
        try {
            return claimMetadataManagementServiceStub.getClaimDialects();
        } catch (RemoteException e) {
            throw e;
        } catch (ClaimMetadataManagementServiceClaimMetadataException e) {
            throw e;
        }
    }

    public void addClaimDialect(ClaimDialectDTO externalClaimDialect) throws RemoteException,
            ClaimMetadataManagementServiceClaimMetadataException {
        try {
            claimMetadataManagementServiceStub.addClaimDialect(externalClaimDialect);
        } catch (RemoteException e) {
            throw e;
        } catch (ClaimMetadataManagementServiceClaimMetadataException e) {
            throw e;
        }
    }

    public void removeClaimDialect(String externalClaimDialect) throws RemoteException,
            ClaimMetadataManagementServiceClaimMetadataException {
        try {
            ClaimDialectDTO claimDialect = new ClaimDialectDTO();
            claimDialect.setClaimDialectURI(externalClaimDialect);
            claimMetadataManagementServiceStub.removeClaimDialect(claimDialect);
        } catch (RemoteException e) {
            throw e;
        } catch (ClaimMetadataManagementServiceClaimMetadataException e) {
            throw e;
        }
    }


    public LocalClaimDTO[] getLocalClaims() throws RemoteException,
            ClaimMetadataManagementServiceClaimMetadataException {
        try {
            return claimMetadataManagementServiceStub.getLocalClaims();
        } catch (RemoteException e) {
            throw e;
        } catch (ClaimMetadataManagementServiceClaimMetadataException e) {
            throw e;
        }
    }

    public void addLocalClaim(LocalClaimDTO localCLaim) throws RemoteException,
            ClaimMetadataManagementServiceClaimMetadataException {

        try {
            claimMetadataManagementServiceStub.addLocalClaim(localCLaim);
        } catch (RemoteException e) {
            throw e;
        } catch (ClaimMetadataManagementServiceClaimMetadataException e) {
            throw e;
        }
    }

    public void updateLocalClaim(LocalClaimDTO localClaim) throws RemoteException,
            ClaimMetadataManagementServiceClaimMetadataException {

        try {
            claimMetadataManagementServiceStub.updateLocalClaim(localClaim);
        } catch (RemoteException e) {
            throw e;
        } catch (ClaimMetadataManagementServiceClaimMetadataException e) {
            throw e;
        }
    }

    public void removeLocalClaim(String localCLaimURI) throws RemoteException,
            ClaimMetadataManagementServiceClaimMetadataException {

        try {
            claimMetadataManagementServiceStub.removeLocalClaim(localCLaimURI);
        } catch (RemoteException e) {
            throw e;
        } catch (ClaimMetadataManagementServiceClaimMetadataException e) {
            throw e;
        }
    }


    public ExternalClaimDTO[] getExternalClaims(String externalClaimDialectURI) throws RemoteException,
            ClaimMetadataManagementServiceClaimMetadataException {

        try {
            return claimMetadataManagementServiceStub.getExternalClaims(externalClaimDialectURI);
        } catch (RemoteException e) {
            throw e;
        } catch (ClaimMetadataManagementServiceClaimMetadataException e) {
            throw e;
        }
    }

    public void addExternalClaim(ExternalClaimDTO externalClaim) throws RemoteException,
            ClaimMetadataManagementServiceClaimMetadataException {

        try {
            claimMetadataManagementServiceStub.addExternalClaim(externalClaim);
        } catch (RemoteException e) {
            throw e;
        } catch (ClaimMetadataManagementServiceClaimMetadataException e) {
            throw e;
        }
    }

    public void updateExternalClaim(ExternalClaimDTO externalClaim) throws RemoteException,
            ClaimMetadataManagementServiceClaimMetadataException {

        try {
            claimMetadataManagementServiceStub.updateExternalClaim(externalClaim);
        } catch (RemoteException e) {
            throw e;
        } catch (ClaimMetadataManagementServiceClaimMetadataException e) {
            throw e;
        }
    }

    public void removeExternalClaim(String externalClaimDialectURI, String externalClaimURI) throws RemoteException,
            ClaimMetadataManagementServiceClaimMetadataException {

        try {
            claimMetadataManagementServiceStub.removeExternalClaim(externalClaimDialectURI, externalClaimURI);
        } catch (RemoteException e) {
            throw e;
        } catch (ClaimMetadataManagementServiceClaimMetadataException e) {
            throw e;
        }
    }
}
