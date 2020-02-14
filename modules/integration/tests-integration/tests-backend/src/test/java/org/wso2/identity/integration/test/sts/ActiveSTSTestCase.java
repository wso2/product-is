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
import static org.wso2.identity.integration.common.clients.sts.ws.trust.util.TestUtils.convertResponseToString;

public class ActiveSTSTestCase extends ISIntegrationTest {

    private String uri;

    @Test(alwaysRun = true, description = "Validate the response obtained " +
            "when a security token is request from the sts.")
    public void validateRequestSecurityTokenRSTR() throws Exception {

        SOAPMessage[] requestSTRequestAndResponse = ClientUtils.callSoapWebService(ACTION_REQUEST);
        uri = (requestSTRequestAndResponse[1].getSOAPBody().getElementsByTagName("wsse:Reference")
                .item(0).getAttributes().getNamedItem("URI").getNodeValue()).substring(1);

        String requestSTResponseTemplate = TestUtils.loadTemplate(REQUEST_ST_RSTR_TEMPLATE);
        String requestSTResponse = convertResponseToString(requestSTRequestAndResponse[1])
                .replace(XML_DECLARATION, "");


        Diff diff = DiffBuilder.compare(requestSTResponseTemplate).ignoreWhitespace()
                .withTest(requestSTResponse).ignoreWhitespace().checkForSimilar().build();

        int noOfDifferences = 0;

        for (Difference difference : diff.getDifferences()) {
            Assert.assertEquals(difference.getComparison().getTestDetails().getXPath(),
                    CHANGING_XPATHS_FOR_REQUEST_ST_RSTR.get(noOfDifferences));
            noOfDifferences++;
        }

        Assert.assertEquals(noOfDifferences, NO_OF_DIFFERENCES_FOR_REQUEST_ST_RSTR);

    }

    @Test(alwaysRun = true, description = "Validate the response obtained when " +
            "a renewal request for a security token is sent to the sts.",
            dependsOnMethods = {"validateRequestSecurityTokenRSTR"})
    public void validateRenewSecurityTokenRSTR() throws Exception {

        SOAPMessage[] renewSTRequestAndResponse = ClientUtils.callSoapWebService(ACTION_RENEW, uri);

        String renewSTResponseTemplate = TestUtils.loadTemplate(RENEW_ST_RSTR_TEMPLATE);
        String renewSTResponse = convertResponseToString(renewSTRequestAndResponse[1])
                .replace(XML_DECLARATION, "");


        Diff diff = DiffBuilder.compare(renewSTResponseTemplate).ignoreWhitespace()
                .withTest(renewSTResponse).ignoreWhitespace().checkForSimilar().build();

        int noOfDifferences = 0;

        for (Difference difference : diff.getDifferences()) {
            Assert.assertEquals(difference.getComparison().getTestDetails().getXPath(),
                    CHANGING_XPATHS_FOR_RENEW_ST_RSTR.get(noOfDifferences));
            noOfDifferences++;
        }

        Assert.assertEquals(noOfDifferences, NO_OF_DIFFERENCES_FOR_RENEW_ST_RSTR);
    }

    @Test(alwaysRun = true, description = "Validate the response obtained when a " +
            "request is sent to validate a security token from the sts.",
            dependsOnMethods = {"validateRenewSecurityTokenRSTR"})
    public void validateSecurityTokenValidateRSTR() throws Exception {

        SOAPMessage[] validateSTRequestAndResponse = ClientUtils.callSoapWebService(ACTION_VALIDATE, uri);

        String validateSTResponseTemplate = TestUtils.loadTemplate(VALIDATE_ST_RSTR_TEMPLATE);
        String validateSTResponse = convertResponseToString(validateSTRequestAndResponse[1])
                .replace(XML_DECLARATION, "");

        compareWithTemplate(validateSTResponseTemplate, validateSTResponse,
                CHANGING_XPATHS_FOR_VALIDATE_ST_RSTR, NO_OF_DIFFERENCES_FOR_VALIDATE_ST_RSTR);

        Diff diff = DiffBuilder.compare(validateSTResponseTemplate).ignoreWhitespace()
                .withTest(validateSTResponse).ignoreWhitespace().checkForSimilar().build();

        List<String> xPaths = new ArrayList<>();

        for (Difference difference : diff.getDifferences()) {
            xPaths.add(difference.getComparison().getTestDetails().getXPath());
        }

        Assert.assertTrue(xPaths.containsAll(CHANGING_XPATHS_FOR_VALIDATE_ST_RSTR));
        Assert.assertEquals(xPaths.size(), NO_OF_DIFFERENCES_FOR_VALIDATE_ST_RSTR);
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
