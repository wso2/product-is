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
import org.apache.http.util.EntityUtils;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.signature.XMLSignature;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
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

import static org.opensaml.DefaultBootstrap.bootstrap;
import static org.wso2.identity.scenarios.commons.util.Constants.ASSERTION_TAG_NAME;
import static org.wso2.identity.scenarios.commons.util.Constants.AUTHN_CONTEXT_CLASS_REF;
import static org.wso2.identity.scenarios.commons.util.Constants.AUTHN_REQUEST;
import static org.wso2.identity.scenarios.commons.util.Constants.HEADER_USER_AGENT;
import static org.wso2.identity.scenarios.commons.util.Constants.INBOUND_AUTH_TYPE_SAML;
import static org.wso2.identity.scenarios.commons.util.Constants.ISSUER;
import static org.wso2.identity.scenarios.commons.util.Constants.NAMESPACE_PREFIX;
import static org.wso2.identity.scenarios.commons.util.Constants.PASSWORD_PROTECTED_TRANSPORT_CLASS;
import static org.wso2.identity.scenarios.commons.util.Constants.RESPONSE_TAG_NAME;
import static org.wso2.identity.scenarios.commons.util.Constants.SAML_ASSERTION_URN;
import static org.wso2.identity.scenarios.commons.util.Constants.SAML_PROTOCOL_URN;
import static org.wso2.identity.scenarios.commons.util.Constants.SAML_REQUEST_PARAM;
import static org.wso2.identity.scenarios.commons.util.Constants.SAML_RESPONSE_PARAM;
import static org.wso2.identity.scenarios.commons.util.Constants.TOCOMMONAUTH;
import static org.wso2.identity.scenarios.commons.util.Constants.XML_DOCUMENT_BUILDER_FACTORY;
import static org.wso2.identity.scenarios.commons.util.Constants.XML_DOCUMENT_BUILDER_FACTORY_IMPL;
import static org.wso2.identity.scenarios.commons.util.DataExtractUtil.extractValueFromResponse;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.sendGetRequest;
import static org.wso2.identity.scenarios.commons.util.SSOUtil.sendLoginPostWithParamsAndHeaders;
import static org.wso2.identity.scenarios.commons.util.SSOUtil.sendRedirectRequest;

/**
 * Base test class for SAML SSO tests.
 */
public class SAML2SSOTestBase extends SSOTestBase {

    private static final Log log = LogFactory.getLog(SAML2SSOTestBase.class);

    protected static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    private static final String SIGNATURE_ALGORITHM_SHA1_RSA = "SHA1withRSA";
    private static final String XML_DIGEST_ALGORITHM_SHA1 = "http://www.w3.org/2000/09/xmldsig#sha1";
    private static final String TENANT_DOMAIN_PARAM = "tenantDomain";
    private static final String TRUSTSTORE_LOCATION = "javax.net.ssl.trustStore";
    private static final String TRUSTSTORE_PASSWORD = "javax.net.ssl.trustStorePassword";
    private static final String SAML_SSO_URL = "%s/samlsso";
    private static final String SAML_IDP_SLO_URL = SAML_SSO_URL + "?slo=true";
    private String defaultPrivatekeyPassword = "wso2carbon";
    private String defaultPrivatekeyAlias = "wso2carbon";
    private String defaultPublicCertAlias = "wso2carbon";
    private String defaultProfileName = "default";

    private static final int ENTITY_EXPANSION_LIMIT = 0;
    public static String DEFAULT_MULTI_ATTRIBUTE_SEPARATOR = ",";

    protected String samlSSOIDPUrl;
    protected String samlIdpSloUrl;
    protected SAMLSSOConfigServiceClient ssoConfigServiceClient;
    protected RemoteUserStoreManagerServiceClient remoteUSMServiceClient;
    private X509Credential defaultX509Cred;

    private static boolean isBootStrapped = false;
    private static Random random = new Random();

    public void init() throws Exception {

        super.init();
        samlSSOIDPUrl = String.format(SAML_SSO_URL, backendURL);
        samlIdpSloUrl = String.format(SAML_IDP_SLO_URL, backendURL);
        ssoConfigServiceClient = new SAMLSSOConfigServiceClient(backendServiceURL, sessionCookie);
        remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient(backendServiceURL, sessionCookie);
        defaultX509Cred = new X509CredentialImpl(new SSOAgentX509KeyStoreCredential(new FileInputStream(
                System.getProperty(TRUSTSTORE_LOCATION)), System.getProperty(TRUSTSTORE_PASSWORD).toCharArray(),
                defaultPublicCertAlias, defaultPrivatekeyAlias, defaultPrivatekeyPassword.toCharArray()));
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
     * @param serviceProvider Service Provider instance.
     * @return SAML SSO service provider DTO.
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
     * @param issuerName Issuer name of the SAML request.
     * @return SAML SSO service provider DTO.
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
     * Build the SAML2 POST Binding request.
     *
     * @param samlAuthRequest SAML authentication request.
     * @param samlConfig SAML configuration.
     * @param x509Credential x509Credential instance.
     * @return Base64 encoded SAML2 request.
     * @throws Exception
     */
    protected String buildSAMLPOSTRequest(AuthnRequest samlAuthRequest, SAMLConfig samlConfig,
                                          X509Credential x509Credential) throws Exception {

        if (samlConfig.isSigningEnabled()) {
            setSignature(samlAuthRequest, samlConfig.getXmlSignatureAlgorithm(), samlConfig.getXmlDigestAlgorithm(),
                    true, x509Credential);
        }
        return encodeRequestMessage(samlAuthRequest, SAMLConstants.SAML2_POST_BINDING_URI);
    }

    /**
     * Send SAML2 Authentication request and get the SessionDataKey from IDP.
     *
     * @param client                    Closable HTTP Client.
     * @param saml2AuthRequest          SAML2 authentication request.
     * @param samlConfig                SAML configuration.
     * @param idpURL                    IDP URL.
     * @param samlssoServiceProviderDTO SAMLSSO Service Provider DTO.
     * @param x509Credential            x509Credential implementation.
     * @return HTTP Response with SessionDataKey from the IDP.
     * @throws Exception
     */
    protected HttpResponse sendSAMLAuthenticationRequest(CloseableHttpClient client, AuthnRequest saml2AuthRequest,
                                                         SAMLConfig samlConfig, String idpURL,
                                                         SAMLSSOServiceProviderDTO samlssoServiceProviderDTO,
                                                         X509Credential x509Credential) throws Exception {

        HttpResponse response;
        if (SAMLConstants.SAML2_POST_BINDING_URI.equals(samlConfig.getHttpBinding())) {
            String samlPostRequest = buildSAMLPOSTRequest(saml2AuthRequest, samlConfig, x509Credential);
            response = sendSAMLPostMessage(client, idpURL, SAML_REQUEST_PARAM,
                    samlPostRequest, samlConfig);
            EntityUtils.consume(response.getEntity());

            response = sendRedirectRequest(response, USER_AGENT, samlssoServiceProviderDTO
                    .getDefaultAssertionConsumerUrl(), samlConfig.getArtifact(), client);
        } else if (SAMLConstants.SAML2_REDIRECT_BINDING_URI.equals(samlConfig.getHttpBinding())) {
            String redirectRequest = buildRedirectRequest(saml2AuthRequest, samlConfig, idpURL, x509Credential);
            response = sendGetRequest(client, redirectRequest, null, new Header[]{new BasicHeader(HttpHeaders
                    .USER_AGENT, USER_AGENT)});
        } else {
            throw new Exception("Unsupported HTTP binding format " + samlConfig.getHttpBinding());
        }
        return response;
    }

    /**
     * Send SAML POST message.
     *
     * @param idpURL IDP URL.
     * @param samlMsgKey SAML request param name.
     * @param samlMsgValue Encoded SAML request message.
     * @return Redirection response to authentication endpoint.
     * @throws IOException
     */
    protected HttpResponse sendSAMLPostMessage(CloseableHttpClient client, String idpURL, String samlMsgKey,
                                               String samlMsgValue, SAMLConfig samlConfig) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        HttpPost post = new HttpPost(idpURL);
        post.setHeader(HEADER_USER_AGENT, USER_AGENT);
        urlParameters.add(new BasicNameValuePair(samlMsgKey, samlMsgValue));
        if (samlConfig.getUserMode() == TestUserMode.TENANT_ADMIN ||
                samlConfig.getUserMode() == TestUserMode.TENANT_USER) {
            urlParameters.add(new BasicNameValuePair(TENANT_DOMAIN_PARAM, samlConfig.getUser().getTenantDomain()));
        }

        if (samlConfig.getParams() != null && !samlConfig.getParams().isEmpty()) {
            for (Map.Entry<String, String[]> entry : samlConfig.getParams().entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null && entry.getValue().length > 0) {
                    for (String param : entry.getValue()) {
                        urlParameters.add(new BasicNameValuePair(entry.getKey(), param));
                    }
                }
            }
        }
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return client.execute(post);
    }

    /**
     * Build SAML2 authentication request from SAMLSSO service provider DTO.
     *
     * @param samlssoSPDTO SAMLSSO service provider DTO.
     * @param isPassiveAuthn isPassiveAuthn
     * @param isForceAuthn isForceAuthn
     * @param samlConfig SAML configuration.
     * @param destinationUrl Destination URL.
     * @return
     */
    protected AuthnRequest buildAuthnRequest(SAMLSSOServiceProviderDTO samlssoSPDTO, boolean isPassiveAuthn,
                                             boolean isForceAuthn, SAMLConfig samlConfig,
                                             String destinationUrl) {

        return buildAuthnRequest(samlssoSPDTO.getIssuer(), samlssoSPDTO.getNameIDFormat(), isForceAuthn,
                isPassiveAuthn, samlConfig.getHttpBinding(), samlssoSPDTO.getDefaultAssertionConsumerUrl(),
                destinationUrl);
    }

    /**
     * Build SAML2 authentication request including extensions.
     *
     * @param samlssoSPDTO SAMLSSO service provider DTO.
     * @param isPassiveAuthn isPassiveAuthn
     * @param isForceAuthn isForceAuthn
     * @param samlConfig SAML configuration.
     * @param destinationUrl Destination URL.
     * @param extensions SAML extensions.
     * @return
     */
    protected AuthnRequest buildAuthnRequest(SAMLSSOServiceProviderDTO samlssoSPDTO, boolean isPassiveAuthn,
                                             boolean isForceAuthn, SAMLConfig samlConfig,
                                             String destinationUrl, Extensions extensions) {

        AuthnRequest authRequest = buildAuthnRequest(samlssoSPDTO, isPassiveAuthn, isForceAuthn, samlConfig,
                destinationUrl);
        authRequest.setExtensions(extensions);
        return authRequest;
    }

    /**
     * Build SAML2 authentication request including assertion consumer service index.
     *
     * @param samlssoSPDTO SAMLSSO service provider DTO.
     * @param isPassiveAuthn isPassiveAuthn
     * @param isForceAuthn isForceAuthn
     * @param samlConfig SAML configuration.
     * @param destinationUrl Destination URL.
     * @param consumerServiceIndex Assertion consumer service index.
     * @return
     */
    protected AuthnRequest buildAuthnRequest(SAMLSSOServiceProviderDTO samlssoSPDTO, boolean isPassiveAuthn,
                                             boolean isForceAuthn, SAMLConfig samlConfig,
                                             String destinationUrl, Integer consumerServiceIndex) {

        AuthnRequest authRequest = buildAuthnRequest(samlssoSPDTO, isPassiveAuthn, isForceAuthn,
                samlConfig, destinationUrl);
        // Requesting Attributes. This Index value is registered in the IDP.
        authRequest.setAssertionConsumerServiceIndex(consumerServiceIndex);
        return authRequest;
    }

    /**
     * Build SAML2 authentication request including extensions and assertion consumer service index.
     *
     * @param samlssoSPDTO SAMLSSO service provider DTO.
     * @param isPassiveAuthn isPassiveAuthn
     * @param isForceAuthn isForceAuthn
     * @param samlConfig SAML configuration.
     * @param destinationUrl Destination URL.
     * @param extensions SAML extensions.
     * @param consumerServiceIndex Assertion consumer service index.
     * @return
     */
    protected AuthnRequest buildAuthnRequest(SAMLSSOServiceProviderDTO samlssoSPDTO, boolean isPassiveAuthn,
                                             boolean isForceAuthn, SAMLConfig samlConfig,
                                             String destinationUrl, Extensions extensions,
                                             Integer consumerServiceIndex) {

        AuthnRequest authRequest = buildAuthnRequest(samlssoSPDTO, isPassiveAuthn,
                isForceAuthn, samlConfig, destinationUrl);
        authRequest.setExtensions(extensions);
        // Requesting Attributes. This Index value is registered in the IDP.
        authRequest.setAssertionConsumerServiceIndex(consumerServiceIndex);
        return authRequest;
    }

    /**
     * Build SAML2 authentication request.
     *
     * @param spEntityID Service Provider entity ID.
     * @param nameIDFormat Name ID format.
     * @param isPassiveAuthn isPassiveAuth
     * @param isForceAuthn isForceAuth
     * @param httpBinding HTTP Binding for SAML request.
     * @param acsUrl Assertion consumer URL.
     * @param destinationUrl Destination URL.
     * @return SAML2 Request instance.
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

    /**
     * Build the SAML2 Redirect binding request.
     *
     * @param samlAuthRequest SAML authentication request.
     * @param samlConfig SAML configuration.
     * @param idpURL IDP URL.
     * @param x509Credential x509Credential instance.
     * @return the redirection URL with the appended SAML Request message.
     * @throws Exception
     */
    protected String buildRedirectRequest(AuthnRequest samlAuthRequest, SAMLConfig samlConfig, String idpURL,
                                          X509Credential x509Credential) throws Exception {

        String redirectRequestURL;
        String encodedRequestMessage = encodeRequestMessage(samlAuthRequest,
                SAMLConstants.SAML2_REDIRECT_BINDING_URI);
        StringBuilder httpQueryString = new StringBuilder(SAML_REQUEST_PARAM + "=" + encodedRequestMessage);
        if (samlConfig.getParams() != null && !samlConfig.getParams().isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, String[]> entry : samlConfig.getParams().entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null && entry.getValue().length > 0) {
                    for (String param : entry.getValue()) {
                        try {
                            builder.append("&").append(entry.getKey()).append("=").append(
                                    URLEncoder.encode(param, StandardCharsets.UTF_8.name()));
                        } catch (UnsupportedEncodingException e) {
                            throw new Exception("Error occurred while URLEncoding " + entry.getKey(), e);
                        }
                    }
                }
            }
            httpQueryString.append(builder);
        }
        if (samlConfig.isSigningEnabled()) {
            addDeflateSignatureToHTTPQueryString(httpQueryString, samlConfig.getSignatureAlgorithm(),
                    samlConfig.getXmlSignatureAlgorithm(), x509Credential);
        }
        if (idpURL.contains("?")) {
            redirectRequestURL = idpURL.concat("&").concat(httpQueryString.toString());
        } else {
            redirectRequestURL = idpURL.concat("?").concat(httpQueryString.toString());
        }
        return redirectRequestURL;
    }

    private void addDeflateSignatureToHTTPQueryString(StringBuilder httpQueryString, String signatureAlg,
                                                      String xmlSignatureAlg, X509Credential cred)
            throws Exception {

        doBootstrap();
        if (StringUtils.isEmpty(xmlSignatureAlg)) {
            xmlSignatureAlg = XMLSignature.ALGO_ID_SIGNATURE_RSA;
        }
        if (StringUtils.isEmpty(signatureAlg)) {
            signatureAlg = SIGNATURE_ALGORITHM_SHA1_RSA;
        }
        try {
            httpQueryString.append("&SigAlg="
                    + URLEncoder.encode(xmlSignatureAlg, StandardCharsets.UTF_8.name()).trim());
            java.security.Signature signature = java.security.Signature.getInstance(signatureAlg);
            signature.initSign(cred.getPrivateKey());
            signature.update(httpQueryString.toString().getBytes(Charset.forName(StandardCharsets.UTF_8.name())));
            byte[] signatureByteArray = signature.sign();

            String signatureBase64encodedString = Base64.encodeBytes(signatureByteArray,
                    Base64.DONT_BREAK_LINES);
            httpQueryString.append("&Signature="
                    + URLEncoder.encode(signatureBase64encodedString, StandardCharsets.UTF_8.name()).trim());
        } catch (Exception e) {
            throw new Exception("Error applying SAML2 Redirect Binding signature", e);
        }
    }

    /**
     * Base64 encoding of the SAML request.
     *
     * @param requestMessage SAML authentication request.
     * @param binding SAML binding type.
     * @return Base64 encoded SAML request.
     * @throws Exception
     */
    protected String encodeRequestMessage(SignableSAMLObject requestMessage, String binding) throws Exception {

        doBootstrap();
        System.setProperty(XML_DOCUMENT_BUILDER_FACTORY, XML_DOCUMENT_BUILDER_FACTORY_IMPL);
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
            deflaterOutputStream.write(rspWrt.toString().getBytes(Charset.forName(StandardCharsets.UTF_8.name())));
            deflaterOutputStream.close();
            String encodedRequestMessage = Base64.encodeBytes(byteArrayOutputStream
                    .toByteArray(), Base64.DONT_BREAK_LINES);
            return URLEncoder.encode(encodedRequestMessage, StandardCharsets.UTF_8.name()).trim();
        } else if (SAMLConstants.SAML2_POST_BINDING_URI.equals(binding)) {
            return Base64.encodeBytes(rspWrt.toString().getBytes(),
                    Base64.DONT_BREAK_LINES);
        } else {
            log.warn("Unsupported SAML2 HTTP Binding. Defaulting to " + SAMLConstants.SAML2_POST_BINDING_URI);
            return Base64.encodeBytes(rspWrt.toString().getBytes(), Base64.DONT_BREAK_LINES);
        }
    }

    /**
     * Add Signature to SAML POST request
     *
     * @param request SAML authentication request.
     * @param signatureAlgorithm Signature Algorithm.
     * @param digestAlgorithm Digest algorithm to be used while digesting message.
     * @param includeCert Whether to include certificate in request or not.
     * @throws Exception
     */
    protected void setSignature(RequestAbstractType request, String signatureAlgorithm, String digestAlgorithm,
                                boolean includeCert, X509Credential x509Credential) throws Exception {

        doBootstrap();
        if (StringUtils.isEmpty(signatureAlgorithm)) {
            signatureAlgorithm = XMLSignature.ALGO_ID_SIGNATURE_RSA;
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
     * Send basic authentication credentials to commonauth endpoint.
     *
     * @param response Response from the authentication endpoint with sessionDataKey.
     * @param url CommmonAuth endpoint URL.
     * @param userAgent User agent header value.
     * @param acsUrl Assertion consumer URL.
     * @param artifact Artifact name.
     * @param userName Username
     * @param password Password
     * @param httpClient Closable HTTP client.
     * @return Authentication response from the commonauth endpoint.
     * @throws Exception
     */
    protected HttpResponse sendLoginPostMessage(HttpResponse response, String url, String userAgent, String acsUrl,
                                                String artifact, String userName, String password, HttpClient
                                                        httpClient) throws Exception {

        Header[] headers = new Header[2];
        headers[0] = new BasicHeader(HttpHeaders.USER_AGENT, userAgent);
        headers[1] = new BasicHeader(HttpHeaders.REFERER, String.format(acsUrl, artifact));
        Map<String, String> urlParameters = new HashMap<>();
        urlParameters.put(TOCOMMONAUTH, "true");
        return sendLoginPostWithParamsAndHeaders(httpClient, getSessionDataKey(response), url, userName, password,
                urlParameters, headers);
    }

    /**
     * Get SAML response object from the HTTP response.
     *
     * @param response HTTP response
     * @return SAML response instance.
     * @throws Exception
     */
    protected Response extractAndProcessSAMLResponse(HttpResponse response) throws Exception {

        String encodedSAML2ResponseString = extractSAMLResponse(response);
        EntityUtils.consume(response.getEntity());
        String saml2ResponseString = new String(Base64.decode(encodedSAML2ResponseString), Charset.forName
                (StandardCharsets.UTF_8.name()));
        XMLObject samlResponse = unmarshall(saml2ResponseString);

        // Check for duplicate samlp:Response
        NodeList list = samlResponse.getDOM().getElementsByTagNameNS(SAMLConstants.SAML20P_NS, RESPONSE_TAG_NAME);
        if (list.getLength() > 0) {
            log.error("Invalid schema for the SAML2 response. Multiple Response elements found.");
            throw new Exception("Error occurred while processing SAML2 response.");
        }

        // Checking for multiple Assertions
        NodeList assertionList = samlResponse.getDOM().getElementsByTagNameNS(SAMLConstants.SAML20_NS,
                ASSERTION_TAG_NAME);
        if (assertionList.getLength() > 1) {
            log.error("Invalid schema for the SAML2 response. Multiple Assertion elements found.");
            throw new Exception("Error occurred while processing SAML2 response.");
        }

        return (Response) samlResponse;
    }

    /**
     * Extract SAML Assertion from the SAML Response.
     *
     * @param samlResponse SAML Response.
     * @param samlssoSPDTO SAMLSSO service Provider DTO.
     * @param x509Credential x509Credential instance.
     * @return SAML Response instance.
     * @throws Exception
     */
    protected Assertion getAssertionFromSAMLResponse(Response samlResponse, SAMLSSOServiceProviderDTO samlssoSPDTO,
                                                     X509Credential x509Credential) throws Exception {

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
     * Retrieve the attribute map from the SAML Assertion.
     *
     * @param assertion SAML Assertion
     * @param multiAttributeSeparator Multi attribute separator.
     * @return Attrbute map.
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
     * Validate whether the audience restrictions in SAMLSSO SP configuration are present under the audience
     * restriction in SAML2 Response.
     *
     * @param samlResponse SAML2 Response instance.
     * @param samlssoSPDTO SAMLSSO service provider DTO.
     * @param x509Credential x509Credential instance.
     * @return returns true if the audiences in the SAMLSSO SP config are found.
     * @throws Exception
     */
    protected boolean validateAudienceRestrictionBySAMLSSOSPConfig(Response samlResponse,
                                                                   SAMLSSOServiceProviderDTO samlssoSPDTO,
                                                                   X509Credential x509Credential) throws Exception {

        boolean audienceFound = false;
        for (String refAudience : samlssoSPDTO.getRequestedAudiences()) {
            if (!validateAudienceRestrictionByRefAudienceValue(samlResponse, samlssoSPDTO, refAudience,
                    x509Credential)) {
                audienceFound = false;
                break;
            } else {
                audienceFound = true;
            }
        }
        return audienceFound;
    }

    /**
     * Validate whether a given audience value is present under the audience restriction in SAML2 Response.
     *
     * @param samlResponse SAML2 Response instance.
     * @param samlssoSPDTO SAMLSSO service provider DTO.
     * @param refAudience Audience value to be tested.
     * @param x509Credential x509Credential instance.
     * @return returns true if given audience is found.
     * @throws Exception
     */
    protected boolean validateAudienceRestrictionByRefAudienceValue(Response samlResponse,
                                                                    SAMLSSOServiceProviderDTO samlssoSPDTO,
                                                                    String refAudience, X509Credential x509Credential)
            throws Exception {

        Assertion assertion = getAssertionFromSAMLResponse(samlResponse, samlssoSPDTO, x509Credential);
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
                                if (audience.getAudienceURI().equals(refAudience)) {
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
     * @param response SAML2 Response.
     * @param samlssoServiceProviderDTO SAMLSSO service provider DTO.
     * @param x509Credential x509Credential instance.
     * @return returns true if SAML2 response signature validation is successful.
     * @throws Exception
     */
    protected boolean validateSAMLResponseSignature(Response response,
                                                    SAMLSSOServiceProviderDTO samlssoServiceProviderDTO,
                                                    X509Credential x509Credential) throws Exception {

        boolean responseSignatureValid = false;
        if (samlssoServiceProviderDTO.isDoSignResponseSpecified()) {
            if (response.getSignature() == null) {
                throw new Exception("SAML2 Response signing is enabled, but signature element not " +
                        "found in SAML2 Response element");
            } else {
                validateSignature(response.getSignature(), x509Credential);
                responseSignatureValid = true;
            }
        } else {
            throw new Exception("SAML2 Response signing is disabled");
        }
        return responseSignatureValid;
    }

    /**
     * Validate the signature of a SAML2 Assertion.
     *
     * @param samlResponse SAML2 Response.
     * @param samlssoSPDTO SAMLSSO service provider DTO.
     * @param x509Credential x509Credential instance.
     * @return returns true if SAML2 assertion signature validation is successful.
     * @throws Exception
     */
    protected boolean validateSAMLAssertionSignature(Response samlResponse,
                                                     SAMLSSOServiceProviderDTO samlssoSPDTO,
                                                     X509Credential x509Credential) throws Exception {

        boolean assertionSignatureValid = false;
        Assertion assertion = getAssertionFromSAMLResponse(samlResponse, samlssoSPDTO, x509Credential);
        if (samlssoSPDTO.isDoSignAssertionsSpecified()) {
            if (assertion.getSignature() == null) {
                throw new Exception("SAML2 Assertion signing is enabled, but signature element not " +
                        "found in SAML2 Assertion element");
            } else {
                validateSignature(assertion.getSignature(), x509Credential);
                assertionSignatureValid = true;
            }
        } else {
            throw new Exception("SAML2 Assertion signing is disabled");
        }
        return assertionSignatureValid;
    }

    private void validateSignature(XMLObject signature, X509Credential x509Credential) throws Exception {

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

    /**
     * Extract SAML response param value from the HTTP response.
     * @param response HTTP Response.
     * @return SAML Response string
     * @throws IOException
     */
    protected static String extractSAMLResponse(HttpResponse response) throws IOException {

        return extractValueFromResponse(response, "name='" + SAML_RESPONSE_PARAM + "'", 5);
    }

    /**
     * Create user.
     *
     * @param samlConfig SAML configuration.
     */
    protected void createUserFromTestConfig(SAMLConfig samlConfig) {

        super.createUser(samlConfig, remoteUSMServiceClient, defaultProfileName);
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

        char[] charMapping = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p'};
        char[] chars = new char[40];
        byte[] bytes = new byte[20]; // 160 bit

        random.nextBytes(bytes);
        for (int i = 0; i < bytes.length; i++) {
            int left = (bytes[i] >> 4) & 0x0f;
            int right = bytes[i] & 0x0f;
            chars[i * 2] = charMapping[left];
            chars[i * 2 + 1] = charMapping[right];
        }

        return String.valueOf(chars);
    }
}
