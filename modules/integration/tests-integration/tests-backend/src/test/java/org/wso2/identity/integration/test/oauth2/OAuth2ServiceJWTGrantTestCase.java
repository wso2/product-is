/*
 * Copyright (c) 2018, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.identity.integration.test.oauth2;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.nimbusds.oauth2.sdk.TokenRevocationRequest;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.oauth2.sdk.token.Token;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationPatchModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ClaimConfiguration.DialectEnum;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.rest.api.server.claim.management.v1.model.ExternalClaimReq;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.Claims;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.IdentityProviderPOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.idp.v1.model.IdentityProviderPOSTRequest.Certificate;
import org.wso2.identity.integration.test.rest.api.server.oidc.scope.management.v1.model.ScopeUpdateRequest;
import org.wso2.identity.integration.test.rest.api.user.common.model.Email;
import org.wso2.identity.integration.test.rest.api.user.common.model.ListObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.Name;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.RoleItemAddGroupobj;
import org.wso2.identity.integration.test.rest.api.user.common.model.ScimSchemaExtensionSystem;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.restclients.ClaimManagementRestClient;
import org.wso2.identity.integration.test.restclients.IdpMgtRestClient;
import org.wso2.identity.integration.test.restclients.OIDCScopeMgtRestClient;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * This is a test class for self contained access tokens with JWT bearer grant type.
 */
public class OAuth2ServiceJWTGrantTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String USERS_PATH = "users";
    private static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----\n";
    private static final String END_CERTIFICATE = "\n-----END CERTIFICATE-----";
    private static final String JWKS_BASE_PATH = "/oauth2/jwks";
    private static final String COUNTRY_CLAIM_VALUE = "USA";
    private static final String COUNTRY_OIDC_CLAIM = "country";
    private static final String COUNTRY_NEW_OIDC_CLAIM = "customclaim";
    private static final String COUNTRY_LOCAL_CLAIM_URI = "http://wso2.org/claims/country";
    private static final String STATE_LOCAL_CLAIM_URI = "http://wso2.org/claims/stateorprovince";
    private static final String EMAIL_OIDC_CLAIM = "email";
    private static final String EMAIL_CLAIM_VALUE = "email@email.com";
    private static final String EMAIL_LOCAL_CLAIM_URI = "http://wso2.org/claims/emailaddress";
    private static final String ENCODED_OIDC_CLAIM_DIALECT = "aHR0cDovL3dzbzIub3JnL29pZGMvY2xhaW0";
    private static final String COUNTRY_CLAIM_ID = "Y291bnRyeQ";
    private static final String openIdScope = "openid";
    private static final String JWT_USER = "jwtUser";
    private static final String JWT_USER_PASSWORD = "JwtUser@123";

    protected Log log = LogFactory.getLog(getClass());
    private ServerConfigurationManager serverConfigurationManager;
    private SCIM2RestClient scim2RestClient;
    private ClaimManagementRestClient claimManagementRestClient;
    private OIDCScopeMgtRestClient oidcScopeMgtRestClient;
    private IdpMgtRestClient idpMgtRestClient;

    private String jwtAssertion;
    private String alias;
    private String issuer;
    private String refreshToken;
    private String applicationId;
    private String userId;
    private String idpId;
    private String countryClaimId;

    @BeforeClass
    public void setup() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        changeISConfiguration("jwt_token_issuer_enabled.toml");
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        ApplicationResponseModel application = createApplicationWithJWTGrantType();
        applicationId = application.getId();
        OpenIDConnectConfiguration oidcConfig = getOIDCInboundDetailsOfApplication(applicationId);
        consumerKey = oidcConfig.getClientId();
        consumerSecret = oidcConfig.getClientSecret();

        scim2RestClient = new SCIM2RestClient(serverURL, tenantInfo);
        claimManagementRestClient =  new ClaimManagementRestClient(serverURL, tenantInfo);
        oidcScopeMgtRestClient =  new OIDCScopeMgtRestClient(serverURL, tenantInfo);
        idpMgtRestClient = new IdpMgtRestClient(serverURL, tenantInfo);

        addAdminUser();
        changeCountryOIDCDialect();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApp(applicationId);
        scim2RestClient.deleteUser(userId);
        idpMgtRestClient.deleteIdp(idpId);
        claimManagementRestClient.deleteExternalClaim(ENCODED_OIDC_CLAIM_DIALECT, countryClaimId);

        restClient.closeHttpClient();
        scim2RestClient.closeHttpClient();
        oidcScopeMgtRestClient.closeHttpClient();
        idpMgtRestClient.closeHttpClient();

        resetISConfiguration();
    }

    @Test(description = "This test case tests the JWT self contained access token generation using password grant "
            + "type.")
    public void testPasswordGrantBasedSelfContainedAccessTokenGeneration()
            throws IOException, URISyntaxException, ParseException, java.text.ParseException {

        Secret password = new Secret(JWT_USER_PASSWORD);
        AuthorizationGrant passwordGrant = new ResourceOwnerPasswordCredentialsGrant(JWT_USER, password);
        ClientID clientID = new ClientID(consumerKey);
        Secret clientSecret = new Secret(consumerSecret);
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);
        URI tokenEndpoint = new URI(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, passwordGrant,
                new Scope(OAuth2Constant.OAUTH2_SCOPE_OPENID + " " + OAuth2Constant.OAUTH2_SCOPE_EMAIL));

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
        Assert.assertEquals(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(COUNTRY_NEW_OIDC_CLAIM),
                COUNTRY_CLAIM_VALUE, "Requested user claims is not returned back in self contained access token based" +
                        " on password claim.");
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
    }

    @Test(description = "This test case tests the behaviour when ConvertOIDCDialect is set to true in identity.xml "
            + "and when there is no mapping IDP side but with mapping in SP side and AddRemainingUserAttributes is "
            + "false", dependsOnMethods = "testPasswordGrantBasedSelfContainedAccessTokenGeneration")
    public void testJWTGrantTypeWithConvertOIDCDialectWithoutIDPMappingWithSPMapping() throws Exception {

        addFederatedIdentityProvider();
        OIDCTokens oidcTokens = makeJWTBearerGrantRequest();
        Assert.assertEquals(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(COUNTRY_NEW_OIDC_CLAIM),
                COUNTRY_CLAIM_VALUE, "User claims is not returned back with SP claim mapping set and " +
                        "no IDP claim mapping when ConvertToOIDCDialect is set to true in identity.xml");
        Assert.assertEquals(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(EMAIL_OIDC_CLAIM), EMAIL_CLAIM_VALUE,
                "User claims is not returned back with SP claim mapping set and " +
                        "no IDP claim mapping when ConvertToOIDCDialect is set to true in identity.xml");
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

        ApplicationPatchModel applicationPatch = new ApplicationPatchModel();
        applicationPatch.setClaimConfiguration(new ClaimConfiguration().dialect(DialectEnum.LOCAL));
        updateApplication(applicationId, applicationPatch);

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

        Claims idpClaims = new Claims().provisioningClaims(new ArrayList<>());
        idpClaims.setMappings(new ArrayList<>());
        idpMgtRestClient.updateIdpClaimConfig(idpId, idpClaims);

        OIDCTokens oidcTokens = makeJWTBearerGrantRequest();
        Assert.assertEquals(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(EMAIL_OIDC_CLAIM), EMAIL_CLAIM_VALUE,
                "User claims are not proxied when there are no SP and IDP Claim mappings "
                        + "returned when ConvertToOIDCDialect is set to true in identity.xml");
        Assert.assertNull(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(COUNTRY_OIDC_CLAIM),
                "User claims contains claims which are not configured in requested scopes");
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

        ApplicationPatchModel applicationPatch = new ApplicationPatchModel();
        applicationPatch.setClaimConfiguration(setApplicationClaimConfig());
        updateApplication(applicationId, applicationPatch);

        resetISConfiguration();
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        changeISConfiguration("jwt_token_issuer_add_remaining_user_attribute.toml");
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

    @Test(description = "This test case tests access token revocation flow of JWTBearerGrant.", dependsOnMethods =
            "testRefreshTokenFlow")
    public void testAccessTokenRevokeFlow() throws Exception {

        OIDCTokens firstTokenSet = makeJWTBearerGrantRequest();
        AccessToken firstAccessToken = firstTokenSet.getAccessToken();

        makeTokenRevokeRequest(firstAccessToken);

        OIDCTokens secondTokenSet = makeJWTBearerGrantRequest();
        AccessToken secondAccessToken = secondTokenSet.getAccessToken();

        Assert.assertNotEquals(secondAccessToken.toJSONString(), firstAccessToken.toJSONString(), "Same access "
                + "token is returned even after the access token issued from JWT Bearer grant has been revoked. ");
    }

    @Test(description = "This test case tests refresh token revocation flow of JWTBearerGrant.", dependsOnMethods =
            "testAccessTokenRevokeFlow")
    public void testRefreshTokenRevokeFlow() throws Exception {

        OIDCTokens firstTokenSet = makeJWTBearerGrantRequest();
        RefreshToken firstRefreshToken = firstTokenSet.getRefreshToken();

        makeTokenRevokeRequest(firstRefreshToken);

        OIDCTokens secondTokenSet = makeJWTBearerGrantRequest();
        RefreshToken refreshToken = secondTokenSet.getRefreshToken();

        Assert.assertNotEquals(refreshToken.toJSONString(), firstRefreshToken.toJSONString(), "Same refresh "
                + "token is returned even after the refresh token issued from JWT Bearer grant has been revoked ");
    }

    @Test(description = "This test case tests the behaviour when ConvertToOIDCDialect is false in identity.xml, this "
            + "is a pass through scenarios to send the claims as it is regardless of claim mapping in SP and IDP "
            + "claim mapping", dependsOnMethods = "testRefreshTokenRevokeFlow")
    public void testJWTGrantTypeWithConvertOIDCDialectFalse() throws Exception {

        resetISConfiguration();
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        changeISConfiguration("jwt_token_issuer_convert_to_oidc.toml");
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        idpMgtRestClient.deleteIdp(idpId);
        addFederatedIdentityProvider();
        OIDCTokens oidcTokens = makeJWTBearerGrantRequest();
        Assert.assertNull(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(COUNTRY_OIDC_CLAIM),
                "User claims contains claims which are not configured in requested scopes");
        updateIdentityProviderWithClaimMappings();
        oidcTokens = makeJWTBearerGrantRequest();
        Assert.assertEquals(oidcTokens.getIDToken().getJWTClaimsSet().getClaim(EMAIL_OIDC_CLAIM), EMAIL_CLAIM_VALUE,
                "User claims are not returned back as it is when ConvertToOIDCDialect is set to false");
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
                new Scope(OAuth2Constant.OAUTH2_SCOPE_OPENID + " " + OAuth2Constant.OAUTH2_SCOPE_EMAIL));

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

    private void makeTokenRevokeRequest(Token token) throws URISyntaxException, IOException {

        ClientID clientID = new ClientID(consumerKey);
        Secret clientSecret = new Secret(consumerSecret);
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);
        URI tokenRevokeEndpoint = new URI(OAuth2Constant.TOKEN_REVOKE_ENDPOINT);
        TokenRevocationRequest revocationRequest = new TokenRevocationRequest(tokenRevokeEndpoint, clientAuth, token);

        HTTPResponse revocationResp = revocationRequest.toHTTPRequest().send();
        Assert.assertNotNull(revocationResp, "Token revocation response is null.");
    }

    /**
     * To reset configurations to default configurations after the change needed for
     *
     * @throws Exception If an error occurred while resetting IS configurations to default.
     */
    private void resetISConfiguration() throws Exception {

        log.info("Replacing identity.xml with default configurations");
        serverConfigurationManager.restoreToLastConfiguration(false);
    }

    /**
     * To create consumer application that supports JWT bearer grant type
     *
     * @return ApplicationResponseModel.
     * @throws Exception If an error occurred while creating an application with jwt grant type.
     */
    private ApplicationResponseModel createApplicationWithJWTGrantType() throws Exception {

        ApplicationModel application = new ApplicationModel();

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, "authorization_code", "implicit", "password", "client_credentials",
                "refresh_token", "urn:ietf:params:oauth:grant-type:saml2-bearer", "iwa:ntlm",
                "urn:ietf:params:oauth:grant-type:jwt-bearer");

        List<String> callBackUrls = new ArrayList<>();
        Collections.addAll(callBackUrls, OAuth2Constant.CALLBACK_URL);

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setCallbackURLs(callBackUrls);

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setName(SERVICE_PROVIDER_NAME);
        application.setIsManagementApp(true);
        application.setClaimConfiguration(setApplicationClaimConfig());

        String appId = addApplication(application);

        return getApplication(appId);
    }

    /**
     * To add the identity provider.
     *
     * @throws Exception If an error occurred while adding the identity provider.
     */
    private void addFederatedIdentityProvider() throws Exception {

        IdentityProviderPOSTRequest idpCreateReq = new IdentityProviderPOSTRequest();
        idpCreateReq.setName(issuer);
        idpCreateReq.setAlias(alias);
        idpCreateReq.setCertificate(new Certificate().addCertificates(getEncodedCertificate()));
        idpId = idpMgtRestClient.createIdentityProvider(idpCreateReq);
    }

    /**
     * Get public certificate.
     *
     * @return Encoded certificate string.
     * @throws Exception If an error occurred while getting the public certificate.
     */
    private String getEncodedCertificate() throws Exception {

        CloseableHttpClient client = HttpClients.createDefault();
        String jwksEndpoint = serverURL + getTenantedRelativePath(JWKS_BASE_PATH, tenantInfo.getDomain());
        String certificate = BEGIN_CERTIFICATE + getPublicCertificate(client, jwksEndpoint) + END_CERTIFICATE;

        return new String(Base64.getEncoder().encode(certificate.getBytes(StandardCharsets.UTF_8)),
                (StandardCharsets.UTF_8));
    }

    /**
     * To update identity provider with claim mappings.
     *
     * @throws Exception If an error occurred while updating identity provider with claim mappings.
     */
    private void updateIdentityProviderWithClaimMappings() throws Exception {

        Claims.ClaimMapping claimMapping = new Claims.ClaimMapping().idpClaim(COUNTRY_NEW_OIDC_CLAIM);
        Claims.Claim localClaim = new Claims.Claim().uri(COUNTRY_LOCAL_CLAIM_URI);
        claimMapping.setLocalClaim(localClaim);
        Claims idpClaims = new Claims().provisioningClaims(new ArrayList<>());
        idpClaims.addMappings(claimMapping);
        idpMgtRestClient.updateIdpClaimConfig(idpId, idpClaims);
    }

    /**
     * To change the identity.xml with the configurations needed.
     *
     * @param fileName The name of the file.
     * @throws Exception If an error occurred while changing IS configurations.
     */
    private void changeISConfiguration(String fileName) throws Exception {

        log.info("Replacing identity.xml to use the JWT Token Generator instead of default token generator");
        String carbonHome = Utils.getResidentCarbonHome();
        File defaultTomlFile = getDeploymentTomlFile(carbonHome);
        File configuredTomlFile = new File
                (getISResourceLocation() + File.separator + "oauth" + File.separator + fileName);
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredTomlFile, defaultTomlFile, true);
        serverConfigurationManager.restartForcefully();
    }

    /**
     * Add a new user with admin role.
     *
     * @throws Exception If an error occurred while adding a new user with admin role.
     */
    private void addAdminUser() throws Exception {

        UserObject userInfo = new UserObject();
        userInfo.setUserName(JWT_USER);
        userInfo.setPassword(JWT_USER_PASSWORD);
        userInfo.setName(new Name().givenName(JWT_USER));
        userInfo.addEmail(new Email().value(EMAIL_CLAIM_VALUE));
        userInfo.setScimSchemaExtensionSystem(new ScimSchemaExtensionSystem().country(COUNTRY_CLAIM_VALUE));

        userId = scim2RestClient.createUser(userInfo);
        String roleId = scim2RestClient.getRoleIdByName("admin");

        RoleItemAddGroupobj patchRoleItem = new RoleItemAddGroupobj();
        patchRoleItem.setOp(RoleItemAddGroupobj.OpEnum.ADD);
        patchRoleItem.setPath(USERS_PATH);
        patchRoleItem.addValue(new ListObject().value(userId));

        scim2RestClient.updateUserRole(new PatchOperationRequestObject().addOperations(patchRoleItem), roleId);
    }

    /**
     * Change the OIDC dialect claim for local claim country.
     *
     * @throws Exception If an error occurred while changing the OIDC dialect claim for local claim.
     */
    private void changeCountryOIDCDialect() throws Exception {

        ExternalClaimReq updateCountryReq = new ExternalClaimReq().claimURI(COUNTRY_OIDC_CLAIM)
                .mappedLocalClaimURI(STATE_LOCAL_CLAIM_URI);
        claimManagementRestClient.updateExternalClaim(ENCODED_OIDC_CLAIM_DIALECT, COUNTRY_CLAIM_ID, updateCountryReq);

        ExternalClaimReq externalClaimReq = new ExternalClaimReq();
        externalClaimReq.setClaimURI(COUNTRY_NEW_OIDC_CLAIM);
        externalClaimReq.setMappedLocalClaimURI(COUNTRY_LOCAL_CLAIM_URI);
        countryClaimId = claimManagementRestClient.addExternalClaim(ENCODED_OIDC_CLAIM_DIALECT, externalClaimReq);

        org.json.simple.JSONObject scope = oidcScopeMgtRestClient.getScope(openIdScope);
        scope.remove("name");
        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        ScopeUpdateRequest scopeUpdateReq = jsonWriter.readValue(scope.toString(), ScopeUpdateRequest.class);
        scopeUpdateReq.addClaims(COUNTRY_NEW_OIDC_CLAIM);

        oidcScopeMgtRestClient.updateScope(openIdScope, scopeUpdateReq);
    }
}
