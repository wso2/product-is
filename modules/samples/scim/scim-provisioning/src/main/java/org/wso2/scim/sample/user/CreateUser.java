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
package org.wso2.scim.sample.user;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.wso2.charon.core.attributes.DefaultAttributeFactory;
import org.wso2.charon.core.attributes.MultiValuedAttribute;
import org.wso2.charon.core.client.SCIMClient;
import org.wso2.charon.core.exceptions.CharonException;
import org.wso2.charon.core.objects.User;
import org.wso2.charon.core.schema.SCIMConstants;
import org.wso2.charon.core.schema.SCIMSchemaDefinitions;
import org.wso2.scim.sample.utils.SCIMSamplesUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class CreateUser {
    //user details
    private static String externalID = "hasini@wso2.com";
    private static String[] emails = {"hasini@gmail.com"};
    private static String displayName = "Hasini";
    private static String password = "dummyPW1";
    private static String language = "Sinhala";
    private static String phone_number = "0772508354";

    public static void main(String[] args) {

        try {
            //load sample configuration
            SCIMSamplesUtils.loadConfiguration();
            //set the keystore
            SCIMSamplesUtils.setKeyStore();
            //create SCIM client
            SCIMClient scimClient = new SCIMClient();
            //create a user according to SCIM User Schema
            User scimUser = scimClient.createUser();
            scimUser.setUserName(SCIMSamplesUtils.userNameToCreateUser);
            scimUser.setExternalId(externalID);
            scimUser.setEmails(emails);
            scimUser.setDisplayName(displayName);
            scimUser.setPassword(password);
            //scimUser.setPreferredLanguage(language);
            scimUser.setPhoneNumber(phone_number, "work", false);
            scimUser.setFamilyName("Gunasinghe");
            
            //adding a non-ready-made attribute
            Map<String, Object> subs = new HashMap<String, Object>();
            subs.put("display", "ABC User");
            subs.put("value", "00eU0000000EAs4");
            MultiValuedAttribute entAttr = new MultiValuedAttribute("entitlements");
            entAttr.setComplexValue(subs);
            entAttr = (MultiValuedAttribute) DefaultAttributeFactory.createAttribute(
                    SCIMSchemaDefinitions.ENTITLEMENTS, entAttr);
            scimUser.setAttribute(entAttr);
            
            //encode the user in JSON format
            String encodedUser = scimClient.encodeSCIMObject(scimUser, SCIMConstants.JSON);

            System.out.println("");
            System.out.println("");
            System.out.println("/******User to be created in json format: " + encodedUser + "******/");
            System.out.println("");

            PostMethod postMethod = new PostMethod(SCIMSamplesUtils.userEndpointURL);
            //add authorization header
            String authHeader = SCIMSamplesUtils.getAuthorizationHeader();
            postMethod.addRequestHeader(SCIMConstants.AUTHORIZATION_HEADER, authHeader);
            //create request entity with the payload.
            RequestEntity requestEntity = new StringRequestEntity(encodedUser, SCIMSamplesUtils.CONTENT_TYPE, null);
            postMethod.setRequestEntity(requestEntity);

            //create http client
            HttpClient httpClient = new HttpClient();
            //send the request
            int responseStatus = httpClient.executeMethod(postMethod);

            String response = postMethod.getResponseBodyAsString();

            System.out.println("");
            System.out.println("");
            System.out.println("/******SCIM user creation response status: " + responseStatus);
            System.out.println("SCIM user creation response data: " + response + "******/");
            System.out.println("");

        } catch (CharonException e) {
            e.printStackTrace();
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
