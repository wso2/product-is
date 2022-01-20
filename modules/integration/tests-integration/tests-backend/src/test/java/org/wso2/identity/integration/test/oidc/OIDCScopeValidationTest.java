package org.wso2.identity.integration.test.oidc;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.identity.integration.test.oidc.bean.OIDCApplication;
import org.wso2.identity.integration.test.oidc.bean.OIDCUser;
import org.wso2.identity.integration.test.util.Utils;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OIDCScopeValidationTest extends OIDCAbstractIntegrationTest {

    protected OIDCUser user;
    protected String accessToken;
    protected String sessionDataKeyConsent;
    protected String sessionDataKey;
    protected String authorizationCode;

    CookieStore cookieStore = new BasicCookieStore();

    protected HttpClient client;
    protected List<NameValuePair> consentParameters = new ArrayList<>();
    OIDCApplication playgroundApp;
    ServiceProvider serviceProvider;
    private String claimsToGetConsent;
    private String scopes;

    private static final String INTERNAL_LOGIN_SCOPE = "internal_login";
    private static final String SYSTEM_SCOPE = "SYSTEM";
    private static final String INTERNAL_USER_MGT_UPDATE_SCOPE = "internal_user_mgt_update"; // admin permission.
    private static final String INVALID_SCOPE = "internal_user_update"; // This scope does not exist.


    @BeforeMethod(alwaysRun = true)
    public void testInit(Method method) throws Exception {

        super.init();
        String scope = OIDCUtilTest.role;
        if (method.getName().equalsIgnoreCase("testScopeValidationWithAdminUser")) {
            scope = "admin";
        }
        initUser(scope);
        createUser(user);
        userInfo.setUserName(user.getUsername());
        userInfo.setPassword(user.getPassword());

        playgroundApp = initApplication();
        serviceProvider = createApplication(new ServiceProvider(), playgroundApp);
        client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();


    }

    @AfterMethod(alwaysRun = true)
    public void testClear() throws Exception {

        deleteUser(user);
        deleteApplication(playgroundApp);
        clear();
    }



    @Test(groups = "wso2.is", description = "Test scope validation with a user which has 'internal/everyone' role.")
    public void testScopeValidationWithNormalUser() throws Exception {
        testSendAuthenticationRequest(playgroundApp, client);
        testAuthentication(playgroundApp);
        Assert.assertEquals(claimsToGetConsent, "0_Email,1_First Name",
                "Requested claims were not prompted to ask the consent.");
        Assert.assertNotNull(scopes);
        Assert.assertEquals(INTERNAL_LOGIN_SCOPE, scopes, "'" + INTERNAL_LOGIN_SCOPE + "' should return " +
                "with the redirect URL");
        Assert.assertFalse(scopes.contains(SYSTEM_SCOPE), "'" + SYSTEM_SCOPE + "' scopes should not exist after the validation.");
        Assert.assertFalse(scopes.contains(INTERNAL_USER_MGT_UPDATE_SCOPE),
                "'" + INTERNAL_USER_MGT_UPDATE_SCOPE + "' scopes should not exist after the validation. " +
                        "Since user does not have permission for this scope.");
        Assert.assertFalse(scopes.contains(INVALID_SCOPE), "'" + INVALID_SCOPE + "' scopes should not exist" +
                " after the validation. This scope does not exist.");

    }

    @Test(groups = "wso2.is", dependsOnMethods = {"testScopeValidationWithNormalUser"}, description = "Test scope" +
            " validation with an admin user.")
    public void testScopeValidationWithAdminUser() throws Exception {
        testSendAuthenticationRequest(playgroundApp, client);
        testAuthentication(playgroundApp);
        Assert.assertEquals(claimsToGetConsent, "0_Email,1_First Name",
                "Requested claims were not prompted to ask the consent.");
        Assert.assertNotNull(scopes);
        String[] scopesArray = scopes.split(" ");
        Assert.assertTrue(ArrayUtils.contains(scopesArray, INTERNAL_LOGIN_SCOPE), "'" + INTERNAL_LOGIN_SCOPE +
                "' should return with the redirect URL");
        Assert.assertTrue(ArrayUtils.contains(scopesArray, INTERNAL_USER_MGT_UPDATE_SCOPE),
                "'" + INTERNAL_USER_MGT_UPDATE_SCOPE + "' should return with the redirect URL.");
        Assert.assertFalse(ArrayUtils.contains(scopesArray, SYSTEM_SCOPE), "'" + SYSTEM_SCOPE + "' scopes " +
                "should not exist after the validation.");
        Assert.assertFalse(ArrayUtils.contains(scopesArray, INVALID_SCOPE), "'" + INVALID_SCOPE +
                "' scopes should not exist after the validation. This scope does not exist.");
    }

    private void initUser(String role) {

        user = new OIDCUser(OIDCUtilTest.username, OIDCUtilTest.password);
        user.setProfile(OIDCUtilTest.profile);
        user.addUserClaim(OIDCUtilTest.emailClaimUri, OIDCUtilTest.email);
        user.addUserClaim(OIDCUtilTest.firstNameClaimUri, OIDCUtilTest.firstName);
        user.addUserClaim(OIDCUtilTest.lastNameClaimUri, OIDCUtilTest.lastName);
        user.addRole(role);
    }

    private OIDCApplication initApplication() {

        playgroundApp = new OIDCApplication(OIDCUtilTest.playgroundAppOneAppName,
                OIDCUtilTest.playgroundAppOneAppContext,
                OIDCUtilTest.playgroundAppOneAppCallBackUri);
        playgroundApp.addRequiredClaim(OIDCUtilTest.emailClaimUri);
        playgroundApp.addRequiredClaim(OIDCUtilTest.firstNameClaimUri);
        return playgroundApp;
    }

    private void testSendAuthenticationRequest(OIDCApplication application, HttpClient client)
            throws Exception {

        List<NameValuePair> urlParameters = getNameValuePairs(application);
        HttpResponse response = sendPostRequestWithParameters(client, urlParameters, String.format
                (OIDCUtilTest.targetApplicationUrl, application.getApplicationContext() +
                        OAuth2Constant.PlaygroundAppPaths.appUserAuthorizePath));
        Assert.assertNotNull(response, "Authorization request failed for " + application.getApplicationName() +
                ". Authorized response is null.");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);

        Assert.assertNotNull(locationHeader, "Authorization request failed for " +
                application.getApplicationName() + ". Authorized response header is null.");
        EntityUtils.consume(response.getEntity());
        response = sendGetRequest(client, locationHeader.getValue());
        Assert.assertNotNull(response, "Authorization request failed for " +
                application.getApplicationName() + ". Authorized user response is null.");

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response,
                keyPositionMap);
        Assert.assertNotNull(keyValues, "The sessionDataKey value is null for " +
                application.getApplicationName());

        sessionDataKey = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKey, "Invalid sessionDataKey for " + application.getApplicationName());

        EntityUtils.consume(response.getEntity());
    }

    private void testAuthentication(OIDCApplication application) throws Exception {

        HttpResponse response = sendLoginPost(client, sessionDataKey);
        Assert.assertNotNull(response, "Login request failed for " + application.getApplicationName() +
                ". response is null.");

        Header locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null for " +
                application.getApplicationName());
        EntityUtils.consume(response.getEntity());

        HttpClient httpClientWithoutAutoRedirections = HttpClientBuilder.create().disableRedirectHandling()
                .setDefaultCookieStore(cookieStore).build();
        HttpGet getRequest = new HttpGet(locationHeader.getValue());
        getRequest.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        response = httpClientWithoutAutoRedirections.execute(getRequest);

        claimsToGetConsent = claimsToGetConsent(response);
        scopes = getScopes(response);
        consentParameters.addAll(Utils.getConsentRequiredClaimsFromResponse(response));
        locationHeader = response.getFirstHeader(OAuth2Constant.HTTP_RESPONSE_HEADER_LOCATION);
        EntityUtils.consume(response.getEntity());
        response = sendGetRequest(httpClientWithoutAutoRedirections, locationHeader.getValue());
        HttpClientBuilder.create().setDefaultCookieStore(cookieStore);
        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"sessionDataKeyConsent\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractSessionConsentDataFromResponse(response,
                keyPositionMap);
        Assert.assertNotNull(keyValues, "SessionDataKeyConsent key value is null for " + application
                .getApplicationName());

        sessionDataKeyConsent = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKeyConsent, "Invalid sessionDataKeyConsent for " + application
                .getApplicationName());
        EntityUtils.consume(response.getEntity());
    }

    private static String claimsToGetConsent(HttpResponse response) throws Exception {

        String redirectUrl = Utils.getRedirectUrl(response);
        Map<String, String> queryParams = Utils.getQueryParams(redirectUrl);
        String requestedClaims = queryParams.get("requestedClaims");
        String mandatoryClaims = queryParams.get("mandatoryClaims");
        if (StringUtils.isNotEmpty(requestedClaims)) {
            return requestedClaims;
        }
        if (StringUtils.isNotEmpty(mandatoryClaims)) {
            return mandatoryClaims;
        }
        return StringUtils.EMPTY;
    }

    private static String getScopes(HttpResponse response) throws Exception {
        String redirectUrl = Utils.getRedirectUrl(response);
        Map<String, String> queryParams = Utils.getQueryParams(redirectUrl);
        Assert.assertTrue(queryParams.containsKey("scope"));
        return queryParams.get("scope");
    }


    private static List<NameValuePair> getNameValuePairs(OIDCApplication application) {

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("grantType", OAuth2Constant.OAUTH2_GRANT_TYPE_CODE));
        urlParameters.add(new BasicNameValuePair("consumerKey", application.getClientId()));
        urlParameters.add(new BasicNameValuePair("callbackurl", application.getCallBackURL()));
        urlParameters.add(new BasicNameValuePair("authorizeEndpoint", OAuth2Constant.APPROVAL_URL));
        urlParameters.add(new BasicNameValuePair("authorize", OAuth2Constant.AUTHORIZE_PARAM));
        urlParameters.add(new BasicNameValuePair("scope", OAuth2Constant.OAUTH2_SCOPE_OPENID + " " +
                OAuth2Constant.OAUTH2_SCOPE_EMAIL + " " + OAuth2Constant.OAUTH2_SCOPE_PROFILE + " " +
                INTERNAL_LOGIN_SCOPE + " " + INTERNAL_USER_MGT_UPDATE_SCOPE + " " + INVALID_SCOPE + " " +
                SYSTEM_SCOPE));
        return urlParameters;
    }

}
