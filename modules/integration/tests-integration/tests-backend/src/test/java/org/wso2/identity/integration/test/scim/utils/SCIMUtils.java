/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.scim.utils;

import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.client.handlers.ClientHandler;
import org.wso2.charon.core.client.SCIMClient;
import org.wso2.charon.core.exceptions.CharonException;
import org.wso2.charon.core.objects.Group;
import org.wso2.charon.core.objects.User;
import org.wso2.charon.core.schema.SCIMConstants;
import org.wso2.identity.integration.test.utils.BasicAuthHandler;
import org.wso2.identity.integration.test.utils.BasicAuthInfo;

/**
 * This class defines utility methods for SCIM Test Cases.
 */
public class SCIMUtils {
    /**
     * Construct and return basic authentication information of the given user.
     *
     * @param userInfo user representation
     * @return         BasicAuthInfo instance containing user authentication information
     */
    public static BasicAuthInfo getBasicAuthInfo(org.wso2.carbon.automation.engine.context.beans.User userInfo) {

        BasicAuthInfo basicAuthInfo = new BasicAuthInfo();
        basicAuthInfo.setUserName(userInfo.getUserName());
        basicAuthInfo.setPassword(userInfo.getPassword());

        BasicAuthHandler basicAuthHandler = new BasicAuthHandler();
        return (BasicAuthInfo) basicAuthHandler.getAuthenticationToken(basicAuthInfo);
    }

    /**
     * Returns the UserResource REST access point of SCIM endpoint.
     *
     * @param scimClient SCIMClient instance
     * @param scimUrl    SCIM base endpoint URL
     * @return           UserResource REST access point client instance
     */
    public static Resource getUserResource(SCIMClient scimClient, String scimUrl) {

        SCIMResponseHandler responseHandler = new SCIMResponseHandler();
        responseHandler.setSCIMClient(scimClient);
        //set the handler in wink client config
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.handlers(new ClientHandler[] { responseHandler });
        //create a wink rest client with the above config
        RestClient restClient = new RestClient(clientConfig);
        //create resource endpoint to access User resource
        return restClient.resource(scimUrl + "Users");
    }

    /**
     * Returns the UserResource REST access point of SCIM endpoint for given user Id.
     *
     * @param scimClient SCIMClient instance
     * @param scimUrl    SCIM base endpoint URL
     * @param scimId     SCIM ID of the user resource
     * @return           UserResource REST access point client instance
     */
    public static Resource getUserResource(SCIMClient scimClient, String scimUrl, String scimId) {

        SCIMResponseHandler responseHandler = new SCIMResponseHandler();
        responseHandler.setSCIMClient(scimClient);
        //set the handler in wink client config
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.handlers(new ClientHandler[] { responseHandler });
        //create a wink rest client with the above config
        RestClient restClient = new RestClient(clientConfig);
        //create resource endpoint to access User resource
        return restClient.resource(scimUrl + "Users/" + scimId);
    }

    /**
     * Returns the GroupResource REST access point of SCIM endpoint.
     *
     * @param scimClient SCIMClient instance
     * @param scimUrl    SCIM base endpoint URL
     * @return           GroupResource REST access point client instance
     */
    public static Resource getGroupResource(SCIMClient scimClient, String scimUrl) {

        SCIMResponseHandler responseHandler = new SCIMResponseHandler();
        responseHandler.setSCIMClient(scimClient);
        //set the handler in wink client config
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.handlers(new ClientHandler[] { responseHandler });
        //create a wink rest client with the above config
        RestClient restClient = new RestClient(clientConfig);
        //create resource endpoint to access User resource
        return restClient.resource(scimUrl + "Groups");
    }

    /**
     * Constructs a User instance according to the SCIM Schema and returns the encoded JSON string of User
     * representation.
     *
     * @param scimClient    SCIMClient instance
     * @param username      Username
     * @param externalID    External ID of the user
     * @param emails        String array of emails
     * @param displayName   Display Name of the User
     * @param password      Password of the User
     * @param language      Language
     * @param phoneNumber   Phone No
     * @return              JSON String representation of the User
     * @throws CharonException
     */
    public static String getEncodedSCIMUser(SCIMClient scimClient, String username, String externalID, String[] emails,
                                            String displayName, String password, String language, String phoneNumber)
            throws CharonException {

        return scimClient.encodeSCIMObject(getSCIMUser(scimClient, username, externalID, emails, displayName,
                                                       password, language, phoneNumber), SCIMConstants.JSON);
    }

    /**
     * Constructs and returns a Group instance according to the SCIM Schema.
     *
     * @param scimClient    SCIMClient instance
     * @param scimUserId    SCIM User Id
     * @param username      Username
     * @param externalID    External ID of the Group
     * @param displayName   Display Name of the Group
     * @return              Group instance
     * @throws CharonException
     */
    public static Group getSCIMGroup(SCIMClient scimClient, String scimUserId, String username, String externalID,
                                     String displayName) throws CharonException {
        Group scimGroup = scimClient.createGroup();
        scimGroup.setExternalId(externalID);
        scimGroup.setDisplayName(displayName);
        //set group members
        scimGroup.setMember(scimUserId, username);
        return scimGroup;

    }

    /**
     * Constructs and returns a User instance according to the SCIM Schema.
     *
     * @param scimClient    SCIMClient instance
     * @param username      Username
     * @param externalID    External ID of the user
     * @param emails        String array of emails
     * @param displayName   Display Name of the User
     * @param password      Password of the User
     * @param language      Language
     * @param phoneNumber   Phone No
     * @return              User instance
     * @throws CharonException
     */
    public static User getSCIMUser(SCIMClient scimClient, String username, String externalID, String[] emails,
                                   String displayName, String password, String language, String phoneNumber)
            throws CharonException {
        //create a user according to SCIM User Schema
        User scimUser = scimClient.createUser();
        scimUser.setUserName(username);
        scimUser.setExternalId(externalID);
        scimUser.setEmails(emails);
        scimUser.setDisplayName(displayName);
        scimUser.setPassword(password);
        scimUser.setPreferredLanguage(language);
        scimUser.setPhoneNumber(phoneNumber, null, false);
        return scimUser;
    }

}
