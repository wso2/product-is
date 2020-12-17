package org.wso2.identity.integration.test.identityServlet;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

public class ExtendSessionEndpointAuthCodeGrantTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String CALLBACK_URL = "https://localhost/callback";
    private static final String IDP_SESSION_KEY_CLAIM_NAME = "isk";
    private static final String SESSION_EXTENDER_ENDPOINT_URL = "https://localhost:9853/identity/extend-session";
    private static final String SESSION_EXTENDER_ENDPOINT_GET_URL = SESSION_EXTENDER_ENDPOINT_URL + "?%s=%s";
    private static final String SESSIONS_ENDPOINT_URI = "https://localhost:9853/api/users/v1/me/sessions";

    private CloseableHttpClient firstPartyClient;
    private CloseableHttpClient thirdPartyClient;
    private String sessionDataKey;
    private String sessionDataKeyConsent;
    private AuthorizationCode authorizationCode;
    private String idToken;
    private String idpSessionKey;
    private String authenticatingUserName;
    private String authenticatingCredential;
    private AutomationContext context;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
        context = isServer;
        this.authenticatingUserName = context.getContextTenant().getContextUser().getUserName();
        this.authenticatingCredential = context.getContextTenant().getContextUser().getPassword();
        firstPartyClient = HttpClientBuilder.create().disableRedirectHandling().build();
        thirdPartyClient = HttpClientBuilder.create().disableRedirectHandling().build();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        deleteApplication();
        removeOAuthApplicationData();

        consumerKey = null;
        consumerSecret = null;
        firstPartyClient.close();
        thirdPartyClient.close();
    }

    @Test(groups = "wso2.is", description = "Check Oauth2 application registration.")
    public void testRegisterApplication() throws Exception {

        OAuthConsumerAppDTO oAuthConsumerAppDTO = getBasicOAuthApp(CALLBACK_URL);
        ServiceProvider serviceProvider = registerServiceProviderWithOAuthInboundConfigs(oAuthConsumerAppDTO);
        Assert.assertNotNull(serviceProvider, "OAuth App creation failed.");
        Assert.assertNotNull(consumerKey, "Consumer Key is null.");
        Assert.assertNotNull(consumerSecret, "Consumer Secret is null.");
    }

    @Test(groups = "wso2.is", description = "Send authorize user request for authorization code grant type.",
            dependsOnMethods = "testRegisterApplication")
    public void testAuthCodeGrantSendAuthRequestPost() throws Exception {

        // Send a direct auth code request to IS instance.
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_RESPONSE_TYPE,
                OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_CLIENT_ID, consumerKey));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_REDIRECT_URI, CALLBACK_URL));
        urlParameters.add(new BasicNameValuePair(OAuth2Constant.OAUTH2_SCOPE, OAuth2Constant.OAUTH2_SCOPE_OPENID));

        HttpResponse response = sendPostRequestWithParameters(firstPartyClient, urlParameters,
                        OAuth2Constant.AUTHORIZE_ENDPOINT_URL);
        Assert.assertNotNull(response, "Authorization request failed. Authorized response is null");

        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains(OAuth2Constant.SESSION_DATA_KEY),
                "sessionDataKey not found in response.");

        // Extract sessionDataKey from the location value.
        sessionDataKey = DataExtractUtil.getParamFromURIString(locationValue, OAuth2Constant.SESSION_DATA_KEY);
        Assert.assertNotNull(sessionDataKey, "Session data key is null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Send login post request.",
            dependsOnMethods = "testAuthCodeGrantSendAuthRequestPost")
    public void testAuthCodeGrantSendLoginPost() throws Exception {

        sessionDataKeyConsent = getSessionDataKeyConsent(firstPartyClient, sessionDataKey);
        Assert.assertNotNull(sessionDataKeyConsent, "Invalid session key consent.");
    }

    @Test(groups = "wso2.is", description = "Send approval post request.",
            dependsOnMethods = "testAuthCodeGrantSendLoginPost")
    public void testAuthCodeGrantSendApprovalPost() throws Exception {

        HttpResponse response = sendApprovalPost(firstPartyClient, sessionDataKeyConsent);
        Assert.assertNotNull(response, "Approval request failed. response is invalid.");

        Header locationHeader =
                response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Approval request failed. Location header is null.");

        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains(OAuth2Constant.AUTHORIZATION_CODE_NAME),
                "Authorization code not found in the response.");

        // Extract authorization code from the location value.
        authorizationCode = new AuthorizationCode(DataExtractUtil.getParamFromURIString(locationValue,
                OAuth2Constant.AUTHORIZATION_CODE_NAME));
        Assert.assertNotNull(authorizationCode, "Authorization code is null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(groups = "wso2.is", description = "Send get access token request.",
            dependsOnMethods = "testAuthCodeGrantSendApprovalPost")
    public void testAuthCodeGrantSendGetTokensPost() throws Exception {

        ClientID clientID = new ClientID(consumerKey);
        Secret clientSecret = new Secret(consumerSecret);
        ClientSecretBasic clientSecretBasic = new ClientSecretBasic(clientID, clientSecret);

        URI callbackURI = new URI(CALLBACK_URL);
        AuthorizationCodeGrant authorizationCodeGrant = new AuthorizationCodeGrant(authorizationCode, callbackURI);

        TokenRequest tokenReq = new TokenRequest(new URI(OAuth2Constant.ACCESS_TOKEN_ENDPOINT), clientSecretBasic,
                authorizationCodeGrant);

        HTTPResponse tokenHTTPResp = tokenReq.toHTTPRequest().send();
        Assert.assertNotNull(tokenHTTPResp, "Access token http response is null.");

        TokenResponse tokenResponse = OIDCTokenResponseParser.parse(tokenHTTPResp);
        Assert.assertNotNull(tokenResponse, "Access token response is null.");

        Assert.assertFalse(tokenResponse instanceof TokenErrorResponse,
                "Access token response contains errors.");

        OIDCTokenResponse oidcTokenResponse = (OIDCTokenResponse) tokenResponse;
        OIDCTokens oidcTokens = oidcTokenResponse.getOIDCTokens();
        Assert.assertNotNull(oidcTokens, "OIDC Tokens object is null.");

        idToken = oidcTokens.getIDTokenString();
        Assert.assertNotNull(idToken, "ID token is null");
    }

    @Test(groups = "wso2.is",
            description = "Checks whether the IDP session key is available as a claim in the ID token",
            dependsOnMethods = "testAuthCodeGrantSendGetTokensPost")
    public void testIdTokenClaimAvailability() throws Exception {

        JWTClaimsSet claims = SignedJWT.parse(idToken).getJWTClaimsSet();
        Assert.assertNotNull(claims, "ID token claim set is null");

        idpSessionKey = (String) claims.getClaim(IDP_SESSION_KEY_CLAIM_NAME);
        Assert.assertNotNull(idpSessionKey, "IDP session key not available in ID token.");
    }

    @Test(groups = "wso2.is", description = "Sends a request for session extension with a valid cookie.",
            dependsOnMethods = "testIdTokenClaimAvailability")
    public void testSessionExtensionWithValidCookie() throws Exception {

        Long lastAccessedTimeBeforeExtension = getLastAccessedTimeOfSession();

        // A valid commonAuthIdCookie is already available in the HttpClient, which had been obtained during the
        // authorization code grant.
        HttpResponse response = sendGetRequest(firstPartyClient, SESSION_EXTENDER_ENDPOINT_URL);
        Assert.assertNotNull(response, "Session extension request failed. Response is invalid.");

        int statusCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(statusCode, HttpServletResponse.SC_OK, "Session extension failed for request with valid " +
                "cookie");
        EntityUtils.consume(response.getEntity());

        Long lastAccessedTimeAfterExtension = getLastAccessedTimeOfSession();
        Assert.assertTrue(lastAccessedTimeAfterExtension > lastAccessedTimeBeforeExtension,
                "Session has not been extended.");
    }

    @Test(groups = "wso2.is", description = "Sends a request for session extension with a valid cookie.",
            dependsOnMethods = "testSessionExtensionWithValidCookie")
    public void testSessionExtensionWithMismatchingCookieAndParameter() throws Exception {

        // A valid commonAuthIdCookie is already available in the HttpClient, which had been obtained during the
        // authorization code grant.
        String locationUrl = String.format(SESSION_EXTENDER_ENDPOINT_GET_URL, "idpSessionKey", "abcd1234");
        testSessionExtensionResponse(firstPartyClient, locationUrl, HttpServletResponse.SC_CONFLICT, "ISE-60005");
    }

    @Test(groups = "wso2.is", description = "Sends a valid request for session extension.",
            dependsOnMethods = "testSessionExtensionWithMismatchingCookieAndParameter")
    public void testSessionExtensionWithValidParameters() throws Exception {

        Long lastAccessedTimeBeforeExtension = getLastAccessedTimeOfSession();

        // Using the fresh third party client without any cookies as the firstPartyClient holds a valid cookie,
        String locationUrl = String.format(SESSION_EXTENDER_ENDPOINT_GET_URL, "idpSessionKey", idpSessionKey);
        HttpResponse response = sendGetRequest(thirdPartyClient, locationUrl);
        Assert.assertNotNull(response, "Session extension request failed. Response is invalid.");

        int statusCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(statusCode, HttpServletResponse.SC_OK, "Session extension failed for request with valid " +
                "session key parameter.");
        EntityUtils.consume(response.getEntity());

        Long lastAccessedTimeAfterExtension = getLastAccessedTimeOfSession();
        Assert.assertTrue(lastAccessedTimeAfterExtension > lastAccessedTimeBeforeExtension,
                "Session has not been extended.");
    }

    @Test(groups = "wso2.is", description = "Sends a valid request for session extension.",
            dependsOnMethods = "testSessionExtensionWithValidParameters")
    public void testSessionExtensionWithInValidParameters() throws Exception {

        // Using the fresh third party client without any cookies as the firstPartyClient holds a valid cookie,
        String invalidParamNameUrl = String.format(SESSION_EXTENDER_ENDPOINT_GET_URL, "idpKey", idpSessionKey);
        testSessionExtensionResponse(thirdPartyClient, invalidParamNameUrl, HttpServletResponse.SC_BAD_REQUEST,
                "ISE-60001");

        String invalidKeyValueUrl =  String.format(SESSION_EXTENDER_ENDPOINT_GET_URL, "idpSessionKey", "12345");
        testSessionExtensionResponse(thirdPartyClient, invalidKeyValueUrl, HttpServletResponse.SC_BAD_REQUEST,
                "ISE-60004");
    }

    /**
     * Method for testing the error responses from the API.
     *
     * @param url                   URL of the request.
     * @param expectedStatusCode    Expected status code of the response.
     * @param expectedErrorCode     Expected error code of the API response JSON.
     * @throws Exception            Error if executing the request fails.
     */
    private void testSessionExtensionResponse(CloseableHttpClient client, String url, int expectedStatusCode,
                                              String expectedErrorCode)
            throws Exception {

        HttpResponse response = sendGetRequest(client, url);
        Assert.assertNotNull(response, "Session extension request failed. Response is invalid.");

        int statusCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(statusCode, expectedStatusCode);

        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        JSONObject responseJson = new JSONObject(responseString);
        Assert.assertEquals(responseJson.get("code"), expectedErrorCode);
        EntityUtils.consume(entity);
    }

    /**
     * Extract the location header value from a HttpResponse.
     *
     * @param response HttpResponse object that needs the header extracted.
     * @return String value of the location header.
     */
    private String getLocationHeaderValue(HttpResponse response) {

        Header location = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(location);
        return location.getValue();
    }

    /**
     * Sends a log in post to the IS instance and extract and return the sessionDataKeyConsent from the response.
     *
     * @param client         CloseableHttpClient object to send the login post.
     * @param sessionDataKey String sessionDataKey obtained.
     * @return Extracted sessionDataKeyConsent.
     * @throws IOException
     * @throws URISyntaxException
     */
    private String getSessionDataKeyConsent(CloseableHttpClient client, String sessionDataKey)
            throws IOException, URISyntaxException {

        HttpResponse response = sendLoginPost(client, sessionDataKey);
        Assert.assertNotNull(response, "Login request failed. response is null.");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null");
        EntityUtils.consume(response.getEntity());

        // Request will return with a 302 to the authorize end point. Doing a GET will give the sessionDataKeyConsent
        response = sendGetRequest(client, locationHeader.getValue());

        String locationValue = getLocationHeaderValue(response);
        Assert.assertTrue(locationValue.contains(OAuth2Constant.SESSION_DATA_KEY_CONSENT),
                "sessionDataKeyConsent not found in response.");

        EntityUtils.consume(response.getEntity());

        // Extract sessionDataKeyConsent from the location value.
        return DataExtractUtil.getParamFromURIString(locationValue, OAuth2Constant.SESSION_DATA_KEY_CONSENT);
    }

    private Long getLastAccessedTimeOfSession() throws Exception {

        HttpResponse response = sendGetRequestToRestApi(firstPartyClient, SESSIONS_ENDPOINT_URI);
        Assert.assertNotNull(response.getEntity(), "Session extension validation request failed. Response is invalid.");

        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        Assert.assertTrue(responseString.startsWith("{"), "No session information returned by the sessions endpoint.");

        JSONObject responseJson = new JSONObject(responseString);
        Assert.assertNotNull(responseJson.get("sessions"), "No session information returned by the sessions endpoint.");
        JSONArray sessionsList = (JSONArray) responseJson.get("sessions");
        JSONObject session = (JSONObject) sessionsList.get(0);
        String lastAccessTime = (String) session.get("lastAccessTime");
        Assert.assertNotNull(lastAccessTime, "No session information returned by the sessions endpoint.");
        EntityUtils.consume(entity);
        return Long.parseLong(lastAccessTime);
    }

    private HttpResponse sendGetRequestToRestApi(HttpClient client, String locationURL)
            throws IOException {

        String encodedCredentials =
                Base64.encodeBase64String((authenticatingUserName + ":" + authenticatingCredential).getBytes());
        HttpGet getRequest = new HttpGet(locationURL);
        getRequest.setHeader(HttpHeaders.USER_AGENT, OAuth2Constant.USER_AGENT);
        getRequest.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials);
        HttpResponse response = client.execute(getRequest);

        return response;
    }
}
