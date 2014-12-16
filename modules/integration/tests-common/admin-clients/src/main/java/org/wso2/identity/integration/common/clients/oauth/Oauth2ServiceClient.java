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
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuthRevocationRequestDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuthRevocationResponseDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2ClientValidationResponseDTO;
import org.wso2.carbon.identity.oauth2.stub.OAuth2ServiceStub;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2AuthorizeReqDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2AuthorizeRespDTO;

public class Oauth2ServiceClient {

	OAuth2ServiceStub oauth2stub;
	private final String serviceName = "OAuth2Service";
	
	public Oauth2ServiceClient(String backendURL, String sessionCookie) throws AxisFault {

        String endPoint = backendURL + serviceName;
        oauth2stub = new OAuth2ServiceStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, oauth2stub);
	}
	
    public Oauth2ServiceClient(String backendURL, String userName, String password)
            throws AxisFault {

        String endPoint = backendURL + serviceName;
        oauth2stub = new OAuth2ServiceStub(endPoint);
        AuthenticateStub.authenticateStub(userName, password, oauth2stub);
    }
    
    public OAuth2AuthorizeRespDTO authorize(OAuth2AuthorizeReqDTO reqDto) throws RemoteException {
    	return oauth2stub.authorize(reqDto);
    }
    
    public OAuth2AccessTokenRespDTO issueAccessToken(OAuth2AccessTokenReqDTO tokenReqDTO) throws RemoteException {
    	return oauth2stub.issueAccessToken(tokenReqDTO);
    }
    
    public OAuth2ClientValidationResponseDTO validateClientInfo(String clientId, String callbackURI) throws RemoteException {
    	return oauth2stub.validateClientInfo(clientId, callbackURI);
    }
    
    public OAuthRevocationResponseDTO revokeTokenByOAuthClient(OAuthRevocationRequestDTO revokeRequestDTO) throws RemoteException {
    	return oauth2stub.revokeTokenByOAuthClient(revokeRequestDTO);
    }
}
