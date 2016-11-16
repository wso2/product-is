/*
*  Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.identity.integration.test.requestPathAuthenticator;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class RequestPathAuthenticatorInvalidUserTestCase extends RequestPathAuthenticatorBaseTestCase {

    @Test(alwaysRun = true, description = "Request path authenticator login fail",
            dependsOnMethods = { "testLoginSuccess" })
    public void testLoginFail() throws Exception {
        HttpPost request = new HttpPost(TRAVELOCITY_SAMPLE_APP_URL + "/samlsso?SAML2.HTTPBinding=HTTP-POST");
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("username", super.adminUsername));
        urlParameters.add(new BasicNameValuePair("password", "admin123"));
        request.setEntity(new UrlEncodedFormEntity(urlParameters));
        HttpResponse response = client.execute(request);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line;
        String samlRequest = "";
        String secToken = "";

        while ((line = rd.readLine()) != null) {
            if (line.contains("name='SAMLRequest'")) {
                String[] tokens = line.split("'");
                samlRequest = tokens[5];
            }
            if (line.contains("name='sectoken'")) {
                String[] tokens = line.split("'");
                secToken = tokens[5];
            }
        }
        EntityUtils.consume(response.getEntity());
        request = new HttpPost(isURL + "samlsso");
        urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("sectoken", secToken));
        urlParameters.add(new BasicNameValuePair("SAMLRequest", samlRequest));
        request.setEntity(new UrlEncodedFormEntity(urlParameters));
        HttpResponse response2 = client.execute(request);
        int responseCode = response2.getStatusLine().getStatusCode();
        Assert.assertEquals(responseCode, 302, "Login failure response returned code " + responseCode);
        Header location = response2.getFirstHeader("location");
        String SAMLResponse = location.getValue().split("&SAMLResponse=")[1].split("&")[0];
        SAMLResponse = decode(java.net.URLDecoder.decode(SAMLResponse, (DEFAULT_CHARSET)));
        Assert.assertTrue(SAMLResponse.contains("User authentication failed"),
                "SAML Response does not contained " + "error message at login failure.");
    }

    /**
     * Decoding and deflating the encoded AuthReq
     *
     * @param encodedStr encoded AuthReq
     * @return decoded AuthReq
     */
    private static String decode(String encodedStr) {
        try {
            Base64 base64Decoder = new Base64();
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
                String decodedString = new String(xmlMessageBytes, 0, resultLength, (DEFAULT_CHARSET));
                return decodedString;

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
                String decodedStr = new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);

                return decodedStr;
            }
        } catch (IOException e) {
            Assert.fail("Error while decoding SAML response", e);
            return "";
        }
    }

}
