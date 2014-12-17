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
package org.wso2.identity.integration.common.clients.oauth;

import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceException;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceStub;
import org.wso2.identity.integration.common.clients.AuthenticateStub;

public class OauthAdminClient {

	OAuthAdminServiceStub oauthAdminStub;
	private final String serviceName = "OAuthAdminService";
	
	public OauthAdminClient(String backendURL, String sessionCookie) throws AxisFault {

        String endPoint = backendURL + serviceName;
        oauthAdminStub = new OAuthAdminServiceStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, oauthAdminStub);
	}
	
    public OauthAdminClient(String backendURL, String userName, String password)
            throws AxisFault {

        String endPoint = backendURL + serviceName;
        oauthAdminStub = new OAuthAdminServiceStub(endPoint);
        AuthenticateStub.authenticateStub(userName, password, oauthAdminStub);        
    }
    
    public void registerOAuthApplicationData(OAuthConsumerAppDTO application) throws RemoteException, OAuthAdminServiceException {
    	oauthAdminStub.registerOAuthApplicationData(application);
    }
    
    public OAuthConsumerAppDTO[] getAllOAuthApplicationData() throws RemoteException, OAuthAdminServiceException {
    	
    	OAuthConsumerAppDTO[] appDtos = null;
    	appDtos = oauthAdminStub.getAllOAuthApplicationData();
    	return appDtos;
    }
    
    public void removeOAuthApplicationData(String consumerKey) throws RemoteException, OAuthAdminServiceException{
    	oauthAdminStub.removeOAuthApplicationData(consumerKey);
    }
}
