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

import org.apache.axis2.AxisFault;
import org.wso2.carbon.identity.oauth2.stub.OAuth2TokenValidationServiceStub;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationResponseDTO;
import org.wso2.identity.integration.common.clients.AuthenticateStub;

import java.rmi.RemoteException;

public class Oauth2TokenValidationClient {

    OAuth2TokenValidationServiceStub oAuth2TokenValidationServiceStub;
	private final String serviceName = "OAuth2TokenValidationService";

	public Oauth2TokenValidationClient(String backendURL, String sessionCookie) throws AxisFault {

        String endPoint = backendURL + serviceName;
        oAuth2TokenValidationServiceStub = new OAuth2TokenValidationServiceStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, oAuth2TokenValidationServiceStub);
	}

    public OAuth2TokenValidationResponseDTO validateToken(OAuth2TokenValidationRequestDTO reqDto) throws RemoteException{
        return oAuth2TokenValidationServiceStub.validate(reqDto);
    }

    
  }
