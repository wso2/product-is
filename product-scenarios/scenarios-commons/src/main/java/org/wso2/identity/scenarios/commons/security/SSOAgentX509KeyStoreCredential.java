/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *
 */

package org.wso2.identity.scenarios.commons.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

/**
 * Keystore implementation of the SSO Agent.
 */
public class SSOAgentX509KeyStoreCredential implements SSOAgentX509Credential {

    private static final Log log = LogFactory.getLog(SSOAgentX509KeyStoreCredential.class);
    private PublicKey publicKey = null;
    private PrivateKey privateKey = null;
    private X509Certificate entityCertificate = null;

    public SSOAgentX509KeyStoreCredential(KeyStore keyStore, String publicCertAlias,
                                          String privateKeyAlias, char[] privateKeyPassword)
            throws Exception {

        readX509Credentials(keyStore, publicCertAlias, privateKeyAlias, privateKeyPassword);
    }

    public SSOAgentX509KeyStoreCredential(InputStream keyStoreInputStream, char[] keyStorePassword,
                                          String publicCertAlias, String privateKeyAlias,
                                          char[] privateKeyPassword)
            throws Exception {

        readX509Credentials(keyStoreInputStream, keyStorePassword, publicCertAlias,
                privateKeyAlias, privateKeyPassword);
    }

    @Override
    public PublicKey getPublicKey() {
        return publicKey;
    }

    @Override
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    @Override
    public X509Certificate getEntityCertificate()  {
        return entityCertificate;
    }

    protected void readX509Credentials(KeyStore keyStore, String publicCertAlias,
                                       String privateKeyAlias, char[] privateKeyPassword)
            throws Exception {

        try {
            entityCertificate = (X509Certificate) keyStore.getCertificate(publicCertAlias);
        } catch (KeyStoreException e) {
            throw new Exception(
                    "Error occurred while retrieving public certificate for alias " +
                            publicCertAlias, e);
        }
        publicKey = entityCertificate.getPublicKey();
        try {
            privateKey = (PrivateKey) keyStore.getKey(privateKeyAlias, privateKeyPassword);
        } catch (KeyStoreException e) {
            throw new Exception(
                    "Error occurred while retrieving private key for alias " +
                            privateKeyAlias, e);
        }
    }

    protected void readX509Credentials(InputStream keyStoreInputStream, char[] keyStorePassword,
                                       String publicCertAlias, String privateKeyAlias,
                                       char[] privateKeyPassword)
            throws Exception {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(keyStoreInputStream, keyStorePassword);
            readX509Credentials(keyStore, publicCertAlias, privateKeyAlias, privateKeyPassword);
        } catch (Exception e) {
            throw new Exception("Error while loading key store file", e);
        } finally {
            if (keyStoreInputStream != null) {
                try {
                    keyStoreInputStream.close();
                } catch (IOException ignored) {
                    if (log.isDebugEnabled()){
                        log.debug("Ignoring IO Exception : ", ignored);
                    }
                }
            }
        }
    }
}
