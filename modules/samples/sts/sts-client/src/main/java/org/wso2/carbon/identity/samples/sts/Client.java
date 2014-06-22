/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.samples.sts;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.rahas.RahasConstants;
import org.apache.rahas.Token;
import org.apache.rahas.TokenStorage;
import org.apache.rahas.TrustException;
import org.apache.rahas.TrustUtil;
import org.apache.rahas.client.STSClient;
import org.apache.rampart.RampartMessageData;
import org.apache.rampart.policy.model.CryptoConfig;
import org.apache.rampart.policy.model.RampartConfig;
import org.apache.ws.secpolicy.SP11Constants;

/**
 * Demonstrates the functionality of a java client which invokes a service
 * secured with a security policy which requires a SAML security token with
 * bearer or holder-of-key subject confirmation.
 */
public class Client {

    private boolean enableRelyingParty;
    private boolean enableValidateBinding;
    private String tokenType;
    private String subjectConfirmationMethod;
    private String keystorePath;
    private String repoPath;
    private String stsEPR;
    private String relyingPartyEPR;
    private String stsPolicyPath;
    private String relyingPartyPolicyPath;
    private String echoRequestMsg;
    private String username;
    private String claimDialect;
    private String[] claimUris;
    private String encryptionUser;
    private String userCertAlias;
    private String pwdCallbackClass;
    private String keystorePwd;
    
    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    private void run() {
        try {
            loadConfigurations();

            // set the trust store as a system property for communication over
            // TLS.
            System.setProperty("javax.net.ssl.trustStore", keystorePath);
            System.setProperty("javax.net.ssl.trustStorePassword", keystorePwd);

            // create configuration context
            ConfigurationContext configCtx = ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem(repoPath);

            // create STS client
            STSClient stsClient = new STSClient(configCtx);
            stsClient.setRstTemplate(getRSTTemplate());

            String action = null;
            String responseTokenID = null;

            action = TrustUtil.getActionValue(RahasConstants.VERSION_05_02,
                    RahasConstants.RST_ACTION_ISSUE);
            stsClient.setAction(action);

            // request the security token from STS.
            Token responseToken;
            
            Policy stsPolicy = loadPolicy(stsPolicyPath);

            // add rampart config assertion to the ws-sec policies
            RampartConfig rampartConfig = buildRampartConfig();
            stsPolicy.addAssertion(rampartConfig);
            
            responseToken = stsClient.requestSecurityToken(null, stsEPR, stsPolicy, relyingPartyEPR);

            // store the obtained token in token store to be used in future
            // communication.
            TokenStorage store = TrustUtil.getTokenStore(configCtx);
            responseTokenID = responseToken.getId();
            store.add(responseToken);

            // print token
            System.out.println(responseToken.getToken().toString());

            //Validate the token
            if (enableValidateBinding) {
                // validating the token.
                stsClient = new STSClient(configCtx);
                action = TrustUtil.getActionValue(RahasConstants.VERSION_05_02,
                        RahasConstants.RST_ACTION_VALIDATE);
                stsClient.setAction(action);

                boolean isValid = stsClient.validateToken(responseTokenID,
                        stsEPR, stsPolicy);

                if (isValid) {
                    System.out.println("Token is valid");
                } else {
                    System.out.println("Token is invalid");
                }
            }

            //Send the token to relying party
            if (enableRelyingParty) {
                /* Invoke secured service using the obtained token */
                OMElement responseElem = null;

                // create service client
                ServiceClient serClient = new ServiceClient(configCtx, null);

                // engage modules
                serClient.engageModule("addressing");
                serClient.engageModule("rampart");

                // load policy of secured service
                Policy sec_policy = loadPolicy(relyingPartyPolicyPath);

                // add rampart config to the ws-sec policies
                sec_policy.addAssertion(rampartConfig);

                // set in/out security policies in client opts
                serClient.getOptions().setProperty(RampartMessageData.KEY_RAMPART_POLICY,
                        sec_policy);

                // Set the token id as a property in the Axis2 client scope, so that
                // this will be picked up when creating the secure message to invoke
                // the endpoint.
                serClient.getOptions().setProperty(RampartMessageData.KEY_CUSTOM_ISSUED_TOKEN,
                        responseTokenID);

                // set action of the Hello Service to be invoked.
                serClient.getOptions().setAction("urn:echoString");
                serClient.getOptions().setTo(new EndpointReference(relyingPartyEPR));

                // invoke the service
                responseElem = serClient.sendReceive(getPayload(echoRequestMsg));
                // cleanup transports
                serClient.getOptions().setCallTransportCleanup(true);

                System.out.println(responseElem.toString());
                
                System.exit(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TrustException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    private void loadConfigurations() throws IOException {
        Properties properties = new Properties();
        FileInputStream freader = new FileInputStream(ClientConstants.PROPERTIES_FILE_PATH);
        properties.load(freader);
        
        enableRelyingParty = Boolean.parseBoolean(properties
                .getProperty(ClientConstants.ENABLE_RELYING_PARTY));
        enableValidateBinding = Boolean.parseBoolean(properties
                .getProperty(ClientConstants.ENABLE_VALIDATE_BINDING));
        tokenType = properties.getProperty(ClientConstants.SAML_TOKEN_TYPE);
        subjectConfirmationMethod = properties
                .getProperty(ClientConstants.SUBJECT_CONFIRMATION_METHOD);
        keystorePath = ClientConstants.RESOURCE_PATH
                + properties.getProperty(ClientConstants.KEYSTORE_PATH);
        repoPath = ClientConstants.RESOURCE_PATH
                + properties.getProperty(ClientConstants.REPO_PATH);
        stsEPR = properties.getProperty(ClientConstants.STS_ADDRESS);
        relyingPartyEPR = properties.getProperty(ClientConstants.RELYING_PARTY_ADDRESS);
        stsPolicyPath = ClientConstants.RESOURCE_PATH
                + properties.getProperty(ClientConstants.STS_POLICY_PATH);
        relyingPartyPolicyPath = ClientConstants.RESOURCE_PATH
                + properties.getProperty(ClientConstants.RELYING_PARTY_POLICY_PATH);
        echoRequestMsg = properties.getProperty(ClientConstants.RELYING_PARTY_MESSAGE);
        username = properties.getProperty(ClientConstants.UT_USERNAME);
        claimDialect = properties.getProperty(ClientConstants.CLAIM_DIALECT);
        claimUris = properties.getProperty(ClientConstants.CLAIM_URIS).split(",");
        encryptionUser = properties.getProperty(ClientConstants.ENCRYPTION_USER);
        userCertAlias = properties.getProperty(ClientConstants.USER_CERTIFICATE_ALIAS);
        pwdCallbackClass = properties.getProperty(ClientConstants.PASSWORD_CALLBACK_CLASS);
        keystorePwd = properties.getProperty(ClientConstants.KEYSTORE_PASSWORD);
    }

    private OMElement getRSTTemplate() throws TrustException {
        OMFactory omFac = OMAbstractFactory.getOMFactory();
        OMElement element = omFac.createOMElement(SP11Constants.REQUEST_SECURITY_TOKEN_TEMPLATE);

        if (ClientConstants.SAML_TOKEN_TYPE_20.equals(tokenType)) {
            TrustUtil.createTokenTypeElement(RahasConstants.VERSION_05_02, element).setText(
                    RahasConstants.TOK_TYPE_SAML_20);
        } else if (ClientConstants.SAML_TOKEN_TYPE_11.equals(tokenType)) {
            TrustUtil.createTokenTypeElement(RahasConstants.VERSION_05_02, element).setText(
                    RahasConstants.TOK_TYPE_SAML_10);
        }

        if (ClientConstants.SUBJECT_CONFIRMATION_BEARER.equals(subjectConfirmationMethod)) {
            TrustUtil.createKeyTypeElement(RahasConstants.VERSION_05_02, element,
                    RahasConstants.KEY_TYPE_BEARER);
        } else if (ClientConstants.SUBJECT_CONFIRMATION_HOLDER_OF_KEY
                .equals(subjectConfirmationMethod)) {
            TrustUtil.createKeyTypeElement(RahasConstants.VERSION_05_02, element,
                    RahasConstants.KEY_TYPE_SYMM_KEY);
        }

        // request claims in the token.
        OMElement claimElement = TrustUtil.createClaims(RahasConstants.VERSION_05_02, element,claimDialect);
        // Populate the <Claims/> element with the <ClaimType/> elements
        addClaimType(claimElement, claimUris);

        return element;
    }

    private void addClaimType(OMElement parent, String[] claimUris) {
        OMElement element = null;
        // For each and every claim uri, create an <ClaimType/> elem
        for (String attr : claimUris) {
            element = parent.getOMFactory()
                    .createOMElement(
                            new QName("http://schemas.xmlsoap.org/ws/2005/05/identity",
                                    "ClaimType", "wsid"), parent);
            element.addAttribute(parent.getOMFactory().createOMAttribute("Uri", null, attr));
        }
    }

    private Policy loadPolicy(String policyPath) throws XMLStreamException, FileNotFoundException {
        StAXOMBuilder omBuilder = new StAXOMBuilder(policyPath);
        return PolicyEngine.getPolicy(omBuilder.getDocumentElement());
    }

    private RampartConfig buildRampartConfig() {
        RampartConfig rampartConfig = new RampartConfig();
        rampartConfig.setUser(username);
        rampartConfig.setEncryptionUser(encryptionUser);
        rampartConfig.setUserCertAlias(userCertAlias);
        rampartConfig.setPwCbClass(pwdCallbackClass);

        Properties cryptoProperties = new Properties();
        cryptoProperties.put("org.apache.ws.security.crypto.merlin.keystore.type", "JKS");
        cryptoProperties.put("org.apache.ws.security.crypto.merlin.file", keystorePath);
        cryptoProperties
                .put("org.apache.ws.security.crypto.merlin.keystore.password", keystorePwd);

        CryptoConfig cryptoConfig = new CryptoConfig();
        cryptoConfig.setProvider("org.apache.ws.security.components.crypto.Merlin");
        cryptoConfig.setProp(cryptoProperties);

        rampartConfig.setEncrCryptoConfig(cryptoConfig);
        rampartConfig.setSigCryptoConfig(cryptoConfig);

        return rampartConfig;
    }

    private OMElement getPayload(String value) {
        OMFactory factory = null;
        OMNamespace ns = null;
        OMElement elem = null;
        OMElement childElem = null;

        factory = OMAbstractFactory.getOMFactory();
        ns = factory.createOMNamespace("http://echo.services.core.carbon.wso2.org", "ns");
        elem = factory.createOMElement("echoString", ns);
        childElem = factory.createOMElement("in", null);
        childElem.setText(value);
        elem.addChild(childElem);

        return elem;
    }
}
