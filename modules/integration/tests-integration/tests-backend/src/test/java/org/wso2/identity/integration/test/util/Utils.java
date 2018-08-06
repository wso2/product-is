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

import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.identity.integration.test.provisioning.JustInTimeProvisioningTestCase;
import org.wso2.identity.integration.test.utils.CommonConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static org.apache.commons.lang.StringUtils.isNotBlank;

public class Utils {

    private static String RESIDENT_CARBON_HOME;
    private static final String SAML_SSO_URL = "https://localhost:9853/samlsso";
    public static final String MODIFIED_USER_NAME = "modifiedUserName";
    public static final String PASSWORD = "password";
    public static final String USER_AGENT = "User-Agent";
    public static final String REFERER = "Referer";
    public static final String SET_COOKIE = "Set-Cookie";

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

    public static void setSystemProperties(Class classIn) {
        URL resourceUrl = classIn.getResource(File.separator + "keystores" + File.separator + "products" + File
                .separator + "wso2carbon.jks");
        System.setProperty("javax.net.ssl.trustStore", resourceUrl.getPath());
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
    }

    public static void startTomcat(Tomcat tomcat, String webAppUrl, String webAppPath)
            throws LifecycleException {
        tomcat.addWebapp(tomcat.getHost(), webAppUrl, webAppPath);
        tomcat.start();
    }

    public static HttpResponse sendPOSTMessage(String sessionKey, String url, String userAgent, String
            acsUrl, String artifact, String userName, String password, HttpClient httpClient) throws Exception {
        HttpPost post = new HttpPost(url);
        post.setHeader("User-Agent", userAgent);
        post.addHeader("Referer", String.format(acsUrl, artifact));
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("username", userName));
        urlParameters.add(new BasicNameValuePair("password", password));
        if (StringUtils.equals(url, SAML_SSO_URL)) {
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
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionKey));
        urlParameters.add(new BasicNameValuePair("consent", "approve"));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(post);
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

    public static HttpResponse sendSAMLMessage(String url, Map<String, String> parameters, String userAgent, TestUserMode userMode, String tenantDomainParam, String tenantDomain, HttpClient httpClient) throws IOException {
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        HttpPost post = new HttpPost(url);
        post.setHeader("User-Agent", userAgent);
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            urlParameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        if (userMode == TestUserMode.TENANT_ADMIN || userMode == TestUserMode.TENANT_USER) {
            urlParameters.add(new BasicNameValuePair(tenantDomainParam, tenantDomain));
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

    public static List<NameValuePair> getConsentRequiredClaimsFromResponse(HttpResponse response) throws Exception {

        String redirectUrl = Utils.getRedirectUrl(response);
        Map<String, String> queryParams = Utils.getQueryParams(redirectUrl);
        List<NameValuePair> urlParameters = new ArrayList<>();
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
                    urlParameters.add(new BasicNameValuePair("consent_" + claimMeta[0], "on"));
                }
            }
        }
        return urlParameters;
    }
}
