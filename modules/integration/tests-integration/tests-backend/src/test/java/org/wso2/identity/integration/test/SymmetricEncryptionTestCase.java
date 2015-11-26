/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.identity.integration.test;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.util.Assert;
import org.w3c.dom.NamedNodeMap;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;


public class SymmetricEncryptionTestCase {

    private static final Log log = LogFactory
            .getLog(SymmetricEncryptionTestCase.class);

    private static SecretKey symmetricKey = null;
    private static boolean isSymmetricKeyFromFile = false;
    private static String symmetricKeyEncryptAlgoDefault = "AES";
    private static String symmetricKeySecureVaultAliasDefault = "symmetric.key.value";
    private String propertyKey = "symmetric.key";
    private String symmetricKeyEncryptEnabled;
    private String symmetricKeyEncryptAlgo;
    private String symmetricKeySecureVaultAlias;
    private static final String resourcePath = "identity/config/symmetricKey";
    private String passwordString = "admin";

    @BeforeClass(alwaysRun = true)
    public void read() throws CryptoException {
        try {
            readSymmetricKey();
        } catch (CryptoException e) {
            throw new CryptoException("Error in reading symmetric key.", e);
        }
    }

    @Test(groups = "wso2.is", description = "Check the symmetric encryption")
    public void encrypt() {
        CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
        try {
            String encryptedString = Base64.encode(cryptoUtil.encrypt(passwordString.getBytes()));
            String encryptedStringTest = Base64.encode(encryptWithSymmetricKey(cryptoUtil.encrypt(passwordString.getBytes())));
            if(!encryptedString.equals(encryptedStringTest)){
                Assert.hasText(encryptedString, "Error in encrypting with symmetric key");
            }
        } catch (CryptoException e) {
            e.printStackTrace();
        }
    }

    private void readSymmetricKey() throws CryptoException {
        FileInputStream fileInputStream = null;
        OutputStream output = null;
        KeyGenerator generator = null;
        String secretAlias;
        String encryptionAlgo;
        Properties properties;

        try {
            ServerConfiguration serverConfiguration = ServerConfiguration.getInstance();
            symmetricKeyEncryptEnabled = serverConfiguration.getFirstProperty("SymmetricEncryption.IsEnabled");
            symmetricKeyEncryptAlgo = serverConfiguration.getFirstProperty("SymmetricEncryption.Algorithm");
            symmetricKeySecureVaultAlias = serverConfiguration.getFirstProperty("SymmetricEncryption.SecureVaultAlias");

            String filePath = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "resources" +
                    File.separator + "security" + File.separator + "symmetric-key.properties";

            File file = new File(filePath);
            if (file.exists()) {
                fileInputStream = new FileInputStream(file);
                properties = new Properties();
                properties.load(fileInputStream);

                SecretResolver secretResolver = SecretResolverFactory.create(properties);
                if (symmetricKeySecureVaultAlias == null) {
                    secretAlias = symmetricKeySecureVaultAliasDefault;
                } else {
                    secretAlias = symmetricKeySecureVaultAlias;
                }

                if (symmetricKeyEncryptAlgo == null) {
                    encryptionAlgo = symmetricKeyEncryptAlgoDefault;
                } else {
                    encryptionAlgo = symmetricKeyEncryptAlgo;
                }

                if (secretResolver != null && secretResolver.isInitialized()) {
                    if (secretResolver.isTokenProtected(secretAlias)) {
                        symmetricKey = new SecretKeySpec(Base64.decode(secretResolver.resolve(secretAlias)), 0,
                                Base64.decode(secretResolver.resolve(secretAlias)).length, encryptionAlgo);
                    } else {
                        symmetricKey = new SecretKeySpec(Base64.decode((String) properties.get(secretAlias)), 0,
                                Base64.decode((String) properties.get(secretAlias)).length, encryptionAlgo);
                    }
                } else if (properties.containsKey(propertyKey)) {
                    symmetricKey = new SecretKeySpec(Base64.decode(properties.getProperty(propertyKey)), 0,
                            Base64.decode(properties.getProperty(propertyKey)).length, encryptionAlgo);
                }

                if (symmetricKey != null) {
                    isSymmetricKeyFromFile = true;
                }
            }
        } catch (Exception e) {
            throw new CryptoException("Error in generating symmetric key", e);
        }
    }

    private byte[] encryptWithSymmetricKey(byte[] plainText) throws CryptoException {
        Cipher c = null;
        byte[] encryptedData = null;
        String encryptionAlgo;
        String symmetricKeyInRegistry;
        try {
            if (symmetricKeyEncryptAlgo == null) {
                encryptionAlgo = symmetricKeyEncryptAlgoDefault;
            } else {
                encryptionAlgo = symmetricKeyEncryptAlgo;
            }
            c = Cipher.getInstance(encryptionAlgo);
            c.init(Cipher.ENCRYPT_MODE, symmetricKey);
            encryptedData = c.doFinal(plainText);
        } catch (NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException |
                NoSuchPaddingException | InvalidKeyException e) {
            throw new CryptoException("Error when encrypting data.", e);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedData;

    }
}
