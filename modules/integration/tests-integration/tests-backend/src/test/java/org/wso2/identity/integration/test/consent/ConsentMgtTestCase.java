/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.consent;

import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.client.handlers.BasicAuthSecurityHandler;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import javax.ws.rs.core.MediaType;

public class ConsentMgtTestCase extends ISIntegrationTest {

    public static final String CONSNT_ENDPOINT_SUFFIX = "/api/identity/consent-mgt/v1.0/consents";
    private String isServerBackendUrl;
    private String consentEndpoint;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        isServerBackendUrl = isServer.getContextUrls().getWebAppURLHttps();
        consentEndpoint = isServerBackendUrl + CONSNT_ENDPOINT_SUFFIX;
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() {

    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Add PII Category test")
    public void testAddPIICategory() {

        String name = "http://wso2.org/claims/country";
        String description = "Country";
        JSONObject response = addPIICategory(name, description);

        Assert.assertEquals(name, response.get("piiCategory"));
        Assert.assertEquals(description, response.get("description"));
        Assert.assertEquals(true, response.get("sensitive"));
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Add Purpose Category test", dependsOnMethods = {"testAddPIICategory"})
    public void testAddPurposeCategory() {

        String name = "Financial";
        String description = "Financial Purpose";
        JSONObject response = addPurposeCategory(name, description);

        Assert.assertEquals(name, response.get("purposeCategory"));
        Assert.assertEquals(description, response.get("description"));
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Add Purpose test", dependsOnMethods = {"testAddPurposeCategory"})
    public void testAddPurpose() {

        String name = "Financial 01";
        String description = "Financial Purpose 01";
        String group = "SIGNUP";
        String groupType = "SYSTEM";
        JSONObject response = addPurpose(name, description, group, groupType);

        Assert.assertEquals(response.get("purpose"), name);
        Assert.assertEquals(response.get("description"), description);
        Assert.assertEquals(response.get("group"), group);
        Assert.assertEquals(response.get("groupType"), groupType);
        Assert.assertNotNull(response.get("piiCategories"));
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Add Receipt test", dependsOnMethods = {"testAddPurpose"})
    public void testAddReceipt() {

        String piiPrincipalId = "admin";
        String service = "travelocity.com";
        String serviceDisplayName = "Travelocity";
        String serviceDescription = "Travel City Guide";
        String consentType = "Sample";
        String collectionMethod = "Web";
        String jurisdiction = "NC";
        String language = "en-US";
        String policyURL = "http://test.com";

        JSONObject response = addReceipt(piiPrincipalId, service, serviceDisplayName, serviceDescription,
                consentType, collectionMethod, jurisdiction, language, policyURL);

        Assert.assertEquals(response.get("piiPrincipalId"), piiPrincipalId);
        Assert.assertEquals(response.get("language"), language);
    }

    private JSONObject addPIICategory(String name, String description) {

        ClientConfig clientConfig = new ClientConfig();
        BasicAuthSecurityHandler basicAuth = new BasicAuthSecurityHandler();
        basicAuth.setUserName(userInfo.getUserName());
        basicAuth.setPassword(userInfo.getPassword());
        clientConfig.handlers(basicAuth);

        RestClient restClient = new RestClient(clientConfig);
        Resource piiCatResource = restClient.resource(consentEndpoint + "/" + "pii-categories");

        String addPIICatString = "{\"piiCategory\": " + "\"" + name + "\"" + ", \"description\": " + "\"" +
                description + "\" , \"sensitive\": \"" + true + "\"}";

        String response = piiCatResource.contentType(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON)
                .post(String.class, addPIICatString);

        return (JSONObject) JSONValue.parse(response);
    }

    private JSONObject addPurposeCategory(String name, String description) {

        ClientConfig clientConfig = new ClientConfig();
        BasicAuthSecurityHandler basicAuth = new BasicAuthSecurityHandler();
        basicAuth.setUserName(userInfo.getUserName());
        basicAuth.setPassword(userInfo.getPassword());
        clientConfig.handlers(basicAuth);

        RestClient restClient = new RestClient(clientConfig);
        Resource piiCatResource = restClient.resource(consentEndpoint + "/" + "purpose-categories");

        String addPurposeCatString = "{\"purposeCategory\": " + "\"" + name + "\"" + ", \"description\": " + "\"" +
                description + "\"}";

        String response = piiCatResource.contentType(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON)
                .post(String.class, addPurposeCatString);

        return (JSONObject) JSONValue.parse(response);
    }

    private JSONObject addPurpose(String name, String description, String group, String groupType) {

        ClientConfig clientConfig = new ClientConfig();
        BasicAuthSecurityHandler basicAuth = new BasicAuthSecurityHandler();
        basicAuth.setUserName(userInfo.getUserName());
        basicAuth.setPassword(userInfo.getPassword());
        clientConfig.handlers(basicAuth);

        RestClient restClient = new RestClient(clientConfig);
        Resource piiCatResource = restClient.resource(consentEndpoint + "/" + "purposes");

        String addPurposeString = "{" +
                                  "  \"purpose\": \"" + name + "\"," +
                                  "  \"description\": \"" + description + "\"," +
                                  "  \"group\": \"" + group + "\"," +
                                  "  \"groupType\": \"" + groupType + "\"," +
                                  "  \"piiCategories\": [" +
                                  "    {" +
                                  "      \"piiCategoryId\": 1," +
                                  "      \"mandatory\": true" +
                                  "    }" +
                                  "  ]" +
                                  "}";

        String response = piiCatResource.contentType(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON)
                .post(String.class, addPurposeString);

        return (JSONObject) JSONValue.parse(response);
    }

    private JSONObject addReceipt(String piiPrincipalId, String service, String serviceDisplayName, String
            serviceDescription, String consentType, String collectionMethod, String jurisdiction, String language,
                                  String policyURL) {

        ClientConfig clientConfig = new ClientConfig();
        BasicAuthSecurityHandler basicAuth = new BasicAuthSecurityHandler();
        basicAuth.setUserName(userInfo.getUserName());
        basicAuth.setPassword(userInfo.getPassword());
        clientConfig.handlers(basicAuth);

        RestClient restClient = new RestClient(clientConfig);
        Resource piiCatResource = restClient.resource(consentEndpoint);

        String addReceiptString = "{" +
                "  \"services\": [" +
                "    {" +
                "      \"service\": \"" + service + "\"," +
                "      \"serviceDisplayName\": \"" + serviceDisplayName + "\"," +
                "      \"serviceDescription\": \"" + serviceDescription + "\"," +
                "      \"purposes\": [" +
                "        {" +
                "          \"purposeId\": 1," +
                "          \"purposeCategoryId\": [" +
                "            1" +
                "          ]," +
                "          \"consentType\": \"" + consentType + "\"," +
                "          \"piiCategory\": [" +
                "            {" +
                "              \"piiCategoryId\": 1," +
                "              \"validity\": \"3\"" +
                "            }" +
                "          ]," +
                "          \"primaryPurpose\": true," +
                "          \"termination\": \"string\"," +
                "          \"thirdPartyDisclosure\": true," +
                "          \"thirdPartyName\": \"string\"" +
                "        }" +
                "      ]" +
                "    }" +
                "  ]," +
                "  \"collectionMethod\": \"" + collectionMethod + "\"," +
                "  \"jurisdiction\": \"" + jurisdiction + "\"," +
                "  \"language\": \"" + language + "\"," +
                "  \"policyURL\": \"" + policyURL + "\"" +
                "}";

        String response = piiCatResource.contentType(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON)
                .post(String.class, addReceiptString);

        return (JSONObject) JSONValue.parse(response);
    }
}
