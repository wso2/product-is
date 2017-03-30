/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.is.saml;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.xml.security.utils.EncryptionConstants;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.NameIdentifier;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.NameIDPolicy;
import org.opensaml.saml2.core.NameIDType;
import org.opensaml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.StatusMessage;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml2.core.impl.AttributeBuilder;
import org.opensaml.saml2.core.impl.AttributeStatementBuilder;
import org.opensaml.saml2.core.impl.AudienceBuilder;
import org.opensaml.saml2.core.impl.AudienceRestrictionBuilder;
import org.opensaml.saml2.core.impl.AuthnContextBuilder;
import org.opensaml.saml2.core.impl.AuthnContextClassRefBuilder;
import org.opensaml.saml2.core.impl.AuthnRequestBuilder;
import org.opensaml.saml2.core.impl.AuthnStatementBuilder;
import org.opensaml.saml2.core.impl.ConditionsBuilder;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml2.core.impl.NameIDPolicyBuilder;
import org.opensaml.saml2.core.impl.RequestedAuthnContextBuilder;
import org.opensaml.saml2.core.impl.StatusBuilder;
import org.opensaml.saml2.core.impl.StatusCodeBuilder;
import org.opensaml.saml2.core.impl.StatusMessageBuilder;
import org.opensaml.saml2.core.impl.SubjectBuilder;
import org.opensaml.saml2.core.impl.SubjectConfirmationBuilder;
import org.opensaml.saml2.core.impl.SubjectConfirmationDataBuilder;
import org.opensaml.saml2.encryption.Encrypter;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.encryption.EncryptionException;
import org.opensaml.xml.encryption.EncryptionParameters;
import org.opensaml.xml.encryption.KeyEncryptionParameters;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSStringBuilder;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.util.Base64;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.wso2.carbon.identity.auth.saml2.common.SAML2AuthConstants;
import org.wso2.carbon.identity.auth.saml2.common.SAML2AuthUtils;
import org.wso2.carbon.identity.auth.saml2.common.X509CredentialImpl;
import org.wso2.carbon.identity.authenticator.inbound.saml2sso.exception.SAML2SSOServerException;
import org.wso2.carbon.identity.common.base.exception.IdentityException;
import org.wso2.carbon.identity.common.util.keystore.KeyStoreUtils;
import org.wso2.carbon.identity.gateway.common.model.sp.ServiceProviderConfig;
import org.wso2.carbon.identity.gateway.resource.util.Utils;
import org.wso2.carbon.identity.gateway.store.ServiceProviderConfigStore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Utilities for SAML inbound tests.
 */
public class TestUtils {

    private static Logger logger = LoggerFactory.getLogger(TestUtils.class);

    public static HttpURLConnection request(String path, String method, boolean keepAlive) throws IOException {

        URL url = new URL(path);

        HttpURLConnection httpURLConnection = null;

        httpURLConnection = (HttpURLConnection) url.openConnection();

        httpURLConnection.setRequestMethod(method);
        if (!keepAlive) {
            httpURLConnection.setRequestProperty("CONNECTION", "CLOSE");
        }
        return httpURLConnection;

    }

    public static String getContent(HttpURLConnection urlConn) throws IOException {
        return new String(IOUtils.toByteArray(urlConn.getInputStream()), Charsets.UTF_8);
    }

    public static String getResponseHeader(String headerName, HttpURLConnection urlConnection) {
        return ((HttpURLConnection) urlConnection).getHeaderField(headerName);
    }


    public static Response getSAMLResponse(String samlResponse) throws SAML2SSOServerException {
        String decodedResponse = new String(Base64.decode(samlResponse));
        XMLObject xmlObject = SAML2AuthUtils.unmarshall(decodedResponse);

        return (Response) xmlObject;
    }

    public static ServiceProviderConfig getServiceProviderConfigs(String uniqueId, BundleContext bundleContext) {
        ServiceProviderConfigStore serviceProviderConfigStore = bundleContext.getService(bundleContext
                .getServiceReference(ServiceProviderConfigStore.class));
        return serviceProviderConfigStore.getServiceProvider(uniqueId);
    }


    public static AuthnRequest buildAuthnRequest(String idpUrl, boolean isForce, boolean isPassive, String
            issuerName, String acsUrl) {

        IssuerBuilder issuerBuilder = new IssuerBuilder();
        Issuer issuer = issuerBuilder.buildObject("urn:oasis:names:tc:SAML:2.0:assertion", "Issuer", "samlp");

        String spEntityId = issuerName;
        issuer.setValue(spEntityId);

        DateTime issueInstant = new DateTime();

        AuthnRequestBuilder authRequestBuilder = new AuthnRequestBuilder();
        AuthnRequest authRequest = authRequestBuilder.buildObject("urn:oasis:names:tc:SAML:2.0:protocol",
                "AuthnRequest", "samlp");
        authRequest.setForceAuthn(isForce);
        authRequest.setIsPassive(isPassive);
        authRequest.setIssueInstant(issueInstant);

        // how about redirect binding URI?
        authRequest.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        authRequest.setAssertionConsumerServiceURL(acsUrl);
        authRequest.setIssuer(issuer);
        authRequest.setID(SAML2AuthUtils.createID());
        authRequest.setVersion(SAMLVersion.VERSION_20);
        authRequest.setDestination(idpUrl);

        String attributeConsumingServiceIndex = "2342342";
        if (StringUtils.isNotBlank(attributeConsumingServiceIndex)) {
            try {
                authRequest.setAttributeConsumingServiceIndex(Integer.valueOf(attributeConsumingServiceIndex));
            } catch (NumberFormatException e) {
                logger.error("Error while setting AttributeConsumingServiceIndex to SAMLRequest.", e);
            }
        }

        boolean includeNameIDPolicy = true;
        if (includeNameIDPolicy) {
            NameIDPolicyBuilder nameIdPolicyBuilder = new NameIDPolicyBuilder();
            NameIDPolicy nameIdPolicy = nameIdPolicyBuilder.buildObject();
            nameIdPolicy.setFormat(NameIDType.UNSPECIFIED);
            //nameIdPolicy.setSPNameQualifier("Issuer");
            nameIdPolicy.setAllowCreate(true);
            authRequest.setNameIDPolicy(nameIdPolicy);
        }

        buildRequestedAuthnContext(authRequest);
        return authRequest;
    }

    public static void buildRequestedAuthnContext(AuthnRequest authnRequest) {

        RequestedAuthnContextBuilder requestedAuthnContextBuilder = null;
        RequestedAuthnContext requestedAuthnContext = null;

        String includeAuthnContext = "";
        requestedAuthnContextBuilder = new RequestedAuthnContextBuilder();
        requestedAuthnContext = requestedAuthnContextBuilder.buildObject();
        AuthnContextClassRefBuilder authnContextClassRefBuilder = new AuthnContextClassRefBuilder();
        AuthnContextClassRef authnContextClassRef = authnContextClassRefBuilder
                .buildObject(SAMLConstants.SAML20_NS,
                        AuthnContextClassRef.DEFAULT_ELEMENT_LOCAL_NAME,
                        SAMLConstants.SAML20_PREFIX);

        String authnContext = "urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport";
        if (StringUtils.isNotBlank(authnContext)) {
            authnContextClassRef.setAuthnContextClassRef(authnContext);
        } else {
            authnContextClassRef.setAuthnContextClassRef(AuthnContext.PPT_AUTHN_CTX);
        }

        String authnContextComparison = "exact";
        if (StringUtils.isNotEmpty(authnContextComparison)) {
            if (AuthnContextComparisonTypeEnumeration.EXACT.toString().equalsIgnoreCase(
                    authnContextComparison)) {
                requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.EXACT);
            } else if (AuthnContextComparisonTypeEnumeration.MINIMUM.toString().equalsIgnoreCase(
                    authnContextComparison)) {
                requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.MINIMUM);
            } else if (AuthnContextComparisonTypeEnumeration.MAXIMUM.toString().equalsIgnoreCase(
                    authnContextComparison)) {
                requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.MAXIMUM);
            } else if (AuthnContextComparisonTypeEnumeration.BETTER.toString().equalsIgnoreCase(
                    authnContextComparison)) {
                requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.BETTER);
            }
        } else {
            requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.EXACT);
        }
        requestedAuthnContext.getAuthnContextClassRefs().add(authnContextClassRef);
        if (requestedAuthnContext != null) {
            authnRequest.setRequestedAuthnContext(requestedAuthnContext);
        }
    }

    public static String getParameterFromHTML(String html, String startingTag, String endingTag) {
        if (StringUtils.isEmpty(html) || StringUtils.isEmpty(startingTag) || StringUtils.isEmpty(endingTag)) {
            return null;
        }
        String secondPart = html.split(startingTag)[1];
        if (StringUtils.isNotBlank(secondPart)) {
            return secondPart.split(endingTag)[0];
        }

        return null;
    }


    public static String getSAMLResponse(boolean isEncryptedAssertion, String audience, boolean setSignature, boolean
            isSignAssertions) throws IdentityException {

        Response response = new org.opensaml.saml2.core.impl.ResponseBuilder().buildObject();
        response.setIssuer(getIssuer());
        response.setID(createID());
        response.setDestination("https://localhost:9292/gateway");
        response.setStatus(buildStatus("urn:oasis:names:tc:SAML:2.0:status:Success", null));
        response.setVersion(SAMLVersion.VERSION_20);
        DateTime issueInstant = new DateTime();
        DateTime notOnOrAfter = new DateTime(issueInstant.getMillis()
                + 100 * 60 * 1000L);
        response.setIssueInstant(issueInstant);
        //@TODO sessionHandling
        String sessionId = "";
        Assertion assertion = buildAssertion(notOnOrAfter, audience, isSignAssertions);
        if (isEncryptedAssertion) {
            encryptAssertion(response, assertion);
        } else {
            response.getAssertions().add(assertion);
        }
        if (setSignature) {
            SAML2AuthUtils.setSignature(response, "http://www.w3.org/2000/09/xmldsig#rsa-sha1", "http://www.w3" +
                    ".org/2000/09/xmldsig#sha1", false, SAML2AuthUtils.getServerCredentials());
        }
        String respString = encode(marshall(response));
        return respString;
    }

    public static Assertion buildAssertion(DateTime notOnOrAfter, String audience, boolean setSignature) throws
            IdentityException {

        try {

            DateTime currentTime = new DateTime();
            Assertion samlAssertion = new AssertionBuilder().buildObject();
            samlAssertion.setID(createID());
            samlAssertion.setVersion(SAMLVersion.VERSION_20);
            samlAssertion.setIssuer(getIssuer());
            samlAssertion.setIssueInstant(currentTime);
            Subject subject = new SubjectBuilder().buildObject();

            NameID nameId = new NameIDBuilder().buildObject();
            // TODO
            nameId.setValue(TestConstants.AUTHENTICATED_USER_NAME);

            nameId.setFormat(NameIdentifier.EMAIL);

            subject.setNameID(nameId);

            SubjectConfirmation subjectConfirmation = new SubjectConfirmationBuilder()
                    .buildObject();
            subjectConfirmation.setMethod("urn:oasis:names:tc:SAML:2.0:cm:bearer");
            SubjectConfirmationData scData = new SubjectConfirmationDataBuilder().buildObject();
            scData.setRecipient("http://localhost:8080/gateway");
            scData.setNotOnOrAfter(notOnOrAfter);
            subjectConfirmation.setSubjectConfirmationData(scData);
            subject.getSubjectConfirmations().add(subjectConfirmation);
            samlAssertion.setSubject(subject);

            AuthnStatement authStmt = new AuthnStatementBuilder().buildObject();
            authStmt.setAuthnInstant(new DateTime());

            AuthnContext authContext = new AuthnContextBuilder().buildObject();
            AuthnContextClassRef authCtxClassRef = new AuthnContextClassRefBuilder().buildObject();
            authCtxClassRef.setAuthnContextClassRef(AuthnContext.PASSWORD_AUTHN_CTX);
            authContext.setAuthnContextClassRef(authCtxClassRef);
            authStmt.setAuthnContext(authContext);
            samlAssertion.getAuthnStatements().add(authStmt);

            /*
                * If <AttributeConsumingServiceIndex> element is in the <AuthnRequest> and according to
                * the spec 2.0 the subject MUST be in the assertion
                */
            Map<String, String> claims = new HashMap<String, String>();
            claims.put("http://org.sample.idp/claims/email", "testuser@wso2.com");
            claims.put("http://org.sample.idp/claims/fullname", "testuser_fullname");
            claims.put("http://org.sample.idp/claims/gender", "male");
            if (claims != null && !claims.isEmpty()) {
                AttributeStatement attrStmt = buildAttributeStatement(claims);
                if (attrStmt != null) {
                    samlAssertion.getAttributeStatements().add(attrStmt);
                }
            }

            AudienceRestriction audienceRestriction = new AudienceRestrictionBuilder()
                    .buildObject();
            Audience issuerAudience = new AudienceBuilder().buildObject();
//            issuerAudience.setAudienceURI(context.getIssuerWithDomain());
            audienceRestriction.getAudiences().add(issuerAudience);

            Audience audienceObj = new AudienceBuilder().buildObject();
            audienceObj.setAudienceURI(audience);
            audienceRestriction.getAudiences().add(audienceObj);

            Conditions conditions = new ConditionsBuilder().buildObject();
            conditions.setNotBefore(currentTime);
            conditions.setNotOnOrAfter(notOnOrAfter);
            conditions.getAudienceRestrictions().add(audienceRestriction);
            samlAssertion.setConditions(conditions);

            if (setSignature) {
                SAML2AuthUtils.setSignature(samlAssertion, SAML2AuthConstants.XML.SignatureAlgorithmURI.RSA_SHA1,
                        SAML2AuthConstants.XML.DigestAlgorithmURI.SHA1, false, SAML2AuthUtils.getServerCredentials());
            }
            return samlAssertion;
        } catch (Exception e) {
            logger.error("Error while building assertion", e);
        }
        return null;
    }

    public static String createID() {

        try {
            SecureRandomIdentifierGenerator generator = new SecureRandomIdentifierGenerator();
            return generator.generateIdentifier();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error while generating random ID", e);
        }
        return null;
    }

    public static Issuer getIssuer() {
        Issuer issuer = new IssuerBuilder().buildObject();
        String idPEntityId = "localhost";
        issuer.setValue(idPEntityId);
        issuer.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:entity");
        return issuer;
    }

    public static String marshall(XMLObject xmlObject) {

        ByteArrayOutputStream byteArrayOutputStrm = null;
        try {
            doBootstrap();
            MarshallerFactory marshallerFactory = org.opensaml.xml.Configuration.getMarshallerFactory();
            Marshaller marshaller = marshallerFactory.getMarshaller(xmlObject);
            Element element = marshaller.marshall(xmlObject);

            byteArrayOutputStrm = new ByteArrayOutputStream();
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
            LSSerializer writer = impl.createLSSerializer();
            LSOutput output = impl.createLSOutput();
            output.setByteStream(byteArrayOutputStrm);
            writer.write(element, output);
            return byteArrayOutputStrm.toString(StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            logger.error("Error while marshalling xml object", e);
        } finally {
            if (byteArrayOutputStrm != null) {
                try {
                    byteArrayOutputStrm.close();
                } catch (IOException e) {
                    logger.error("Error while closing byteArrayOutputStream", e);
                }
            }
        }
        return null;
    }

    public static void doBootstrap() {
        try {
            DefaultBootstrap.bootstrap();
        } catch (ConfigurationException e) {
            logger.error("Error while bootstrapping opensaml", e);
        }
    }

    public static String encode(String xmlString) {
        // Encoding the message
        String encodedRequestMessage =
                Base64.encodeBytes(xmlString.getBytes(StandardCharsets.UTF_8),
                        Base64.DONT_BREAK_LINES);
        return encodedRequestMessage.trim();
    }

    private static Status buildStatus(String status, String statMsg) {

        Status stat = new StatusBuilder().buildObject();

        // Set the status code
        StatusCode statCode = new StatusCodeBuilder().buildObject();
        statCode.setValue(status);
        stat.setStatusCode(statCode);

        // Set the status Message
        if (statMsg != null) {
            StatusMessage statMesssage = new StatusMessageBuilder().buildObject();
            statMesssage.setMessage(statMsg);
            stat.setStatusMessage(statMesssage);
        }

        return stat;
    }

    public static AttributeStatement buildAttributeStatement(Map<String, String> claims) {

        org.opensaml.saml2.core.AttributeStatement attStmt = new AttributeStatementBuilder().buildObject();
        Iterator<Map.Entry<String, String>> iterator = claims.entrySet().iterator();
        boolean atLeastOneNotEmpty = false;
        for (int i = 0; i < claims.size(); i++) {
            Map.Entry<String, String> claimEntry = iterator.next();
            String claimUri = claimEntry.getKey();
            String claimValue = claimEntry.getValue();
            if (claimUri != null && !claimUri.trim().isEmpty() && claimValue != null && !claimValue.trim().isEmpty()) {
                atLeastOneNotEmpty = true;
                Attribute attribute = new AttributeBuilder().buildObject();
                attribute.setName(claimUri);
                attribute.setNameFormat(Attribute.BASIC);
                XSStringBuilder stringBuilder = (XSStringBuilder) Configuration.getBuilderFactory().
                        getBuilder(XSString.TYPE_NAME);
                XSString stringValue;

                stringValue = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
                stringValue.setValue(claimValue);
                attribute.getAttributeValues().add(stringValue);

                attStmt.getAttributes().add(attribute);
            }
        }
        if (atLeastOneNotEmpty) {
            return attStmt;
        } else {
            return null;
        }
    }

    public static void encryptAssertion(Response response, Assertion assertion) {


        String encodedCert = TestConstants.SERVER_PUB_CERT;
        if (StringUtils.isBlank(encodedCert)) {
            throw new RuntimeException("Encryption certificate is not configured.");
        }
        Certificate certificate = null;
        try {
            certificate = KeyStoreUtils.getInstance().decodeCertificate(encodedCert);
        } catch (CertificateException e) {
            throw new RuntimeException("Error while decoding certificate", e);
        }

        Credential symmetricCredential = null;
        try {
            symmetricCredential = SecurityHelper.getSimpleCredential(
                    SecurityHelper.generateSymmetricKey(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES256));
        } catch (NoSuchAlgorithmException | KeyException e) {
            logger.error("Error while getting symmetric credentials", e);
        }

        EncryptionParameters encParams = new EncryptionParameters();
        encParams.setAlgorithm(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES256);
        encParams.setEncryptionCredential(symmetricCredential);

        KeyEncryptionParameters keyEncryptionParameters = new KeyEncryptionParameters();
        keyEncryptionParameters.setAlgorithm(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSA15);
        keyEncryptionParameters.setEncryptionCredential(new X509CredentialImpl((X509Certificate) certificate));

        Encrypter encrypter = new Encrypter(encParams, keyEncryptionParameters);
        encrypter.setKeyPlacement(Encrypter.KeyPlacement.INLINE);

        EncryptedAssertion encryptedAssertion = null;
        try {
            encryptedAssertion = encrypter.encrypt(assertion);
        } catch (EncryptionException e) {
            logger.error("Error while encrypting assertion", e);
        }

        response.getEncryptedAssertions().add(encryptedAssertion);
    }

    public static Map<String, String> getClaims(String claims) {
        Map<String, String> responseClaimMap = new HashMap<>();
        String[] claimStets = claims.split("-");
        for (String claimSet : claimStets) {
            String[] claim = claimSet.split(",");
            responseClaimMap.put(claim[0], claim[1]);
        }
        return responseClaimMap;
    }

    public static String getQueryParam(String queryString, String param) {
        Map<String, String> queryParamMap = Utils.getQueryParamMap(queryString);
        return queryParamMap.get(param);
    }
}
