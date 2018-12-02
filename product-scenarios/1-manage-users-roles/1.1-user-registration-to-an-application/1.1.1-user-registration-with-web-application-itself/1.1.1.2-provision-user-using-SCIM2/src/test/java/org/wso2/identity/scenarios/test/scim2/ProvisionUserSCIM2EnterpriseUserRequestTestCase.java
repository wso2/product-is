/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.scenarios.test.scim2;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;

import org.wso2.identity.scenarios.commons.util.Constants;
import org.wso2.identity.scenarios.commons.util.SCIMProvisioningUtil;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.getJSONFromResponse;


public class ProvisionUserSCIM2EnterpriseUserRequestTestCase extends ScenarioTestBase {

    private CloseableHttpClient client;
    private String userNameResponse;
    private String userId;

    private String HOMEEMAIL = "test@home.com";
    private String PRIMARYSTATE = "true";
    private String SEPERATOR = "/";
    private String WORKEMAIL = "test@work.com";
    private String PROFILE_URL ="https://login.example.com/bjensen";


    HttpResponse response;


    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        setKeyStoreProperties();
        client = HttpClients.createDefault();
        super.init();
    }

    @Test(description = "1.1.2.1.2.12")
    public void testSCIM2CreateEnterpriseUserRequest() throws Exception {

        JSONObject  rootObject = new JSONObject();
        JSONArray schemas = new JSONArray();
        schemas.add(SCIMConstants.ENTERPRISE_SCHEMA);

        rootObject.put(SCIMConstants.SCHEMAS_ATTRIBUTE,schemas);
        rootObject.put(SCIMConstants.ID_ATTRIBUTE,SCIMConstants.ID_ATTRIBUTE_VALUE);
        rootObject.put(SCIMConstants.EXTERNAL_ID_ATTRIBUTE,SCIMConstants.EXTERNAL_ID_ATTRIBUTE_VALUE);
        rootObject.put(SCIMConstants.USER_NAME_ATTRIBUTE, SCIMConstants.USERNAME);
        rootObject.put(SCIMConstants.PASSWORD_ATTRIBUTE, SCIMConstants.PASSWORD);

        JSONObject names = new JSONObject();
        names.put(SCIMConstants.FORMATTED_NAME_ATTRIBUTE,SCIMConstants.FORMATTED_NAME);
        names.put(SCIMConstants.FAMILY_NAME_ATTRIBUTE, SCIMConstants.FAMILY_NAME_CLAIM_VALUE);
        names.put(SCIMConstants.GIVEN_NAME_ATTRIBUTE, SCIMConstants.GIVEN_NAME_CLAIM_VALUE);
        names.put(SCIMConstants.MIDDLE_NAME_ATTRIBUTE,SCIMConstants.MIDDLE_NAME_CLAIM_VALUE);
        names.put(SCIMConstants.HONORIFIC_PREFIX_ATTRIBUTE,SCIMConstants.HONORIFIC_PREFIX_CLAIM_VALUE);
        names.put(SCIMConstants.HONORIFIC_SUFFIX_ATTRIBUTE,SCIMConstants.HONORIFIC_SURFIX_CLAIM_VALUE);

        rootObject.put(SCIMConstants.NAME_ATTRIBUTE, names);

        rootObject.put(SCIMConstants.DISPLAY_NAME,SCIMConstants.DISPLAY_NAME_CLAIM_VALUE);
        rootObject.put(SCIMConstants.NICK_NAME,SCIMConstants.NICK_NAME_CLAIM_VALUE);
        rootObject.put(SCIMConstants.PROFILE_URL,PROFILE_URL);

        JSONObject emailWork = new JSONObject();
        emailWork.put(SCIMConstants.TYPE_PARAM, SCIMConstants.EMAIL_TYPE_WORK_ATTRIBUTE);
        emailWork.put(SCIMConstants.VALUE_PARAM, WORKEMAIL);
        emailWork.put(SCIMConstants.PRIMARY_PARAM, PRIMARYSTATE);

        JSONObject emailHome = new JSONObject();
        emailHome.put(SCIMConstants.TYPE_PARAM, SCIMConstants.EMAIL_TYPE_HOME_ATTRIBUTE);
        emailHome.put(SCIMConstants.VALUE_PARAM, HOMEEMAIL);

        JSONArray emails = new JSONArray();
        emails.add(emailWork);
        emails.add(emailHome);
        rootObject.put(SCIMConstants.EMAILS_ATTRIBUTE, emails);

        JSONObject address = new JSONObject();
        address.put(SCIMConstants.ADDRESS_STREET_ATTRIBUTE,SCIMConstants.ADDRESS_STREET_VALUE);
        address.put(SCIMConstants.ADDRESS_LOCALITY_ATTRIBUTE,SCIMConstants.ADDRESS_LOCALITY_VALUE);
        address.put(SCIMConstants.ADDRESS_REGION_ATTRIBUTE,SCIMConstants.ADDRESS_REGION_VALUE);
        address.put(SCIMConstants.ADDRESS_POSTAL_CODE_ATTRIBUTE,SCIMConstants.ADDRESS_POSTAL_CODE_VALUE);
        address.put(SCIMConstants.ADDRESS_COUNTRY_ATTRIBUTE,SCIMConstants.ADDRESS_COUNTRY_VALUE);
        address.put(SCIMConstants.ADDRESS_FORMATTED_ATTRIBUTE,SCIMConstants.ADDRESS_FORMATTED_VALUE);
        address.put(SCIMConstants.ADDRESS_TYPE_ATTRIBUTE, SCIMConstants.ADDRESS_TYPE_VALUE);
        address.put(SCIMConstants.ADDRESS_PRIMARY_ATTRIBUTE,SCIMConstants.ADDRESS_PRIMARY_VALUE);


        JSONArray phone = new JSONArray();
        phone.add(SCIMConstants.PHONE_NUMBER_VALUE);
        rootObject.put(SCIMConstants.PHONE_NUMBER_ATTRIBUTE, phone);
        rootObject.put(SCIMConstants.PHONE_NUMBER_TYPE_ATTRIBUTE,SCIMConstants.PHONE_NUMBER_TYPE_VALUE);

        JSONArray imsArray = new JSONArray();
        imsArray.add(SCIMConstants.IMS_VALUE);
        rootObject.put(SCIMConstants.IMS_ATTRIBUTE,imsArray);
        rootObject.put(SCIMConstants.IMS_VALUE_ATTRIBUTE,SCIMConstants.IMS_VALUE);
        rootObject.put(SCIMConstants.IMS_TYPE_ATTRIBUTE,SCIMConstants.IMS_TYPE_ATTRIBUTE_VALUE);

       JSONArray photo = new JSONArray();
        phone.add(SCIMConstants.PHOTO_VALUE);
        rootObject.put(SCIMConstants.PHOTO_ATTIBUTE,photo);
        rootObject.put(SCIMConstants.PHOTO_VALUE_ATTRIBUTE,SCIMConstants.PHOTO_VALUE);
        rootObject.put(SCIMConstants.PHOTO_TYPE_ATTRIBUTE,SCIMConstants.PHOTO_TYPE_ATTRIBUTE_VALUE);


        rootObject.put(SCIMConstants.USER_TYPE_ATTRIBUTE,SCIMConstants.USER_TYPE_ATTRIBUTE_VALUE);
        rootObject.put(SCIMConstants.USER_TYPE_TITEL_ATTRIBUTE,SCIMConstants.USER_TYPE_TITLE_ATTRIBUTE_VALUE);
        rootObject.put(SCIMConstants.USER_TYPE_LANGUAGE_ATTRIBUTE,SCIMConstants.USET_TYPE_LANGUAGE_ATTRIBUTE_VALUE);
        rootObject.put(SCIMConstants.USER_TYPE_LOCALE_ATTRIBUTE,SCIMConstants.USER_TYPE_LOCALE_ATTRIBUTE_VALUE);
        rootObject.put(SCIMConstants.USER_TYPE_TIME_ZONE_ATTRIBUTE, SCIMConstants.USER_TYPE_TIME_ZONE_ATTRIBUTE_VALUE);
        rootObject.put(SCIMConstants.USER_TYPE_ACTIVE_ATTRIBUTE,SCIMConstants.USER_TYPE_ACTIVE_ATTRIBUTE_VALUE);
        rootObject.put(SCIMConstants.USER_TYPE_PASSWORD_ATTRIBUTE,SCIMConstants.PASSWORD);


        JSONArray groups1 = new JSONArray();
        groups1.add(SCIMConstants.GROUP_VALUE);
        rootObject.put(SCIMConstants.GROUP_ATTRIBUTE,groups1);

        JSONArray groups2 = new JSONArray();
        groups2.add(SCIMConstants.GROUP_REF_ATTRIBUTE_VALUE);
        rootObject.put(SCIMConstants.GROUP_REF_ATTRIBUTE,groups2);

        JSONArray groups3 = new JSONArray();
        groups3.add(SCIMConstants.GROUP_DISPLAY_ATTRIBUTE_VALUE);
        rootObject.put(SCIMConstants.GROUP_DISPLAY_ATTRIBUTE,groups3);

        rootObject.put(SCIMConstants.EMPLOYEE_NUMBER_ATTRIBUTE,SCIMConstants.EMPLOYEE_NUMBER_ATTRIBUTE_VALUE);
        rootObject.put(SCIMConstants.COST_CENTER_ATTRIBUTE,SCIMConstants.COST_CENTER_ATTRIBUTE_VALUE);
        rootObject.put(SCIMConstants.ORGANIZATION_ATTRIBUTE,SCIMConstants.ORGANIZATION_ATTRIBUTE_VALUE);
        rootObject.put(SCIMConstants.DIVISION_ATTRIBUTE,SCIMConstants.DIVISION_ATTRIBUTE_VALUE);
        rootObject.put(SCIMConstants.DEPARTMENT_ATTRIBUTE,SCIMConstants.DEPARTMENT_ATTRIBUTE_VALUE);

        JSONArray managerId = new JSONArray();
        managerId.add(SCIMConstants.MANAGER_ID_ATTRIBUTE_VALUE);
        rootObject.put(SCIMConstants.MANAGER_ID_ATTRIBUTE,managerId);


        JSONArray managerRef = new JSONArray();
        managerRef.add(SCIMConstants.MANAGER_REF_ATTRIBUTE_VALUE);
        rootObject.put(SCIMConstants.MANAGER_REF_ATTRIBUTE,managerRef);


        JSONArray created = new JSONArray();
        created.add(SCIMConstants.META_CREATED_ATTRIBUTE_VALUE);
        rootObject.put(SCIMConstants.META_CREATED_ATTRIBUTE,created);


        response = SCIMProvisioningUtil.provisionUserSCIM(backendURL, rootObject, Constants.SCIMEndpoints.SCIM2_ENDPOINT, Constants.SCIMEndpoints.SCIM_ENDPOINT_USER, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED, "User has not been created successfully");

        userNameResponse = rootObject.get(SCIMConstants.USER_NAME_ATTRIBUTE).toString();
        assertEquals(userNameResponse, SCIMConstants.USERNAME, "username not found");
   }

    @Test(dependsOnMethods = "testSCIM2CreateEnterpriseUserRequest")
    private void cleanUp() throws Exception {

        JSONObject responseObj = getJSONFromResponse(this.response);
        userId = responseObj.get(SCIMConstants.ID_ATTRIBUTE).toString();
        assertNotNull(userId);

        response = SCIMProvisioningUtil.deleteUser(backendURL, userId, Constants.SCIMEndpoints.SCIM2_ENDPOINT, Constants.SCIMEndpoints.SCIM_ENDPOINT_USER, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NO_CONTENT, "User has not been deleted successfully");
    }
}
