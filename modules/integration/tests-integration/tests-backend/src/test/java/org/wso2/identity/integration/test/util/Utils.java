/*
 *Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */

package org.wso2.identity.integration.test.util;

import com.icegreen.greenmail.util.GreenMail;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.provisioning.JustInTimeProvisioningTestCase;
import org.wso2.identity.integration.test.utils.BasicAuthHandler;
import org.wso2.identity.integration.test.utils.BasicAuthInfo;
import org.wso2.identity.integration.test.utils.CommonConstants;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class Utils {

    private static String RESIDENT_CARBON_HOME;
    private static final String SAML_SSO_URL = "https://localhost:9853/samlsso";
    public static final String MODIFIED_USER_NAME = "modifiedUserName";
    public static final String PASSWORD = "password";
    public static final String USER_AGENT = "User-Agent";
    public static final String REFERER = "Referer";
    public static final String SET_COOKIE = "Set-Cookie";

    private static GreenMail greenMail;

    private static final Log log = LogFactory.getLog(Utils.class);

    public static boolean nameExists(FlaggedName[] allNames, String inputName) {

        boolean exists = false;

        for (FlaggedName flaggedName : allNames) {
            String name = flaggedName.getItemName();

            if (name.equals(inputName)) {
                exists = true;
                break;
            } else {
                exists = false;
            }
        }

        return exists;
    }

    public static String getResidentCarbonHome() {

        if (StringUtils.isEmpty(RESIDENT_CARBON_HOME)) {
            RESIDENT_CARBON_HOME = System.getProperty("carbon.home");
        }
        return RESIDENT_CARBON_HOME;
    }

    public static Tomcat getTomcat(Class testClass) {

        Tomcat tomcat = new Tomcat();
        tomcat.getService().setContainer(tomcat.getEngine());
        tomcat.setPort(CommonConstants.DEFAULT_TOMCAT_PORT);
        tomcat.setBaseDir("");

        StandardHost stdHost = (StandardHost) tomcat.getHost();

        stdHost.setAppBase("");
        stdHost.setAutoDeploy(true);
        stdHost.setDeployOnStartup(true);
        stdHost.setUnpackWARs(true);
        tomcat.setHost(stdHost);

        setSystemProperties(testClass);
        return tomcat;
    }

    public static GreenMail getMailServer() {
        return greenMail;
    }

    public static void setMailServer(GreenMail greenMail) {
        Utils.greenMail = greenMail;
    }

    public static void setSystemProperties(Class classIn) {

        System.setProperty("javax.net.ssl.trustStore", FrameworkPathUtil.getSystemResourceLocation() + File.separator +
                "keystores" + File.separator + "products" + File.separator + ISIntegrationTest.KEYSTORE_NAME);
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", ISIntegrationTest.KEYSTORE_TYPE);
    }

    public static HttpResponse sendPOSTMessage(String sessionKey, String url, String userAgent, String
            acsUrl, String artifact, String userName, String password, HttpClient httpClient) throws Exception {

        return sendPOSTMessage(sessionKey, url, userAgent, acsUrl, artifact, userName, password, httpClient,
                SAML_SSO_URL);
    }

    public static HttpResponse sendPOSTMessage(String sessionKey, String url, String userAgent, String
            acsUrl, String artifact, String userName, String password, HttpClient httpClient, String samlSSOUrl) throws Exception {

        HttpPost post = new HttpPost(url);
        post.setHeader("User-Agent", userAgent);
        post.addHeader("Referer", String.format(acsUrl, artifact));
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("username", userName));
        urlParameters.add(new BasicNameValuePair("password", password));
        if (StringUtils.equals(url, samlSSOUrl)) {
            urlParameters.add(new BasicNameValuePair("tocommonauth", "true"));
        }
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionKey));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(post);
    }

    public static HttpResponse sendPOSTClaimMessage(HttpResponse response, String commonAuthUrl, String userAgent, String
            acsUrl, String artifact, HttpClient httpClient) throws Exception {

        Map<String, String> queryParams = getQueryParams(getRedirectUrl(response));
        String sessionKey = queryParams.get("sessionDataKey");
        String[] claims = queryParams.get("missingClaims").split(",");

        HttpPost post = new HttpPost(commonAuthUrl);
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();

        for (int i = 0; i < claims.length; i++) {
            urlParameters.add(new BasicNameValuePair("claim_mand_" + claims[i], "providedClaimValue"));
        }
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionKey));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(post);
    }

    /**
     * To send the response to jit post authentication handler.
     *
     * @param response      Relevant response.
     * @param commonAuthUrl Common Auth URL.
     * @param userAgent     User Agent.
     * @param referer       Referer
     * @param httpClient    Http Client.
     * @param pastreCookie  Pastre Cookie.
     * @return response Relevant response received.
     * @throws Exception Exception
     */
    public static HttpResponse sendPostJITHandlerResponse(HttpResponse response, String commonAuthUrl, String userAgent,
                                                          String referer, HttpClient httpClient, String pastreCookie) throws Exception {

        String redirectUrl = getRedirectUrl(response);
        Map<String, String> queryParams = getQueryParams(redirectUrl);
        String sessionKey = queryParams.get("sessionDataKey");

        HttpPost post = new HttpPost(commonAuthUrl);
        post.setHeader("User-Agent", userAgent);
        post.addHeader("Referer", referer);
        post.addHeader("Cookie", pastreCookie);
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionKey));
        urlParameters.add(new BasicNameValuePair("password", PASSWORD));
        urlParameters.add(new BasicNameValuePair("username",
                JustInTimeProvisioningTestCase.DOMAIN_ID + UserCoreConstants.DOMAIN_SEPARATOR + MODIFIED_USER_NAME));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(post);
    }

    /**
     * To send the response to missing challenge question post authentication handler.
     *
     * @param response      Relevant response.
     * @param commonAuthUrl Common Auth URL.
     * @param userAgent     User Agent.
     * @param referer       Referer
     * @param httpClient    Http Client.
     * @param pastreCookie  Pastre Cookie.
     * @return response Relevant response received.
     * @throws Exception Exception
     */
    public static HttpResponse sendPOSTChallengeQuestionResponse(HttpResponse response, String commonAuthUrl, String
            userAgent, String referer, HttpClient httpClient, String pastreCookie) throws Exception {

        String questionSetId;
        String questionBody;
        String tempQuestionSetIdKey;
        String tempQuestionSetIdValue;
        String tempAnswerSetIdKey;
        String tempAnswerValue;
        List<NameValuePair> urlParameters = new ArrayList<>();

        String redirectUrl = getRedirectUrl(response);
        Map<String, String> queryParams = getQueryParams(redirectUrl);
        String sessionKey = queryParams.get("sessionDataKey");
        String urlData = queryParams.get("data");

        String[] questionSets = null;
        if (urlData != null) {
            questionSets = urlData.split("&");
        } else {
            HttpGet get = new HttpGet(redirectUrl);
            get.setHeader(USER_AGENT, userAgent);
            get.addHeader(REFERER, referer);
            get.addHeader(SET_COOKIE, pastreCookie);
            HttpResponse questionPageResponse = httpClient.execute(get);
            List<String> questionList = extractSecurityQuestions(questionPageResponse);
            questionSets = new String[questionList.size()];
            questionSets = questionList.toArray(questionSets);
            get.releaseConnection();
        }

        if (questionSets != null) {
            for (String question : questionSets) {
                String[] questionProperties = question.split("\\|");
                questionSetId = questionProperties[0];
                questionBody = questionProperties[2];

                tempQuestionSetIdKey = "Q-" + questionSetId;
                tempQuestionSetIdValue = questionBody;

                tempAnswerSetIdKey = "A-" + questionSetId;
                tempAnswerValue = "SampleAnswer";

                urlParameters.add(new BasicNameValuePair(tempQuestionSetIdKey, tempQuestionSetIdValue));
                urlParameters.add(new BasicNameValuePair(tempAnswerSetIdKey, tempAnswerValue));
            }
        }

        HttpPost post = new HttpPost(commonAuthUrl);
        post.setHeader(USER_AGENT, userAgent);
        post.addHeader(REFERER, referer);
        post.addHeader(SET_COOKIE, pastreCookie);
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionKey));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(post);
    }

    private static List<String> extractSecurityQuestions(HttpResponse response) throws IOException {

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String resultPage = rd.lines().collect(Collectors.joining());
        String questionString = resultPage.substring(resultPage.lastIndexOf("<h3>"));
        String[] dataArray = StringUtils.substringsBetween(questionString, "name=Q-", "</option>");
        List<String> questionList = new ArrayList<>();
        for (String dataString : dataArray) {
            String[] splitString = dataString.split("\\s{2,}");
            String qString = splitString[0].substring(0, splitString[0].length() - 1) + "| |" + splitString[2];
            questionList.add(qString);
        }
        return questionList;
    }

    public static HttpResponse sendPOSTConsentMessage(HttpResponse response, String commonAuthUrl, String userAgent,
                                                      String referer, HttpClient httpClient, String
                                                              pastreCookie) throws Exception {

        String redirectUrl = getRedirectUrl(response);
        Map<String, String> queryParams = getQueryParams(redirectUrl);

        String sessionKey = queryParams.get("sessionDataKey");
        String mandatoryClaims = queryParams.get("mandatoryClaims");
        String requestedClaims = queryParams.get("requestedClaims");
        String consentRequiredClaims;

        if (isNotBlank(mandatoryClaims) && isNotBlank(requestedClaims)) {
            StringJoiner joiner = new StringJoiner(",");
            joiner.add(mandatoryClaims);
            joiner.add(requestedClaims);
            consentRequiredClaims = joiner.toString();
        } else if (isNotBlank(mandatoryClaims)) {
            consentRequiredClaims = mandatoryClaims;
        } else {
            consentRequiredClaims = requestedClaims;
        }

        String[] claims;
        if (isNotBlank(consentRequiredClaims)) {
            claims = consentRequiredClaims.split(",");
        } else {
            claims = new String[0];
        }

        HttpPost post = new HttpPost(commonAuthUrl);
        post.setHeader("User-Agent", userAgent);
        post.addHeader("Referer", referer);
        post.addHeader("Cookie", pastreCookie);
        List<NameValuePair> urlParameters = new ArrayList<>();

        for (int i = 0; i < claims.length; i++) {

            if (isNotBlank(claims[i])) {
                String[] claimMeta = claims[i].split("_", 2);
                if (claimMeta.length == 2) {
                    urlParameters.add(new BasicNameValuePair("consent_" + claimMeta[0], "on"));
                }
            }
        }

        // This block fetches the claims when the enable_shortened_urls is true.
        if (isBlank(mandatoryClaims) && isBlank(requestedClaims)) {
            HttpGet get = new HttpGet(redirectUrl);
            get.setHeader("User-Agent", userAgent);
            get.addHeader("Referer", referer);
            get.addHeader("Cookie", pastreCookie);
            HttpResponse consentPageResponse = httpClient.execute(get);
            List<String> fetchedClaims = extractClaims(consentPageResponse);
            for (String claimConsent: fetchedClaims) {
                urlParameters.add(new BasicNameValuePair(claimConsent, "on"));
            }
            get.releaseConnection();
        }

        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionKey));
        urlParameters.add(new BasicNameValuePair("consent", "approve"));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(post);
    }

    private static List<String> extractClaims(HttpResponse response) throws IOException {

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String resultPage = rd.lines().collect(Collectors.joining());
        List<String> attributeList = new ArrayList<>();
        
        String labelOpenTag = "<label for=\"";
        String labelCloseTag = "\">";
        
        // Regular expression to match <div> tags containing "claim-list" class.
        String regex = "<div[^>]*class=\"[^\"]*claim-list[^\"]*\"[^>]*>";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(resultPage);
        
        while (matcher.find()) {
            int divStartIndex = matcher.start();
            
            // Count div tags to find the corresponding closing tag.
            int openDivCount = 1;
            int index = divStartIndex;
            while (openDivCount > 0) {
                int nextOpenDiv = resultPage.indexOf("<div", index + 1);
                int nextCloseDiv = resultPage.indexOf("</div>", index + 1);
                
                // Break the loop if there are no more div tags.
                if (nextOpenDiv == -1 && nextCloseDiv == -1) {
                    break;
                }
                
                // Check the closest next div tag (open or close).
                if (nextCloseDiv != -1 && (nextOpenDiv == -1 || nextCloseDiv < nextOpenDiv)) {
                    openDivCount--;
                    index = nextCloseDiv;
                } else if (nextOpenDiv != -1) {
                    openDivCount++;
                    index = nextOpenDiv;
                }
            }
            
            // Index is now at the start of the closing </div> tag of the claim-list div.
            if (openDivCount == 0) {
                String claimString = resultPage.substring(divStartIndex, index + 6); // 6 is length of "</div>".
                
                // Use a matcher to find each label within the claimString.
                Matcher labelMatcher = Pattern.compile(labelOpenTag + "(.*?)" + labelCloseTag).matcher(claimString);
                while (labelMatcher.find()) {
                    // Add the extracted label (from the 'for' attribute) to the list.
                    attributeList.add(labelMatcher.group(1));
                }
            }
        }
        return attributeList;
    }

    public static boolean requestMissingClaims(HttpResponse response) {

        String redirectUrl = Utils.getRedirectUrl(response);
        return redirectUrl.contains("consent.do");

    }

    public static String getPastreCookie(HttpResponse response) {

        String pastrCookie = null;
        boolean foundPastrCookie = false;
        Header[] headers = response.getHeaders("Set-Cookie");
        if (headers != null) {
            int i = 0;
            while (!foundPastrCookie && i < headers.length) {
                if (headers[i].getValue().contains("pastr")) {
                    pastrCookie = headers[i].getValue().split(";")[0];
                    foundPastrCookie = true;
                }
                i++;
            }
        }
        return pastrCookie;
    }

    public static HttpResponse sendRedirectRequest(HttpResponse response, String userAgent, String acsUrl, String
            artifact, HttpClient httpClient) throws IOException {

        Header[] headers = response.getAllHeaders();
        String url = "";
        for (Header header : headers) {
            if ("Location".equals(header.getName())) {
                url = header.getValue();
            }
        }

        HttpGet request = new HttpGet(url);
        request.addHeader("User-Agent", userAgent);
        request.addHeader("Referer", String.format(acsUrl, artifact));
        return httpClient.execute(request);
    }

    public static String getRedirectUrl(HttpResponse response) {

        Header[] headers = response.getAllHeaders();
        String url = "";
        for (Header header : headers) {
            if ("Location".equals(header.getName())) {
                url = header.getValue();
            }
        }
        return url;
    }

    public static Map<String, String> getQueryParams(String Url) throws Exception {

        Map<String, String> queryParams = new HashMap<>();

        List<NameValuePair> params = URLEncodedUtils.parse(new URI(Url), "UTF-8");
        for (NameValuePair param : params) {
            queryParams.put(param.getName(), param.getValue());
        }
        return queryParams;
    }

    public static HttpResponse sendGetRequest(String url, String userAgent, HttpClient httpClient) throws Exception {

        HttpGet request = new HttpGet(url);
        request.addHeader("User-Agent", userAgent);
        return httpClient.execute(request);
    }

    public static HttpResponse sendECPPostRequest(String url, String userAgent, HttpClient httpClient,
                                                  String username, String password, String soapRequest) throws Exception {

        HttpPost request = new HttpPost(url);
        HttpResponse response;
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(
                auth.getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + new String(encodedAuth);
        request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
        request.setHeader(HttpHeaders.CONTENT_TYPE, "text/xml; charset=utf-8");
        request.setEntity(new StringEntity(soapRequest));
        response = httpClient.execute(request);
        return response;
    }

    public static HttpResponse sendSAMLMessage(String url, Map<String, String> parameters, String userAgent, HttpClient httpClient) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        HttpPost post = new HttpPost(url);
        post.setHeader("User-Agent", userAgent);
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            urlParameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(post);
    }

    public static String extractDataFromResponse(HttpResponse response, String key, int token)
            throws IOException {

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));
        String line;
        String value = "";

        while ((line = rd.readLine()) != null) {
            if (line.contains(key)) {
                String[] tokens = line.split("'");
                value = tokens[token];
            }
        }
        rd.close();
        return value;
    }

    /**
     * Extract data from response for management console requests.
     *
     * @param response HttpResponse
     * @param key      key to determine the line to extract
     * @param token    index of the value after splitting the line
     * @return value extracted
     * @throws IOException IOException
     */
    public static String extractDataFromResponseForManagementConsoleRequests(HttpResponse response, String key,
                                                                             int token)
            throws IOException {

        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()))) {
            String line;
            String value = StringUtils.EMPTY;

            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(key)) {
                    String[] tokens = line.split("\"");
                    value = tokens[token];
                    value = value.trim();
                    break;
                }
            }
            return value;
        }
    }

    public static List<NameValuePair> getConsentRequiredClaimsFromResponse(HttpResponse response)
            throws Exception {

        String redirectUrl = Utils.getRedirectUrl(response);
        Map<String, String> queryParams = Utils.getQueryParams(redirectUrl);
        List<NameValuePair> consentRequiredClaimsList = new ArrayList<>();
        String requestedClaims = queryParams.get("requestedClaims");
        String mandatoryClaims = queryParams.get("mandatoryClaims");

        String consentRequiredClaims;

        if (isNotBlank(mandatoryClaims) && isNotBlank(requestedClaims)) {
            StringJoiner joiner = new StringJoiner(",");
            joiner.add(mandatoryClaims);
            joiner.add(requestedClaims);
            consentRequiredClaims = joiner.toString();
        } else if (isNotBlank(mandatoryClaims)) {
            consentRequiredClaims = mandatoryClaims;
        } else {
            consentRequiredClaims = requestedClaims;
        }

        String[] claims;
        if (isNotBlank(consentRequiredClaims)) {
            claims = consentRequiredClaims.split(",");
        } else {
            claims = new String[0];
        }

        for (String claim : claims) {
            if (isNotBlank(claim)) {
                String[] claimMeta = claim.split("_", 2);
                if (claimMeta.length == 2) {
                    consentRequiredClaimsList.add(new BasicNameValuePair("consent_" + claimMeta[0], "on"));
                }
            }
        }

        // If no claims are found in the url, then extract the claims from the consent page.
        if (consentRequiredClaimsList.isEmpty()) {
            consentRequiredClaimsList = extractConsentRequiredClaimsFromConsentPage(redirectUrl);
        }
        return consentRequiredClaimsList;
    }

    public static List<NameValuePair> extractConsentRequiredClaimsFromConsentPage(String redirectUrl) throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<>();
        List<String> fetchedClaims = fetchClaimsfromConsentPage(redirectUrl);
        for (String claimConsent : fetchedClaims) {
            urlParameters.add(new BasicNameValuePair(claimConsent, "on"));
        }
        return urlParameters;
    }

    public static List<String> fetchClaimsfromConsentPage(String redirectUrl) throws Exception {

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(redirectUrl);
        get.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        HttpResponse consentPageResponse = sendGetRequest(redirectUrl, OAuth2Constant.USER_AGENT, client);
        List<String> fetchedClaims = extractClaims(consentPageResponse);
        return fetchedClaims;
    }

    /**
     * Send a GET request to the data API.
     *
     * @param sessionDataKeyConsent Session data key consent
     * @param userInfo              User info
     * @param tenantInfo            Tenant info
     * @return HttpResponse
     * @throws IOException IOException
     */
    public static HttpResponse sendDataAPIGetRequest(String sessionDataKeyConsent, User userInfo,
                                                     Tenant tenantInfo) throws IOException {

        String dataApiUrl = tenantInfo.getDomain().equalsIgnoreCase("carbon.super") ?
                OAuth2Constant.DATA_API_ENDPOINT + sessionDataKeyConsent :
                OAuth2Constant.TENANT_DATA_API_ENDPOINT + sessionDataKeyConsent;
        HttpClient client = HttpClientBuilder.create().build();
        String authzHeader = getBasicAuthHeader(userInfo);
        HttpGet request = new HttpGet(dataApiUrl);

        request.setHeader("User-Agent", OAuth2Constant.USER_AGENT);
        request.setHeader("Authorization", authzHeader);

        return client.execute(request);
    }

    /**
     * Read audit log lines with a given content.
     *
     * @param content Content to be searched in audit log.
     * @return List of lines which contains the given string.
     * @throws IOException IOException.
     */
    public static List<String> readAuditLogLineWithContent(String content) throws IOException {

        String fileName = CarbonUtils.getCarbonHome()
                + ISIntegrationTest.URL_SEPARATOR +
                "repository" + ISIntegrationTest.URL_SEPARATOR + "logs" + ISIntegrationTest.URL_SEPARATOR +
                "audit.log";
        List<String> results = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            String currentLine;
            while ((currentLine = bufferedReader.readLine()) != null) {
                if (currentLine.contains(content)) {
                    results.add(currentLine);
                }
            }
        }
        return results;
    }

    /**
     * Construct and return basic authentication information of the user.
     */
    public static String getBasicAuthHeader(User userInfo) {

        BasicAuthInfo basicAuthInfo = new BasicAuthInfo();
        basicAuthInfo.setUserName(userInfo.getUserName());
        basicAuthInfo.setPassword(userInfo.getPassword());

        BasicAuthHandler basicAuthHandler = new BasicAuthHandler();
        BasicAuthInfo encodedBasicAuthInfo = (BasicAuthInfo) basicAuthHandler.getAuthenticationToken(basicAuthInfo);
        return encodedBasicAuthInfo.getAuthorizationHeader();
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

    /**
     * Get Java Major Version from System Property.
     *
     * @return Java Major Version
     */
    public static int getJavaVersion() {

        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        return Integer.parseInt(version);
    }
}
