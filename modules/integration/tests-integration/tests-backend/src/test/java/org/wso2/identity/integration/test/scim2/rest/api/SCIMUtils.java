/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.scim2.rest.api;

import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.testng.Assert;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.PropertyDTO;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO;
import org.wso2.identity.integration.common.clients.user.store.config.UserStoreConfigAdminServiceClient;
import org.wso2.identity.integration.common.utils.UserStoreConfigUtils;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Common utils for SCIM related test cases.
 */
public class SCIMUtils {

    private static final String SCIM_USER_SCHEMAS = "[urn:ietf:params:scim:schemas:core:2.0:User, " +
            "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User, urn:scim:wso2:schema, " +
            "urn:scim:schemas:extension:custom:User]";
    private static final UserStoreConfigUtils userStoreConfigUtils = new UserStoreConfigUtils();

    /**
     * Validate the "schema" attribute received in a response for an SCIM operation in Users endpoint.
     * e.g. data type should be an ArrayList of Strings.
     *
     * @param schemasAttribute Value of the "schema" attribute extracted from the SCIM operation response.
     */
    public static void validateSchemasAttributeOfUsersEndpoint(Object schemasAttribute) {

        Assert.assertTrue(schemasAttribute instanceof ArrayList, "'schemas' attribute is not a list of " +
                "strings");
        Assert.assertEquals(((ArrayList) schemasAttribute).size(), 4);
        Assert.assertTrue(StringUtils.equals(SCIM_USER_SCHEMAS, schemasAttribute.toString()));
    }

    /**
     * Validate the "meta" attribute received in a response for an SCIM operation in Users endpoint.
     * e.g. data type should be an ArrayList of LinkedHashMap objects.
     *
     * @param metaAttribute Value of the "meta" attribute extracted from the SCIM operation response.
     * @param response      Response received after performing a SCIM operation.
     * @param endpointURL   SCIM endpoint URL
     */
    public static void validateMetaAttributeOfUsersEndpoint(Object metaAttribute, Response response,
                                                            String endpointURL) {

        Assert.assertTrue(metaAttribute instanceof LinkedHashMap, "'meta' attribute is not a list of " +
                "key-value pairs");
        Assert.assertEquals(((LinkedHashMap) metaAttribute).size(), 4, "'meta' attribute object does" +
                " not have 4 key-value pairs");
        Assert.assertEquals(((String) ((LinkedHashMap) metaAttribute).get("resourceType")), "User",
                "Resource type is not 'User'");
        Assert.assertTrue(((String) ((LinkedHashMap) metaAttribute).get("location")).contains(endpointURL),
                "'location' meta attribute does not contain: " + endpointURL);
        Assert.assertTrue(response.getHeader(HttpHeaders.LOCATION).contains(endpointURL), "'location' " +
                "HTTP header does not contain: " + endpointURL);
        Assert.assertTrue(isValidSCIMDate(((String) ((LinkedHashMap) metaAttribute).get("created"))),
                "Date format of 'created' meta attribute does not comply with SCIM date format");
        Assert.assertTrue(isValidSCIMDate(((String) ((LinkedHashMap) metaAttribute).get("lastModified"))),
                "Date format of 'lastModified' meta attribute does not comply with the SCIM date format");

        /* TODO: 1/20/20 Uncomment the below test after fixing https://github.com/wso2/product-is/issues/7317.
        Assert.assertTrue(StringUtils.equals(((String) ((LinkedHashMap) metaAttribute).get("location")),
                response.getHeader(HttpHeaders.LOCATION)), "'location' meta attribute and 'location' HTTP " +
                "header values are not equal");
         */
    }

    /**
     * Conform whether a given date string adheres to the SCIM standard date format.
     * @param date Target date ing string format.
     * @return true if valid, else false.
     */
    public static boolean isValidSCIMDate(String date) {

        try {
            LocalDateTime.parse(date).toInstant(ZoneOffset.UTC);
        } catch (DateTimeException e) {
            try {
                OffsetDateTime.parse(date);
            } catch (DateTimeException dte) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create a secondary user store.
     *
     * @param userStoreType       User store type.
     * @param userStoreDomain     User store domain.
     * @param userStoreProperties Configuration properties for the user store.
     * @param backendURL          Backend URL of the Identity Server.
     * @param sessionCookie       Session Cookie.
     * @throws Exception Thrown if the user store creation fails.
     */
    public static void createSecondaryUserStore(String userStoreType, String userStoreDomain,
                                                PropertyDTO[] userStoreProperties, String backendURL,
                                                String sessionCookie) throws Exception {

        UserStoreConfigAdminServiceClient userStoreConfigAdminServiceClient =
                new UserStoreConfigAdminServiceClient(backendURL, sessionCookie);
        UserStoreDTO userStoreDTO = userStoreConfigAdminServiceClient.createUserStoreDTO(userStoreType, userStoreDomain,
                userStoreProperties);
        userStoreConfigAdminServiceClient.addUserStore(userStoreDTO);
        Thread.sleep(5000);
        Assert.assertTrue(userStoreConfigUtils.waitForUserStoreDeployment(userStoreConfigAdminServiceClient,
                userStoreDomain), "Domain addition via DTO has failed.");

    }

    /**
     * Delete a secondary user store.
     *
     * @param userStoreDomain User store domain.
     * @param backendURL      Backend URL of the Identity Server.
     * @param sessionCookie   Session Cookie.
     * @throws Exception Thrown if the user store deletion fails.
     */
    public static void deleteSecondaryUserStore(String userStoreDomain, String backendURL, String sessionCookie)
            throws Exception {

        UserStoreConfigAdminServiceClient userStoreConfigAdminServiceClient =
                new UserStoreConfigAdminServiceClient(backendURL, sessionCookie);
        userStoreConfigAdminServiceClient.deleteUserStore(userStoreDomain);
    }
}
