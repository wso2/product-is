/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.openid;

import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.*;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenIDSSOTestCase extends ISIntegrationTest {
    private static final Log log = LogFactory.getLog(OpenIDSSOTestCase.class);

    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.6)";
    private static final String COMMON_AUTH_URL = "https://localhost:9443/commonauth";
    private static final String OPEN_ID_PROFILE_URL = "https://localhost:9443/authenticationendpoint/openid_profile.do";
    private static final String APPROVAL_URL = "https://localhost:9443/openidserver";
    private static final String OPEN_ID_URL = "http://localhost:8090/%s/openid?OpenId" +
                                              ".ClaimedId=https://localhost:9443/openid/";
    //Claim Uris
    private static final String firstNameClaimURI = "http://axschema.org/namePerson/first";
    private static final String emailClaimURI = "http://axschema.org/contact/email";
    private static final String profileName = "default";

    private OpenIDUtils.OpenIDConfig config;
    private RemoteUserStoreManagerServiceClient remoteUSMServiceClient;
    private HttpClient client;
    private File identityXML;
    private ServerConfigurationManager serverConfigurationManager;
    private Tomcat tomcatServer;

    @Factory(dataProvider = "openIdConfigBeanProvider")
    public OpenIDSSOTestCase(OpenIDUtils.OpenIDConfig configBean) {
        if (log.isDebugEnabled()){
            log.info("OpenId Test initialized for " + configBean);
        }

        this.config = configBean;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();

        if (config.getUserConsent() == OpenIDUtils.UserConsent.SKIP){
            changeISConfiguration();
            super.init();
        }

        remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
        startTomcat();
    }

    @AfterClass(alwaysRun = true)
    public void clear() throws Exception{
        if (config.getUserConsent() == OpenIDUtils.UserConsent.SKIP){
            resetISConfiguration();
        }

        remoteUSMServiceClient = null;
        stopTomcat();
    }

    @BeforeMethod
    public void createUser(){
        OpenIDUtils.User user = config.getUser();

        log.info("Creating User " + user.getUsername());

        ClaimValue[] claimValues = new ClaimValue[2];

        ClaimValue firstName = new ClaimValue();
        firstName.setClaimURI(firstNameClaimURI);
        firstName.setValue(user.getUsername());
        claimValues[1] = firstName;

        ClaimValue email = new ClaimValue();
        email.setClaimURI(emailClaimURI);
        email.setValue(user.getEmail());
        claimValues[0] = email;

        try {
            // creating the user
            remoteUSMServiceClient.addUser(user.getUsername(), user.getPassword(),
                    new String[]{user.getRole()}, claimValues,
                    profileName, true);
        } catch (Exception e) {
            Assert.fail("Error while creating the user", e);
        }

    }

    @AfterMethod
    public void deleteUser(){
        log.info("Deleting User " + config.getUser().getUsername());
        try {
            remoteUSMServiceClient.deleteUser(config.getUser().getUsername());
        } catch (Exception e) {
            Assert.fail("Error while deleting the user", e);
        }
    }

    @Test(alwaysRun = true, groups = "wso2.is", description = "Testing OpenId sample parameterized test")
    public void testOpenId() throws IOException {
        if (log.isDebugEnabled()){
            log.debug("Executing Test Case for " + config);
        }

        HttpResponse response;
        String results;

        client = new DefaultHttpClient();

        response = executePhaseBeforeApproval();

        if (config.getUserConsent() != OpenIDUtils.UserConsent.SKIP) {
            response = executePhaseAfterApproval(response);
        }

        results = extractDataFromResponse(response);

        assertLogin(results);

        if (config.getAppType() == OpenIDUtils.AppType.SMART_WITH_CLAIMS ||
                config.getAppType() == OpenIDUtils.AppType.DUMB_WITH_CLAIMS){
            assertAttributes(results);
        }

        if (config.getUserConsent() == OpenIDUtils.UserConsent.APPROVE_ALWAYS){
            client = new DefaultHttpClient();

            response = executePhaseBeforeApproval();
            results = extractDataFromResponse(response);

            assertLogin(results);
        }
    }

    private void startTomcat() throws Exception {
        log.info("Starting Tomcat");
        tomcatServer = getTomcat();
        URL resourceURL =
                getClass().getResource(File.separator + "samples" + File.separator + config.getAppType().getArtifact()
                                       + ".war");
        tomcatServer.addWebapp(tomcatServer.getHost(), "/" + config.getAppType().getArtifact(), resourceURL.getPath());
        tomcatServer.start();
    }

    private void stopTomcat() throws Exception {
        log.info("Stopping Tomcat");
        tomcatServer.stop();
        tomcatServer.destroy();
        Thread.sleep(10000);
    }

    private HttpResponse executePhaseBeforeApproval() throws IOException {
        HttpResponse response;
        String sessionKey;
        Map<String, Integer> keyPositionMap;

        response = sendOpenIdGet();
        keyPositionMap = new HashMap<String, Integer>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        sessionKey = extractDataFromResponse(response, keyPositionMap).get(0).getValue();

        response = sendLoginPost(sessionKey);
        EntityUtils.consume(response.getEntity());

        return sendRedirectGet(response);
    }

    private HttpResponse executePhaseAfterApproval(HttpResponse response) throws IOException {
        Map<String, Integer> keyPositionMap;
        List<KeyValue> keyValues;

        keyPositionMap = new HashMap<String, Integer>();
        keyPositionMap.put("openid.identity", 5);
        keyPositionMap.put("openid.return_to", 5);
        keyPositionMap.put("claimTag", 5);
        keyPositionMap.put("claimValue", 5);

        keyValues = extractDataFromResponse(response,keyPositionMap);
        assertClaims(keyValues);

        response = sendOpenIdProfilePost(keyValues);
        EntityUtils.consume(response.getEntity());

        response = sendApprovalPost();
        EntityUtils.consume(response.getEntity());

        return sendRedirectGet(response);
    }

    @DataProvider(name = "openIdConfigBeanProvider")
    public static OpenIDUtils.OpenIDConfig[][] openIdConfigBeanProvider(){
        return new OpenIDUtils.OpenIDConfig[][]{
                {new OpenIDUtils.OpenIDConfig(OpenIDUtils.User.ADMIN,
                        OpenIDUtils.UserConsent.APPROVE, OpenIDUtils.AppType.SMART_WITH_CLAIMS)},
                {new OpenIDUtils.OpenIDConfig(OpenIDUtils.User.ADMIN,
                        OpenIDUtils.UserConsent.APPROVE, OpenIDUtils.AppType.SMART_WITHOUT_CLAIMS)},
                {new OpenIDUtils.OpenIDConfig(OpenIDUtils.User.ADMIN,
                        OpenIDUtils.UserConsent.APPROVE, OpenIDUtils.AppType.DUMB_WITH_CLAIMS)},
                {new OpenIDUtils.OpenIDConfig(OpenIDUtils.User.ADMIN,
                        OpenIDUtils.UserConsent.APPROVE, OpenIDUtils.AppType.DUMB_WITHOUT_CLAIMS)},
                {new OpenIDUtils.OpenIDConfig(OpenIDUtils.User.USER1,
                        OpenIDUtils.UserConsent.APPROVE, OpenIDUtils.AppType.SMART_WITH_CLAIMS)},
                {new OpenIDUtils.OpenIDConfig(OpenIDUtils.User.USER1,
                        OpenIDUtils.UserConsent.APPROVE, OpenIDUtils.AppType.SMART_WITHOUT_CLAIMS)},
                {new OpenIDUtils.OpenIDConfig(OpenIDUtils.User.USER1,
                        OpenIDUtils.UserConsent.APPROVE, OpenIDUtils.AppType.DUMB_WITH_CLAIMS)},
                {new OpenIDUtils.OpenIDConfig(OpenIDUtils.User.USER1,
                        OpenIDUtils.UserConsent.APPROVE, OpenIDUtils.AppType.DUMB_WITHOUT_CLAIMS)},
                {new OpenIDUtils.OpenIDConfig(OpenIDUtils.User.USER2,
                        OpenIDUtils.UserConsent.APPROVE_ALWAYS, OpenIDUtils.AppType.SMART_WITH_CLAIMS)},
                {new OpenIDUtils.OpenIDConfig(OpenIDUtils.User.USER2,
                        OpenIDUtils.UserConsent.APPROVE_ALWAYS, OpenIDUtils.AppType.SMART_WITHOUT_CLAIMS)},
                {new OpenIDUtils.OpenIDConfig(OpenIDUtils.User.USER2,
                        OpenIDUtils.UserConsent.APPROVE_ALWAYS, OpenIDUtils.AppType.DUMB_WITH_CLAIMS)},
                {new OpenIDUtils.OpenIDConfig(OpenIDUtils.User.USER2,
                        OpenIDUtils.UserConsent.APPROVE_ALWAYS, OpenIDUtils.AppType.DUMB_WITHOUT_CLAIMS)},
                /*{new OpenIDUtils.OpenIDConfig(OpenIDUtils.User.USER1,
                        OpenIDUtils.UserConsent.SKIP, OpenIDUtils.AppType.SMART_WITH_CLAIMS)},
                {new OpenIDUtils.OpenIDConfig(OpenIDUtils.User.USER1,
                        OpenIDUtils.UserConsent.SKIP, OpenIDUtils.AppType.SMART_WITHOUT_CLAIMS)},
                {new OpenIDUtils.OpenIDConfig(OpenIDUtils.User.USER1,
                        OpenIDUtils.UserConsent.SKIP, OpenIDUtils.AppType.DUMB_WITH_CLAIMS)},
                {new OpenIDUtils.OpenIDConfig(OpenIDUtils.User.USER1,
                        OpenIDUtils.UserConsent.SKIP, OpenIDUtils.AppType.DUMB_WITHOUT_CLAIMS)},*/
        };
    }

    private Tomcat getTomcat() {
        Tomcat tomcat = new Tomcat();
        tomcat.getService().setContainer(tomcat.getEngine());
        tomcat.setPort(8090);
        tomcat.setBaseDir("");

        StandardHost stdHost = (StandardHost) tomcat.getHost();

        stdHost.setAppBase("");
        stdHost.setAutoDeploy(true);
        stdHost.setDeployOnStartup(true);
        stdHost.setUnpackWARs(true);
        tomcat.setHost(stdHost);

        setSystemProperties();
        return tomcat;
    }

    private void setSystemProperties() {
        URL resourceUrl = getClass().getResource(File.separator + "keystores" + File.separator
                + "products" + File.separator + "wso2carbon.jks");
        System.setProperty("javax.net.ssl.trustStore", resourceUrl.getPath());
        System.setProperty("javax.net.ssl.trustStorePassword",
                "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
    }

    private List<KeyValue> extractDataFromResponse(HttpResponse response, Map<String,
            Integer> keyPositionMap) throws IOException {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));
        String line;
        while ((line = rd.readLine()) != null) {
            for (String key: keyPositionMap.keySet()){
                if (line.contains(key)) {
                    String[] tokens = line.split("'");
                    KeyValue keyValue = new KeyValue(key, tokens[keyPositionMap.get(key)]);
                    keyValues.add(keyValue);
                }
            }
        }
        rd.close();

        return keyValues;
    }

    private String extractDataFromResponse(HttpResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        return sb.toString();
    }

    private HttpResponse sendOpenIdGet() throws IOException {
        HttpGet request = new HttpGet(String.format(OPEN_ID_URL, config.getAppType().getArtifact()));
        request.addHeader("User-Agent", USER_AGENT);

        return client.execute(request);
    }

    private HttpResponse sendLoginPost(String sessionKey) throws IOException {
        HttpPost request = new HttpPost(COMMON_AUTH_URL);
        request.setHeader("User-Agent", USER_AGENT);

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("username", config.getUser().getUsername()));
        urlParameters.add(new BasicNameValuePair("password", config.getUser().getPassword()));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionKey));

        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        return client.execute(request);
    }

    private HttpResponse sendRedirectGet(HttpResponse response) throws IOException {
        Header[] headers = response.getAllHeaders();
        String url = "";
        for (Header header : headers) {
            if ("Location".equals(header.getName())) {
                url = header.getValue();
            }
        }

        HttpGet request = new HttpGet(url);
        request.addHeader("User-Agent", USER_AGENT);

        return client.execute(request);
    }

    private HttpResponse sendOpenIdProfilePost(List<KeyValue> keyValues) throws IOException {
        HttpPost request = new HttpPost(OPEN_ID_PROFILE_URL);
        request.setHeader("User-Agent", USER_AGENT);

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();

        for (KeyValue keyValue:keyValues){
            urlParameters.add(new BasicNameValuePair(keyValue.getKey(), keyValue.getValue()));
        }

        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        return client.execute(request);
    }

    private HttpResponse sendApprovalPost() throws IOException {
        HttpPost request = new HttpPost(APPROVAL_URL);
        request.setHeader("User-Agent", USER_AGENT);

        boolean approvedAlways = (config.getUserConsent() == OpenIDUtils.UserConsent.APPROVE_ALWAYS);

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>(1);
        urlParameters.add(new BasicNameValuePair("hasApprovedAlways", String.valueOf(approvedAlways)));

        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        return client.execute(request);
    }

    private void assertClaims(List<KeyValue> keyValues){
        StringBuilder sb = new StringBuilder();
        for (KeyValue keyValue:keyValues){
            if (keyValue.key.equals("claimTag")){
                sb.append(keyValue.getValue());
                sb.append(",");
            }

            if (keyValue.key.equals("claimValue")){
                sb.append(keyValue.getValue());
                sb.append(",");
            }
        }

        String claims = sb.toString();
        Assert.assertTrue(claims.contains(firstNameClaimURI));
        Assert.assertTrue(claims.contains(config.getUser().getUsername()));
        Assert.assertTrue(claims.contains(emailClaimURI));
        Assert.assertTrue(claims.contains(config.getUser().getEmail()));
    }

    private void assertLogin(String results){
        Assert.assertTrue(results.contains("You are logged in as " +
                        "https://localhost:9443/openid/" + config.getUser().getUsername()),
                "OpenId sso login has failed for " + config);
    }

    private void assertAttributes(String results){
        String str = results.substring(results.lastIndexOf("<table>"));

        String[] dataArray = StringUtils.substringsBetween(str, "<td>", "</td>");
        Map<String,String> attributeMap = new HashMap<String, String>();
        String key = null;
        String value;
        for (int i = 0; i< dataArray.length; i++){
            if((i%2) == 0){
                key = dataArray[i];
            }else{
                value = dataArray[i].trim();
                attributeMap.put(key,value);
            }
        }

        OpenIDUtils.User user = config.getUser();

        Assert.assertTrue(attributeMap.containsKey("email"), "Claim email is expected");
        Assert.assertEquals(attributeMap.get("email"), user.getEmail(),
                "Expected claim value for email is " + user.getEmail());
        Assert.assertTrue(attributeMap.containsKey("nickname"), "Claim nickname is expected");
        Assert.assertEquals(attributeMap.get("nickname"), user.getUsername(),
                "Expected claim value for nickname is " + user.getUsername());
        Assert.assertTrue(attributeMap.containsKey("lastname"), "Claim lastname is expected");
        Assert.assertEquals(attributeMap.get("lastname"), user.getUsername(),
                "Expected claim value for lastname is " + user.getUsername());
    }

    private void changeISConfiguration() throws Exception {
        log.info("Replacing identity.xml with OpenIDSkipUserConsent property set to true");

        String carbonHome = CarbonUtils.getCarbonHome();
        identityXML = new File(carbonHome + File.separator
                + "repository" + File.separator + "conf" + File.separator + "identity.xml");
        File configuredIdentityXML = new File(getISResourceLocation()
                + File.separator + "openId" + File.separator
                + "identity-skipuserconsent.xml");

        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredIdentityXML, identityXML, true);
        serverConfigurationManager.restartGracefully();
    }

    private void resetISConfiguration() throws Exception{
        log.info("Replacing identity.xml with default configurations");

        File defaultIdentityXML = new File(getISResourceLocation()
                + File.separator + "openId" + File.separator
                + "identity-default.xml");

        serverConfigurationManager.applyConfigurationWithoutRestart(defaultIdentityXML, identityXML, true);
        serverConfigurationManager.restartGracefully();
    }

    static class KeyValue{
        private String key;
        private String value;

        KeyValue(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public String getKey() {
            return key;
        }
    }
}
