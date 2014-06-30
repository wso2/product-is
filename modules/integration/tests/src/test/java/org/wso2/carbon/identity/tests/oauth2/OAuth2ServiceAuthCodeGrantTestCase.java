/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.tests.oauth2;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.wso2.carbon.automation.api.clients.identity.oauth.Oauth2ServiceClient;
import org.wso2.carbon.automation.api.clients.identity.oauth.Oauth2TokenValidationClient;
import org.wso2.carbon.automation.api.clients.identity.oauth.OauthAdminClient;
import org.wso2.carbon.automation.core.utils.LoginLogoutUtil;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.*;
import org.wso2.carbon.identity.tests.ISIntegrationTest;

public class OAuth2ServiceAuthCodeGrantTestCase extends ISIntegrationTest{

	private LoginLogoutUtil logManger;
    private String adminUsername;
    private String adminPassword;
    private Oauth2ServiceClient oauth2client;
    private OauthAdminClient adminClient;
    private Oauth2TokenValidationClient oauth2TokenValidationClient;
	private String consumerKey;
	private String accessToken;
	private String authCode;
	private String consumerSecret;
    
	@BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(0);
        logManger = new LoginLogoutUtil(isServer.getBackEndUrl());
        
        adminUsername = userInfo.getUserName();
        adminPassword = userInfo.getPassword();
        
        logManger.login(adminUsername, adminPassword, isServer.getBackEndUrl());
        
        oauth2client = new Oauth2ServiceClient(isServer.getBackEndUrl(), isServer.getSessionCookie());
        adminClient = new OauthAdminClient(isServer.getBackEndUrl(), isServer.getSessionCookie());
        oauth2TokenValidationClient = new Oauth2TokenValidationClient(isServer.getBackEndUrl(), isServer.getSessionCookie());
    }
    
    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        
    	adminClient.removeOAuthApplicationData(consumerKey);
    	
        logManger = null;
        oauth2client = null;
        consumerKey = null;
        accessToken = null;
    }
    
    @Test(groups = "wso2.is", description = "Check Oauth2 application registration")
    public void testRegisterApplication() throws Exception {
    	
    	OAuthConsumerAppDTO app = new OAuthConsumerAppDTO();
    	app.setApplicationName("oauthTestApp");
    	app.setCallbackUrl("https://localhost:8080/oauthPlayground");
    	app.setGrantTypes("authorization_code");
    	app.setOAuthVersion("OAuth-2.0");
    	
    	adminClient.registerOAuthApplicationData(app);
    	
    	OAuthConsumerAppDTO[] appDtos = adminClient.getAllOAuthApplicationData();
    	
    	Assert.assertNotNull(appDtos, "getAllOAuthApplicationData returned null.");
    	
    	for (OAuthConsumerAppDTO appDto : appDtos) {
	        if(appDto.getApplicationName().equals("oauthTestApp")) {
	        	consumerKey = appDto.getOauthConsumerKey();
	        	consumerSecret = appDto.getOauthConsumerSecret();
	        }
        }
    	
    	Assert.assertNotNull(consumerKey, "Consumer key is null.");
    }
    
    @Test(groups = "wso2.is", description = "Check Oauth2 authorize request", dependsOnMethods="testRegisterApplication")
    public void testAuthorize() throws Exception {

    	OAuth2AuthorizeReqDTO reqDto = new OAuth2AuthorizeReqDTO();
    	reqDto.setCallbackUrl("https://localhost:8080/oauthPlayground");
    	reqDto.setConsumerKey(consumerKey);
    	reqDto.setResponseType("code");
    	reqDto.setScopes(new String[]{"test"});
    	reqDto.setUsername(adminUsername);
    	
    	OAuth2AuthorizeRespDTO resDto = oauth2client.authorize(reqDto);
    	
    	Assert.assertNotNull(resDto, "Authorization response is null.");
    	Assert.assertTrue(resDto.getAuthenticated(), "Authentication is false.");
    	Assert.assertTrue(resDto.getAuthorized(), "Authorization is false.");
    	Assert.assertNotNull(resDto.getAuthorizationCode(), "Authorization code is null.");

    	authCode = resDto.getAuthorizationCode();
    	
    }
    
    @Test(groups = "wso2.is", description = "Check Oauth2 token issue", dependsOnMethods="testAuthorize")
    public void testIssueAccessToken() throws Exception {
    	
    	OAuth2AccessTokenReqDTO reqDto = new OAuth2AccessTokenReqDTO();
    	reqDto.setAuthorizationCode(authCode);
    	reqDto.setGrantType("authorization_code");
    	reqDto.setClientId(consumerKey);
    	reqDto.setClientSecret(consumerSecret);
    	reqDto.setCallbackURI("https://localhost:8080/oauthPlayground");

    	OAuth2AccessTokenRespDTO resDto = oauth2client.issueAccessToken(reqDto);
    	
    	Assert.assertNotNull(resDto, "issue token response is null.");
    	Assert.assertNotNull(resDto.getAccessToken(), "Access Token is null");
    	
    	accessToken = resDto.getAccessToken();
    }
    
    @Test(groups = "wso2.is", description = "Check Oauth2 validate access token", dependsOnMethods="testIssueAccessToken")
    public void testValidateClientInfo() throws Exception {
    	
    	OAuth2ClientValidationResponseDTO resDto = oauth2client.validateClientInfo(consumerKey, "https://localhost:8080/oauthPlayground");
    	
    	Assert.assertNotNull(resDto, "Validation response is null.");
    	Assert.assertTrue(resDto.getValidClient(), "Valid client is false");
    	Assert.assertEquals(resDto.getCallbackURL(), "https://localhost:8080/oauthPlayground");
    	Assert.assertEquals(resDto.getApplicationName(), "oauthTestApp");
    }

    @Test(groups = "wso2.is", description = "Check Oauth2 validate access token", dependsOnMethods="testValidateClientInfo")
    public void testValidateAccessToken() throws Exception {

        OAuth2TokenValidationRequestDTO valReq = new OAuth2TokenValidationRequestDTO();
        OAuth2TokenValidationRequestDTO_OAuth2AccessToken accessTokenDto =  new OAuth2TokenValidationRequestDTO_OAuth2AccessToken();
        accessTokenDto.setTokenType("bearer");
        accessTokenDto.setIdentifier(accessToken);
        valReq.setAccessToken(accessTokenDto);

        OAuth2TokenValidationResponseDTO responseDTO = oauth2TokenValidationClient.validateToken(valReq);
        Assert.assertTrue(responseDTO.getValid(), " Invalid Token ");
    }
    
    @Test(groups = "wso2.is", description = "Check Oauth2 revoke token", dependsOnMethods="testValidateAccessToken")
    public void testRevokeTokenByOAuthClient() throws Exception {
    	
    	OAuthRevocationRequestDTO revokeRequestDTO =  new OAuthRevocationRequestDTO();
    	revokeRequestDTO.setConsumerKey(consumerKey);
    	revokeRequestDTO.setConsumerSecret(consumerSecret);
    	revokeRequestDTO.setToken(accessToken);
    	
    	OAuthRevocationResponseDTO revokeResponseDTO = oauth2client.revokeTokenByOAuthClient(revokeRequestDTO);
    	
    	Assert.assertNotNull(revokeRequestDTO, "Revoke token response is null.");
    }
}
