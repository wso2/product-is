/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.integration.test.scim2;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Tomcat;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.application.mgt.AbstractIdentityFederationTestCase;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SCIM2TestSuiteTestCase extends ISIntegrationTest {

    private static final int TOMCAT_8490 = 8490;
    private Map<Integer, Tomcat> tomcatServers;
    JSONArray jsonArray;
    private static final String SUCCESS = "Success";

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {

        tomcatServers = new HashMap<Integer, Tomcat>();
        // Deploy webapp in Tomcat
        startTomcat(TOMCAT_8490);
        File configuredIdentityXML = new File(getISResourceLocation()
                + File.separator + "scim2" + File.separator
                + "scimproxycompliance.war");
        addWebAppToTomcat(TOMCAT_8490, "/scimproxycompliance", configuredIdentityXML.getAbsolutePath());
        runTestSuite();
    }

    @DataProvider(name = "scim2-compliance")
    public Object[][] results() {

        String[][] resultArray = new String[25][2];
        for (int i = 0; i < 25; i++) {
            resultArray[i][0] = ((JSONObject) (jsonArray.get(i))).get("status_text").toString();
            resultArray[i][1] = ((JSONObject) (jsonArray.get(i))).get("name").toString();
        }
        return resultArray;
    }

    // This test will run 25 times
    @Test(dataProvider = "scim2-compliance")
    public void testResults(String status, String name) {

        System.out.println(status + " " + name);
        Assert.assertEquals(SUCCESS,
                status, name);
    }

    public void addWebAppToTomcat(int port, String webAppUrl, String webAppPath)
            throws LifecycleException {

        tomcatServers.get(port).addWebapp(tomcatServers.get(port).getHost(), webAppUrl, webAppPath);
    }

    public void startTomcat(int port) throws LifecycleException {

        Tomcat tomcat = new Tomcat();
        tomcat.getService().setContainer(tomcat.getEngine());
        tomcat.setPort(port);
        tomcat.setBaseDir("");

        StandardHost stdHost = (StandardHost) tomcat.getHost();
        stdHost.setAppBase("");
        stdHost.setAutoDeploy(true);
        stdHost.setDeployOnStartup(true);
        stdHost.setUnpackWARs(true);
        tomcat.setHost(stdHost);

        setSystemProperties();
        tomcatServers.put(port, tomcat);
        tomcat.start();
    }

    private void setSystemProperties() {

        URL resourceUrl = getClass().getResource(File.separator + "keystores" + File.separator + "products" + File.separator + "wso2carbon.jks");
        System.setProperty("javax.net.ssl.trustStore", resourceUrl.getPath());
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
    }

    public void runTestSuite() throws Exception {

        List<NameValuePair> postParameters;
        HttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://localhost:" + "8080" + "/scimproxycompliance/compliance2/test2");
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("url", "https://localhost:9853/scim2"));
        postParameters.add(new BasicNameValuePair("authMethod", "basicAuth"));
        postParameters.add(new BasicNameValuePair("username", "admin"));
        postParameters.add(new BasicNameValuePair("password", "admin"));
        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
        HttpResponse response = client.execute(httpPost);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";

        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        String a = result.toString();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(a);
        jsonArray = (JSONArray) json.get("results");
    }
}
