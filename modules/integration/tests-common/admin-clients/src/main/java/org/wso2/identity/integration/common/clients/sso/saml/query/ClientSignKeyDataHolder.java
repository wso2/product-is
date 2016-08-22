/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.identity.integration.common.clients.sso.saml.query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.signature.XMLSignature;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.CredentialContextSet;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.x509.X509Credential;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Collection;
import javax.crypto.SecretKey;

/**
 * This class is used as Sign key data holder for signature process
 */
public class ClientSignKeyDataHolder implements X509Credential {

    private static final Log log = LogFactory.getLog(ClientSignKeyDataHolder.class);

    private static final String DSA_ENCRYPTION_ALGORITHM = "DSA";

    private String signatureAlgorithm = null;

    private X509Certificate[] issuerCerts = null;

    private PrivateKey privateKey = null;

    private PublicKey publicKey = null;

    /**
     * Constructor method
     * @param keyStorePath path to the key store
     * @param password password of keystore
     * @param keyAlias key alias of keystore
     * @throws Exception if, Algorithm fails, input stream fails
     */
    public ClientSignKeyDataHolder(String keyStorePath,  String password, String keyAlias) throws Exception {

        Certificate[] certificates;
        InputStream is = null;

        try {
            File file = new File(keyStorePath);
            is = new FileInputStream(file);
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(is, password.toCharArray());

            privateKey = (PrivateKey) keystore.getKey(keyAlias, password.toCharArray());

            certificates = keystore.getCertificateChain(keyAlias);

            issuerCerts = new X509Certificate[certificates.length];

            int i = 0;
            for (Certificate certificate : certificates) {
                issuerCerts[i++] = (X509Certificate) certificate;
            }

            signatureAlgorithm = XMLSignature.ALGO_ID_SIGNATURE_RSA;

            publicKey = issuerCerts[0].getPublicKey();
            String pubKeyAlgo = publicKey.getAlgorithm();
            if (DSA_ENCRYPTION_ALGORITHM.equalsIgnoreCase(pubKeyAlgo)) {
                signatureAlgorithm = XMLSignature.ALGO_ID_SIGNATURE_DSA;
            }
        } catch (CertificateException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
            String mgs = "Error while initializing credentials";
            log.error(mgs, e);
            throw  new Exception(mgs);
        } finally {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    log.error("Unable to close input stream",e);
                }
            }
        }
    }

    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }


    public String getEntityId() {
        return null;
    }

    public UsageType getUsageType() {
        return null;
    }


    public Collection<String> getKeyNames() {
        return null;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public SecretKey getSecretKey() {
        return null;
    }

    public CredentialContextSet getCredentialContextSet() {
        return null;
    }


    public Class<? extends Credential> getCredentialType() {
        return null;
    }


    public X509Certificate getEntityCertificate() {
        return issuerCerts[0];
    }


    public Collection<X509Certificate> getEntityCertificateChain() {
        return null;
    }

    public Collection<X509CRL> getCRLs() {
        return null;
    }
}
