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

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class SCIMUtils {

    private static final String SCIM_USER_SCHEMAS = "[urn:ietf:params:scim:schemas:core:2.0:User, " +
            "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User]";

    public static void validateSchemasAttribute(Object schemasAttribute) {

        Assert.assertTrue(schemasAttribute instanceof ArrayList, "'schemas' attribute is not a list of " +
                "strings");
        Assert.assertEquals(((ArrayList) schemasAttribute).size(), 2);
        Assert.assertTrue(StringUtils.equals(SCIM_USER_SCHEMAS, schemasAttribute.toString()));
    }

    public static void validateMetaAttribute(Object metaAttribute, Response response, String endpointURL) {

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
}
