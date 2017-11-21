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
package org.wso2.scim.sample.group;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.wso2.charon.core.client.SCIMClient;
import org.wso2.charon.core.exceptions.CharonException;
import org.wso2.charon.core.objects.Group;
import org.wso2.charon.core.objects.ListedResource;
import org.wso2.charon.core.objects.SCIMObject;
import org.wso2.charon.core.objects.User;
import org.wso2.charon.core.schema.SCIMConstants;
import org.wso2.scim.sample.utils.SCIMSamplesUtils;

import java.io.IOException;
import java.util.List;

public class CreateGroup {
    //user details
    private static String userName = "HasiniG";
    

    public static void main(String[] args) {

        try {
            //load sample configuration
            SCIMSamplesUtils.loadConfiguration();
            //set the keystore
            SCIMSamplesUtils.setKeyStore();
            //create SCIM client
            SCIMClient scimClient = new SCIMClient();
            //create a group according to SCIM Group Schema
            Group scimGroup = scimClient.createGroup();
            scimGroup.setExternalId(SCIMSamplesUtils.groupDisplayNameToCreateGroup);
            scimGroup.setDisplayName(SCIMSamplesUtils.groupDisplayNameToCreateGroup);
            /************Uncomment the following if you want to add members to group*************/
            //set group members
            /*for (String member : members) {
                scimGroup.setMember(member);
            }*/
            String userId = getSCIMIdOfUser(userName);
            scimGroup.setMember(userId);
            //encode the group in JSON format
            String encodedGroup = scimClient.encodeSCIMObject(scimGroup, SCIMConstants.JSON);

            System.out.println("");
            System.out.println("");
            System.out.println("/******Group to be created in json format: " + encodedGroup + "******/");
            System.out.println("");


            PostMethod postMethod = new PostMethod(SCIMSamplesUtils.groupEndpointURL);
            //add authorization header
            String authHeader = SCIMSamplesUtils.getAuthorizationHeader();
            postMethod.addRequestHeader(SCIMConstants.AUTHORIZATION_HEADER, authHeader);
            //create request entity with the payload.
            RequestEntity requestEntity = new StringRequestEntity(encodedGroup, SCIMSamplesUtils.CONTENT_TYPE, null);
            postMethod.setRequestEntity(requestEntity);

            //create http client
            HttpClient httpClient = new HttpClient();
            //send the request
            int responseStatus = httpClient.executeMethod(postMethod);

            String response = postMethod.getResponseBodyAsString();

            System.out.println("");
            System.out.println("");
            System.out.println("/******SCIM group creation response status: " + responseStatus);
            System.out.println("SCIM group creation response data: " + response + "******/");
            System.out.println("");


        } catch (CharonException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getSCIMIdOfUser(String userName) throws Exception {
        String userId = null;
        try {
            //create http client
            HttpClient httpFilterUserClient = new HttpClient();
            //create get method for filtering
            GetMethod getMethod = new GetMethod(SCIMSamplesUtils.userEndpointURL);
            //add authorization header
            String authHeader = SCIMSamplesUtils.getAuthorizationHeader();
            getMethod.addRequestHeader(SCIMConstants.AUTHORIZATION_HEADER, authHeader);
            //get corresponding userIds
            String filter = SCIMSamplesUtils.USER_FILTER + userName;
            getMethod.setQueryString(filter);
            int responseCode = httpFilterUserClient.executeMethod(getMethod);
            String response = getMethod.getResponseBodyAsString();

            SCIMClient scimClient = new SCIMClient();
            //check for success of the response
            if (scimClient.evaluateResponseStatus(responseCode)) {
                ListedResource listedUserResource =
                        scimClient.decodeSCIMResponseWithListedResource(
                                response, SCIMConstants.identifyFormat(SCIMSamplesUtils.CONTENT_TYPE),
                                SCIMConstants.USER_INT);
                List<SCIMObject> filteredUsers = listedUserResource.getScimObjects();
                for (SCIMObject filteredUser : filteredUsers) {
                    //we expect only one result here
                    userId = ((User) filteredUser).getId();
                }

            }
        } catch (IOException e) {
            throw new Exception("Error in obtaining the SCIM Id for user: " + userName);
        } catch (CharonException e) {
            throw new Exception("Error in obtaining the SCIM Id for user: " + userName);
        }
        return userId;
    }
}
