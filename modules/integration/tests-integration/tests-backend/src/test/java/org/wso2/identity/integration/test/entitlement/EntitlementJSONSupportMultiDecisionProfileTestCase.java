/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.entitlement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO;
import org.wso2.identity.integration.common.clients.entitlement.EntitlementPolicyServiceClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * This is to test the full flow of XACML Multi Decision Profile scenario
 */
public class EntitlementJSONSupportMultiDecisionProfileTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(EntitlementJSONSupportMultiDecisionProfileTestCase.class);
    private EntitlementPolicyServiceClient entitlementPolicyClient;
    private RemoteUserStoreManagerServiceClient remoteUserStoreManagerServiceClient;
    private final static String ENDPOINT_ADDRESS = "https://localhost:9853/api/identity/entitlement/decision";
    private final static String PASSWORD = "abc123";
    private final static String ROLE = "admin";


    private static final String POLICY_ID = "urn:oasis:names:tc:xacml:3.0:web-filter-policy";
    private static final String POLICY = "<Policy xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\"  " +
            "PolicyId=\"urn:oasis:names:tc:xacml:3.0:web-filter-policy\" RuleCombiningAlgId=\"" +
            "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable\" Version=\"1.0\">\n" +
            "   <Target>\n" +
            "      <AnyOf>\n" +
            "         <AllOf>\n" +
            "            <Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\">\n" +
            "               <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">index.jsp</AttributeValue>\n" +
            "               <AttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\" Category=" +
            "\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"" +
            " MustBePresent=\"true\"></AttributeDesignator>\n" +
            "            </Match>\n" +
            "         </AllOf>\n" +
            "      </AnyOf>\n" +
            "   </Target>\n" +
            "   <Rule Effect=\"Permit\" RuleId=\"Rule_for_all_groups\">\n" +
            "      <Target>\n" +
            "         <AnyOf>\n" +
            "            <AllOf>\n" +
            "               <Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\">\n" +
            "                  <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">publicUser</AttributeValue>\n" +
            "                  <AttributeDesignator AttributeId=\"http://wso2.org/identity/user/username\" Category=" +
            "\"http://wso2.org/identity/user\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"true\">" +
            "</AttributeDesignator>\n" +
            "               </Match>\n" +
            "            </AllOf>\n" +
            "         </AnyOf>\n" +
            "      </Target>\n" +
            "      <Condition>\n" +
            "         <Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of\">\n" +
            "            <Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-bag\">\n" +
            "               <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">view-welcome</AttributeValue>\n" +
            "               <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">view-summary</AttributeValue>\n" +
            "            </Apply>\n" +
            "            <AttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" " +
            "Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"" +
            " MustBePresent=\"true\"></AttributeDesignator>\n" +
            "         </Apply>\n" +
            "      </Condition>\n" +
            "   </Rule>\n" +
            "   <Rule Effect=\"Permit\" RuleId=\"Rule_for_all_internal_user_group\">\n" +
            "      <Target>\n" +
            "         <AnyOf>\n" +
            "            <AllOf>\n" +
            "               <Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\">\n" +
            "                  <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">internalUser</AttributeValue>\n" +
            "                  <AttributeDesignator AttributeId=\"http://wso2.org/identity/user/username\" Category=" +
            "\"http://wso2.org/identity/user\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"true\">" +
            "</AttributeDesignator>\n" +
            "               </Match>\n" +
            "            </AllOf>\n" +
            "         </AnyOf>\n" +
            "      </Target>\n" +
            "      <Condition>\n" +
            "         <Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of\">\n" +
            "            <Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-bag\">\n" +
            "               <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">view-status</AttributeValue>\n" +
            "            </Apply>\n" +
            "            <AttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" " +
            "Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"" +
            " MustBePresent=\"true\"></AttributeDesignator>\n" +
            "         </Apply>\n" +
            "      </Condition>\n" +
            "   </Rule>\n" +
            "   <Rule Effect=\"Permit\" RuleId=\"Rule_for_all_admin_user_group\">\n" +
            "      <Target>\n" +
            "         <AnyOf>\n" +
            "            <AllOf>\n" +
            "               <Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\">\n" +
            "                  <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">adminUser</AttributeValue>\n" +
            "                  <AttributeDesignator AttributeId=\"http://wso2.org/identity/user/username\" " +
            "Category=\"http://wso2.org/identity/user\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"" +
            " MustBePresent=\"true\"></AttributeDesignator>\n" +
            "               </Match>\n" +
            "            </AllOf>\n" +
            "         </AnyOf>\n" +
            "      </Target>\n" +
            "      <Condition>\n" +
            "         <Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of\">\n" +
            "            <Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-bag\">\n" +
            "               <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">modify-welcome</AttributeValue>\n" +
            "               <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">modify-summary</AttributeValue>\n" +
            "            </Apply>\n" +
            "            <AttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" " +
            "Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" " +
            "MustBePresent=\"true\"></AttributeDesignator>\n" +
            "         </Apply>\n" +
            "      </Condition>\n" +
            "   </Rule>\n" +
            "   <Rule Effect=\"Deny\" RuleId=\"Rule_deny_all\"></Rule>\n" +
            "   <ObligationExpressions>\n" +
            "      <ObligationExpression FulfillOn=\"Deny\" ObligationId=\"fail_to_permit\">\n" +
            "         <AttributeAssignmentExpression AttributeId=\"obligation-id\">\n" +
            "            <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">You can access the" +
            " resource index.jsp</AttributeValue>\n" +
            "         </AttributeAssignmentExpression>\n" +
            "      </ObligationExpression>\n" +
            "   </ObligationExpressions>\n" +
            "</Policy> ";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        entitlementPolicyClient = new EntitlementPolicyServiceClient(backendURL, sessionCookie);
        remoteUserStoreManagerServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
        PolicyDTO policy = new PolicyDTO();
        policy.setPolicy(POLICY);
        entitlementPolicyClient.addPolicy(policy);
        entitlementPolicyClient.publishPolicies(new String[]{POLICY_ID}, new String[]{"PDP Subscriber"}, "CREATE",
                true, null, 1);
        remoteUserStoreManagerServiceClient.addUser("adminUser",PASSWORD, new String[]{ROLE}, null,
                "adminUser",false);
        remoteUserStoreManagerServiceClient.addUser("publicUser",PASSWORD, new String[]{ROLE}, null,
                "publicUser",false);
        remoteUserStoreManagerServiceClient.addUser("localUser",PASSWORD, new String[]{ROLE}, null,
                "localUser",false);
    }

    @Test(groups = "wso2-is", description = "A simple JSON request sample")
    public void testPdpJSONSimpleRequest() throws JSONException {

        WebClient client = WebClient.create(ENDPOINT_ADDRESS);

        client.header("Authorization", "Basic YWRtaW46YWRtaW4=");
        client.type("application/json");
        client.accept("application/json");

        client.path("pdp");

        String request = readReource("entitlement/json/simpleRequest.json");
        String response = readReource("entitlement/json/simpleResponse.json");
        JSONObject objExpected = new JSONObject(response);

        String webRespose = client.post(request, String.class);
        JSONObject objReturn = new JSONObject(webRespose);
        Assert.assertTrue(areJSONObjectsEqual(objExpected, objReturn), "The response is wrong it should be :"+ response + " But" +
                " the response is :" + webRespose);
    }

    @Test(groups = "wso2-is", description = "A complex multi-decision JSON request sample")
    public void testPdpJSONMultiDecisionRequest() throws  JSONException {

        WebClient client = WebClient.create(ENDPOINT_ADDRESS);

        client.header("Authorization", "Basic YWRtaW46YWRtaW4=");
        client.type("application/json");
        client.accept("application/json");

        client.path("pdp");

        String request = readReource("entitlement/json/complexMDPRequest.json");
        String response = readReource("entitlement/json/complexMDPResponse.json");
        JSONObject objExpected = new JSONObject(response);

        String webRespose = client.post(request, String.class);
        JSONObject objReturn = new JSONObject(webRespose);
        Assert.assertTrue(areJSONObjectsEqual(objExpected, objReturn), "The response is wrong it should be :"+ response + " But" +
                " the response is :" + webRespose);

    }

    @Test(groups = "wso2-is", description = "A complex multi-decision JSON request sample in simple form")
    public void testPdpJSONMultiDecisionRequestSimpleForm() throws JSONException {

        WebClient client = WebClient.create(ENDPOINT_ADDRESS);

        client.header("Authorization", "Basic YWRtaW46YWRtaW4=");
        client.type("application/json");
        client.accept("application/json");

        client.path("pdp");

        String request = readReource("entitlement/json/simpleMDPRequest.json");
        String response = readReource("entitlement/json/simpleMDPResponse.json");
        JSONObject objExpected = new JSONObject(response);

        String webRespose = client.post(request, String.class);
        JSONObject objReturn = new JSONObject(webRespose);
        Assert.assertTrue(areJSONObjectsEqual(objExpected, objReturn), "The response is wrong it should be :"+ response + " But" +
                " the response is :" + webRespose);
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {

        // Remove added policy
        entitlementPolicyClient = new EntitlementPolicyServiceClient(backendURL, sessionCookie);
        remoteUserStoreManagerServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
        entitlementPolicyClient.enableDisablePolicy(POLICY_ID, false);
        entitlementPolicyClient.removePolicy(POLICY_ID);
        remoteUserStoreManagerServiceClient.deleteUser("adminUser");
        remoteUserStoreManagerServiceClient.deleteUser("publicUser");
        remoteUserStoreManagerServiceClient.deleteUser("localUser");
    }

    public static boolean areJSONObjectsEqual(Object ob1, Object ob2) throws JSONException {

        Object obj1Converted = convertJsonElement(ob1);
        Object obj2Converted = convertJsonElement(ob2);
        return obj1Converted.equals(obj2Converted);
    }

    private static Object convertJsonElement(Object elem) throws JSONException {

        if (elem instanceof JSONObject) {
            JSONObject obj = (JSONObject) elem;
            Iterator<String> keys = obj.keys();
            Map<String, Object> jsonMap = new HashMap<>();
            while (keys.hasNext()) {
                String key = keys.next();
                jsonMap.put(key, convertJsonElement(obj.get(key)));
            }
            return jsonMap;
        } else if (elem instanceof JSONArray) {
            JSONArray arr = (JSONArray) elem;
            Set<Object> jsonSet = new HashSet<>();
            for (int i = 0; i < arr.length(); i++) {
                jsonSet.add(convertJsonElement(arr.get(i)));
            }
            return jsonSet;
        } else {
            return elem;
        }
    }

    private String readReource(String path) {

        StringBuilder result = new StringBuilder();
        Scanner scanner = null;
        try {
            //Get file from resources folder
            ClassLoader classLoader = getClass().getClassLoader();
            URI filepath = new URI(classLoader.getResource(path).toString());

            File file = new File(filepath);

            scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }

        } catch (IOException e) {
            log.error("Error occured when reading the file.", e);
        } catch (URISyntaxException e) {
            log.error("URI syntax error.", e);
        } finally {
            scanner.close();
        }
        return result.toString().replaceAll("\\n\\r|\\n|\\r|\\t|\\s{2,}", "").replaceAll(": ", ":");
    }

}
