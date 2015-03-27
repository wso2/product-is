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

package org.wso2.identity.integration.test.scim;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.client.handlers.ClientHandler;
import org.wso2.charon.core.objects.User;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.scim.utils.SCIMResponseHandler;
import org.wso2.charon.core.client.SCIMClient;
import org.wso2.charon.core.exceptions.CharonException;
import org.wso2.charon.core.objects.Group;
import org.wso2.charon.core.schema.SCIMConstants;
import org.wso2.identity.integration.test.utils.BasicAuthHandler;
import org.wso2.identity.integration.test.utils.BasicAuthInfo;

public class MasterSCIMInitiator extends ISIntegrationTest {
    private static final Log log = LogFactory.getLog(MasterSCIMInitiator.class);
    protected static final String userName = "dharshana";
    private static final String externalID = "test";
    private static final String[] emails = {"dkasunw@gmail.com", "dharshanaw@wso2.com"};
    private static final String displayName = "dharshana";
    private static final String password = "testPW";
    private static final String language = "Sinhala";
    private static final String phone_number = "0772202595";
    SCIMClient scimClient;

    public void initTest() throws Exception {
        super.init();
    }

    protected BasicAuthInfo getBasicAuthInfo(org.wso2.carbon.automation.engine.context.beans.User provider_userInfo) {
        BasicAuthInfo basicAuthInfo = new BasicAuthInfo();
        basicAuthInfo.setUserName(provider_userInfo.getUserName());
        basicAuthInfo.setPassword(provider_userInfo.getPassword());

        BasicAuthHandler basicAuthHandler = new BasicAuthHandler();
        return (BasicAuthInfo) basicAuthHandler.getAuthenticationToken(basicAuthInfo);
    }

    protected Resource getResource(SCIMClient scimClient, String skim_url) {
        SCIMResponseHandler responseHandler = new SCIMResponseHandler();
        responseHandler.setSCIMClient(scimClient);
        //set the handler in wink client config
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.handlers(new ClientHandler[]{responseHandler});
        //create a wink rest client with the above config
        RestClient restClient = new RestClient(clientConfig);
        //create resource endpoint to access User resource
        return restClient.resource(skim_url + "Users");
    }

    protected Resource getResource(SCIMClient scimClient, String skim_url, String skimId) {
        SCIMResponseHandler responseHandler = new SCIMResponseHandler();
        responseHandler.setSCIMClient(scimClient);
        //set the handler in wink client config
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.handlers(new ClientHandler[]{responseHandler});
        //create a wink rest client with the above config
        RestClient restClient = new RestClient(clientConfig);
        //create resource endpoint to access User resource
        return restClient.resource(skim_url + "Users/" + skimId);
    }

    protected Resource getGroupResource(SCIMClient scimClient, String skim_url) {
        SCIMResponseHandler responseHandler = new SCIMResponseHandler();
        responseHandler.setSCIMClient(scimClient);
        //set the handler in wink client config
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.handlers(new ClientHandler[]{responseHandler});
        //create a wink rest client with the above config
        RestClient restClient = new RestClient(clientConfig);
        //create resource endpoint to access User resource
        return restClient.resource(skim_url + "Groups");
    }

    protected String getScimUser() throws CharonException {
        //create a user according to SCIM User Schema
        User scimUser = scimClient.createUser();
        scimUser.setUserName(userName);
        scimUser.setExternalId(externalID);
        scimUser.setEmails(emails);
        scimUser.setDisplayName(displayName);
        scimUser.setPassword(password);
        scimUser.setPreferredLanguage(language);
        scimUser.setPhoneNumber(phone_number, null, false);
        //encode the user in JSON format
        return scimClient.encodeSCIMObject(scimUser, SCIMConstants.JSON);
    }

    protected Group getSCIMGroup(String scimUserId,String externalID,String displayName) throws CharonException {
        Group scimGroup = scimClient.createGroup();
        scimGroup.setExternalId(externalID);
        scimGroup.setDisplayName(displayName);
        //set group members
        scimGroup.setMember(scimUserId, userName);
        return scimGroup;

    }

    protected User getScimUserUnEncoded() throws CharonException {
        //create a user according to SCIM User Schema
        User scimUser = scimClient.createUser();
        scimUser.setUserName(userName);
        scimUser.setExternalId(externalID);
        scimUser.setEmails(emails);
        scimUser.setDisplayName(displayName);
        scimUser.setPassword(password);
        scimUser.setPreferredLanguage(language);
        scimUser.setPhoneNumber(phone_number, null, false);
        //encode the user in JSON format
        return scimUser;
    }


}