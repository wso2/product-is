/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.scenarios.commons;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.apache.xml.security.c14n.Canonicalizer;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.SignableSAMLObject;
import org.opensaml.common.impl.SAMLObjectContentReference;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.common.Extensions;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameIDPolicy;
import org.opensaml.saml2.core.RequestAbstractType;
import org.opensaml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.impl.AuthnContextClassRefBuilder;
import org.opensaml.saml2.core.impl.AuthnRequestBuilder;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.NameIDPolicyBuilder;
import org.opensaml.saml2.core.impl.RequestedAuthnContextBuilder;
import org.opensaml.saml2.encryption.Decrypter;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.encryption.EncryptedKey;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.security.keyinfo.StaticKeyInfoCredentialResolver;
import org.opensaml.xml.security.x509.X509Credential;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.signature.X509Data;
import org.opensaml.xml.signature.impl.SignatureImpl;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.XMLHelper;
import org.opensaml.xml.validation.ValidationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.sso.saml.stub.IdentitySAMLSSOConfigServiceIdentityException;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderInfoDTO;
import org.wso2.identity.scenarios.commons.clients.sso.saml.SAMLSSOConfigServiceClient;
import org.wso2.identity.scenarios.commons.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.scenarios.commons.security.SSOAgentX509KeyStoreCredential;
import org.wso2.identity.scenarios.commons.security.X509CredentialImpl;
import org.xml.sax.SAXException;

import javax.crypto.SecretKey;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import static org.opensaml.DefaultBootstrap.bootstrap;
import static org.wso2.identity.scenarios.commons.util.Constants.AUTHN_CONTEXT_CLASS_REF;
import static org.wso2.identity.scenarios.commons.util.Constants.AUTHN_REQUEST;
import static org.wso2.identity.scenarios.commons.util.Constants.INBOUND_AUTH_TYPE_SAML;
import static org.wso2.identity.scenarios.commons.util.Constants.ISSUER;
import static org.wso2.identity.scenarios.commons.util.Constants.NAMESPACE_PREFIX;
import static org.wso2.identity.scenarios.commons.util.Constants.PASSWORD_PROTECTED_TRANSPORT_CLASS;
import static org.wso2.identity.scenarios.commons.util.Constants.SAML_ASSERTION_URN;
import static org.wso2.identity.scenarios.commons.util.Constants.SAML_PROTOCOL_URN;
import static org.wso2.identity.scenarios.commons.util.Constants.SAML_RESPONSE_PARAM;
import static org.wso2.identity.scenarios.commons.util.Constants.TOCOMMONAUTH;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.extractValueFromResponse;
import static org.wso2.identity.scenarios.commons.util.SSOUtil.sendLoginPostWithParamsAndHeaders;

/**
 * Base test class for SAML SSO tests.
 */
public class SAML2SSOTestBase extends SSOTestBase {

    private static final Log log = LogFactory.getLog(SAML2SSOTestBase.class);

    private static Random random = new Random();

    protected static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";

    protected static final String XML_SIGNATURE_ALGORITHM_SHA1_RSA = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";

    protected static final String XML_DIGEST_ALGORITHM_SHA1 = "http://www.w3.org/2000/09/xmldsig#sha1";

    private static final String TENANT_DOMAIN_PARAM = "tenantDomain";

    private static final String TRUSTSTORE_LOCATION = "javax.net.ssl.trustStore";

    private static final String TRUSTSTORE_PASSWORD = "javax.net.ssl.trustStorePassword";

    protected static final String SAML_SSO_URL = "%s/samlsso";

    protected static final String SAML_IDP_SLO_URL = SAML_SSO_URL + "?slo=true";

    protected static final String COMMON_AUTH_URL = "%s/commonauth";

    private static final String KEYSTORE_FILE_NAME = "wso2carbon.jks";

    private String privatekeyPassword = "wso2carbon";

    private String privatekeyAlias = "wso2carbon";

    private String publicCertAlias = "wso2carbon";

    private static final int ENTITY_EXPANSION_LIMIT = 0;

    private static String DEFAULT_MULTI_ATTRIBUTE_SEPARATOR = ",";

    protected String samlSSOIDPUrl;

    protected String samlIdpSloUrl;

    protected String commonAuthUrl;

    protected SAMLSSOConfigServiceClient ssoConfigServiceClient;

    protected RemoteUserStoreManagerServiceClient remoteUSMServiceClient;

    protected X509Credential defaultX509Cred;

    private static boolean isBootStrapped = false;


    public void init() throws Exception {
        super.init();
        samlSSOIDPUrl = String.format(SAML_SSO_URL, backendURL);
        samlIdpSloUrl = String.format(SAML_SSO_URL, backendURL);
        commonAuthUrl = String.format(COMMON_AUTH_URL, backendURL);
        ssoConfigServiceClient = new SAMLSSOConfigServiceClient(backendServiceURL, sessionCookie);
        remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendServiceURL, sessionCookie);

        defaultX509Cred = new X509CredentialImpl(new SSOAgentX509KeyStoreCredential(new FileInputStream(
                System.getProperty(TRUSTSTORE_LOCATION)), System.getProperty(TRUSTSTORE_PASSWORD).toCharArray(),
                publicCertAlias, privatekeyAlias, privatekeyPassword.toCharArray()));
    }

    /**
     * Get default X509 credentials.
     *
     * @return
     */
    protected X509Credential getDefaultX509Cred() {
        return defaultX509Cred;
    }

    /**
     * Retrieve the SAML SSO service provider from a service provider.
     *
     * @param serviceProvider
     * @return
     * @throws RemoteException
     * @throws IdentitySAMLSSOConfigServiceIdentityException
     */
    protected SAMLSSOServiceProviderDTO getSAMLSSOServiceProvider(ServiceProvider serviceProvider)
            throws RemoteException, IdentitySAMLSSOConfigServiceIdentityException {
        InboundAuthenticationRequestConfig[] inboundAuthRequestConfigs = serviceProvider
                .getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs();

        for (InboundAuthenticationRequestConfig inboundAuthRequestConfig : inboundAuthRequestConfigs) {
            if (INBOUND_AUTH_TYPE_SAML.equals(inboundAuthRequestConfig.getInboundAuthType())) {
                return getSAMLSSOServiceProviderByIssuer(inboundAuthRequestConfig.getInboundAuthKey());
            }
        }
        return null;
    }

    /**
     * Retrieve the SAML SSO service provider from the issuer name.
     *
     * @param issuerName
     * @return
     * @throws RemoteException
     * @throws IdentitySAMLSSOConfigServiceIdentityException
     */
    protected SAMLSSOServiceProviderDTO getSAMLSSOServiceProviderByIssuer(String issuerName)
            throws RemoteException, IdentitySAMLSSOConfigServiceIdentityException {
        SAMLSSOServiceProviderInfoDTO samlssoSPInfoDTO = ssoConfigServiceClient.getServiceProviders();

        for (SAMLSSOServiceProviderDTO samlssoSPDTO : samlssoSPInfoDTO.getServiceProviders()) {
            if (issuerName.equals(samlssoSPDTO.getIssuer())) {
                return samlssoSPDTO;
            }
        }
        return null;
    }

    /**
     * Send SAML Post message.
     *
     * @param url
     * @param samlMsgKey
     * @param samlMsgValue
     * @return
     * @throws IOException
     */
    protected HttpResponse sendSAMLPostMessage(CloseableHttpClient client, String url, String samlMsgKey,
                                               String samlMsgValue, TestConfig testConfig)
            throws IOException {
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        HttpPost post = new HttpPost(url);
        post.setHeader("User-Agent", USER_AGENT);
        urlParameters.add(new BasicNameValuePair(samlMsgKey, samlMsgValue));
        if (testConfig.getUserMode() == TestUserMode.TENANT_ADMIN ||
                testConfig.getUserMode() == TestUserMode.TENANT_USER) {
            urlParameters.add(new BasicNameValuePair(TENANT_DOMAIN_PARAM, testConfig.getUser()
                    .getTenantDomain()));
        }
        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        return client.execute(post);
    }

    /**
     * Build SAML2 Authentication Request
     *
     * @param spEntityID
     * @param nameIDFormat
     * @param isPassiveAuthn
     * @param isForceAuthn
     * @param httpBinding
     * @param acsUrl
     * @param destinationUrl
     * @return
     */
    protected AuthnRequest buildAuthnRequest(String spEntityID, String nameIDFormat, boolean isPassiveAuthn,
                                             boolean isForceAuthn, String httpBinding, String acsUrl,
                                             String destinationUrl) {
        IssuerBuilder issuerBuilder = new IssuerBuilder();
        Issuer issuer = issuerBuilder.buildObject(SAML_ASSERTION_URN, ISSUER, NAMESPACE_PREFIX);
        issuer.setValue(spEntityID);

        NameIDPolicyBuilder nameIdPolicyBuilder = new NameIDPolicyBuilder();
        NameIDPolicy nameIdPolicy = nameIdPolicyBuilder.buildObject();
        nameIdPolicy.setFormat(nameIDFormat);
        nameIdPolicy.setSPNameQualifier(ISSUER);
        nameIdPolicy.setAllowCreate(true);

        AuthnContextClassRefBuilder authnContextClassRefBuilder = new AuthnContextClassRefBuilder();
        AuthnContextClassRef authnContextClassRef =
                authnContextClassRefBuilder.buildObject(SAML_ASSERTION_URN, AUTHN_CONTEXT_CLASS_REF,
                        NAMESPACE_PREFIX);
        authnContextClassRef.setAuthnContextClassRef(PASSWORD_PROTECTED_TRANSPORT_CLASS);


        RequestedAuthnContextBuilder requestedAuthnContextBuilder =
                new RequestedAuthnContextBuilder();
        RequestedAuthnContext requestedAuthnContext = requestedAuthnContextBuilder.buildObject();
        requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.EXACT);
        requestedAuthnContext.getAuthnContextClassRefs().add(authnContextClassRef);

        DateTime issueInstant = new DateTime();

        AuthnRequestBuilder authRequestBuilder = new AuthnRequestBuilder();
        AuthnRequest authRequest =
                authRequestBuilder.buildObject(SAML_PROTOCOL_URN, AUTHN_REQUEST, NAMESPACE_PREFIX);

        authRequest.setForceAuthn(isForceAuthn);
        authRequest.setIsPassive(isPassiveAuthn);
        authRequest.setIssueInstant(issueInstant);
        authRequest.setProtocolBinding(httpBinding);
        authRequest.setAssertionConsumerServiceURL(acsUrl);
        authRequest.setIssuer(issuer);
        authRequest.setNameIDPolicy(nameIdPolicy);
        authRequest.setRequestedAuthnContext(requestedAuthnContext);
        authRequest.setID(createID());
        authRequest.setVersion(SAMLVersion.VERSION_20);
        authRequest.setDestination(destinationUrl);

        return authRequest;
    }

    protected AuthnRequest buildAuthnRequest(SAMLSSOServiceProviderDTO samlssoSPDTO, boolean isPassiveAuthn,
                                             boolean isForceAuthn, String httpBinding,
                                             String destinationUrl, Extensions extensions) {
        AuthnRequest authRequest = buildAuthnRequest(samlssoSPDTO, isPassiveAuthn,
                isForceAuthn, httpBinding, destinationUrl);
        authRequest.setExtensions(extensions);
        return authRequest;
    }

    protected AuthnRequest buildAuthnRequest(SAMLSSOServiceProviderDTO samlssoSPDTO, boolean isPassiveAuthn,
                                             boolean isForceAuthn, String httpBinding,
                                             String destinationUrl, Integer consumerServiceIndex) {
        AuthnRequest authRequest = buildAuthnRequest(samlssoSPDTO, isPassiveAuthn, isForceAuthn,
                httpBinding, destinationUrl);
        // Requesting Attributes. This Index value is registered in the IDP.
        authRequest.setAssertionConsumerServiceIndex(consumerServiceIndex);
        return authRequest;
    }

    protected AuthnRequest buildAuthnRequest(SAMLSSOServiceProviderDTO samlssoSPDTO, boolean isPassiveAuthn,
                                             boolean isForceAuthn, String httpBinding,
                                             String destinationUrl, Extensions extensions,
                                             Integer consumerServiceIndex) {
        AuthnRequest authRequest = buildAuthnRequest(samlssoSPDTO, isPassiveAuthn,
                isForceAuthn, httpBinding, destinationUrl);
        authRequest.setExtensions(extensions);
        // Requesting Attributes. This Index value is registered in the IDP.
        authRequest.setAssertionConsumerServiceIndex(consumerServiceIndex);
        return authRequest;
    }

    protected AuthnRequest buildAuthnRequest(SAMLSSOServiceProviderDTO samlssoSPDTO, boolean isPassiveAuthn,
                                             boolean isForceAuthn, String httpBinding,
                                             String destinationUrl) {
        return buildAuthnRequest(samlssoSPDTO.getIssuer(), samlssoSPDTO.getNameIDFormat(), isForceAuthn,
                isPassiveAuthn, httpBinding, samlssoSPDTO.getDefaultAssertionConsumerUrl(), destinationUrl);
    }

    /**
     * Base64 encoding of the SAML request.
     *
     * @param requestMessage
     * @param binding
     * @return
     * @throws MarshallingException
     * @throws IOException
     * @throws ConfigurationException
     */
    protected String encodeRequestMessage(SignableSAMLObject requestMessage, String binding)
            throws Exception {
        doBootstrap();
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");

        Marshaller marshaller = Configuration.getMarshallerFactory().getMarshaller(requestMessage);
        Element authDOM = null;
        authDOM = marshaller.marshall(requestMessage);
        StringWriter rspWrt = new StringWriter();
        XMLHelper.writeNode(authDOM, rspWrt);
        if (SAMLConstants.SAML2_REDIRECT_BINDING_URI.equals(binding)) {
            //Compress the message, Base 64 encode and URL encode
            Deflater deflater = new Deflater(Deflater.DEFLATED, true);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream
                    (byteArrayOutputStream, deflater);
            deflaterOutputStream.write(rspWrt.toString().getBytes(Charset.forName("UTF-8")));
            deflaterOutputStream.close();
            String encodedRequestMessage = Base64.encodeBytes(byteArrayOutputStream
                    .toByteArray(), Base64.DONT_BREAK_LINES);
            return URLEncoder.encode(encodedRequestMessage, "UTF-8").trim();
        } else if (SAMLConstants.SAML2_POST_BINDING_URI.equals(binding)) {
            return Base64.encodeBytes(rspWrt.toString().getBytes(),
                    Base64.DONT_BREAK_LINES);
        } else {
            log.warn("Unsupported SAML2 HTTP Binding. Defaulting to " +
                    SAMLConstants.SAML2_POST_BINDING_URI);
            return Base64.encodeBytes(rspWrt.toString().getBytes(),
                    Base64.DONT_BREAK_LINES);
        }
    }

    /**
     * Add Signature to xml post request
     *
     * @param request            AuthnReuqest
     * @param signatureAlgorithm Signature Algorithm
     * @param digestAlgorithm    Digest algorithm to be used while digesting message
     * @param includeCert        Whether to include certificate in request or not
     * @throws Exception
     */
    protected void setSignature(RequestAbstractType request, String signatureAlgorithm,
                                String digestAlgorithm, boolean includeCert,
                                X509Credential x509Credential) throws Exception {
        doBootstrap();

        if (StringUtils.isEmpty(signatureAlgorithm)) {
            signatureAlgorithm = XML_SIGNATURE_ALGORITHM_SHA1_RSA;
        }
        if (StringUtils.isEmpty(digestAlgorithm)) {
            digestAlgorithm = XML_DIGEST_ALGORITHM_SHA1;
        }

        Signature signature = (Signature) buildXMLObject(Signature.DEFAULT_ELEMENT_NAME);
        signature.setSigningCredential(x509Credential);
        signature.setSignatureAlgorithm(signatureAlgorithm);
        signature.setCanonicalizationAlgorithm(Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

        if (includeCert) {
            KeyInfo keyInfo = (KeyInfo) buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME);
            X509Data data = (X509Data) buildXMLObject(X509Data.DEFAULT_ELEMENT_NAME);
            org.opensaml.xml.signature.X509Certificate cert = (org.opensaml.xml.signature.X509Certificate)
                    buildXMLObject(org.opensaml.xml.signature.X509Certificate.DEFAULT_ELEMENT_NAME);
            String value = null;
            value = org.apache.xml.security.utils.Base64.encode(x509Credential.getEntityCertificate()
                    .getEncoded());
            cert.setValue(value);
            data.getX509Certificates().add(cert);
            keyInfo.getX509Datas().add(data);
            signature.setKeyInfo(keyInfo);
        }

        request.setSignature(signature);
        ((SAMLObjectContentReference) signature.getContentReferences().get(0))
                .setDigestAlgorithm(digestAlgorithm);

        List<Signature> signatureList = new ArrayList<Signature>();
        signatureList.add(signature);
        // Marshall and Sign
        MarshallerFactory marshallerFactory = org.opensaml.xml.Configuration.getMarshallerFactory();
        Marshaller marshaller = marshallerFactory.getMarshaller(request);
        marshaller.marshall(request);
        org.apache.xml.security.Init.init();
        Signer.signObjects(signatureList);
    }

    /**
     * Send SAML POST request
     *
     * @param sessionKey
     * @param url
     * @param userAgent
     * @param acsUrl
     * @param artifact
     * @param userName
     * @param password
     * @param httpClient
     * @return
     * @throws Exception
     */
    protected HttpResponse sendLoginPostMessage(String sessionKey, String url, String userAgent, String
            acsUrl, String artifact, String userName, String password, HttpClient httpClient) throws Exception {
        Header[] headers = new Header[2];
        headers[0] = new BasicHeader(HttpHeaders.USER_AGENT, userAgent);
        headers[1] = new BasicHeader(HttpHeaders.REFERER, String.format(acsUrl, artifact));
        Map<String, String> urlParameters = new HashMap<>();
        urlParameters.put(TOCOMMONAUTH, "true");
        return sendLoginPostWithParamsAndHeaders(httpClient, sessionKey, url, userName, password, urlParameters,
                headers);
    }

    /**
     * Get SAML response object from the SAML response string.
     *
     * @param samlResponse
     * @return
     * @throws Exception
     */
    protected Response processSAMLResponse(String samlResponse) throws Exception {
        String saml2ResponseString =
                new String(Base64.decode(samlResponse), Charset.forName("UTF-8"));
        XMLObject response = unmarshall(saml2ResponseString);

        // Check for duplicate samlp:Response
        NodeList list = response.getDOM().getElementsByTagNameNS(SAMLConstants.SAML20P_NS, "Response");
        if (list.getLength() > 0) {
            log.error("Invalid schema for the SAML2 response. Multiple Response elements found.");
            throw new Exception("Error occurred while processing SAML2 response.");
        }

        // Checking for multiple Assertions
        NodeList assertionList = response.getDOM().getElementsByTagNameNS(SAMLConstants.SAML20_NS, "Assertion");
        if (assertionList.getLength() > 1) {
            log.error("Invalid schema for the SAML2 response. Multiple Assertion elements found.");
            throw new Exception("Error occurred while processing SAML2 response.");
        }

        return (Response) response;
    }

    /**
     * Extract SAML Assertion from the response.
     *
     * @param samlResponse
     * @param samlssoSPDTO
     * @param x509Credential
     * @return
     * @throws Exception
     */
    protected Assertion getAssertionFromSAMLResponse(Response samlResponse, SAMLSSOServiceProviderDTO samlssoSPDTO,
                                                     X509Credential x509Credential)
            throws Exception {
        Assertion assertion = null;

        if (samlssoSPDTO.isDoEnableEncryptedAssertionSpecified()) {
            List<EncryptedAssertion> encryptedAssertions = samlResponse.getEncryptedAssertions();
            EncryptedAssertion encryptedAssertion = null;
            if (!CollectionUtils.isEmpty(encryptedAssertions)) {
                encryptedAssertion = encryptedAssertions.get(0);
                try {
                    assertion = getDecryptedAssertion(encryptedAssertion, x509Credential);
                } catch (Exception e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Assertion decryption failure : ", e);
                    }
                    throw new Exception("Unable to decrypt the SAML2 Assertion", e);
                }
            }
        } else {
            List<Assertion> assertions = samlResponse.getAssertions();
            if (assertions != null && !assertions.isEmpty()) {
                assertion = assertions.get(0);
            }
        }
        if (assertion == null && !isNoPassive(samlResponse)) {
            throw new Exception("SAML2 Assertion not found in the Response");
        }
        return assertion;
    }

    /**
     * Retrieve the map of attributes from the SAML2 Assertion.
     *
     * @param assertion
     * @return
     */
    protected Map<String, String> getAttributesMapFromAssertion(Assertion assertion) {
        return getAttributesMapFromAssertion(assertion, DEFAULT_MULTI_ATTRIBUTE_SEPARATOR);
    }

    /**
     * Retrieve the map of attributes from the SAML2 Assertion.
     *
     * @param assertion
     * @param multiAttributeSeparator
     * @return
     */
    protected Map<String, String> getAttributesMapFromAssertion(Assertion assertion, String multiAttributeSeparator) {
        Map<String, String> results = new HashMap<String, String>();

        if (assertion != null && assertion.getAttributeStatements() != null) {
            List<AttributeStatement> attributeStatementList = assertion.getAttributeStatements();

            for (AttributeStatement statement : attributeStatementList) {
                List<Attribute> attributesList = statement.getAttributes();
                for (Attribute attribute : attributesList) {
                    List<XMLObject> multipleAttributeValues = attribute.getAttributeValues();
                    if (CollectionUtils.isNotEmpty(multipleAttributeValues)) {
                        List<String> valueList = new ArrayList<>();
                        for (XMLObject attributeVal : multipleAttributeValues) {
                            Element value = attributeVal.getDOM();
                            valueList.add(value.getTextContent());
                        }
                        String attributeValue = StringUtils.join(valueList.iterator(), multiAttributeSeparator);
                        results.put(attribute.getName(), attributeValue);
                    }
                }
            }
        }
        return results;
    }

    private boolean isNoPassive(Response response) {
        return response.getStatus() != null &&
                response.getStatus().getStatusCode() != null &&
                response.getStatus().getStatusCode().getValue().equals(StatusCode.RESPONDER_URI) &&
                response.getStatus().getStatusCode().getStatusCode() != null &&
                response.getStatus().getStatusCode().getStatusCode().getValue().equals(
                        StatusCode.NO_PASSIVE_URI);
    }

    /**
     * Validate the AudienceRestriction of SAML2 Response.
     *
     * @param assertion SAML2 Assertion
     * @return validity
     */
    protected boolean validateAudienceRestriction(Assertion assertion, String refAudienceURI) throws Exception {
        if (assertion != null) {
            Conditions conditions = assertion.getConditions();
            if (conditions != null) {
                List<AudienceRestriction> audienceRestrictions = conditions.getAudienceRestrictions();
                if (audienceRestrictions != null && !audienceRestrictions.isEmpty()) {
                    boolean audienceFound = false;
                    for (AudienceRestriction audienceRestriction : audienceRestrictions) {
                        if (audienceRestriction.getAudiences() != null &&
                                !audienceRestriction.getAudiences().isEmpty()) {
                            for (Audience audience : audienceRestriction.getAudiences()) {
                                if (refAudienceURI.equals(audience.getAudienceURI())) {
                                    audienceFound = true;
                                    break;
                                }
                            }
                        }
                        if (audienceFound) {
                            break;
                        }
                    }
                    return audienceFound;
                } else {
                    throw new Exception("SAML2 Response doesn't contain AudienceRestrictions");
                }
            } else {
                throw new Exception("SAML2 Response doesn't contain Conditions");
            }
        } else {
            throw new Exception("SAML2 Assertion not found");
        }
    }

    /**
     * Validate the signature of a SAML2 Response.
     *
     * @param response SAML2 Response
     */
    protected void validateSAMLResponseSignature(Response response,
                                                 SAMLSSOServiceProviderDTO samlssoServiceProviderDTO,
                                                 X509Credential x509Credential)
            throws Exception {
        if (samlssoServiceProviderDTO.isDoSignResponseSpecified()) {
            if (response.getSignature() == null) {
                throw new Exception("SAML2 Response signing is enabled, but signature element not " +
                        "found in SAML2 Response element");
            } else {
                validateSignature(response.getSignature(), x509Credential);
            }
        }
    }

    /**
     * Validate the signature of a SAML2 Assertion.
     *
     * @param assertion SAML2 Assertion
     */
    protected void validateSAMLAssertionSignature(Assertion assertion,
                                                  SAMLSSOServiceProviderDTO samlssoServiceProviderDTO,
                                                  X509Credential x509Credential)
            throws Exception {
        if (samlssoServiceProviderDTO.isDoSignAssertionsSpecified()) {
            if (assertion.getSignature() == null) {
                throw new Exception("SAML2 Response signing is enabled, but signature element not " +
                        "found in SAML2 Response element");
            } else {
                validateSignature(assertion.getSignature(), x509Credential);
            }
        }
    }

    /**
     * Validates the XML Signature object.
     *
     * @param signature XMLObject
     * @throws Exception
     */
    private void validateSignature(XMLObject signature,
                                   X509Credential x509Credential)
            throws Exception {

        SignatureImpl signImpl = (SignatureImpl) signature;
        try {
            SAMLSignatureProfileValidator signatureProfileValidator = new SAMLSignatureProfileValidator();
            signatureProfileValidator.validate(signImpl);
        } catch (ValidationException ex) {
            String logMsg = "Signature do not confirm to SAML signature profile. Possible XML Signature " +
                    "Wrapping  Attack!";
            if (log.isDebugEnabled()) {
                log.debug(logMsg, ex);
            }
            throw new Exception(logMsg, ex);
        }

        try {
            SignatureValidator validator = new SignatureValidator(x509Credential);
            validator.validate(signImpl);
        } catch (ValidationException e) {
            if (log.isDebugEnabled()) {
                log.debug("Validation exception : ", e);
            }
            throw new Exception("Signature validation failed for SAML2 Element");
        }
    }

    /**
     * Get Decrypted Assertion
     *
     * @param encryptedAssertion
     * @return
     * @throws Exception
     */
    private Assertion getDecryptedAssertion(EncryptedAssertion encryptedAssertion,
                                            X509Credential x509Credential) throws Exception {
        KeyInfoCredentialResolver keyResolver = new StaticKeyInfoCredentialResolver(x509Credential);

        EncryptedKey key = encryptedAssertion.getEncryptedData().
                getKeyInfo().getEncryptedKeys().get(0);
        Decrypter decrypter = new Decrypter(null, keyResolver, null);
        SecretKey dkey = (SecretKey) decrypter.decryptKey(key, encryptedAssertion.getEncryptedData().
                getEncryptionMethod().getAlgorithm());
        Credential shared = SecurityHelper.getSimpleCredential(dkey);
        decrypter = new Decrypter(new StaticKeyInfoCredentialResolver(shared), null, null);
        decrypter.setRootInNewDocument(true);

        return decrypter.decrypt(encryptedAssertion);
    }

    protected static String extractSAMLResponse(HttpResponse response) throws IOException {
        return extractValueFromResponse(response, "name='" + SAML_RESPONSE_PARAM + "'", 5);
    }

    /**
     * Create user
     *
     * @param testConfig
     */
    protected void createUserFromTestConfig(TestConfig testConfig) {
        super.createUser(testConfig, remoteUSMServiceClient, "default");
    }

    private XMLObject buildXMLObject(QName objectQName) throws ConfigurationException {
        doBootstrap();
        XMLObjectBuilder builder = org.opensaml.xml.Configuration.getBuilderFactory().getBuilder(objectQName);
        return builder.buildObject(objectQName.getNamespaceURI(), objectQName.getLocalPart(), objectQName.getPrefix());
    }

    private XMLObject unmarshall(String saml2SSOString) throws Exception {
        doBootstrap();

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setXIncludeAware(false);
        documentBuilderFactory.setExpandEntityReferences(false);
        try {
            documentBuilderFactory
                    .setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
            documentBuilderFactory
                    .setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
            documentBuilderFactory
                    .setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        } catch (ParserConfigurationException e) {
            log.error("Failed to load XML Processor Feature " + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE + " or " +
                    Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or " + Constants.LOAD_EXTERNAL_DTD_FEATURE +
                    " or secure-processing.");
        }

        org.apache.xerces.util.SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
        documentBuilderFactory
                .setAttribute(Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY, securityManager);

        documentBuilderFactory.setIgnoringComments(true);
        Document document = getDocument(documentBuilderFactory, saml2SSOString);
        if (isSignedWithComments(document)) {
            documentBuilderFactory.setIgnoringComments(false);
            document = getDocument(documentBuilderFactory, saml2SSOString);
        }
        Element element = document.getDocumentElement();
        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
        return unmarshaller.unmarshall(element);

    }

    /**
     * Return whether SAML Assertion has the canonicalization method
     * set to 'http://www.w3.org/2001/10/xml-exc-c14n#WithComments'.
     *
     * @param document
     * @return true if canonicalization method equals to 'http://www.w3.org/2001/10/xml-exc-c14n#WithComments'
     */
    private boolean isSignedWithComments(Document document) {

        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            String assertionId = (String) xPath.compile("//*[local-name()='Assertion']/@ID")
                    .evaluate(document, XPathConstants.STRING);

            if (StringUtils.isBlank(assertionId)) {
                return false;
            }

            NodeList nodeList = ((NodeList) xPath.compile(
                    "//*[local-name()='Assertion']" +
                            "/*[local-name()='Signature']" +
                            "/*[local-name()='SignedInfo']" +
                            "/*[local-name()='Reference'][@URI='#" + assertionId + "']" +
                            "/*[local-name()='Transforms']" +
                            "/*[local-name()='Transform']" +
                            "[@Algorithm='http://www.w3.org/2001/10/xml-exc-c14n#WithComments']")
                    .evaluate(document, XPathConstants.NODESET));
            return nodeList != null && nodeList.getLength() > 0;
        } catch (XPathExpressionException e) {
            String message = "Failed to find the canonicalization algorithm of the assertion. Defaulting to: " +
                    "http://www.w3.org/2001/10/xml-exc-c14n#";
            log.warn(message);
            return false;
        }
    }

    private Document getDocument(DocumentBuilderFactory documentBuilderFactory, String samlString)
            throws IOException, SAXException, ParserConfigurationException {

        DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(samlString.getBytes());
        return docBuilder.parse(inputStream);
    }

    private static void doBootstrap() throws ConfigurationException {
        if (!isBootStrapped) {
            bootstrap();
            isBootStrapped = true;
        }
    }

    /**
     * Generates a unique Id for Authentication Requests.
     *
     * @return Generated unique Id
     */
    private static String createID() {
        byte[] bytes = new byte[20]; // 160 bit

        random.nextBytes(bytes);

        char[] charMapping = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p'};
        char[] chars = new char[40];

        for (int i = 0; i < bytes.length; i++) {
            int left = (bytes[i] >> 4) & 0x0f;
            int right = bytes[i] & 0x0f;
            chars[i * 2] = charMapping[left];
            chars[i * 2 + 1] = charMapping[right];
        }

        return String.valueOf(chars);
    }
}
