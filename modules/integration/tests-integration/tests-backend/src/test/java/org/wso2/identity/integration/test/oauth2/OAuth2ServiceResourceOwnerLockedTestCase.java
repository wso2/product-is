/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.identity.integration.test.oauth2;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.user.common.model.ScimSchemaExtensionSystem;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import static org.wso2.identity.integration.test.utils.CommonConstants.USER_IS_LOCKED;
import static org.wso2.identity.integration.test.utils.DataExtractUtil.KeyValue;

public class OAuth2ServiceResourceOwnerLockedTestCase extends OAuth2ServiceAbstractIntegrationTest {

	public static final String OAUTH_2_SERVICE_RESOURCE_OWNER_TOML = "oauth2_service_resource_owner.toml";

	private ServerConfigurationManager serverConfigurationManager;
	private String consumerKey;
	private String consumerSecret;

	private CloseableHttpClient client;
	private AutomationContext context;
	private Tenant tenantInfo;
	private SCIM2RestClient scim2RestClient;

	private static final String lockedUser = "test_locked_user";
	private static final String lockedUserPassword = "Test_locked_user_pass@123";
    private String activeTenant;
	private static final String TENANT_DOMAIN = "wso2.com";
	private String applicationId;
	private String userId;

	@BeforeClass(alwaysRun = true)
	public void testInit() throws Exception {

		super.init();
		changeISConfiguration();
		super.init();
		context = new AutomationContext("IDENTITY", TestUserMode.SUPER_TENANT_ADMIN);
		this.activeTenant = context.getContextTenant().getDomain();

		tenantInfo = context.getContextTenant();

		restClient = new OAuth2RestClient(serverURL, tenantInfo);
		scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);

		setSystemproperties();
		client = HttpClientBuilder.create().build();

        createLockedUser();
		createOIDCApplication();
	}

	@AfterClass(alwaysRun = true)
	public void atEnd() throws Exception {

		scim2RestClient.deleteUser(userId);
		restClient.deleteApplication(applicationId);

		client.close();
		restClient.closeHttpClient();
		scim2RestClient.closeHttpClient();
		consumerKey = null;
		serverConfigurationManager.restoreToLastConfiguration(false);
	}

	private void changeISConfiguration() throws IOException,
			XPathExpressionException, AutomationUtilException {

		log.info("Replacing the deployment.toml file to enabling showing auth failure reason for password grant");
		String carbonHome = Utils.getResidentCarbonHome();
		File defaultTomlFile = getDeploymentTomlFile(carbonHome);
		File configuredTomlFile = new File
					(getISResourceLocation() + File.separator + "oauth" +
							File.separator + OAUTH_2_SERVICE_RESOURCE_OWNER_TOML);
		serverConfigurationManager = new ServerConfigurationManager(isServer);
		serverConfigurationManager.applyConfigurationWithoutRestart(configuredTomlFile, defaultTomlFile, true);
		serverConfigurationManager.restartForcefully();
	}

	@Test(groups = "wso2.is", description = "Send authorize request for locked user")
	public void testSendLockedAuthenticationPost() throws Exception {

		if (!TENANT_DOMAIN.equals(activeTenant)) {
			HttpPost request = new HttpPost(
                    getTenantQualifiedURL(OAuth2Constant.ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain()));
			List<NameValuePair> urlParameters = new ArrayList<>();
			urlParameters.add(new BasicNameValuePair("grant_type",
					OAuth2Constant.OAUTH2_GRANT_TYPE_RESOURCE_OWNER));
			urlParameters.add(new BasicNameValuePair("username", lockedUser));
			urlParameters.add(new BasicNameValuePair("password", lockedUserPassword));

			request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
			request.setHeader("Authorization", "Basic " + Base64.encodeBase64String((consumerKey + ":" + consumerSecret)
					.getBytes()).trim());
			request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
			request.setEntity(new UrlEncodedFormEntity(urlParameters));

			HttpResponse response = client.execute(request);

			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			Object obj = JSONValue.parse(rd);
			String errormsg = ((JSONObject) obj).get("error_description").toString();

			EntityUtils.consume(response.getEntity());
			// Validate the error code of the scenario.
			Assert.assertTrue(errormsg.contains(USER_IS_LOCKED));
		}
	}

	private void createLockedUser() {

		try {
			UserObject userInfo = new UserObject();
			userInfo.setUserName(lockedUser);
			userInfo.setPassword(lockedUserPassword);
			userInfo.setScimSchemaExtensionSystem(new ScimSchemaExtensionSystem().accountLocked(true));
			userId = scim2RestClient.createUser(userInfo);
		} catch (Exception e) {
			Assert.fail("Error while creating the user", e);
		}
	}

	private void createOIDCApplication() throws Exception {

		ApplicationResponseModel application = addApplication();
		applicationId = application.getId();

		OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
		consumerKey = oidcConfig.getClientId();
		consumerSecret = oidcConfig.getClientSecret();
	}
}
