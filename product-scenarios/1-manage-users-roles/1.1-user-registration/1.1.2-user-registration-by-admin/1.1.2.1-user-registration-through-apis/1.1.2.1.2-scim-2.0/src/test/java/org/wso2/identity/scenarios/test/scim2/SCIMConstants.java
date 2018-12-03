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

public class SCIMConstants {

    public static final String SCIM2_USERS_ENDPOINT = "scim2";
    public static final String SCIM2_BULK_USERS_ENDPOINT = "/scim2/Bulk";
    public static final String SCHEMAS_ATTRIBUTE = "schemas";
    public static final String GIVEN_NAME_ATTRIBUTE = "givenName";
    public static final String MIDDLE_NAME_ATTRIBUTE = "middleName";
    public static final String FAMILY_NAME_ATTRIBUTE = "familyName";
    public static final String HONORIFIC_PREFIX_ATTRIBUTE = "honorificPrefix";
    public static final String HONORIFIC_SUFFIX_ATTRIBUTE = "honorificSuffix";
    public static final String EMAILS_ATTRIBUTE = "emails";
    public static final String EMAIL_TYPE_WORK_ATTRIBUTE = "work";
    public static final String EMAIL_TYPE_HOME_ATTRIBUTE = "home";
    public static final String TYPE_PARAM = "type";
    public static final String VALUE_PARAM = "value";
    public static final String PRIMARY_PARAM = "primary";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String USER_NAME_ATTRIBUTE = "userName";
    public static final String PASSWORD_ATTRIBUTE = "password";
    public static final String ID_ATTRIBUTE = "id";
    public static final String ID_ATTRIBUTE_VALUE = "2819c223-7f76-453a-919d-413861904646";
    public static final String EXTERNAL_ID_ATTRIBUTE ="externalId";
    public static final String EXTERNAL_ID_ATTRIBUTE_VALUE = "701984";
    public static final String FAMILY_NAME_CLAIM_VALUE = "scim2";
    public static final String HONORIFIC_PREFIX_CLAIM_VALUE = "Mr";
    public static final String HONORIFIC_SURFIX_CLAIM_VALUE = "III";
    public static final String USERNAME = "scim2user";
    public static final String PASSWORD = "scim2pwd";
    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    public static final String GIVEN_NAME_CLAIM_VALUE = "user1";
    public static final String MIDDLE_NAME_CLAIM_VALUE = "user1";
    public static final String SCIM2USER = "scim2user";
    public static final String SCIM2PASSWORD = "scim2pwd";
    public static final String DISPLAY_NAME = "displayName";
    public static final String DISPLAY_NAME_CLAIM_VALUE = "III";
    public static final String NICK_NAME = "nickName";
    public static final String NICK_NAME_CLAIM_VALUE = "nickname";
    public static final String PROFILE_URL = "profileUrl";
    public static final String DISPLAY = "display";
    public static final String MEMBERS = "members";
    public static final String VALUE = "value";
    public static final String ROLE_NAME = "TestRole";
    public static final String ROLE_NAME_ATTRIBUTE = "roles";
    public static final String FORMATTED_NAME_ATTRIBUTE ="formatted";
    public static final String FORMATTED_NAME = "Ms. Barbara J Jensen III";
    public static final String ADDRESS_STREET_ATTRIBUTE = "streetAddress";
    public static final String ADDRESS_STREET_VALUE = "100 Universal City Plaza";
    public static final String ADDRESS_LOCALITY_ATTRIBUTE = "locality";
    public static final String ADDRESS_LOCALITY_VALUE ="Hollywood";
    public static final String ADDRESS_REGION_ATTRIBUTE ="region";
    public static final String ADDRESS_REGION_VALUE = "CA";
    public static final String ADDRESS_POSTAL_CODE_ATTRIBUTE ="postalCode";
    public static final String ADDRESS_POSTAL_CODE_VALUE = "91608";
    public static final String ADDRESS_COUNTRY_ATTRIBUTE = "country";
    public static final String ADDRESS_COUNTRY_VALUE = "USA";
    public static final String ADDRESS_FORMATTED_ATTRIBUTE ="formatted";
    public static final String ADDRESS_FORMATTED_VALUE = "100 Universal City Plaza\\nHollywood, CA 91608 USA";
    public static final String ADDRESS_TYPE_ATTRIBUTE = "type";
    public static final String ADDRESS_TYPE_VALUE = "work";
    public static final String ADDRESS_PRIMARY_ATTRIBUTE = "primary";
    public static final String ADDRESS_PRIMARY_VALUE = "true";
    public static final String PHONE_NUMBER_ATTRIBUTE = "phoneNumbers";
    public static final String PHONE_NUMBER_VALUE = "555-555-5555";
    public static final String PHONE_NUMBER_TYPE_ATTRIBUTE ="type";
    public static final String PHONE_NUMBER_TYPE_VALUE = "work";
    public static final String IMS_ATTRIBUTE = "ims";
    public static final String IMS_VALUE_ATTRIBUTE = "value";
    public static final String IMS_VALUE = "someaimhandle";
    public static final String IMS_TYPE_ATTRIBUTE = "type";
    public static final String IMS_TYPE_ATTRIBUTE_VALUE = "aim";
    public static final String PHOTO_ATTIBUTE = "photos";
    public static final String PHOTO_VALUE_ATTRIBUTE = "value";
    public static final String PHOTO_VALUE ="https://photos.example.com/profilephoto/72930000000Ccne/F";
    public static final String PHOTO_TYPE_ATTRIBUTE = "type";
    public static final String PHOTO_TYPE_ATTRIBUTE_VALUE ="photo";
    public static final String USER_TYPE_ATTRIBUTE = "userType";
    public static final String USER_TYPE_ATTRIBUTE_VALUE = "Employee";
    public static final String USER_TYPE_TITEL_ATTRIBUTE = "title";
    public static final String USER_TYPE_TITLE_ATTRIBUTE_VALUE = "Tour Guide";
    public static final String USER_TYPE_LANGUAGE_ATTRIBUTE = "preferredLanguage";
    public static final String USET_TYPE_LANGUAGE_ATTRIBUTE_VALUE = "en_US";
    public static final String USER_TYPE_LOCALE_ATTRIBUTE = "locale";
    public static final String USER_TYPE_LOCALE_ATTRIBUTE_VALUE = "en_US";
    public static final String USER_TYPE_TIME_ZONE_ATTRIBUTE = "timezone";
    public static final String USER_TYPE_TIME_ZONE_ATTRIBUTE_VALUE = "America/Los_Angeles";
    public static final String USER_TYPE_ACTIVE_ATTRIBUTE = "active";
    public static final String USER_TYPE_ACTIVE_ATTRIBUTE_VALUE = "true";
    public static final String USER_TYPE_PASSWORD_ATTRIBUTE  = "password";

    public static final String GROUP_ATTRIBUTE = "groups";
    public static final String GROUP_VALUE_ATTRIBUTE = "value";
    public static final String GROUP_VALUE = "e9e30dba-f08f-4109-8486-d5c6a331660a";
    public static final String GROUP_REF_ATTRIBUTE = "$ref";
    public static final String GROUP_REF_ATTRIBUTE_VALUE = "/Groups/e9e30dba-f08f-4109-8486-d5c6a331660a";
    public static final String GROUP_DISPLAY_ATTRIBUTE = "display";
    public static final String GROUP_DISPLAY_ATTRIBUTE_VALUE ="Tour Guides";

    public static final String EMPLOYEE_NUMBER_ATTRIBUTE = "employeeNumber";
    public static final String EMPLOYEE_NUMBER_ATTRIBUTE_VALUE ="701984";
    public static final String COST_CENTER_ATTRIBUTE = "costCenter";
    public static final String COST_CENTER_ATTRIBUTE_VALUE = "4130";
    public static final String ORGANIZATION_ATTRIBUTE = "organization";
    public static final String ORGANIZATION_ATTRIBUTE_VALUE = "Universal Studios";
    public static final String DIVISION_ATTRIBUTE = "division";
    public static final String DIVISION_ATTRIBUTE_VALUE = "Theme Park";
    public static final String DEPARTMENT_ATTRIBUTE = "department";
    public static final String DEPARTMENT_ATTRIBUTE_VALUE = "Tour Operations";

    public static final String X509_CERTIFICAT_ATTRIBUTE = "x509Certificates";
    public static final String URN_ATTRIBUTE = "urn";

    public static final String MANAGER_ATTRIBUTE = "manager";
    public static final String MANAGER_ID_ATTRIBUTE = "managerId";
    public static final String MANAGER_ID_ATTRIBUTE_VALUE = "26118915-6090-4610-87e4-49d8ca9f808d";
    public static final String MANAGER_REF_ATTRIBUTE = "$ref";
    public static final String MANAGER_REF_ATTRIBUTE_VALUE = "/Users/26118915-6090-4610-87e4-49d8ca9f808d";
    public static final String MANAGER_DISPLAY_NAME_ATTRIBUTE = "displayName";
    public static final String MANAGER_DISPLAY_NAME_ATTRIBUTE_VALUE = "John Smith";

    public static final String META_ATTRIBUTE = "meta";
    public static final String META_CREATED_ATTRIBUTE = "created";
    public static final String META_CREATED_ATTRIBUTE_VALUE = "2010-01-23T04:56:22Z";
    public static final String META_LASTMODIFIED_ATTRIBUTE = "lastModified";
    public static final String META_LASTMODIFIED_ATTRIBUTE_VALUE = "2011-05-13T04:42:34Z";
    public static final String META_VERTSION_ATTRIBUTE = "version";
    public static final String META_VERTSION_ATTRIBUTE_VALUE = "W\\//\"3694e05e9dff591\"";
    public static final String META_LOCATION_ATTRIBUTE = "location";
    public static final String META_LOCATION_ATTRIBUTE_VALUE = "https://example.com/v1/Users/2819c223-7f76-453a-919d-413861904646";


    public static final String ERROR_SCHEMA = "urn:ietf:params:scim:api:messages:2.0:Error";
    public static final String ENTERPRISE_SCHEMA = "urn:scim:schemas:extension:enterprise:1.0";
}
