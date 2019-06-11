/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.scenarios.commons.clients.oauth;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceIdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceStub;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.identity.scenarios.commons.util.AuthenticateStubUtil;

import java.rmi.RemoteException;

public class OauthAdminClient {

    private final String serviceName = "OAuthAdminService";
    OAuthAdminServiceStub oauthAdminStub;

    public OauthAdminClient(String backendURL, String sessionCookie) throws AxisFault {

        String endPoint = backendURL + serviceName;
        oauthAdminStub = new OAuthAdminServiceStub(endPoint);
        AuthenticateStubUtil.authenticateStub(sessionCookie, oauthAdminStub);
    }

    public OauthAdminClient(String backendURL, String userName, String password) throws AxisFault {

        String endPoint = backendURL + serviceName;
        oauthAdminStub = new OAuthAdminServiceStub(endPoint);
        AuthenticateStubUtil.authenticateStub(userName, password, oauthAdminStub);
    }

    public void registerOAuthApplicationData(OAuthConsumerAppDTO application) throws Exception {

        oauthAdminStub.registerOAuthApplicationData(application);
    }

    public OAuthConsumerAppDTO[] getAllOAuthApplicationData() throws Exception {

        OAuthConsumerAppDTO[] appDtos = null;
        appDtos = oauthAdminStub.getAllOAuthApplicationData();
        return appDtos;
    }

    public OAuthConsumerAppDTO getOAuthAppByConsumerKey(String consumerKey) throws Exception {

        return oauthAdminStub.getOAuthApplicationData(consumerKey);
    }

    public void updateConsumerApp(OAuthConsumerAppDTO updatedConsumerApp) throws Exception {

        oauthAdminStub.updateConsumerApplication(updatedConsumerApp);
    }

    public void removeOAuthApplicationData(String consumerKey)
            throws RemoteException, OAuthAdminServiceIdentityOAuthAdminException {

        oauthAdminStub.removeOAuthApplicationData(consumerKey);
    }

    public String getOauthApplicationState(String appName) throws Exception {

        OAuthConsumerAppDTO authConsumerAppDTO = oauthAdminStub.getOAuthApplicationDataByAppName(appName);
        return oauthAdminStub.getOauthApplicationState(authConsumerAppDTO.getOauthConsumerKey());
    }

    public void updateConsumerAppState(String appName, String newState) throws Exception {

        OAuthConsumerAppDTO authConsumerAppDTO = oauthAdminStub.getOAuthApplicationDataByAppName(appName);
        oauthAdminStub.updateConsumerAppState(authConsumerAppDTO.getOauthConsumerKey(), newState);
    }

    public void updateOauthSecretKey(String appName) throws Exception {

        OAuthConsumerAppDTO authConsumerAppDTO = oauthAdminStub.getOAuthApplicationDataByAppName(appName);
        oauthAdminStub.updateOauthSecretKey(authConsumerAppDTO.getOauthConsumerKey());
    }

    public OAuthConsumerAppDTO getOAuthAppByName(String applicationName) throws Exception {

        return oauthAdminStub.getOAuthApplicationDataByAppName(applicationName);
    }

    public void updateConsumerApplication(OAuthConsumerAppDTO application) throws Exception {

        oauthAdminStub.updateConsumerApplication(application);
    }

    public void updateScope(String scope, String[] newClaims, String[] deleteClaims) throws Exception {

        oauthAdminStub.updateScope(scope, newClaims, deleteClaims);
    }
}
