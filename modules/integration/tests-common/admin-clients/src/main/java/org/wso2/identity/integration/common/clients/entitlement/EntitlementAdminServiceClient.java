/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.identity.integration.common.clients.entitlement;

import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.entitlement.stub.dto.PolicyFinderDataHolder;
import org.wso2.carbon.identity.entitlement.stub.dto.PIPFinderDataHolder;
import org.wso2.carbon.identity.entitlement.stub.dto.PDPDataHolder;
import org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceIdentityException;
import org.wso2.carbon.identity.entitlement.stub.EntitlementAdminServiceStub;
import org.wso2.identity.integration.common.clients.AuthenticateStub;

public class EntitlementAdminServiceClient {

    private static final Log log = LogFactory.getLog(EntitlementAdminServiceClient.class);

    private final String serviceName = "EntitlementAdminService";
	private EntitlementAdminServiceStub entitlementAdminServiceStub;
    private String endPoint;

    public EntitlementAdminServiceClient(String backEndUrl, String sessionCookie)
            throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        entitlementAdminServiceStub = new EntitlementAdminServiceStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, entitlementAdminServiceStub);
    }

    public EntitlementAdminServiceClient(String backEndUrl, String userName, String password)
            throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        entitlementAdminServiceStub = new EntitlementAdminServiceStub(endPoint);
        AuthenticateStub.authenticateStub(userName, password, entitlementAdminServiceStub);
    }
    
    public String getGlobalPolicyAlgorithm() throws RemoteException{
    	String algo = null;
    	try {
    		algo = entitlementAdminServiceStub.getGlobalPolicyAlgorithm();
		} catch (EntitlementAdminServiceIdentityException e) {
			e.printStackTrace();
		}
    	return algo;    	
    }
    
    public String doTestRequest(String xacmlRequest) throws RemoteException{
    	String requestStatus = null;
    	try {
			requestStatus = entitlementAdminServiceStub.doTestRequest(xacmlRequest);
		} catch (EntitlementAdminServiceIdentityException e) {
			e.printStackTrace();
		}
    	return requestStatus;
    }
    
    public PDPDataHolder getPDPData() throws RemoteException{
    	PDPDataHolder holder = null;
    	holder = entitlementAdminServiceStub.getPDPData();
    	return holder;
    }
    
    public PIPFinderDataHolder getPIPAttributeFinderData(String finder) throws RemoteException{
    	PIPFinderDataHolder holder = null;
    	holder = entitlementAdminServiceStub.getPIPAttributeFinderData(finder);
    	return holder;
    }
    
    public PIPFinderDataHolder getPIPResourceFinderData(String finder) throws RemoteException{
    	PIPFinderDataHolder holder = null;
    	holder = entitlementAdminServiceStub.getPIPResourceFinderData(finder);
    	return holder;
    }
    
    public PolicyFinderDataHolder getPolicyFinderData(String finder) throws RemoteException{
    	PolicyFinderDataHolder holder = null;
    	holder = entitlementAdminServiceStub.getPolicyFinderData(finder);
    	return holder;    	
    }
    
    public void refreshAttributeFinder(String attributeFinder) throws RemoteException{
    	try {
			entitlementAdminServiceStub.refreshAttributeFinder(attributeFinder);
		} catch (EntitlementAdminServiceIdentityException e) {
			e.printStackTrace();
		}
    }
    
    public void refreshPolicyFinders(String policyFinder) throws RemoteException{
    	try {
			entitlementAdminServiceStub.refreshPolicyFinders(policyFinder);
		} catch (EntitlementAdminServiceIdentityException e) {
			e.printStackTrace();
		}
    }
    
    public void refreshResourceFinder(String resourceFinder) throws RemoteException{
    	try {
			entitlementAdminServiceStub.refreshResourceFinder(resourceFinder);
		} catch (EntitlementAdminServiceIdentityException e) {
			e.printStackTrace();
		}
    }
    
    public void setGlobalPolicyAlgorithm(String policyCombiningAlgorithm) throws RemoteException{
    	try {
			entitlementAdminServiceStub.setGlobalPolicyAlgorithm(policyCombiningAlgorithm);
		} catch (EntitlementAdminServiceIdentityException e) {
			e.printStackTrace();
		}
    }
}
