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

package org.wso2.identity.integration.test.sts;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

import javax.xml.soap.SOAPMessage;

import org.wso2.identity.integration.common.clients.sts.ws.trust.util.ClientUtils;
import org.wso2.identity.integration.common.clients.sts.ws.trust.util.TestUtils;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.identity.integration.common.clients.sts.ws.trust.constants.Constants.ACTION_RENEW;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.constants.Constants.ACTION_REQUEST;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.constants.Constants.ACTION_VALIDATE;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.constants.TestConstants.CHANGING_XPATHS_FOR_RENEW_ST_RSTR;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.constants.TestConstants.CHANGING_XPATHS_FOR_REQUEST_ST_RSTR;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.constants.TestConstants.CHANGING_XPATHS_FOR_VALIDATE_ST_RSTR;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.constants.TestConstants.NO_OF_DIFFERENCES_FOR_RENEW_ST_RSTR;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.constants.TestConstants.NO_OF_DIFFERENCES_FOR_REQUEST_ST_RSTR;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.constants.TestConstants.NO_OF_DIFFERENCES_FOR_VALIDATE_ST_RSTR;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.constants.TestConstants.RENEW_ST_RSTR_TEMPLATE;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.constants.TestConstants.REQUEST_ST_RSTR_TEMPLATE;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.constants.TestConstants.VALIDATE_ST_RSTR_TEMPLATE;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.constants.TestConstants.XML_DECLARATION;
import static org.wso2.identity.integration.common.clients.sts.ws.trust.util.TestUtils.convertSoapMessageToString;

public class ActiveSTSTestCase extends ISIntegrationTest {

    private String uri;

    @Test(alwaysRun = true, description = "Validate the response obtained " +
            "when a security token is request from the sts.")
    public void validateRequestSecurityTokenRSTR() throws Exception {

        SOAPMessage[] requestSTRequestAndResponse = ClientUtils.callSoapWebService(ACTION_REQUEST);
        uri = (requestSTRequestAndResponse[1].getSOAPBody().getElementsByTagName("wsse:Reference")
                .item(0).getAttributes().getNamedItem("URI").getNodeValue()).substring(1);

        String requestSTResponseTemplate = TestUtils.loadTemplate(REQUEST_ST_RSTR_TEMPLATE);
        String requestSTResponse = convertSoapMessageToString(requestSTRequestAndResponse[1])
                .replace(XML_DECLARATION, "");

        compareWithTemplate(requestSTResponseTemplate, requestSTResponse,
                CHANGING_XPATHS_FOR_REQUEST_ST_RSTR, NO_OF_DIFFERENCES_FOR_REQUEST_ST_RSTR);
    }

    @Test(alwaysRun = true, description = "Validate the response obtained when " +
            "a renewal request for a security token is sent to the sts.",
            dependsOnMethods = {"validateRequestSecurityTokenRSTR"})
    public void validateRenewSecurityTokenRSTR() throws Exception {

        SOAPMessage[] renewSTRequestAndResponse = ClientUtils.callSoapWebService(ACTION_RENEW, uri);

        String renewSTResponseTemplate = TestUtils.loadTemplate(RENEW_ST_RSTR_TEMPLATE);
        String renewSTResponse = convertSoapMessageToString(renewSTRequestAndResponse[1])
                .replace(XML_DECLARATION, "");

        compareWithTemplate(renewSTResponseTemplate, renewSTResponse,
                CHANGING_XPATHS_FOR_RENEW_ST_RSTR, NO_OF_DIFFERENCES_FOR_RENEW_ST_RSTR);
    }

    @Test(alwaysRun = true, description = "Validate the response obtained when a " +
            "request is sent to validate a security token from the sts.",
            dependsOnMethods = {"validateRenewSecurityTokenRSTR"})
    public void validateSecurityTokenValidateRSTR() throws Exception {

        SOAPMessage[] validateSTRequestAndResponse = ClientUtils.callSoapWebService(ACTION_VALIDATE, uri);

        String validateSTResponseTemplate = TestUtils.loadTemplate(VALIDATE_ST_RSTR_TEMPLATE);
        String validateSTResponse = convertSoapMessageToString(validateSTRequestAndResponse[1])
                .replace(XML_DECLARATION, "");

        compareWithTemplate(validateSTResponseTemplate, validateSTResponse,
                CHANGING_XPATHS_FOR_VALIDATE_ST_RSTR, NO_OF_DIFFERENCES_FOR_VALIDATE_ST_RSTR);
    }

    private void compareWithTemplate(String staticTemplate, String obtainedResponse,
                                     List<String> expectedValues, int expectedNoOfChanges) {

        Diff diff = DiffBuilder.compare(staticTemplate).ignoreWhitespace()
                .withTest(obtainedResponse).ignoreWhitespace().checkForSimilar().build();

        List<String> xPaths = new ArrayList<>();

        for (Difference difference : diff.getDifferences()) {
            xPaths.add(difference.getComparison().getTestDetails().getXPath());
        }

        Assert.assertTrue(xPaths.containsAll(expectedValues));
        Assert.assertEquals(xPaths.size(), expectedNoOfChanges);
    }

    private void printQuery(String message, String query) {

        System.out.println(message + ": \n" + query + "\n");
    }
}
