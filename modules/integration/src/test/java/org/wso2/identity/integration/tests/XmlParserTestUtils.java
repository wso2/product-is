/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.tests;

import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.server.admin.stub.ServerAdminStub;
import org.apache.axis2.AxisFault;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;

public class XmlParserTestUtils {

	private static final Log log = LogFactory.getLog(XmlParserTestUtils.class);

	private AuthenticationAdminStub authenticationAdminStub;
	private ServerAdminStub serverAdminStub;
	private final String SERVER_ADMIN_URL = "https://localhost:9443/services/ServerAdmin";
	private final String AUTHENTICATION_ADMIN_URL = "https://localhost:9443/services/AuthenticationAdmin";

	public void initAuthenticatorClient() throws AxisFault {
		if (log.isDebugEnabled()) {
			log.debug("EndPoint" + AUTHENTICATION_ADMIN_URL);
		}
		try {
			authenticationAdminStub = new AuthenticationAdminStub(
					AUTHENTICATION_ADMIN_URL);
		} catch (AxisFault axisFault) {
			log.info("authenticationAdminStub initialization fails");
			throw new AxisFault("authenticationAdminStub initialization fails");
		}
	}

	public void initServerAdminClient(String sessionCookie) throws AxisFault {
		serverAdminStub = new ServerAdminStub(SERVER_ADMIN_URL);
		AuthenticateStub.authenticateStub(sessionCookie, serverAdminStub);
	}

	public String login(String userName, String password, String host)
			throws LoginAuthenticationExceptionException, RemoteException {
		Boolean loginStatus;
		ServiceContext serviceContext;
		String sessionCookie;

		loginStatus = authenticationAdminStub.login(userName, password, host);

		if (!loginStatus) {
			throw new LoginAuthenticationExceptionException(
					"Login Unsuccessful. Return false as a login status by Server");
		}
		log.info("Login Successful");
		serviceContext = authenticationAdminStub._getServiceClient()
				.getLastOperationContext().getServiceContext();
		sessionCookie = (String) serviceContext
				.getProperty(HTTPConstants.COOKIE_STRING);
		if (log.isDebugEnabled()) {
			log.debug("SessionCookie :" + sessionCookie);
		}
		return sessionCookie;
	}

	public void restartGracefully() throws Exception, RemoteException {
		serverAdminStub.restartGracefully();
	}

	public void shutdownGracefully() throws Exception, RemoteException {
		serverAdminStub.shutdownGracefully();
	}

}
