/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com/).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.identity.integration.test.oauth2.dcrm.api;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.oauth2.dcrm.api.util.DCRUtils;
import org.wso2.identity.integration.test.oauth2.dcrm.api.util.OAuthDCRMConstants;
import org.wso2.identity.integration.test.util.Utils;

import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * FAPI validation test case for the DCR flow
 */
public class FAPIDCRValidationsTestCase extends ISIntegrationTest {

    private HttpClient client;
    private String client_id;
    private String username;
    private String password;
    private String tenant;
    private ServerConfigurationManager serverConfigurationManager;

    @Factory(dataProvider = "dcrmConfigProvider")
    public FAPIDCRValidationsTestCase(TestUserMode userMode) throws Exception {

        AutomationContext context = new AutomationContext("IDENTITY", userMode);
        this.username = context.getContextTenant().getTenantAdmin().getUserName();
        this.password = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();

    }

    @DataProvider(name = "dcrmConfigProvider")
    public static Object[][] dcrmConfigProvider() {

        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN}, {TestUserMode.TENANT_ADMIN}};
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        client = HttpClients.createDefault();
        changeISConfiguration();
    }

    @DataProvider(name = "dcrConfigProvider")
    private static Object[][] dcrConfigProvider() throws Exception {

        String INVALID_CLIENT_METADATA = "invalid_client_metadata";
        String INVALID_SOFTWARE_STATEMENT = "invalid_software_statement";
        return new Object[][]{
                {
                        DCRUtils.getRegisterRequestJSON("request1.json"), INVALID_CLIENT_METADATA,
                        "Invalid token endpoint authentication method requested."
                },
                {
                        DCRUtils.getRegisterRequestJSON("request2.json"), INVALID_CLIENT_METADATA,
                        "Invalid signature algorithm requested"
                },
                {
                        DCRUtils.getRegisterRequestJSON("request3.json"), INVALID_CLIENT_METADATA,
                        "Invalid encryption algorithm requested"
                },
                {
                        DCRUtils.getRegisterRequestJSON("request4.json"), INVALID_CLIENT_METADATA,
                        "Sector identifier URI is needed for PPID calculation"
                },
                {
                        DCRUtils.getRegisterRequestJSON("request5.json"), INVALID_CLIENT_METADATA,
                        "Redirect URI missing in sector identifier URI set"
                },
                {
                        DCRUtils.getRegisterRequestJSON("request8.json"), INVALID_SOFTWARE_STATEMENT,
                        "Signature validation failed for the software statement"
                }
        };
    }

    private void changeISConfiguration() throws Exception {

        log.info("Adding entity id of SSOService to deployment.toml file");
        String carbonHome = Utils.getResidentCarbonHome();
        File defaultConfigFile = getDeploymentTomlFile(carbonHome);
        File configuredIdentityXML = new File(getISResourceLocation() + File.separator + "oauth"
                + File.separator + "dcr-fapi-validation-enabled.toml");
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredIdentityXML,
                defaultConfigFile, true);
        serverConfigurationManager.restartGracefully();
    }

    @Test(alwaysRun = true, groups = "wso2.is", priority = 1,
            description = "Check FAPI validations, PPID and SSA during DCR", dataProvider = "dcrConfigProvider")
    public void validateErrorScenarios(JSONObject requestJSON, String errorCode, String errorMessage) throws Exception {

        HttpPost request = new HttpPost(getTenantQualifiedURL(OAuthDCRMConstants.DCR_ENDPOINT_HOST_PART , tenant));
        request.addHeader(HttpHeaders.AUTHORIZATION, DCRUtils.getAuthzHeader(username, password));
        request.addHeader(HttpHeaders.CONTENT_TYPE, OAuthDCRMConstants.CONTENT_TYPE);
        StringEntity entity = new StringEntity(requestJSON.toJSONString());
        request.setEntity(entity);
        HttpResponse response = client.execute(request);

        assertEquals(response.getStatusLine().getStatusCode(), 400, "Service Provider " +
                "should not be created successfully");
        JSONObject errorResponse = DCRUtils.getPayload(response);
        assertEquals(errorResponse.get("error"), errorCode);
        assertEquals(errorResponse.get("error_description"), errorMessage);
    }

    @Test(alwaysRun = true, groups = "wso2.is", priority = 2,
            description = "Check FAPI validations, PPID and SSA during DCR", dataProvider = "dcrConfigProvider")
    public void validateErrorScenariosForDCRUpdate(JSONObject requestJSON, String errorCode, String errorMessage)
            throws Exception {

        // Create application.
        HttpPost request = new HttpPost(getTenantQualifiedURL(OAuthDCRMConstants.DCR_ENDPOINT_HOST_PART , tenant));
        JSONObject registerRequestJSON  = DCRUtils.getRegisterRequestJSON("request6.json");
        // Removing sending sector identifier uri to validate error message during update request.
        if (errorMessage.equals("Sector identifier URI is needed for PPID calculation")) {
           registerRequestJSON.remove("sector_identifier_uri");
        }
        request.addHeader(HttpHeaders.AUTHORIZATION, DCRUtils.getAuthzHeader(username, password));
        request.addHeader(HttpHeaders.CONTENT_TYPE, OAuthDCRMConstants.CONTENT_TYPE);
        StringEntity entity = new StringEntity(registerRequestJSON.toJSONString());
        request.setEntity(entity);

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 201, "Service Provider " +
                "created successfully");
        JSONObject createResponsePayload  = DCRUtils.getPayload(response);
        client_id = ((JSONObject) createResponsePayload).get("client_id").toString();
        assertNotNull(client_id, "client_id cannot be null");

        // Check error scenarios for update request.
        HttpPut updateRequest = new HttpPut(getTenantQualifiedURL(OAuthDCRMConstants.DCR_ENDPOINT_HOST_PART ,
                tenant) + client_id);
        updateRequest.addHeader(HttpHeaders.AUTHORIZATION, DCRUtils.getAuthzHeader(username, password));
        updateRequest.addHeader(HttpHeaders.CONTENT_TYPE, OAuthDCRMConstants.CONTENT_TYPE);
        entity = new StringEntity(requestJSON.toJSONString());
        updateRequest.setEntity(entity);

        HttpResponse updateResponse = client.execute(updateRequest);
        assertEquals(updateResponse.getStatusLine().getStatusCode(), 400, "Service Provider should " +
                "not be created successfully");
        JSONObject errorResponse = DCRUtils.getPayload(updateResponse);
        assertEquals(errorResponse.get("error"), errorCode);
        assertEquals(errorResponse.get("error_description"), errorMessage);

        // Delete application.
        HttpDelete deleteRequest = new HttpDelete(getTenantQualifiedURL(OAuthDCRMConstants.DCR_ENDPOINT_HOST_PART ,
                tenant) + client_id);
        deleteRequest.addHeader(HttpHeaders.AUTHORIZATION, DCRUtils.getAuthzHeader(username, password));
        HttpResponse deleteResponse = client.execute(deleteRequest);
        assertEquals(deleteResponse.getStatusLine().getStatusCode(), 204, "Service provider " +
                "deletion failed");
    }
}
