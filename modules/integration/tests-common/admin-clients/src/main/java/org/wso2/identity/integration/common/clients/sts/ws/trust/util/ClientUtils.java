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

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.wso2.identity.integration.common.clients.sts.ws.trust.exception.WSTrustClientException;

import static org.wso2.identity.integration.common.clients.sts.ws.trust.constants.Constants.ACTION_RENEW;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.constants.Constants.ACTION_REQUEST;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.constants.Constants.ACTION_VALIDATE;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.constants.Constants.STS_ENDPOINT_URL;

/**
 * Utils class used by the org.wso2.samples.is.sts.wstrust.client.Client to perform common operations.
 */
public class ClientUtils {

    /**
     * Method used to invoke the SOAP web service of WSO2 Identity Server
     * with the help of a SOAP connection.
     *
     * @param action     Action to be performed.
     * @param parameters Identifier for the Security Token.
     * @return a string[] containing the soapRequest containing the Request
     * Security Token sent to the Security Token Service and the soapResponse
     * containing the Request Security Token Response obtained from the
     * Security Token Service.
     * 
     * @throws WSTrustClientException if there are any exceptions throws while
     *                                building and sending the request.
     */
    public static SOAPMessage[] callSoapWebService(String action, String... parameters)
            throws WSTrustClientException {

        SOAPMessage[] requestAndResponse = new SOAPMessage[2];

        SOAPMessage soapRequest;
        SOAPMessage soapResponse;

        try {
            // Create SOAP Connection
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();

            // Send SOAP Message to SOAP Server
            if (action.equals(ACTION_REQUEST)) {
                soapRequest = buildRequest(action);
            } else if (action.equals(ACTION_RENEW) && parameters.length > 0) {
                soapRequest = buildRequest(action, parameters[0]);
            } else if (action.equals(ACTION_VALIDATE) && parameters.length > 0) {
                soapRequest = buildRequest(action, parameters[0]);
            } else {
                throw new WSTrustClientException("Invalid action parameter passed.");
            }

            soapResponse = soapConnection.call(soapRequest, STS_ENDPOINT_URL);

            soapConnection.close();
        } catch (SOAPException e) {
            throw new WSTrustClientException("Error while initializing the SOAP Factory.", e);
        } catch (IOException e) {
            throw new WSTrustClientException("Error occurred while creating a SOAP Message from an input stream.", e);
        }

        requestAndResponse[0] = soapRequest;
        requestAndResponse[1] = soapResponse;

        return requestAndResponse;
    }

    /**
     * Method used to build the RST relevant to the action to be performed.
     *
     * @param parameter            Action to be performed.
     * @param additionalParameters Identifier for the Security Token.
     * @return request SOAP request containing the Request Security Token.
     *
     * @throws IOException            if an error occurs while creating a SOAP Message
     *                                from an input stream.
     * @throws SOAPException          if there was an error in creating the specified
     *                                implementation of MessageFactory.
     * @throws WSTrustClientException if the operation/action type specified
     *                                is not valid.
     */
    private static SOAPMessage buildRequest(String parameter, String... additionalParameters)
            throws IOException, SOAPException, WSTrustClientException {

        SOAPMessage request;
        InputStream byteArrayInputStream;
        String[] timeStamps = generateNewTimeStamps();

        switch (parameter) {

            case ACTION_REQUEST:
                byteArrayInputStream = new ByteArrayInputStream(RequestConstructor
                        .buildRSTToRequestSecurityToken(timeStamps[0], timeStamps[1]).getBytes());
                break;

            case ACTION_RENEW:
                byteArrayInputStream = new ByteArrayInputStream(RequestConstructor
                        .buildRSTToRenewSecurityToken(timeStamps[0], timeStamps[1], additionalParameters[0]).getBytes());
                break;

            case ACTION_VALIDATE:
                byteArrayInputStream = new ByteArrayInputStream(RequestConstructor
                        .buildRSTToValidateSecurityToken(timeStamps[0], timeStamps[1], additionalParameters[0]).getBytes());
                break;

            default:
                throw new WSTrustClientException("Operations of type: Request, Renew and Validate are allowed.");
        }

        request = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL)
                .createMessage(null, byteArrayInputStream);
        byteArrayInputStream.close();

        return request;
    }

    /**
     * Generate new timestamps for the creation and expiry time of the
     * Request Security Token.
     *
     * @return timestamps string[] containing the timestamps for creation
     * and expiry time of the RequestSecurityToken.
     */
    private static String[] generateNewTimeStamps() {

        String[] timestamps = new String[2];

        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(timeZone);

        Calendar calendar = Calendar.getInstance();
        timestamps[0] = dateFormat.format(calendar.getTime());
        calendar.add(Calendar.MINUTE, 5);
        timestamps[1] = dateFormat.format(calendar.getTime());

        return timestamps;
    }
}
