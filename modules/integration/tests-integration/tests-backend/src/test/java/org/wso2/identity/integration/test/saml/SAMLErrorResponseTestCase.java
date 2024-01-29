/*
 * Copyright (c) 2017, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.saml;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAML2Configuration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAML2ServiceProvider;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAMLAssertionConfiguration;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAMLAttributeProfile;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAMLResponseSigning;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SingleLogoutProfile;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SingleSignOnProfile;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import static org.testng.Assert.assertTrue;

public class SAMLErrorResponseTestCase extends ISIntegrationTest {

    // SAML Application attributes
    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    private static final String APPLICATION_NAME = "SAML-SSO-TestApplication";
    private static final String ARTIFACT_ID = "travelocity.com";

    private static final String SAML_SSO_URL = "https://localhost:9853/samlsso";
    private static final String ACS_URL = "http://localhost:8490/%s/home.jsp";
    private static final String INVALID_ACS_URL = "http://localhost:8490/%s/index.jsp";
    private static final String SAML_SSO_LOGIN_URL = "http://localhost:8490/%s/samlsso?SAML2.HTTPBinding=%s";

    private static final String NAMEID_FORMAT = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
    private static final String RELAY_STATE = "token";
    private static final String HTTP_POST_BINDING = "HTTP-POST";
    private static final String JAVAX_NET_SSL_TRUSTORE = "javax.net.ssl.trustStore";
    private static final String JAVAX_NET_SSL_TRUSTORE_PASSWORD = "javax.net.ssl.trustStorePassword";
    private static final String JAVAX_NET_SSL_TRUSTORE_TYPE = "javax.net.ssl.trustStoreType";
    private static final String ERROR_MSG_SAML = "<saml2p:StatusMessage>ALERT: Invalid Assertion Consumer URL value" +
            " \'http://localhost:8490/travelocity.com/home.jsp\' in the AuthnRequest message from  the issuer" +
            " \'travelocity.com\'. Possibly an attempt for a spoofing attack</saml2p:StatusMessage>";
    protected static final String DEFAULT_CHARSET = "UTF-8";
    private CloseableHttpClient httpClient;
    private CookieStore cookieStore = new BasicCookieStore();
    private OAuth2RestClient applicationMgtRestClient;
    private String appId;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        applicationMgtRestClient = new OAuth2RestClient(serverURL, tenantInfo);
        httpClient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();

        createApplication();
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {

        deleteApplication();

        applicationMgtRestClient.closeHttpClient();
        httpClient.close();
    }

    /**
     * Tests whether the RelayState and ACS Url parameters are sent to the notification.do page in SAML error
     * scenarios.
     *
     * @throws Exception Exception
     */
    @Test(alwaysRun = true, description = "Testing Relay state and ACS parameters in SAML error response", groups =
            "wso2.is")
    public void testRelayStateAndACSWithSAMLErrorResponse() throws Exception {

        // Create service provider config with mis-matching ACS to generate SAML error response
        SAML2ServiceProvider saml2AppConfig = applicationMgtRestClient.getSAMLInboundDetails(appId);
        Assert.assertNotNull(saml2AppConfig, "Adding a service provider has failed for " + ARTIFACT_ID);

        HttpResponse response;
        response = sendGetRequest(String.format(SAML_SSO_LOGIN_URL, ARTIFACT_ID, HTTP_POST_BINDING));
        String samlRequest = extractDataFromResponse(response, "SAMLRequest", 5);
        Map<String, String> paramters = new HashMap<String, String>();
        paramters.put("SAMLRequest", samlRequest);
        paramters.put("RelayState", RELAY_STATE);
        response = sendSAMLMessage(SAML_SSO_URL, paramters);
        EntityUtils.consume(response.getEntity());
        String location = response.getFirstHeader("Location").getValue();
        assertTrue(location.contains("RelayState=" + RELAY_STATE), "Redirection header to notification.do" +
                " page should contain RelayState query param sent with the request");
        String[] array = location.split("&");
        String samlResponse = null;
        for (int i=0; i< array.length; i++) {
            if (array[i].contains("SAMLResponse=")) {
                samlResponse = array[i].split("SAMLResponse=")[1];
            }
        }
        String decordedSAMLResponse = decode(URLDecoder.decode(samlResponse, DEFAULT_CHARSET));
        Assert.assertTrue(decordedSAMLResponse.contains(ERROR_MSG_SAML), "SAML response did not contained" +
                " error message");
    }

    private HttpResponse sendGetWithoutRedirect(HttpResponse response) throws IOException {

        HttpClient client = HttpClientBuilder.create().disableRedirectHandling().setDefaultCookieStore
                (cookieStore).build();

        String location = response.getFirstHeader("Location").getValue();

        HttpGet getRequest = new HttpGet(location);
        getRequest.setHeader("User-Agent", USER_AGENT);

        return client.execute(getRequest);
    }

    private void createApplication() throws Exception {

        ApplicationModel applicationCreationModel = new ApplicationModel().name(APPLICATION_NAME)
                .description("This is a test Service Provider")
                .inboundProtocolConfiguration(new InboundProtocols().
                        saml(getSAMLConfigurations()));

        appId = applicationMgtRestClient.createApplication(applicationCreationModel);
    }

    private void setSystemProperties() {

        URL resourceUrl = getClass().getResource(ISIntegrationTest.URL_SEPARATOR + "keystores" + ISIntegrationTest.URL_SEPARATOR
                + "products" + ISIntegrationTest.URL_SEPARATOR + "wso2carbon.p12");
        System.setProperty(JAVAX_NET_SSL_TRUSTORE, resourceUrl.getPath());
        System.setProperty(JAVAX_NET_SSL_TRUSTORE_PASSWORD, "wso2carbon");
        System.setProperty(JAVAX_NET_SSL_TRUSTORE_TYPE, "PKCS12");
    }

    private HttpResponse sendSAMLMessage(String url, Map<String, String> parameters) throws Exception {

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        HttpPost post = new HttpPost(getTenantQualifiedURL(url, tenantInfo.getDomain()));
        post.setHeader("User-Agent", USER_AGENT);
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            urlParameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(post);
    }

    private String extractDataFromResponse(HttpResponse response, String key, int token)
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

    private HttpResponse sendGetRequest(String url) throws Exception {

        HttpGet request = new HttpGet(url);
        request.addHeader("User-Agent", USER_AGENT);
        return httpClient.execute(request);
    }

    private HttpResponse sendRedirectRequest(HttpResponse response) throws IOException {

        Header[] headers = response.getAllHeaders();
        String url = "";
        for (Header header : headers) {
            if ("Location".equals(header.getName())) {
                url = header.getValue();
            }
        }

        HttpGet request = new HttpGet(url);
        request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("Referer", String.format(ACS_URL, ARTIFACT_ID));
        return httpClient.execute(request);
    }

    private HttpResponse sendPOSTMessage(String sessionKey) throws Exception {

        HttpPost post = new HttpPost(SAML_SSO_URL);
        post.setHeader("User-Agent", USER_AGENT);
        post.addHeader("Referer", String.format(ACS_URL, ARTIFACT_ID));
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("username", isServer.getSuperTenant().getTenantAdmin().getUserName()));
        urlParameters.add(new BasicNameValuePair("password", isServer.getSuperTenant().getTenantAdmin().getUserName()));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionKey));
        urlParameters.add(new BasicNameValuePair("tocommonauth", "true"));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClient.execute(post);
    }

    private SAML2Configuration getSAMLConfigurations() {

        SAML2ServiceProvider serviceProvider = new SAML2ServiceProvider()
                .issuer(ARTIFACT_ID)
                .addAssertionConsumerUrl(String.format(INVALID_ACS_URL, ARTIFACT_ID))
                .defaultAssertionConsumerUrl(String.format(INVALID_ACS_URL, ARTIFACT_ID))
                .attributeProfile(new SAMLAttributeProfile()
                        .enabled(false))
                .singleLogoutProfile(new SingleLogoutProfile()
                        .enabled(true))
                .responseSigning(new SAMLResponseSigning()
                        .enabled(false))
                .singleSignOnProfile(new SingleSignOnProfile()
                        .assertion(new SAMLAssertionConfiguration().nameIdFormat(NAMEID_FORMAT)));

        return new SAML2Configuration().manualConfiguration(serviceProvider);
    }

    private void deleteApplication() throws Exception {

        applicationMgtRestClient.deleteApplication(appId);
    }

    private static String decode(String encodedStr) {
        try {
            org.apache.commons.codec.binary.Base64 base64Decoder = new org.apache.commons.codec.binary.Base64();
            byte[] xmlBytes = encodedStr.getBytes(DEFAULT_CHARSET);
            byte[] base64DecodedByteArray = base64Decoder.decode(xmlBytes);

            try {
                Inflater inflater = new Inflater(true);
                inflater.setInput(base64DecodedByteArray);
                byte[] xmlMessageBytes = new byte[5000];
                int resultLength = inflater.inflate(xmlMessageBytes);

                if (!inflater.finished()) {
                    throw new RuntimeException("End of the compressed data stream has NOT been reached");
                }

                inflater.end();
                return new String(xmlMessageBytes, 0, resultLength, (DEFAULT_CHARSET));

            } catch (DataFormatException e) {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(base64DecodedByteArray);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                InflaterInputStream iis = new InflaterInputStream(byteArrayInputStream);
                byte[] buf = new byte[1024];
                int count = iis.read(buf);
                while (count != -1) {
                    byteArrayOutputStream.write(buf, 0, count);
                    count = iis.read(buf);
                }
                iis.close();

                return new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            Assert.fail("Error while decoding SAML response", e);
            return "";
        }
    }
}
