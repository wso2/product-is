/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.sample.identity.oauth2;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.identity.oauth2.stub.OAuth2TokenValidationServiceStub;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationResponseDTO;
import org.wso2.carbon.utils.CarbonUtils;

public class OAuth2ServiceClient {

	private OAuth2TokenValidationServiceStub stub;

	private static final int TIMEOUT_IN_MILLIS = 15 * 60 * 1000;

	/**
	 * Instantiates OAuth2TokenValidationService
	 *
	 * @throws org.apache.axis2.AxisFault
	 */
	public OAuth2ServiceClient() throws AxisFault {
		String serviceURL = OAuth2ClientServlet.serverUrl + "OAuth2TokenValidationService";
		stub = new OAuth2TokenValidationServiceStub(null, serviceURL);
		CarbonUtils.setBasicAccessSecurityHeaders(OAuth2ClientServlet.userName, OAuth2ClientServlet.password, true, stub._getServiceClient());
		ServiceClient client = stub._getServiceClient();
		Options options = client.getOptions();
		options.setTimeOutInMilliSeconds(TIMEOUT_IN_MILLIS);
		options.setProperty(HTTPConstants.SO_TIMEOUT, TIMEOUT_IN_MILLIS);
		options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, TIMEOUT_IN_MILLIS);
		options.setCallTransportCleanup(true);
		options.setManageSession(true);
	}

	/**
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public OAuth2TokenValidationResponseDTO validateAuthenticationRequest(OAuth2TokenValidationRequestDTO params)
			throws Exception {
		OAuth2TokenValidationResponseDTO resp = stub.validate(params);
		return resp;
	}
}
