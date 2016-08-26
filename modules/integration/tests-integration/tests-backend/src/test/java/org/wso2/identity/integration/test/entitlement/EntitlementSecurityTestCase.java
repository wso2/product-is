/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.entitlement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;

import junit.framework.Assert;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.commons.lang.StringEscapeUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import javax.servlet.http.HttpServletResponse;

public class EntitlementSecurityTestCase extends ISIntegrationTest {
    private HttpClient httpClient;
    private String value;
    private String url;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        httpClient = HttpClientBuilder.create().build();
        value = "<script>alert(1);</script>";
        String encodedValue = URLEncoder.encode(value, "UTF-8");
        String temp = backendURL.replaceAll("services/","carbon/policyeditor/prettyPrinter_ajaxprocessor.jsp?xmlString=");
        url = temp + encodedValue;
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        httpClient = null;
    }

    @Test(alwaysRun = true, description = "Test reflected XSS in XACML policy editor")
    public void testXSS() throws IOException {
        HttpGet request = new HttpGet(url);
        HttpResponse response = httpClient.execute(request);

        // If HTTP status code is 200...
        if (response.getStatusLine().getStatusCode() == HttpServletResponse.SC_OK) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line;
            boolean success = false;
            while ((line = rd.readLine()) != null) {
                success = line.equals(StringEscapeUtils.escapeHtml(value));
                if (success){
                    break;
                }
            }

            rd.close();

            if(!success) {
                Assert.fail("Content is not encoded");
            }
        }

        // If HTTP status code is 405...
        else if (response.getStatusLine().getStatusCode() == HttpServletResponse.SC_METHOD_NOT_ALLOWED){
            log.info("GET request to XACML policy editor is blocked as expected");
        }

        // If HTTP status code is neither 200 nor 405...
        else {
            log.info("Unknown response");
        }
    }
}
