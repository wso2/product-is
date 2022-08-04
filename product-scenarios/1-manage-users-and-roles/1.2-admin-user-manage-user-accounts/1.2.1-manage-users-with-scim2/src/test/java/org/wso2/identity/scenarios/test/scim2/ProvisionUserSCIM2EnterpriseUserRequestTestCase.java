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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;
import org.wso2.identity.scenarios.commons.util.Constants;
import org.wso2.identity.scenarios.commons.util.SCIMProvisioningUtil;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.getJSONFromResponse;


public class ProvisionUserSCIM2EnterpriseUserRequestTestCase extends ScenarioTestBase {

    private static final String TEST_USER_NAME = "scim2CreateEnterpriseUserRequest";
    private CloseableHttpClient client;
    private String userNameResponse;
    private String userId;

    private String HOMEEMAIL = "test@home.com";
    private String PRIMARYSTATE = "true";
    private String SEPERATOR = "/";
    private String WORKEMAIL = "test@work.com";
    private String PROFILE_URL ="https://login.example.com/bjensen";

    private String x509CertificateValue = "MIIDQzCCAqygAwIBAgICEAAwDQYJKoZIhvcNAQEFBQAwTjELMAkGA1UEBhMCVVMx\n" +
            "EzARBgNVBAgMCkNhbGlmb3JuaWExFDASBgNVBAoMC2V4YW1wbGUuY29tMRQwEgYD\n" +
            "VQQDDAtleGFtcGxlLmNvbTAeFw0xMTEwMjIwNjI0MzFaFw0xMjEwMDQwNjI0MzFa\n" +
            "MH8xCzAJBgNVBAYTAlVTMRMwEQYDVQQIDApDYWxpZm9ybmlhMRQwEgYDVQQKDAtl\n" +
            "eGFtcGxlLmNvbTEhMB8GA1UEAwwYTXMuIEJhcmJhcmEgSiBKZW5zZW4gSUlJMSIw\n" +
            "IAYJKoZIhvcNAQkBFhNiamVuc2VuQGV4YW1wbGUuY29tMIIBIjANBgkqhkiG9w0B\n" +
            "AQEFAAOCAQ8AMIIBCgKCAQEA7Kr+Dcds/JQ5GwejJFcBIP682X3xpjis56AK02bc\n" +
            "1FLgzdLI8auoR+cC9/Vrh5t66HkQIOdA4unHh0AaZ4xL5PhVbXIPMB5vAPKpzz5i\n" +
            "PSi8xO8SL7I7SDhcBVJhqVqr3HgllEG6UClDdHO7nkLuwXq8HcISKkbT5WFTVfFZ\n" +
            "zidPl8HZ7DhXkZIRtJwBweq4bvm3hM1Os7UQH05ZS6cVDgweKNwdLLrT51ikSQG3\n" +
            "DYrl+ft781UQRIqxgwqCfXEuDiinPh0kkvIi5jivVu1Z9QiwlYEdRbLJ4zJQBmDr\n" +
            "SGTMYn4lRc2HgHO4DqB/bnMVorHB0CC6AV1QoFK4GPe1LwIDAQABo3sweTAJBgNV\n" +
            "HRMEAjAAMCwGCWCGSAGG+EIBDQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZp\n" +
            "Y2F0ZTAdBgNVHQ4EFgQU8pD0U0vsZIsaA16lL8En8bx0F/gwHwYDVR0jBBgwFoAU\n" +
            "dGeKitcaF7gnzsNwDx708kqaVt0wDQYJKoZIhvcNAQEFBQADgYEAA81SsFnOdYJt\n" +
            "Ng5Tcq+/ByEDrBgnusx0jloUhByPMEVkoMZ3J7j1ZgI8rAbOkNngX8+pKfTiDz1R\n" +
            "C4+dx8oU6Za+4NJXUjlL5CvV6BEYb1+QAEJwitTVvxB/A67g42/vzgAtoRUeDov1\n" +
            "GFiBZ+GNF/cAYKcMtGcrs2i97ZkJMo=";

    HttpResponse response;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

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
        rootObject.put(SCIMConstants.USER_NAME_ATTRIBUTE, TEST_USER_NAME);
        rootObject.put(SCIMConstants.PASSWORD_ATTRIBUTE, SCIMConstants.PASSWORD);

        JSONObject names = new JSONObject();
        names.put(SCIMConstants.FORMATTED_NAME_ATTRIBUTE,SCIMConstants.FORMATTED_NAME);
        names.put(SCIMConstants.FAMILY_NAME_ATTRIBUTE, SCIMConstants.FAMILY_NAME_CLAIM_VALUE);
        names.put(SCIMConstants.GIVEN_NAME_ATTRIBUTE, SCIMConstants.GIVEN_NAME_CLAIM_VALUE);

        /* when running with embedded ldap https://github.com/wso2/product-is/issues/3954 error occurs */
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
        photo.add(SCIMConstants.PHOTO_VALUE);
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

        JSONArray cert = new JSONArray();
        cert.add(x509CertificateValue);
        rootObject.put(SCIMConstants.X509_CERTIFICAT_ATTRIBUTE,cert);
        rootObject.put(SCIMConstants.URN_ATTRIBUTE, SCIMConstants.ENTERPRISE_SCHEMA);

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

        rootObject.put(SCIMConstants.MANAGER_DISPLAY_NAME_ATTRIBUTE,SCIMConstants.MANAGER_DISPLAY_NAME_ATTRIBUTE_VALUE);

        JSONArray created = new JSONArray();
        created.add(SCIMConstants.META_CREATED_ATTRIBUTE_VALUE);
        rootObject.put(SCIMConstants.META_CREATED_ATTRIBUTE,created);

        JSONArray lastModified = new JSONArray();
        lastModified.add(SCIMConstants.META_LASTMODIFIED_ATTRIBUTE_VALUE);
        rootObject.put(SCIMConstants.META_LASTMODIFIED_ATTRIBUTE,lastModified);

        JSONArray metaVersion = new JSONArray();
        metaVersion.add(SCIMConstants.META_VERTSION_ATTRIBUTE_VALUE);
        rootObject.put(SCIMConstants.META_VERTSION_ATTRIBUTE,metaVersion);

        JSONArray metaLocation = new JSONArray();
        metaLocation.add(SCIMConstants.META_LOCATION_ATTRIBUTE_VALUE);
        rootObject.put(SCIMConstants.META_LOCATION_ATTRIBUTE,metaLocation);
        
        response = SCIMProvisioningUtil.provisionUserSCIM(backendURL, rootObject, Constants.SCIMEndpoints.SCIM2_ENDPOINT
                , Constants.SCIMEndpoints.SCIM_ENDPOINT_USER, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED, "User has not been " +
                "created successfully");

        userNameResponse = rootObject.get(SCIMConstants.USER_NAME_ATTRIBUTE).toString();
        assertEquals(userNameResponse, TEST_USER_NAME, "username not found");
   }

    @AfterClass(alwaysRun = true)
    private void cleanUp() throws Exception {

        JSONObject responseObj = getJSONFromResponse(this.response);
        userId = responseObj.get(SCIMConstants.ID_ATTRIBUTE).toString();
        assertNotNull(userId);

        response = SCIMProvisioningUtil.deleteUser(backendURL, userId, Constants.SCIMEndpoints.SCIM2_ENDPOINT,
                Constants.SCIMEndpoints.SCIM_ENDPOINT_USER, ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NO_CONTENT, "User has not been " +
                "deleted successfully");
    }
}
