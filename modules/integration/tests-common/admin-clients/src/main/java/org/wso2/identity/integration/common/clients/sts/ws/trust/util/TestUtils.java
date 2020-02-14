/*

 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.common.clients.sts.ws.trust.util;

import org.wso2.identity.integration.common.clients.sts.ws.trust.exception.WSTrustClientException;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class TestUtils {

    private static String convertInputStreamToString(InputStream inputStream)
            throws IOException {

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }

        return result.toString(StandardCharsets.UTF_8.name());

    }

    public static String loadTemplate(String resource) throws WSTrustClientException {

        String templatedRSTR;
        try (InputStream inputStream = TestUtils.class.getClassLoader().getResourceAsStream(resource)) {
            templatedRSTR = convertInputStreamToString(inputStream);
        } catch (IOException e) {
            throw new WSTrustClientException("Error occurred while loading the template.", e);
        }

        return templatedRSTR;
    }

    public static String convertResponseToString(SOAPMessage soapMessage) throws WSTrustClientException {

        String responseAsAString;

        try (OutputStream outputStream = new ByteArrayOutputStream()){
            soapMessage.writeTo(outputStream);
            responseAsAString = (outputStream).toString();
        } catch (SOAPException e) {
            throw new WSTrustClientException("Error while converting the SOAP Message to a string.", e);
        } catch (IOException e) {
            throw new WSTrustClientException("Error while writing the SOAP Message to an output stream.", e);
        }

        return responseAsAString;
    }
}

