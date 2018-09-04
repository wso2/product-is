/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.entitlement;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO;
import org.wso2.identity.integration.common.clients.entitlement.EntitlementPolicyServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * This class includes test cases for Entitlement REST APIs.
 */
public class EntitlementRestServiceTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(EntitlementRestServiceTestCase.class);
    private static final String ENDPOINT = "https://localhost:%s/api/identity/entitlement/decision/%s";
    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    private static final String POLICY_ID = "urn:oasis:names:tc:xacml:3.0:custompolicy";
    private static final String POLICY = "<Policy xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" " +
            "PolicyId=\"urn:oasis:names:tc:xacml:3.0:custompolicy\" " +
            "RuleCombiningAlgId=\"urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides\" " +
            "Version=\"1.0\"><Target><AnyOf><AllOf><Match " +
            "MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www" +
            ".w3.org/2001/XMLSchema#string\">read</AttributeValue><AttributeDesignator " +
            "AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" " +
            "Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\" DataType=\"http://www" +
            ".w3.org/2001/XMLSchema#string\" MustBePresent=\"true\"/></Match></AllOf></AnyOf></Target><Rule " +
            "Effect=\"Permit\" RuleId=\"permit\"/></Policy>";
    private EntitlementPolicyServiceClient entitlementPolicyClient;
    private HttpClient httpClient;
    private String username;
    private String password;
    private String serverPort;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        entitlementPolicyClient = new EntitlementPolicyServiceClient(backendURL, sessionCookie);
        httpClient = HttpClientBuilder.create().build();

        serverPort = isServer.getDefaultInstance().getPorts().get("https");
        username = isServer.getContextTenant().getContextUser().getUserName();
        password = isServer.getContextTenant().getContextUser().getPassword();

    }

    @AfterClass(alwaysRun = true)
    public void testEnd() throws Exception {

        entitlementPolicyClient = null;
        httpClient = null;
    }

    @Test(groups = "wso2.is", description = "Test retrieving API resource list")
    public void testGetAPIResourceList() throws IOException {

        HttpGet getRequest = new HttpGet(String.format(ENDPOINT, serverPort, "home"));
        getRequest.setHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        getRequest.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON);
        getRequest.setHeader(HttpHeaders.ACCEPT, CONTENT_TYPE_APPLICATION_JSON);

        HttpResponse response = httpClient.execute(getRequest);

        assertEquals(response.getStatusLine().getStatusCode(), 200, "Response for API resource list request failed");

        try (BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {

            Object responseObj = JSONValue.parse(rd);
            assertNotNull(responseObj, "Cannot parse JSON response for API resource list request");

            Object resourcesObj = ((JSONObject) responseObj).get("resources");
            assertNotNull(resourcesObj, "Response for API resource list request does not include 'resources'");

            Object pdpResourceObj = ((JSONArray) resourcesObj).get(0);
            assertNotNull(resourcesObj, "Response for API resource list request includes an empty set of 'resources'");

            String linkRelation = ((JSONObject) pdpResourceObj).get("rel").toString();
            assertEquals(linkRelation, "http://docs.oasis-open.org/ns/xacml/relation/pdp", "Response for API " +
                    "resource" + " list request does not include 'pdp' resource");
        }

        getRequest.releaseConnection();
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testGetAPIResourceList"}, description = "Test policy publish")
    public void testPublishPolicy() throws Exception {

        PolicyDTO policy = new PolicyDTO();
        policy.setPolicy(POLICY);
        policy.setPolicy(policy.getPolicy().replaceAll(">\\s+<", "><").trim());
        policy.setVersion("3.0");
        policy.setPolicyId(POLICY_ID);

        log.info("XACML Policy:\n" + policy.getPolicy());
        entitlementPolicyClient.addPolicy(policy);
        PolicyDTO updatedPolicy = entitlementPolicyClient.getPolicy(POLICY_ID, false);
        assertNotNull(updatedPolicy, "Policy not added");

        entitlementPolicyClient.publishPolicies(new String[]{POLICY_ID}, new String[]{"PDP Subscriber"}, "CREATE",
                true, null, 1);
        PolicyDTO publishedPolicy = entitlementPolicyClient.getPolicy(POLICY_ID, true);
        assertNotNull(publishedPolicy, "Policy not published");
    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testPublishPolicy"}, description = "Test get decision")
    public void testGetDecision() throws Exception {

        HttpPost postRequest = new HttpPost(String.format(ENDPOINT, serverPort, "pdp"));
        postRequest.setHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        postRequest.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON);
        postRequest.setHeader(HttpHeaders.ACCEPT, CONTENT_TYPE_APPLICATION_JSON);

        JSONObject subjectAttributeObj = new JSONObject();
        subjectAttributeObj.put("AttributeId", "urn:oasis:names:tc:xacml:1.0:subject:subject-id");
        subjectAttributeObj.put("Value", username);

        JSONArray subjectAttributeArray = new JSONArray();
        subjectAttributeArray.add(subjectAttributeObj);

        JSONObject subjectObj = new JSONObject();
        subjectObj.put("Attribute", subjectAttributeArray);

        JSONObject actionAttributeObj = new JSONObject();
        actionAttributeObj.put("AttributeId", "urn:oasis:names:tc:xacml:1.0:action:action-id");
        actionAttributeObj.put("Value", "read");

        JSONArray actionAttributeArray = new JSONArray();
        actionAttributeArray.add(actionAttributeObj);

        JSONObject actionObj = new JSONObject();
        actionObj.put("Attribute", actionAttributeArray);

        JSONObject resourceAttributeObj = new JSONObject();
        resourceAttributeObj.put("AttributeId", "urn:oasis:names:tc:xacml:1.0:resource:resource-id");
        resourceAttributeObj.put("Value", "http://test.com/service/very_secure/");

        JSONArray resourceAttributeArray = new JSONArray();
        resourceAttributeArray.add(resourceAttributeObj);

        JSONObject resourceObj = new JSONObject();
        resourceObj.put("Attribute", resourceAttributeArray);

        JSONObject requestObj = new JSONObject();
        requestObj.put("AccessSubject", subjectObj);
        requestObj.put("Action", actionObj);
        requestObj.put("Resource", resourceObj);

        JSONObject rootObj = new JSONObject();
        rootObj.put("Request", requestObj);

        String request = rootObj.toJSONString();
        log.info("XACML Request:\n" + request);

        StringEntity entity = new StringEntity(request);
        postRequest.setEntity(entity);

        Thread.sleep(5000);

        HttpResponse response = httpClient.execute(postRequest);

        assertEquals(response.getStatusLine().getStatusCode(), 200, "Response for PDP decision request failed");

        try (BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {

            Object responseObj = JSONValue.parse(rd);
            assertNotNull(responseObj, "Cannot parse JSON response for PDP decision request");

            log.info("XACML Response:\n" + responseObj.toString());

            Object pdpResponseObj = ((JSONObject) responseObj).get("Response");
            assertNotNull(pdpResponseObj, "Response for PDP decision request does not include 'Response'");

            Object pdpDecisionObj = ((JSONArray) pdpResponseObj).get(0);
            assertNotNull(pdpResponseObj, "Response for PDP decision request includes an empty set of 'decisions'");

            String decision = ((JSONObject) pdpDecisionObj).get("Decision").toString();
            assertEquals(decision, "Permit", "Response for PDP decision request does not return 'Permit'");
        }

        postRequest.releaseConnection();
    }

    private String getAuthzHeader() {
        return "Basic " + Base64.encodeBase64String((username + ":" + password).getBytes()).trim();
    }
}
