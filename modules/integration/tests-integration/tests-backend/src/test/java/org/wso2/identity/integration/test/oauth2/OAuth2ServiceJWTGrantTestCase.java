/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.oauth2;

import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.JWTBearerGrant;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.RefreshTokenGrant;
import com.nimbusds.oauth2.sdk.ResourceOwnerPasswordCredentialsGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.idp.xsd.Claim;
import org.wso2.carbon.identity.application.common.model.idp.xsd.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.ClaimMetadataManagementServiceClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.ExternalClaimDTO;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException;
import org.wso2.carbon.identity.user.profile.stub.types.UserFieldDTO;
import org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.clients.oauth.OauthAdminClient;
import org.wso2.identity.integration.common.clients.UserProfileMgtServiceClient;
import org.wso2.identity.integration.common.clients.claim.metadata.mgt.ClaimMetadataManagementServiceClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

/**
 * This is a test class for self contained access tokens with JWT bearer grant type.
 */
public class OAuth2ServiceJWTGrantTestCase extends OAuth2ServiceAbstractIntegrationTest {

    protected Log log = LogFactory.getLog(getClass());
    private ServerConfigurationManager serverConfigurationManager;
    private UserManagementClient userManagementClient;
    private UserProfileMgtServiceClient userProfileMgtServiceClient;
    private OauthAdminClient oauthAdminClient;
    private ClaimMetadataManagementServiceClient claimMetadataManagementServiceClient;
    private final String JWT_USER = "jwtUser";
    private String jwtAssertion;
    private String alias;
    private String issuer;
    private IdentityProviderMgtServiceClient identityProviderMgtServiceClient;
    private final String COUNTRY_CLAIM_VALUE = "USA";
    private final String COUNTRY_OIDC_CLAIM = "country";
    private final String COUNTRY_NEW_OIDC_CLAIM = "customclaim";
    private final String COUNTRY_LOCAL_CLAIM_URI = "http://wso2.org/claims/country";
    private final String EMAIL_OIDC_CLAIM = "email";
    private final String EMAIL_CLAIM_VALUE = "email@email.com";
    private final String EMAIL_LOCAL_CLAIM_URI = "http://wso2.org/claims/emailaddress";
    private String refreshToken;
    String openIdScope = "openid";

    @BeforeClass
    public void setup() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        changeISConfiguration("jwt-token-issuer-enabled-identity.xml");
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        OAuthConsumerAppDTO appDto = createApplication(createApplicationWithJWTGrantType());
        consumerKey = appDto.getOauthConsumerKey();
        consumerSecret = appDto.getOauthConsumerSecret();
        userManagementClient = new UserManagementClient(backendURL, sessionCookie);
        oauthAdminClient = new OauthAdminClient(backendURL, sessionCookie);
        userProfileMgtServiceClient = new UserProfileMgtServiceClient(backendURL, sessionCookie);
        identityProviderMgtServiceClient = new IdentityProviderMgtServiceClient(sessionCookie, backendURL);
        addNewUserWithClaims();
        claimMetadataManagementServiceClient = new ClaimMetadataManagementServiceClient(backendURL, sessionCookie);

        ExternalClaimDTO externalClaimDTO = new ExternalClaimDTO();
        externalClaimDTO
                .setExternalClaimDialectURI(OAuth2ServiceAuthCodeGrantOpenIdRequestObjectTestCase.OIDC_CLAIM_DIALECT);
        externalClaimDTO.setMappedLocalClaimURI(COUNTRY_LOCAL_CLAIM_URI);
        externalClaimDTO.setExternalClaimURI(COUNTRY_NEW_OIDC_CLAIM);
        claimMetadataManagementServiceClient.addExternalClaim(externalClaimDTO);
        String[] openidValue = new String[1];
        openidValue[0] = COUNTRY_NEW_OIDC_CLAIM;
        oauthAdminClient.updateScope(openIdScope, openidValue, null);
    }

    @Test(description = "This test case tests the JWT self contained access token generation using password grant "
            + "type.")
    public void testPasswordGrantBasedSelfContainedAccessTokenGeneration()
            throws IOException, URISyntaxException, ParseException, java.text.ParseException,
            ClaimMetadataManagementServiceClaimMetadataException {

        Secret password = new Secret(JWT_USER);
        AuthorizationGrant passwordGrant = new ResourceOwnerPasswordCredentialsGrant(JWT_USER, password);
        ClientID clientID = new ClientID(consumerKey);
        Secret clientSecret = new Secret(consumerSecret);
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);
        URI tokenEndpoint = new URI(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, passwordGrant,
                new Scope(OAuth2Constant.OAUTH2_SCOPE_OPENID));

        HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();
        Assert.assertNotNull(tokenHTTPResp, "JWT access token http response is null.");

        TokenResponse tokenResponse = OIDCTokenResponseParser.parse(tokenHTTPResp);
        Assert.assertNotNull(tokenResponse, "Token response of JWT access token response is null.");
        Assert.assertFalse(tokenResponse instanceof TokenErrorResponse, "JWT access token response contains errors.");

        OIDCTokenResponse oidcTokenResponse = (OIDCTokenResponse) tokenResponse;
        OIDCTokens oidcTokens = oidcTokenResponse.getOIDCTokens();
        Assert.assertNotNull(oidcTokens, "OIDC Tokens object is null in JWT token");

        jwtAssertion = oidcTokens.getIDTokenString();
        alias = oidcTokens.getIDToken().getJWTClaimsSet().getAudience().get(0);
        issuer = oidcTokens.getIDToken().getJWTClaimsSet().getIssuer();
        Assert.assertEquals(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(COUNTRY_OIDC_CLAIM), COUNTRY_CLAIM_VALUE,
                "Requested user claims is not returned back in self contained access token based on password "
                        + "claim.");
        Assert.assertEquals(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(EMAIL_OIDC_CLAIM), EMAIL_CLAIM_VALUE,
                "Requested user claims is not returned back in self contained access token based on password "
                        + "claim.");
        String GIVEN_NAME_OIDC_CLAIM = "given_name";
        Assert.assertNull(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(GIVEN_NAME_OIDC_CLAIM),
                "Non-requested user claim " + GIVEN_NAME_OIDC_CLAIM + " is not returned back in self contained access "
                        + "token based on password claim");
        Assert.assertNull(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(EMAIL_LOCAL_CLAIM_URI),
                "User claim " + EMAIL_LOCAL_CLAIM_URI + " is not returned in local claim uri format without being "
                        + "converted to OIDC claim");
        changeCountryOIDDialect();
    }

    @Test(description = "This test case tests the behaviour when ConvertToOIDCDialect is false in identity.xml, this "
            + "is a pass through scenarios to send the claims as it is regardless of claim mapping in SP and IDP "
            + "claim mapping", dependsOnMethods = "testPasswordGrantBasedSelfContainedAccessTokenGeneration")
    public void testJWTGrantTypeWithConvertOIDCDialectFalse() throws Exception {

        addFederatedIdentityProvider();
        OIDCTokens oidcTokens = makeJWTBearerGrantRequest();
        Assert.assertEquals(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(COUNTRY_OIDC_CLAIM),
                COUNTRY_CLAIM_VALUE,
                "User claims is not returned back as it is when ConvertToOIDCDialect is set to false");
        updateIdentityProviderWithClaimMappings();
        oidcTokens = makeJWTBearerGrantRequest();
        Assert.assertEquals(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(EMAIL_OIDC_CLAIM), EMAIL_CLAIM_VALUE,
                "User claims is not returned back as it is when ConvertToOIDCDialect is set to false");
        identityProviderMgtServiceClient.deleteIdP(issuer);
    }

    @Test(description = "This test case tests the behaviour when ConvertOIDCDialect is set to true in identity.xml "
            + "and when there is no mapping IDP side but with mapping in SP side and AddRemainingUserAttributes is "
            + "false", dependsOnMethods = "testJWTGrantTypeWithConvertOIDCDialectFalse")
    public void testJWTGrantTypeWithConvertOIDCDialectWithoutIDPMappingWithSPMapping() throws Exception {

        serverConfigurationManager.restoreToLastConfiguration();
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        changeISConfiguration("jwt-token-issuer-convertToOIDC.xml");
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        identityProviderMgtServiceClient = new IdentityProviderMgtServiceClient(sessionCookie, backendURL);
        addFederatedIdentityProvider();
        OIDCTokens oidcTokens = makeJWTBearerGrantRequest();
        Assert.assertNull(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(COUNTRY_OIDC_CLAIM),
                "User claims is returned back without mappings in SP and IDP side when ConvertToOIDCDialect is "
                        + "set to true in identity.xml");
        Assert.assertNull(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(EMAIL_OIDC_CLAIM),
                "User claims is returned back without mappings in SP and IDP side when ConvertToOIDCDialect is "
                        + "set to true in identity.xml");
        Assert.assertNull(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(EMAIL_LOCAL_CLAIM_URI),
                "User claims is returned back when IDP claim mapping is not set and with only SP Claim mapping");
    }

    @Test(description = "This test case tests the behaviour when ConvertOIDCDialect is set to true in identity.xml "
            + "and when there are mappings in IDP and SP side and AddRemainingUserAttributes is "
            + "false", dependsOnMethods = "testJWTGrantTypeWithConvertOIDCDialectWithoutIDPMappingWithSPMapping")
    public void testJWTGrantTypeWithConvertOIDCDialectWithIDPMappingWithSPMapping() throws Exception {

        updateIdentityProviderWithClaimMappings();
        OIDCTokens oidcTokens = makeJWTBearerGrantRequest();
        Assert.assertNull(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(COUNTRY_OIDC_CLAIM),
                "User claims is returned back without mappings in SP and IDP side when ConvertToOIDCDialect is "
                        + "set to true in identity.xml");
        Assert.assertNull(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(EMAIL_OIDC_CLAIM),
                "User claims is returned back without mappings in SP and IDP side when ConvertToOIDCDialect is "
                        + "set to true in identity.xml");
        Assert.assertEquals(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(COUNTRY_NEW_OIDC_CLAIM),
                COUNTRY_CLAIM_VALUE, "User claims is not returned back with proper mappings in SP and IDP side when "
                        + "ConvertToOIDCDialect is set to true in identity.xml");
        Assert.assertNull(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(EMAIL_LOCAL_CLAIM_URI),
                "User claims conversion is wrong as per the claim mapping in SP and IDP");
    }

    @Test(description = "This test case tests the behaviour when ConvertOIDCDialect is set to true in identity.xml "
            + "and when there are mappings in IDP and when thare are no mapping in SP side",
            dependsOnMethods = "testJWTGrantTypeWithConvertOIDCDialectWithIDPMappingWithSPMapping")
    public void testJWTGrantTypeWithConvertOIDCDialectWithIDPMappingWithoutSPMapping() throws Exception {

        ServiceProvider serviceProvider = appMgtclient.getApplication(SERVICE_PROVIDER_NAME);
        org.wso2.carbon.identity.application.common.model.xsd.ClaimConfig claimConfig = new org.wso2.carbon.identity.application.common.model.xsd.ClaimConfig();
        claimConfig.setLocalClaimDialect(true);
        serviceProvider.setClaimConfig(claimConfig);
        appMgtclient.updateApplicationData(serviceProvider);

        OIDCTokens oidcTokens = makeJWTBearerGrantRequest();
        Assert.assertNull(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(COUNTRY_OIDC_CLAIM),
                "User claims is returned back without mappings in SP side when ConvertToOIDCDialect is "
                        + "set to true in identity.xml");
        Assert.assertNull(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(EMAIL_OIDC_CLAIM),
                "User claims is returned back without mappings in SP side when ConvertToOIDCDialect is "
                        + "set to true in identity.xml");
        Assert.assertNull(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(COUNTRY_LOCAL_CLAIM_URI),
                "User claims is returned back without mappings in SP side when ConvertToOIDCDialect is "
                        + "set to true in identity.xml");
    }

    @Test(description = "This test case tests the behaviour when ConvertOIDCDialect is set to true in identity.xml "
            + "and when there are no mappings in IDP and  SP side. In this scenario, this "
            + "should work as pass through", dependsOnMethods =
            "testJWTGrantTypeWithConvertOIDCDialectWithIDPMappingWithoutSPMapping")
    public void testJWTGrantTypeWithConvertOIDCDialectWithoutIDPMappingWithoutSPMapping() throws Exception {

        IdentityProvider identityProvider = identityProviderMgtServiceClient.getIdPByName(issuer);
        ClaimConfig claimConfig = new ClaimConfig();
        claimConfig.setLocalClaimDialect(true);
        identityProvider.setClaimConfig(claimConfig);
        identityProviderMgtServiceClient.updateIdP(issuer, identityProvider);

        OIDCTokens oidcTokens = makeJWTBearerGrantRequest();
        Assert.assertEquals(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(EMAIL_OIDC_CLAIM), EMAIL_CLAIM_VALUE,
                "User claims are not proxied when there are no SP and IDP Claim mappings "
                        + "returned when ConvertToOIDCDialect is set to true in identity.xml");
        Assert.assertEquals(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(COUNTRY_OIDC_CLAIM), COUNTRY_CLAIM_VALUE,
                "User claims are not proxied when there are no SP and IDP Claim mappings "
                        + "returned when ConvertToOIDCDialect is set to true in identity.xml");
        Assert.assertNull(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(COUNTRY_LOCAL_CLAIM_URI),
                "User claims conversion happened wrongly.");
        Assert.assertNotNull(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(COUNTRY_NEW_OIDC_CLAIM),
                "User claims conversion happened wrongly.");
    }

    @Test(description = "This test case tests the behaviour when ConvertOIDCDialect, AddRemainingUserAttributes is set "
            + "to true in identity.xml and when there are some SP and IDP mappings.",
            dependsOnMethods = "testJWTGrantTypeWithConvertOIDCDialectWithoutIDPMappingWithoutSPMapping")
    public void testJWTGrantTypeWithConvertOIDCDialectWithIDPMappingWithSPMappingWithAddRemainingUserAttributes()
            throws Exception {

        updateIdentityProviderWithClaimMappings();
        ServiceProvider serviceProvider = appMgtclient.getApplication(SERVICE_PROVIDER_NAME);
        serviceProvider = setServiceProviderClaimConfig(serviceProvider);
        appMgtclient.updateApplicationData(serviceProvider);
        serverConfigurationManager.restoreToLastConfiguration();
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        changeISConfiguration("jwt-token-issuer-addremaininguserattribute.xml");
        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        OIDCTokens oidcTokens = makeJWTBearerGrantRequest();
        Assert.assertEquals(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(COUNTRY_NEW_OIDC_CLAIM),
                COUNTRY_CLAIM_VALUE, "User claims are not mapped correctly when AddRemainingUserAttributes and "
                        + "ConvertToOIDCDialect is set to true in identity.xml");
        Assert.assertEquals(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(EMAIL_OIDC_CLAIM), EMAIL_CLAIM_VALUE,
                "User claims are not mapped correctly when AddRemainingUserAttributes and "
                        + "ConvertToOIDCDialect is set to true in identity.xml");
        Assert.assertNull(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(COUNTRY_LOCAL_CLAIM_URI),
                "User claims conversion happened wrongly.");
        Assert.assertNull(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(COUNTRY_OIDC_CLAIM),
                "Duplicated claims while adding missing attributes.");
    }

    @Test(description = "This test case tests refresh token flow of JWTBearerGrant.", dependsOnMethods =
            "testJWTGrantTypeWithConvertOIDCDialectWithIDPMappingWithSPMappingWithAddRemainingUserAttributes")
    public void testRefreshTokenFlow() throws Exception {

        AuthorizationGrant refreshGrant = new RefreshTokenGrant(new RefreshToken(refreshToken));
        OIDCTokens oidcTokens = makeTokenRequest(refreshGrant);
        Assert.assertEquals(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(COUNTRY_NEW_OIDC_CLAIM),
                COUNTRY_CLAIM_VALUE, "User claims are not mapped correctly when AddRemainingUserAttributes and "
                        + "ConvertToOIDCDialect is set to true in identity.xml");
        Assert.assertEquals(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(EMAIL_OIDC_CLAIM), EMAIL_CLAIM_VALUE,
                "User claims are not mapped correctly when AddRemainingUserAttributes and "
                        + "ConvertToOIDCDialect is set to true in identity.xml");
        Assert.assertNull(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(COUNTRY_LOCAL_CLAIM_URI),
                "User claims conversion happened wrongly.");
        Assert.assertNull(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(COUNTRY_OIDC_CLAIM),
                "Duplicated claims while adding missing attributes.");
    }

    /**
     * To make the JWT Bearer Grant request.
     *
     * @return OIDC Tokens.
     * @throws java.text.ParseException Parse Exception.
     * @throws URISyntaxException       URI Syntax Exception.
     * @throws IOException              IO Exception.
     * @throws ParseException           Parse Exception.
     */
    private OIDCTokens makeJWTBearerGrantRequest()
            throws java.text.ParseException, URISyntaxException, IOException, ParseException {

        SignedJWT signedJWT = SignedJWT.parse(jwtAssertion);
        AuthorizationGrant jwtGrant = new JWTBearerGrant(signedJWT);
        return makeTokenRequest(jwtGrant);
    }

    /**
     * To make a token request with specified grant.
     *
     * @param authorizationGrant Relevant authorization grant.
     * @return OIDC tokens coming from request.
     * @throws URISyntaxException URI Syntax Exception.
     * @throws IOException        IO Exception.
     * @throws ParseException     Parse Exception.
     */
    private OIDCTokens makeTokenRequest(AuthorizationGrant authorizationGrant)
            throws URISyntaxException, IOException, ParseException {

        ClientID clientID = new ClientID(consumerKey);
        Secret clientSecret = new Secret(consumerSecret);
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);
        URI tokenEndpoint = new URI(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, authorizationGrant,
                new Scope(OAuth2Constant.OAUTH2_SCOPE_OPENID));

        HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();
        Assert.assertNotNull(tokenHTTPResp, "JWT access token http response is null.");

        TokenResponse tokenResponse = OIDCTokenResponseParser.parse(tokenHTTPResp);
        Assert.assertNotNull(tokenResponse, "Token response of JWT access token response is null.");

        Assert.assertFalse(tokenResponse instanceof TokenErrorResponse, "JWT access token response contains errors.");

        OIDCTokenResponse oidcTokenResponse = (OIDCTokenResponse) tokenResponse;
        JSONObject jsonObject = ((OIDCTokenResponse) tokenResponse).toJSONObject();
        refreshToken = String.valueOf(jsonObject.get("refresh_token"));
        OIDCTokens oidcTokens = oidcTokenResponse.getOIDCTokens();
        Assert.assertNotNull(oidcTokens, "OIDC Tokens object is null in JWT token");
        return oidcTokens;
    }

    /**
     * To reset configurations to default configurations after the change needed for
     *
     * @throws Exception Exception
     */
    private void resetISConfiguration() throws Exception {

        log.info("Replacing identity.xml with default configurations");
        serverConfigurationManager.restoreToLastConfiguration();
    }

    /**
     * To create consumer application that supports JWT bearer grant type
     *
     * @return OauthConsumerApp
     */
    private OAuthConsumerAppDTO createApplicationWithJWTGrantType() {

        OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
        appDTO.setApplicationName(OAuth2Constant.OAUTH_APPLICATION_NAME);
        appDTO.setCallbackUrl(OAuth2Constant.CALLBACK_URL);
        appDTO.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
        appDTO.setIdTokenExpiryTime(3600);
        appDTO.setGrantTypes("authorization_code implicit password client_credentials refresh_token "
                + "urn:ietf:params:oauth:grant-type:saml2-bearer iwa:ntlm urn:ietf:params:oauth:grant-type:jwt-bearer");
        return appDTO;
    }

    /**
     * To add the identity provider.
     *
     * @throws Exception Exception.
     */
    private void addFederatedIdentityProvider() throws Exception {

        IdentityProvider residentIdP = identityProviderMgtServiceClient.getResidentIdP();
        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setEnable(true);
        identityProvider.setAlias(alias);
        identityProvider.setDisplayName(issuer);
        identityProvider.setCertificate(residentIdP.getCertificate());
        identityProvider.setIdentityProviderName(issuer);
        identityProviderMgtServiceClient.addIdP(identityProvider);
    }

    /**
     * To update identity provider with claim mappings.
     *
     * @throws Exception Exception.
     */
    private void updateIdentityProviderWithClaimMappings() throws Exception {

        IdentityProvider identityProvider = identityProviderMgtServiceClient.getIdPByName(issuer);
        ClaimConfig claimConfig = new ClaimConfig();
        Claim emailClaim = new Claim();
        emailClaim.setClaimUri(COUNTRY_LOCAL_CLAIM_URI);
        Claim emailRemoteClaim = new Claim();
        emailRemoteClaim.setClaimUri(COUNTRY_OIDC_CLAIM);
        ClaimMapping emailClaimMapping = new ClaimMapping();
        emailClaimMapping.setLocalClaim(emailClaim);
        emailClaimMapping.setRemoteClaim(emailRemoteClaim);
        claimConfig.addIdpClaims(emailRemoteClaim);
        claimConfig.setClaimMappings(new ClaimMapping[]{emailClaimMapping});
        identityProvider.setClaimConfig(claimConfig);
        identityProviderMgtServiceClient.updateIdP(issuer, identityProvider);
    }

    /**
     * To change the identity.xml with the configurations needed.
     *
     * @param fileName the name of the file.
     * @throws Exception Exception
     */
    private void changeISConfiguration(String fileName) throws Exception {

        log.info("Replacing identity.xml to use the JWT Token Generator instead of default token generator");
        String carbonHome = CarbonUtils.getCarbonHome();
        File identityXML = new File(
                carbonHome + File.separator + "repository" + File.separator + "conf" + File.separator + "identity"
                        + File.separator + "identity.xml");
        File configuredIdentityXML = new File(
                getISResourceLocation() + File.separator + "oauth" + File.separator + fileName);
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredIdentityXML, identityXML, true);
        serverConfigurationManager.restartGracefully();
    }

    /**
     * Add a new with 3 user claims.
     *
     * @throws RemoteException                                    Remote Exception.
     * @throws UserAdminUserAdminException                        User Admin User Admin Exception.
     * @throws UserProfileMgtServiceUserProfileExceptionException User Profile Mgt Service User Profile Exception.
     */
    private void addNewUserWithClaims()
            throws RemoteException, UserAdminUserAdminException, UserProfileMgtServiceUserProfileExceptionException {

        String profileName = "default";
        String adminRoleName = "admin";
        String countryLocalClaimUri = "http://wso2.org/claims/country";
        String givenNameLocalClaimUri = "http://wso2.org/claims/givenname";

        userManagementClient.addUser(JWT_USER, JWT_USER, new String[]{adminRoleName}, profileName);
        UserProfileDTO profile = new UserProfileDTO();
        profile.setProfileName(profileName);
        UserFieldDTO country = new UserFieldDTO();
        country.setClaimUri(countryLocalClaimUri);
        country.setFieldValue(COUNTRY_CLAIM_VALUE);
        UserFieldDTO givenname = new UserFieldDTO();
        givenname.setClaimUri(givenNameLocalClaimUri);
        givenname.setFieldValue(JWT_USER);
        UserFieldDTO email = new UserFieldDTO();
        email.setClaimUri(EMAIL_LOCAL_CLAIM_URI);
        email.setFieldValue(EMAIL_CLAIM_VALUE);
        UserFieldDTO[] fields = new UserFieldDTO[3];
        fields[0] = country;
        fields[1] = givenname;
        fields[2] = email;
        profile.setFieldValues(fields);
        userProfileMgtServiceClient.setUserProfile(JWT_USER, profile);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApplication();
        removeOAuthApplicationData();
        identityProviderMgtServiceClient = new IdentityProviderMgtServiceClient(sessionCookie, backendURL);
        identityProviderMgtServiceClient.deleteIdP(issuer);
        resetISConfiguration();
    }

    /**
     * Change the OIDC dialect claim for local claim country.
     *
     * @throws RemoteException                                      Remote Exception.
     * @throws ClaimMetadataManagementServiceClaimMetadataException Claim
     *                                                              MetadataManagementServiceClaimMetadataException
     */
    private void changeCountryOIDDialect()
            throws RemoteException, ClaimMetadataManagementServiceClaimMetadataException {

        ClaimMetadataManagementServiceClient claimMetadataManagementServiceClient = new ClaimMetadataManagementServiceClient(
                backendURL, sessionCookie);
        claimMetadataManagementServiceClient
                .removeExternalClaim(OAuth2ServiceAuthCodeGrantOpenIdRequestObjectTestCase.OIDC_CLAIM_DIALECT,
                        COUNTRY_OIDC_CLAIM);
    }
}
