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

/**
 * TestUtils contains utilities used by the ActiveSTSTestcase.
 */
public class TestUtils {

    /**
     * Convert a value input stream to a string.
     *
     * @param inputStream Input stream containing a value to be converted to a string.
     * @return result.toString() Received input stream value as a string.
     *
     * @throws IOException if an error occurs while converting the input stream value to a string.
     */
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

    /**
     * Load an xml template file from resources.
     *
     * @param resource Path to the resource.
     * @return templatedRSTR A template of a RSTR as a string.
     *
     * @throws WSTrustClientException if an error occurs while loading the template.
     */
    public static String loadTemplate(String resource) throws WSTrustClientException {

        String templatedRSTR;
        try (InputStream inputStream = TestUtils.class.getClassLoader().getResourceAsStream(resource)) {
            templatedRSTR = convertInputStreamToString(inputStream);
        } catch (IOException e) {
            throw new WSTrustClientException("Error occurred while loading the template.", e);
        }

        return templatedRSTR;
    }

    /**
     * Convert a SOAPMessage to a String.
     *
     * @param soapMessage SOAP Message to be converted to string.
     * @return stringSoapMessage Received SOAP Message as a string.
     *
     * @throws WSTrustClientException if an error occurs while converting the SOAP Message to a string.
     */
    public static String convertSoapMessageToString(SOAPMessage soapMessage) throws WSTrustClientException {

        String stringSoapMessage;

        try (OutputStream outputStream = new ByteArrayOutputStream()) {
            soapMessage.writeTo(outputStream);
            stringSoapMessage = (outputStream).toString();
        } catch (SOAPException e) {
            throw new WSTrustClientException("Error while converting the SOAP Message to a string.", e);
        } catch (IOException e) {
            throw new WSTrustClientException("Error while writing the SOAP Message to an output stream.", e);
        }

        return stringSoapMessage;
    }
}

