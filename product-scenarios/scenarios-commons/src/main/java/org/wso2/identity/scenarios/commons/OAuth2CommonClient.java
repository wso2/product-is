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

package org.wso2.identity.scenarios.commons;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.identity.scenarios.commons.clients.oauth.OauthAdminClient;
import org.wso2.identity.scenarios.commons.data.DeploymentDataHolder;
import org.wso2.identity.scenarios.commons.util.Constants;
import org.wso2.identity.scenarios.commons.util.DataExtractUtil;
import org.wso2.identity.scenarios.commons.util.OAuth2Constants;
import org.wso2.identity.scenarios.commons.util.SSOConstants;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.identity.scenarios.commons.util.Constants.CONTENT_TYPE_APPLICATION_FORM;
import static org.wso2.identity.scenarios.commons.util.Constants.CONTENT_TYPE_APPLICATION_JSON;
import static org.wso2.identity.scenarios.commons.util.Constants.HTTP_RESPONSE_HEADER_LOCATION;
import static org.wso2.identity.scenarios.commons.util.Constants.IS_HTTPS_URL;

public class OAuth2CommonClient {

    private static final Log log = LogFactory.getLog(OAuth2CommonClient.class);

    private static final String DCR_REQUESTS_LOCATION = "dcr.requests.location";

    private HTTPCommonClient httpCommonClient;

    private String authorizeEndpoint;

    private String tokenEndpoint;

    private String dcrEndpoint;

    private String introspectEndpoint;

    private JSONParser parser;

    private String tenantDomain;

    public OAuth2CommonClient(HTTPCommonClient httpCommonClient, String serverHTTPsUrl, String tenantDomain) {

        this.httpCommonClient = httpCommonClient;
        this.tenantDomain = tenantDomain;
        this.parser = new JSONParser();
        setEndpoints(serverHTTPsUrl);
    }

    /**
     * Create OAuth2 application using DCR endpoint.
     *
     * @param dcrRequestJSON DCR request body.
     * @param username       Username.
     * @param password       Password.
     * @return Http response.
     * @throws IOException IO Exception.
     */
    public HttpResponse createOAuth2Application(JSONObject dcrRequestJSON, String username, String password)
            throws IOException {

        return httpCommonClient
                .sendPostRequestWithJSON(dcrEndpoint, dcrRequestJSON, getCommonHeadersJSON(username, password));
    }

    /**
     * Create OAuth2 application using DCR endpoint.
     *
     * @param dcrRequestFile File name which contains the DCR request body.
     * @param username       Username.
     * @param password       Password.
     * @return Http response.
     * @throws IOException    IO Exception.
     * @throws ParseException Parse Exception.
     */
    public HttpResponse createOAuth2Application(String dcrRequestFile, String username, String password)
            throws IOException, ParseException {

        return httpCommonClient.sendPostRequestWithJSON(dcrEndpoint, getRequestJSON(dcrRequestFile),
                getCommonHeadersJSON(username, password));
    }

    /**
     * Delete OAuth2 application by client id.
     *
     * @param clientId Client id.
     * @param username Username.
     * @param password Password.
     * @return Http response.
     * @throws IOException IO Exception.
     */
    public HttpResponse deleteOAuth2Application(String clientId, String username, String password) throws IOException {

        return httpCommonClient
                .sendDeleteRequest(dcrEndpoint + "/" + clientId, getCommonHeadersJSON(username, password));
    }

    /**
     * Validate application creation response.
     *
     * @param dcrRequestFile File name which contains the DCR request body.
     * @param responseJSON   DCR response body.
     * @throws IOException    IO Exception.
     * @throws ParseException Parse Exception.
     */
    public void validateApplicationCreationResponse(String dcrRequestFile, JSONObject responseJSON)
            throws IOException, ParseException {

        validateApplicationCreationResponse(getRequestJSON(dcrRequestFile), responseJSON);
    }

    /**
     * Validate application creation response.
     *
     * @param requestJSON  DCR request body.
     * @param responseJSON DCR response body.
     */
    public void validateApplicationCreationResponse(JSONObject requestJSON, JSONObject responseJSON) {

        assertEquals(responseJSON.get(OAuth2Constants.DCRResponseElements.CLIENT_NAME).toString(),
                requestJSON.get(OAuth2Constants.DCRRequestElements.CLIENT_NAME).toString(),
                "Received client_name value is invalid. Request Object: " + requestJSON.toJSONString()
                        + ", Response Object: " + responseJSON.toJSONString());

        if (requestJSON.get(OAuth2Constants.DCRRequestElements.CLIENT_ID) != null) {
            assertEquals(responseJSON.get(OAuth2Constants.DCRResponseElements.CLIENT_ID).toString(),
                    requestJSON.get(OAuth2Constants.DCRRequestElements.CLIENT_ID).toString(),
                    "Received client_id value is invalid. Request Object: " + requestJSON.toJSONString()
                            + ", Response Object: " + responseJSON.toJSONString());
        } else {
            assertNotNull(responseJSON.get(OAuth2Constants.DCRResponseElements.CLIENT_ID),
                    "Received client_id value is null. Request Object: " + requestJSON.toJSONString()
                            + ", Response Object: " + responseJSON.toJSONString());
        }

        if (requestJSON.get(OAuth2Constants.DCRRequestElements.CLIENT_SECRET) != null) {
            assertEquals(responseJSON.get(OAuth2Constants.DCRResponseElements.CLIENT_SECRET).toString(),
                    requestJSON.get(OAuth2Constants.DCRRequestElements.CLIENT_SECRET).toString(),
                    "Received client_secret value is invalid. Request Object: " + requestJSON.toJSONString()
                            + ", Response Object: " + responseJSON.toJSONString());
        } else {
            assertNotNull(responseJSON.get(OAuth2Constants.DCRResponseElements.CLIENT_SECRET),
                    "Received client_secret value is null. Request Object: " + requestJSON.toJSONString()
                            + ", Response Object: " + responseJSON.toJSONString());
        }

        if (requestJSON.get(OAuth2Constants.DCRRequestElements.REDIRECT_URIS) != null
                && ((JSONArray) requestJSON.get(OAuth2Constants.DCRResponseElements.REDIRECT_URIS)).size() > 0) {

            JSONArray redirectUris = (JSONArray) requestJSON.get(OAuth2Constants.DCRRequestElements.REDIRECT_URIS);

            assertNotNull(responseJSON.get(OAuth2Constants.DCRResponseElements.REDIRECT_URIS),
                    "Received redirect_uris value is null. Request Object: " + requestJSON.toJSONString()
                            + ", Response Object: " + responseJSON.toJSONString());

            JSONArray returnedRedirectUris = (JSONArray) responseJSON
                    .get(OAuth2Constants.DCRResponseElements.REDIRECT_URIS);
            assertEquals(returnedRedirectUris.size(), redirectUris.size(),
                    "Received redirect_uris size is invalid. Request Object: " + requestJSON.toJSONString()
                            + ", Response Object: " + responseJSON.toJSONString());

            for (Object redirectUrl : redirectUris) {
                assertTrue(returnedRedirectUris.contains(redirectUrl),
                        "Received redirect_uris content is invalid. Request Object: " + requestJSON.toJSONString()
                                + ", Response Object: " + responseJSON.toJSONString());
            }
        }
    }

    /**
     * Send authorize GET request.
     *
     * @param clientId     Client id of the OAuth2 application.
     * @param scope        Scope for the request.
     * @param redirectUri  Redirect uri.
     * @param responseType Response type.
     * @param params       Additional params.
     * @return Http response.
     * @throws IOException        IO Exception.
     * @throws URISyntaxException URI Syntax Exception.
     */
    public HttpResponse sendAuthorizeGet(String clientId, String scope, String redirectUri, String responseType,
            Map<String, String> params) throws IOException, URISyntaxException {

        Map<String, String> requestParams = new HashMap<>();
        requestParams.put(OAuth2Constants.RequestParams.RESPONSE_TYPE, responseType);
        requestParams.put(OAuth2Constants.RequestParams.CLIENT_ID, clientId);
        requestParams.put(OAuth2Constants.RequestParams.REDIRECT_URI, redirectUri);
        if (StringUtils.isNotBlank(scope)) {
            requestParams.put(OAuth2Constants.RequestParams.SCOPE, scope);
        }
        if (params != null) {
            requestParams.putAll(params);
        }
        log.info("###>: " + authorizeEndpoint);
        log.info("###>: " + responseType);
        log.info("###>: " + clientId);
        log.info("###>: " + redirectUri);
        log.info("###>: " + scope);
        HttpResponse response = httpCommonClient.sendGetRequest(authorizeEndpoint, requestParams, null);
        Header locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
        if (locationHeader != null) {
            log.info("###>: " + locationHeader.getValue());
        }
        log.info(response.getStatusLine().getStatusCode());
        return response;
    }

    /**
     * Sends OAuth consent submit request. This consent will be an 'Approve Once' or 'Approve Always' or 'Deny' submit
     * and only for OAuth and not for user attribute sharing related consent.
     *
     * @param sessionDataKeyConsent Session key related to the consent request.
     * @param approveType           'Approve Once' or 'Approve Always' or 'Deny'.
     * @return HttpResponse with the consent submit response.
     * @throws Exception If error occurs while sending the consent submit.
     */
    public HttpResponse sendOAuthConsentApprovePost(String sessionDataKeyConsent, String approveType) throws Exception {

        List<NameValuePair> requestParameters = new ArrayList<>();
        requestParameters.add(new BasicNameValuePair(OAuth2Constants.RequestParams.CONSENT, approveType));
        requestParameters.add(new BasicNameValuePair("consent_0", "on"));
        requestParameters.add(new BasicNameValuePair("user_claims_consent", "true"));
        requestParameters.add(new BasicNameValuePair(SSOConstants.CommonAuthParams.SESSION_DATA_KEY_CONSENT,
                sessionDataKeyConsent));

        return httpCommonClient.sendPostRequestWithParameters(authorizeEndpoint, requestParameters, null);
    }

    /**
     * Send code grant token request.
     *
     * @param authorizationCode Authorization code.
     * @param redirectUri       Redirect uri.
     * @param clientId          Client id.
     * @param clientSecret      Client secret.
     * @return Http response.
     * @throws IOException IO Exception.
     */
    public HttpResponse sendCodeGrantTokenRequest(String authorizationCode, String redirectUri, String clientId,
            String clientSecret, String pkceVerifier) throws IOException {

        List<NameValuePair> requestParameters = new ArrayList<>();
        requestParameters.add(new BasicNameValuePair(OAuth2Constants.RequestParams.GRANT_TYPE,
                OAuth2Constants.GrantTypes.AUTHORIZATION_CODE));
        requestParameters.add(new BasicNameValuePair(OAuth2Constants.RequestParams.CODE, authorizationCode));
        requestParameters.add(new BasicNameValuePair(OAuth2Constants.RequestParams.REDIRECT_URI, redirectUri));
        if(StringUtils.isNotBlank(pkceVerifier)){
            requestParameters.add(new BasicNameValuePair(OAuth2Constants.PKCERequestElements.CODE_VERIFIER, pkceVerifier));
        }

        return httpCommonClient.sendPostRequestWithParameters(tokenEndpoint, requestParameters,
                getCommonHeadersURLEncoded(clientId, clientSecret));
    }

    /**
     * Send client credentials grant token request.
     *
     * @param clientId     Client id.
     * @param clientSecret Client secret.
     * @param scope        Scope.
     * @return Http response.
     * @throws IOException IO Exception.
     */
    public HttpResponse sendClientCredentialsGrantTokenRequest(String clientId, String clientSecret, String scope)
            throws IOException {

        List<NameValuePair> requestParameters = new ArrayList<>();
        requestParameters.add(new BasicNameValuePair(OAuth2Constants.RequestParams.GRANT_TYPE,
                OAuth2Constants.GrantTypes.CLIENT_CREDENTIALS));
        if (StringUtils.isNotBlank(scope)) {
            requestParameters.add(new BasicNameValuePair(OAuth2Constants.RequestParams.SCOPE, scope));
        }

        return httpCommonClient.sendPostRequestWithParameters(tokenEndpoint, requestParameters,
                getCommonHeadersURLEncoded(clientId, clientSecret));
    }

    /**
     * Send password grant token request.
     *
     * @param username     Username.
     * @param password     Password.
     * @param clientId     Client id.
     * @param clientSecret Client secret.
     * @param scope        Scope.
     * @return Http response.
     * @throws IOException IO Exception.
     */
    public HttpResponse sendPasswordGrantTokenRequest(String username, String password, String clientId,
            String clientSecret, String scope) throws IOException {

        List<NameValuePair> requestParameters = new ArrayList<>();
        requestParameters.add(new BasicNameValuePair(OAuth2Constants.RequestParams.GRANT_TYPE,
                OAuth2Constants.GrantTypes.PASSWORD));
        requestParameters.add(new BasicNameValuePair(OAuth2Constants.RequestParams.USERNAME, username));
        requestParameters.add(new BasicNameValuePair(OAuth2Constants.RequestParams.PASSWORD, password));
        if (StringUtils.isNotBlank(scope)) {
            requestParameters.add(new BasicNameValuePair(OAuth2Constants.RequestParams.SCOPE, scope));
        }

        return httpCommonClient.sendPostRequestWithParameters(tokenEndpoint, requestParameters,
                getCommonHeadersURLEncoded(clientId, clientSecret));
    }

    /**
     * Send refresh token grant request.
     *
     * @param clientId     client id.
     * @param clientSecret client secret.
     * @param refreshToken refresh token.
     * @param scope        scope.
     * @return Http response.
     * @throws IOException IO Exception.
     */
    public HttpResponse sendRefreshTokenRequest(String clientId, String clientSecret, String refreshToken,
                                                String scope) throws IOException {

        List<NameValuePair> requestParameters = new ArrayList<>();
        requestParameters.add(new BasicNameValuePair(OAuth2Constants.RequestParams.GRANT_TYPE,
                OAuth2Constants.GrantTypes.REFRESH_TOKEN));
        requestParameters.add(new BasicNameValuePair(OAuth2Constants.RequestParams.REFRESH_TOKEN, refreshToken));
        if (StringUtils.isNotBlank(scope)) {
            requestParameters.add(new BasicNameValuePair(OAuth2Constants.RequestParams.SCOPE, scope));
        }
        return httpCommonClient.sendPostRequestWithParameters(tokenEndpoint, requestParameters,
                getCommonHeadersURLEncoded(clientId, clientSecret));
    }

    /**
     * Send introspect request.
     *
     * @param token    Access token.
     * @param username Username.
     * @param password Password.
     * @return Http response.
     * @throws IOException IO Exception.
     */
    public HttpResponse sendIntrospectRequest(String token, String username, String password) throws IOException {

        List<NameValuePair> requestParameters = new ArrayList<>();
        requestParameters.add(new BasicNameValuePair(OAuth2Constants.RequestParams.TOKEN, token));

        return httpCommonClient.sendPostRequestWithParameters(introspectEndpoint, requestParameters,
                getCommonHeadersURLEncoded(username, password));
    }

    /**
     * Validate token response.
     *
     * @param responseJSON response JSON.
     */
    public void validateAccessToken(JSONObject responseJSON, boolean refreshTokenAvailable) {

        assertNotNull(responseJSON.get(OAuth2Constants.TokenResponseElements.ACCESS_TOKEN),
                "access_token parameter is not available.");

        if (refreshTokenAvailable) {
            assertNotNull(responseJSON.get(OAuth2Constants.TokenResponseElements.REFRESH_TOKEN),
                    "refresh_token parameter is not available.");
        }

        assertEquals(responseJSON.get(OAuth2Constants.TokenResponseElements.TOKEN_TYPE).toString(),
                OAuth2Constants.TokenTypes.BEARER, "token_type parameter is invalid.");

        assertNotNull(responseJSON.get(OAuth2Constants.TokenResponseElements.EXPIRES_IN),
                "expires_in parameter is not available.");
    }

    /**
     * Validate token response.
     *
     * @param responseJSON response JSON.
     */
    public void validateIntrospectResponse(JSONObject responseJSON) {

        assertEquals(responseJSON.get(OAuth2Constants.IntrospectResponseElements.ACTIVE).toString(), "true",
                "Access token is invalid.");
    }

    /**
     * Get authorization code
     *
     * @param response HttpResponse with the authorization code.
     * @return Authorization code.
     * @throws Exception Exception.
     */
    public String getAuthorizeCode(HttpResponse response) throws Exception {

        if (response == null) {
            return null;
        }

        Header locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
        if (locationHeader != null) {
            return DataExtractUtil.getParamFromURIString(locationHeader.getValue(), OAuth2Constants.ResponseTypes.CODE);
        }
        return null;
    }

    /**
     * Get access token in the location header.
     *
     * @param response HttpResponse with the access token.
     * @return Access code.
     * @throws Exception Exception.
     */
    public String getAccessToken(HttpResponse response) throws Exception {

        if (response == null) {
            return null;
        }

        Header locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
        if (locationHeader != null) {

            return DataExtractUtil.getParamFromURIString(locationHeader.getValue().replace("#", "?"),
                    OAuth2Constants.TokenResponseElements.ACCESS_TOKEN);
        }
        return null;
    }

    private Header[] getCommonHeadersJSON(String username, String password) {

        return new Header[] {
                new BasicHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON),
                new BasicHeader(HttpHeaders.AUTHORIZATION, httpCommonClient.getBasicAuthorizeHeader(username, password))
        };
    }

    private Header[] getCommonHeadersURLEncoded(String username, String password) {

        return new Header[] {
                new BasicHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_APPLICATION_FORM),
                new BasicHeader(HttpHeaders.AUTHORIZATION, httpCommonClient.getBasicAuthorizeHeader(username, password))
        };
    }

    private JSONObject getRequestJSON(String fileName) throws IOException, ParseException {

        return (JSONObject) parser.parse(new FileReader(getFilePath(DCR_REQUESTS_LOCATION, fileName)));
    }

    private String getFilePath(String folderPath, String fileName) throws FileNotFoundException {

        Path path = Paths.get(System.getProperty(folderPath) + fileName);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("Failed to find file: " + path.toString());
        }
        return path.toString();
    }

    private void setEndpoints(String serverHTTPsUrl) {

        if (StringUtils.isBlank(serverHTTPsUrl)) {
            serverHTTPsUrl = DeploymentDataHolder.getInstance().getProperties().getProperty(IS_HTTPS_URL);
        }

        this.authorizeEndpoint = serverHTTPsUrl + "/oauth2/authorize";
        this.tokenEndpoint = serverHTTPsUrl + "/oauth2/token";

        if (tenantDomain == null || SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            this.dcrEndpoint = serverHTTPsUrl + "/api/identity/oauth2/dcr/v1.1/register";
            this.introspectEndpoint = serverHTTPsUrl + "/oauth2/introspect";
        } else {
            this.dcrEndpoint = serverHTTPsUrl + "/t/" + tenantDomain + "/api/identity/oauth2/dcr/v1.1/register";
            this.introspectEndpoint = serverHTTPsUrl + "/t/" + tenantDomain + "/oauth2/introspect";
        }
    }

    /**
     * Returns OAuthConsumerAppDTO of a service provider.
     *
     * @param serviceProvider
     * @param oauthAdminClient
     * @return OAuthConsumerAppDTO
     * @throws Exception
     */
    public OAuthConsumerAppDTO getOAuthConsumerApp(ServiceProvider serviceProvider, OauthAdminClient oauthAdminClient) throws Exception {

        InboundAuthenticationRequestConfig[] inboundAuthRequestConfigs = serviceProvider
                .getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs();
        for (InboundAuthenticationRequestConfig inboundAuthRequestConfig : inboundAuthRequestConfigs) {
            if (Constants.INBOUND_AUTH_TYPE_OAUTH2.equals(inboundAuthRequestConfig.getInboundAuthType())) {
                return getOAuthSSOServiceProviderByName(serviceProvider.getApplicationName(), oauthAdminClient);
            }
        }
        return null;
    }

    private OAuthConsumerAppDTO getOAuthSSOServiceProviderByName(String spName, OauthAdminClient oauthAdminClient) throws Exception {

        OAuthConsumerAppDTO ssoSPInfoDTO = oauthAdminClient.getOAuthAppByName(spName);
        if (ssoSPInfoDTO != null) {
            return ssoSPInfoDTO;
        }
        return null;
    }
}
