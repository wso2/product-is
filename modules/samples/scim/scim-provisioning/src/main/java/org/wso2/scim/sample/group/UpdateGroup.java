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
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.wso2.charon.core.client.SCIMClient;
import org.wso2.charon.core.exceptions.CharonException;
import org.wso2.charon.core.objects.Group;
import org.wso2.charon.core.objects.ListedResource;
import org.wso2.charon.core.objects.SCIMObject;
import org.wso2.charon.core.schema.SCIMConstants;
import org.wso2.scim.sample.utils.SCIMSamplesUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class UpdateGroup {
    
    public static void main(String[] args) {

        try {
            //load sample configuration
            SCIMSamplesUtils.loadConfiguration();
            //set the keystore
            SCIMSamplesUtils.setKeyStore();
            //create SCIM client
            SCIMClient scimClient = new SCIMClient();
            //create a user according to SCIM User Schema
            Group scimGroup = scimClient.createGroup();
            //scimGroup.setExternalId(SCIMSamplesUtils.groupDisplayNameToCreateGroup);
            scimGroup.setDisplayName(SCIMSamplesUtils.updatedGroupDisplayName);

            //encode the user in JSON format
            String encodedGroup = scimClient.encodeSCIMObject(scimGroup, SCIMConstants.JSON);

            String groupId = getSCIMIdOfGroup(SCIMSamplesUtils.groupDisplayNameToCreateGroup);

            System.out.println("");
            System.out.println("");
            System.out.println("/******Updated Group in json format: " + encodedGroup + "******/");
            System.out.println("");

            String url = SCIMSamplesUtils.groupEndpointURL + "/" + groupId;
            //now send the update request.
            PutMethod putMethod = new PutMethod(url);
            //add authorization header
            String authHeader = SCIMSamplesUtils.getAuthorizationHeader();
            putMethod.addRequestHeader(SCIMConstants.AUTHORIZATION_HEADER, authHeader);

            RequestEntity putRequestEntity = new StringRequestEntity(encodedGroup, SCIMSamplesUtils.CONTENT_TYPE, null);
            putMethod.setRequestEntity(putRequestEntity);

            HttpClient httpUpdateClient = new HttpClient();
            int updateResponseStatus = httpUpdateClient.executeMethod(putMethod);
            String updateResponse = putMethod.getResponseBodyAsString();

            System.out.println("");
            System.out.println("");
            System.out.println("/******SCIM group update response status: " + updateResponseStatus);
            System.out.println("SCIM group update response data: " + updateResponse + "******/");
            System.out.println("");

        } catch (CharonException e) {
            e.printStackTrace();
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getSCIMIdOfGroup(String groupName) throws Exception {
        String groupId = null;
        try {
            //create http client
            HttpClient httpFilterUserClient = new HttpClient();
            //create get method for filtering
            GetMethod getMethod = new GetMethod(SCIMSamplesUtils.groupEndpointURL);
            //add authorization header
            String authHeader = SCIMSamplesUtils.getAuthorizationHeader();
            getMethod.addRequestHeader(SCIMConstants.AUTHORIZATION_HEADER, authHeader);
            //get corresponding userIds
            String filter = SCIMSamplesUtils.GROUP_FILTER + groupName;
            getMethod.setQueryString(filter);
            int responseCode = httpFilterUserClient.executeMethod(getMethod);
            String response = getMethod.getResponseBodyAsString();

            SCIMClient scimClient = new SCIMClient();
            //check for success of the response
            if (scimClient.evaluateResponseStatus(responseCode)) {
                ListedResource listedGroupResource =
                        scimClient.decodeSCIMResponseWithListedResource(
                                response, SCIMConstants.identifyFormat(SCIMSamplesUtils.CONTENT_TYPE),
                                SCIMConstants.GROUP_INT);
                List<SCIMObject> filteredGroups = listedGroupResource.getScimObjects();
                for (SCIMObject filteredGroup : filteredGroups) {
                    //we expect only one result here
                    groupId = ((Group) filteredGroup).getId();
                }

            }
        } catch (IOException e) {
            throw new Exception("Error in obtaining the SCIM Id for user: " + groupName);
        } catch (CharonException e) {
            throw new Exception("Error in obtaining the SCIM Id for user: " + groupName);
        }
        return groupId;
    }
}
