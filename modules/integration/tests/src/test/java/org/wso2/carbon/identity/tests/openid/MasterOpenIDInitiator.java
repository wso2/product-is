/*
 *Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */
package org.wso2.carbon.identity.tests.openid;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.automation.api.clients.identity.openid.OpenIDProviderServiceClient;
import org.wso2.carbon.automation.api.clients.identity.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentVariables;

public class MasterOpenIDInitiator {

    protected EnvironmentVariables isEnvironment = null;
    protected UserInfo userInfo = null;
    protected OpenIDProviderServiceClient openidServiceClient = null;
    protected RemoteUserStoreManagerServiceClient remoteUSMServiceClient = null;

    protected void init(int userId) throws AxisFault {
        EnvironmentBuilder builder = new EnvironmentBuilder();
        isEnvironment = builder.build().getIs();
        userInfo = UserListCsvReader.getUserInfo(userId);
        String backEndUrl = isEnvironment.getBackEndUrl();
        String sessionCookie = isEnvironment.getSessionCookie();
        openidServiceClient = new OpenIDProviderServiceClient(backEndUrl, sessionCookie);
        remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backEndUrl, sessionCookie);
    }
}