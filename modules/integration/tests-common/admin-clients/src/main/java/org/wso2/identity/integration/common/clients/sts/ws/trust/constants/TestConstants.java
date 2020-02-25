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

package org.wso2.identity.integration.common.clients.sts.ws.trust.constants;

import java.util.Arrays;
import java.util.List;

/**
 * TestConstants contains constants which are used by ActiveSTSTesCase.
 */
public class TestConstants {

    // Location of template files.
    public static final String REQUEST_ST_RSTR_TEMPLATE = "ws-trust-templates/request_security_token_RSTR.xml";
    public static final String RENEW_ST_RSTR_TEMPLATE = "ws-trust-templates/renew_security_token_RSTR.xml";
    public static final String VALIDATE_ST_RSTR_TEMPLATE = "ws-trust-templates/validate_security_token_RSTR.xml";

    public static final String XML_DECLARATION = "<?xml version='1.0' encoding='utf-8'?>";

    // Resources used to validate the RSTR received when a security token is requested.
    public static final int NO_OF_DIFFERENCES_FOR_REQUEST_ST_RSTR = 17;
    public static final List<String> CHANGING_XPATHS_FOR_REQUEST_ST_RSTR = Arrays.asList(
            "/Envelope[1]/Header[1]/Security[1]/Timestamp[1]/@Id",
            "/Envelope[1]/Header[1]/Security[1]/Timestamp[1]/Created[1]/text()[1]",
            "/Envelope[1]/Header[1]/Security[1]/Timestamp[1]/Expires[1]/text()[1]",
            "/Envelope[1]/Header[1]/MessageID[1]/text()[1]",
            "/Envelope[1]/Body[1]/RequestSecurityTokenResponse[1]/RequestedAttachedReference[1]/SecurityTokenReference[1]/Reference[1]/@URI",
            "/Envelope[1]/Body[1]/RequestSecurityTokenResponse[1]/RequestedUnattachedReference[1]/SecurityTokenReference[1]/Reference[1]/@URI",
            "/Envelope[1]/Body[1]/RequestSecurityTokenResponse[1]/Lifetime[1]/Created[1]/text()[1]",
            "/Envelope[1]/Body[1]/RequestSecurityTokenResponse[1]/Lifetime[1]/Expires[1]/text()[1]",
            "/Envelope[1]/Body[1]/RequestSecurityTokenResponse[1]/RequestedSecurityToken[1]/Assertion[1]/@ID",
            "/Envelope[1]/Body[1]/RequestSecurityTokenResponse[1]/RequestedSecurityToken[1]/Assertion[1]/@IssueInstant",
            "/Envelope[1]/Body[1]/RequestSecurityTokenResponse[1]/RequestedSecurityToken[1]/Assertion[1]/Signature[1]/SignedInfo[1]/Reference[1]/@URI",
            "/Envelope[1]/Body[1]/RequestSecurityTokenResponse[1]/RequestedSecurityToken[1]/Assertion[1]/Signature[1]/SignedInfo[1]/Reference[1]/DigestValue[1]/text()[1]",
            "/Envelope[1]/Body[1]/RequestSecurityTokenResponse[1]/RequestedSecurityToken[1]/Assertion[1]/Signature[1]/SignatureValue[1]/text()[1]",
            "/Envelope[1]/Body[1]/RequestSecurityTokenResponse[1]/RequestedSecurityToken[1]/Assertion[1]/Signature[1]/KeyInfo[1]/X509Data[1]/X509Certificate[1]/text()[1]",
            "/Envelope[1]/Body[1]/RequestSecurityTokenResponse[1]/RequestedSecurityToken[1]/Assertion[1]/Conditions[1]/@NotBefore",
            "/Envelope[1]/Body[1]/RequestSecurityTokenResponse[1]/RequestedSecurityToken[1]/Assertion[1]/Conditions[1]/@NotOnOrAfter",
            "/Envelope[1]/Body[1]/RequestSecurityTokenResponse[1]/RequestedSecurityToken[1]/Assertion[1]/AuthnStatement[1]/@AuthnInstant"
    );

    // Resources used to validate the RSTR received when a security token is renewed.
    public static final int NO_OF_DIFFERENCES_FOR_RENEW_ST_RSTR = 15;
    public static final List<String> CHANGING_XPATHS_FOR_RENEW_ST_RSTR = Arrays.asList(
            "/Envelope[1]/Header[1]/Security[1]/Timestamp[1]/@Id",
            "/Envelope[1]/Header[1]/Security[1]/Timestamp[1]/Created[1]/text()[1]",
            "/Envelope[1]/Header[1]/Security[1]/Timestamp[1]/Expires[1]/text()[1]",
            "/Envelope[1]/Header[1]/MessageID[1]/text()[1]",
            "/Envelope[1]/Body[1]/RequestSecurityTokenResponse[1]/Lifetime[1]/Created[1]/text()[1]",
            "/Envelope[1]/Body[1]/RequestSecurityTokenResponse[1]/Lifetime[1]/Expires[1]/text()[1]",
            "/Envelope[1]/Body[1]/RequestSecurityTokenResponse[1]/RequestedSecurityToken[1]/Assertion[1]/@ID",
            "/Envelope[1]/Body[1]/RequestSecurityTokenResponse[1]/RequestedSecurityToken[1]/Assertion[1]/@IssueInstant",
            "/Envelope[1]/Body[1]/RequestSecurityTokenResponse[1]/RequestedSecurityToken[1]/Assertion[1]/Signature[1]/SignedInfo[1]/Reference[1]/@URI",
            "/Envelope[1]/Body[1]/RequestSecurityTokenResponse[1]/RequestedSecurityToken[1]/Assertion[1]/Signature[1]/SignedInfo[1]/Reference[1]/DigestValue[1]/text()[1]",
            "/Envelope[1]/Body[1]/RequestSecurityTokenResponse[1]/RequestedSecurityToken[1]/Assertion[1]/Signature[1]/SignatureValue[1]/text()[1]",
            "/Envelope[1]/Body[1]/RequestSecurityTokenResponse[1]/RequestedSecurityToken[1]/Assertion[1]/Signature[1]/KeyInfo[1]/X509Data[1]/X509Certificate[1]/text()[1]",
            "/Envelope[1]/Body[1]/RequestSecurityTokenResponse[1]/RequestedSecurityToken[1]/Assertion[1]/Conditions[1]/@NotBefore",
            "/Envelope[1]/Body[1]/RequestSecurityTokenResponse[1]/RequestedSecurityToken[1]/Assertion[1]/Conditions[1]/@NotOnOrAfter",
            "/Envelope[1]/Body[1]/RequestSecurityTokenResponse[1]/RequestedSecurityToken[1]/Assertion[1]/AuthnStatement[1]/@AuthnInstant"
    );

    // Resources used to validate the RSTR received when a security token is validated.
    public static final int NO_OF_DIFFERENCES_FOR_VALIDATE_ST_RSTR = 4;
    public static final List<String> CHANGING_XPATHS_FOR_VALIDATE_ST_RSTR = Arrays.asList(
            "/Envelope[1]/Header[1]/Security[1]/Timestamp[1]/@Id",
            "/Envelope[1]/Header[1]/Security[1]/Timestamp[1]/Created[1]/text()[1]",
            "/Envelope[1]/Header[1]/Security[1]/Timestamp[1]/Expires[1]/text()[1]",
            "/Envelope[1]/Header[1]/MessageID[1]/text()[1]"
    );
}
