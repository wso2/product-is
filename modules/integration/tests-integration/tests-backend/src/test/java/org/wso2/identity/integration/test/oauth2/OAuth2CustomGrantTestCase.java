/*
* Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.identity.integration.test.oauth2;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.oauth2.stub.OAuth2TokenValidationService;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationResponseDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.utils.FileManager;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.clients.oauth.Oauth2TokenValidationClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import static org.wso2.identity.integration.test.utils.DataExtractUtil.KeyValue;

public class OAuth2CustomGrantTestCase extends OAuth2ServiceAbstractIntegrationTest {
	private AuthenticatorClient logManger;
	private ServerConfigurationManager serverConfigurationManager;
    private String resourcePath = getISResourceLocation() + File.separator + "oauth" + File.separator + "mobile-grant"
            + File.separator;
    private static final String TOKEN_API_ENDPOINT = "https://localhost:9853/oauth2/token";
    private Oauth2TokenValidationClient oauth2TokenValidationClient;
	private String adminUsername;
	private String adminPassword;
	private String accessToken;
	private String consumerKey;
	private String consumerSecret;

	private DefaultHttpClient client;

	@BeforeClass(alwaysRun = true)
	public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
        oauth2TokenValidationClient = new Oauth2TokenValidationClient(backendURL, sessionCookie);
        // Apply file based configurations
        applyConfigurationsToIS();
	}

	@AfterClass(alwaysRun = true)
	public void atEnd() throws Exception {
		deleteApplication();
		removeOAuthApplicationData();

		logManger = null;
		consumerKey = null;
        consumerSecret = null;
		accessToken = null;
	}

	@Test(alwaysRun = true, groups = "wso2.is", description = "Check Oauth2 application registration")
	public void testRegisterApplication() throws Exception {

		OAuthConsumerAppDTO appDto = createApplication();
		Assert.assertNotNull(appDto, "Application creation failed.");

		consumerKey = appDto.getOauthConsumerKey();
		Assert.assertNotNull(consumerKey, "Application creation failed.");

		consumerSecret = appDto.getOauthConsumerSecret();
		Assert.assertNotNull(consumerSecret, "Application creation failed.");

	}

	@Test(groups = "wso2.is", description = "Send authorize user request", dependsOnMethods = "testRegisterApplication")
	public void testGetAccessToken() throws Exception {
        ArrayList<NameValuePair> postParameters;
        HttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(TOKEN_API_ENDPOINT);
        //generate post request
        httpPost.setHeader("Authorization", "Basic " + getBase64EncodedString(consumerKey, consumerSecret));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("mobileNumber", "0333444777"));
        postParameters.add(new BasicNameValuePair("scope", "testScope"));
        postParameters.add(new BasicNameValuePair("grant_type", "mobile"));
        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
        HttpResponse response = client.execute(httpPost);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        //Get access token from the response
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(responseString);

        accessToken = json.get("access_token").toString();
        Assert.assertNotNull(accessToken, "Should have received an access token.");

        String refreshToken = json.get("refresh_token").toString();
        Assert.assertNotNull(refreshToken, "Should have received a refresh token.");
	}

	@Test(groups = "wso2.is", description = "Validate access token", dependsOnMethods = "testGetAccessToken")
	public void testValidateAccessToken() throws Exception {

        OAuth2TokenValidationRequestDTO oAuth2TokenValidationRequestDTO = new OAuth2TokenValidationRequestDTO();
        OAuth2TokenValidationResponseDTO oAuth2TokenValidationResponseDTO = oauth2TokenValidationClient.validateToken(oAuth2TokenValidationRequestDTO);

        Assert.assertNotNull(oAuth2TokenValidationResponseDTO, "Should have received a validation response.");

        Assert.assertTrue(oAuth2TokenValidationResponseDTO.getValid(), "Received access token should be valid." );

        Assert.assertEquals("0333444777@carbon.super", oAuth2TokenValidationResponseDTO.getAuthorizedUser(),
                "Authorized user name should be equal to the mobile number appended with tenant domain.");
	}

	protected void applyConfigurationsToIS() throws IOException, AutomationUtilException, XPathExpressionException {

        String carbonHome = Utils.getResidentCarbonHome();
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        File identityXMLFile = new File(resourcePath + "identity-oauth-mobile-grant.xml");
        File defaultIdentityXml = new File(carbonHome + File.separator
                + "repository" + File.separator + "conf" + File.separator + "identity" + File.separator
                + "identity.xml");

		copyToLib(new File(resourcePath + "custom-grant-5.4.0-SNAPSHOT.jar"));
		serverConfigurationManager.applyConfiguration(identityXMLFile, defaultIdentityXml, true, true);
	}

    protected void copyToLib(File sourceFile) throws IOException {

        String libPath = Utils.getResidentCarbonHome() + File.separator + "repository" + File.separator +
                "components" + File.separator + "lib";
        FileManager.copyResourceToFileSystem(sourceFile.getAbsolutePath(), libPath, sourceFile.getName());
    }

    protected void removeFromIdentity(String fileName, String targetDirectory) throws IOException {

        String identityConfigPath = Utils.getResidentCarbonHome() + File.separator + "repository" + File.separator +
                "conf" + File.separator + "identity";
        if (StringUtils.isNotBlank(targetDirectory)) {
            identityConfigPath = identityConfigPath.concat(File.separator + targetDirectory);
        }

        File file = new File(identityConfigPath + File.separator + fileName);
        if (file.exists()) {
            FileManager.deleteFile(file.getAbsolutePath());
        }
    }

    /**
     * Create Application with the given app configurations
     *
     * @return OAuthConsumerAppDTO
     * @throws Exception
     */
    public OAuthConsumerAppDTO createApplication() throws Exception {
        OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
        appDTO.setApplicationName(OAuth2Constant.OAUTH_APPLICATION_NAME);
        appDTO.setCallbackUrl(OAuth2Constant.CALLBACK_URL);
        appDTO.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
        appDTO.setGrantTypes("authorization_code mobile");
        return createApplication(appDTO);
    }

    /**
     * Get base64 encoded string of consumer key and secret
     *
     * @param consumerKey    consumer key of the application
     * @param consumerSecret consumer secret of the application
     * @return base 64 encoded string
     */
    private static String getBase64EncodedString(String consumerKey, String consumerSecret) {
        return new String(Base64.encodeBase64((consumerKey + ":" + consumerSecret).getBytes()));
    }

}