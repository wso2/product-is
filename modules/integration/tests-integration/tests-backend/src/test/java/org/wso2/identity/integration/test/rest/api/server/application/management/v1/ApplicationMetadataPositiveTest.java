/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.integration.test.rest.api.server.application.management.v1;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthProtocolMetadata;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OIDCMetaData;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAMLMetaData;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.WSTrustMetaData;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Tests for happy paths of the Application Metadata REST API.
 */
public class ApplicationMetadataPositiveTest extends ApplicationManagementBaseTest {

    private static final String INBOUND_PROTOCOLS_PATH = "inbound-protocols";
    private static final String OIDC_PATH = "oidc";
    private static final String SAML_PATH = "saml";
    private static final String WS_TRUST_PATH = "ws-trust";
    private static final String ADAPTIVE_AUTH_PATH = "adaptive-auth-templates";
    public static final String JSON_KEY_TEMPLATES_JSON = "templatesJSON";
    public static final String JSON_KEY_TEMPLATES = "templates";

    private List<AuthProtocolMetadata> allInboundProtocolsResponse;
    private OIDCMetaData oidcMetaData;
    private SAMLMetaData samlMetaDataSuperTenant;
    private SAMLMetaData samlMetaDataTenant;
    private WSTrustMetaData wsTrustMetaDataSuperTenant;
    private WSTrustMetaData wsTrustMetaDataTenant;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ApplicationMetadataPositiveTest(TestUserMode userMode) throws Exception {

        super(userMode);
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws IOException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);

        // Init getAllEmailTemplateTypes method response
        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        String expectedResponse = readResource("all-inbound-protocols-response.json");
        allInboundProtocolsResponse =
                Arrays.asList(jsonWriter.readValue(expectedResponse, AuthProtocolMetadata[].class));

        // Init OIDC Metadata
        expectedResponse = readResource("oidc-metadata.json");
        oidcMetaData = jsonWriter.readValue(expectedResponse, OIDCMetaData.class);

        // Init SAML Metadata
        expectedResponse = readResource("saml-metadata-super-tenant.json");
        samlMetaDataSuperTenant = jsonWriter.readValue(expectedResponse, SAMLMetaData.class);
        expectedResponse = readResource("saml-metadata-tenant.json");
        samlMetaDataTenant = jsonWriter.readValue(expectedResponse, SAMLMetaData.class);

        // Init WS Trust Metadata
        expectedResponse = readResource("ws-trust-metadata-super-tenant.json");
        wsTrustMetaDataSuperTenant = jsonWriter.readValue(expectedResponse, WSTrustMetaData.class);
        expectedResponse = readResource("ws-trust-metadata-tenant.json");
        wsTrustMetaDataTenant = jsonWriter.readValue(expectedResponse, WSTrustMetaData.class);
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() {

        super.conclude();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN}, {TestUserMode.TENANT_ADMIN}};
    }

    @Test
    public void testGetAllInboundProtocols() throws IOException {

        Response response = getResponseOfGet(METADATA_API_BASE_PATH + PATH_SEPARATOR + INBOUND_PROTOCOLS_PATH);
        response.then()
                .log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        List<AuthProtocolMetadata> responseFound =
                Arrays.asList(jsonWriter.readValue(response.asString(), AuthProtocolMetadata[].class));
        Assert.assertEquals(responseFound, allInboundProtocolsResponse,
                "Response of the get all inbound protocols doesn't match.");
    }

     @Test
     public void testGetOIDCMetadata() throws IOException {

         Response response = getResponseOfGet(METADATA_API_BASE_PATH +
                 PATH_SEPARATOR + INBOUND_PROTOCOLS_PATH +
                 PATH_SEPARATOR + OIDC_PATH);
         response.then()
                 .log()
                 .ifValidationFails()
                 .assertThat()
                 .statusCode(HttpStatus.SC_OK);
         ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
         OIDCMetaData responseFound = jsonWriter.readValue(response.asString(), OIDCMetaData.class);
         System.out.println("=======");
         System.out.println(responseFound.toString());
         System.out.println("=======");
         Assert.assertEquals(responseFound.toString(), oidcMetaData.toString(),
                 "OIDC Metadata returned from the API doesn't match.");
     }

    @Test
    public void testGetSAMLMetadata() throws IOException {

        Response response = getResponseOfGet(METADATA_API_BASE_PATH +
                PATH_SEPARATOR + INBOUND_PROTOCOLS_PATH +
                PATH_SEPARATOR + SAML_PATH);
        response.then()
                .log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        SAMLMetaData responseFound = jsonWriter.readValue(response.asString(), SAMLMetaData.class);

        if (this.tenant.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            Assert.assertEquals(responseFound, samlMetaDataSuperTenant,
                    "SAML Metadata returned from the API doesn't match.");
        } else {
            Assert.assertEquals(responseFound, samlMetaDataTenant,
                    "SAML Metadata returned from the API doesn't match.");
        }
    }

    /* Disabling the test below since the WS-Trust functionality is provided
       as a connector and it does not exist in the product by default.*/
//    @Test
//    public void testGetWSTrustMetadata() throws IOException {
//
//        Response response = getResponseOfGet(METADATA_API_BASE_PATH +
//                PATH_SEPARATOR + INBOUND_PROTOCOLS_PATH +
//                PATH_SEPARATOR + WS_TRUST_PATH);
//        response.then()
//                .log()
//                .ifValidationFails()
//                .assertThat()
//                .statusCode(HttpStatus.SC_OK);
//        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
//        WSTrustMetaData responseFound = jsonWriter.readValue(response.asString(), WSTrustMetaData.class);
//
//        if (this.tenant.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
//            Assert.assertEquals(responseFound, wsTrustMetaDataSuperTenant,
//                    "WS Trust Metadata returned from the API doesn't match.");
//        } else {
//            Assert.assertEquals(responseFound, wsTrustMetaDataTenant,
//                    "WS Trust Metadata returned from the API doesn't match.");
//        }
//    }

    @Test
    public void testGetAdaptiveAuthTemplates() throws IOException, JSONException {

        Response response = getResponseOfGet(METADATA_API_BASE_PATH + PATH_SEPARATOR + ADAPTIVE_AUTH_PATH);
        response.then()
                .log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        JSONObject expected = new JSONObject(new JSONObject(readResource("adaptive-metadata.json")).get(
                JSON_KEY_TEMPLATES_JSON).toString());
        JSONObject received = new JSONObject(new JSONObject(response.asString()).get(
                JSON_KEY_TEMPLATES_JSON).toString());
        Iterator<String> iterator = expected.keys();

        while (iterator.hasNext()) {
            String result = iterator.next();
            if (!"uncategorized".equals(result)) {
                JSONArray expectedTemplates = expected.getJSONObject(result).getJSONArray(JSON_KEY_TEMPLATES);
                JSONArray receivedTemplates = received.getJSONObject(result).getJSONArray(JSON_KEY_TEMPLATES);

                Assert.assertTrue(equalSets(expectedTemplates, receivedTemplates),
                        "Adaptive auth templates returned from the API doesn't match.");
            }
        }
    }

    private boolean equalSets(JSONArray ja1, JSONArray ja2) throws JSONException {

        if (ja1 == ja2) {
            return true;
        } else if (ja1 != null && ja2 != null && ja1.length() == ja2.length()) {
            Set<Object> s1 = new JSONSet();
            Set<Object> s2 = new JSONSet();

            for (int i = 0; i < ja1.length(); ++i) {
                s1.add(ja1.get(i));
                s2.add(ja2.get(i));
            }

            return s1.equals(s2);
        } else {
            return false;
        }
    }

    class JSONSet<E> extends HashSet {

        public boolean contains(Object o) {

            Iterator it = iterator();
            if (o == null) {
                while (it.hasNext()) {
                    if (it.next() == null) {
                        return true;
                    }
                }
            } else {
                ObjectMapper mapper = new ObjectMapper();
                while (it.hasNext()) {
                    try {
                        if (mapper.readTree(o.toString())
                                .equals(mapper.readTree(it.next().toString()))) {
                            return true;
                        }
                    } catch (IOException e) {
                        return false;
                    }
                }
            }
            return false;
        }
    }
}
