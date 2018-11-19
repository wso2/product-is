/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.is.migration.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.core.RegistryResources.SecurityManagement;
import org.wso2.carbon.core.util.CipherHolder;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.is.migration.internal.ISMigrationServiceDataHolder;

import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import javax.crypto.Cipher;

/**
 * The utility class to encrypt/decrypt passwords to be stored in the
 * database.
 */
public class SecondaryUserstoreCryptoUtil {

    private static final String CIPHER_TRANSFORMATION_SYSTEM_PROPERTY = "org.wso2.CipherTransformation";
    private static Log log = LogFactory.getLog(SecondaryUserstoreCryptoUtil.class);
    private String primaryKeyStoreAlias;
    private String primaryKeyStoreKeyPass;
    private Gson gson = new Gson();
    private static SecondaryUserstoreCryptoUtil instance = new SecondaryUserstoreCryptoUtil();

    public static SecondaryUserstoreCryptoUtil getInstance() {

        return instance;
    }

    private SecondaryUserstoreCryptoUtil() {

        ServerConfigurationService serverConfigService = ISMigrationServiceDataHolder.getServerConfigurationService();
        this.primaryKeyStoreAlias = serverConfigService.getFirstProperty(SecurityManagement.
                SERVER_PRIMARY_KEYSTORE_KEY_ALIAS);
        this.primaryKeyStoreKeyPass = serverConfigService.getFirstProperty(SecurityManagement.
                SERVER_PRIVATE_KEY_PASSWORD);
    }

    /**
     * Encrypt a given plain text
     *
     * @param plainTextBytes                The plaintext bytes to be encrypted
     * @param cipherTransformation          The transformation that need to encrypt. If it is null, RSA is used as default
     * @param returnSelfContainedCipherText Create self-contained cipher text if true, return simple encrypted
     *                                      ciphertext otherwise.
     * @return The cipher text bytes
     * @throws CryptoException On error during encryption
     */
    public byte[] encrypt(byte[] plainTextBytes, String cipherTransformation, boolean returnSelfContainedCipherText)
            throws CryptoException {

        byte[] encryptedKey;

        try {
            Cipher keyStoreCipher;
            KeyStore keyStore;
            Certificate[] certs;
            KeyStoreManager keyMan = KeyStoreManager.getInstance(
                    MultitenantConstants.SUPER_TENANT_ID,
                    ISMigrationServiceDataHolder.getServerConfigurationService(),
                    ISMigrationServiceDataHolder.getRegistryService());
            keyStore = keyMan.getPrimaryKeyStore();
            certs = keyStore.getCertificateChain(primaryKeyStoreAlias);
            boolean isCipherTransformEnabled = false;

            if (cipherTransformation != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Cipher transformation for encryption : " + cipherTransformation);
                }
                keyStoreCipher = Cipher.getInstance(cipherTransformation, "BC");
                isCipherTransformEnabled = true;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Default Cipher transformation for encryption : RSA");
                }
                keyStoreCipher = Cipher.getInstance("RSA", "BC");
            }

            keyStoreCipher.init(Cipher.ENCRYPT_MODE, certs[0].getPublicKey());
            if (isCipherTransformEnabled && plainTextBytes.length == 0) {
                encryptedKey = "".getBytes();
                if (log.isDebugEnabled()) {
                    log.debug("Empty value for plainTextBytes null will persist to DB");
                }
            } else {
                encryptedKey = keyStoreCipher.doFinal(plainTextBytes);
            }
            if (isCipherTransformEnabled && returnSelfContainedCipherText) {
                encryptedKey = CryptoUtil.getDefaultCryptoUtil().createSelfContainedCiphertext(encryptedKey,
                        cipherTransformation, certs[0]);
            }

        } catch (Exception e) {
            throw new CryptoException("Error during encryption", e);
        }
        return encryptedKey;
    }

    /**
     * Encrypt a given plain text
     *
     * @param plainTextBytes The plaintext bytes to be encrypted
     * @return The cipher text bytes (self-contained ciphertext)
     * @throws CryptoException On error during encryption
     */
    public byte[] encrypt(byte[] plainTextBytes) throws CryptoException {
        //encrypt with transformation configured in carbon.properties as self contained ciphertext
        return encrypt(plainTextBytes, System.getProperty(CIPHER_TRANSFORMATION_SYSTEM_PROPERTY), true);
    }

    /**
     * Encrypt the given plain text and base64 encode the encrypted content.
     *
     * @param plainText The plaintext value to be encrypted and base64
     *                  encoded
     * @return The base64 encoded cipher text
     * @throws CryptoException On error during encryption
     */
    public String encryptAndBase64Encode(byte[] plainText) throws
            CryptoException {

        return Base64.encode(encrypt(plainText));
    }

    /**
     * Decrypt the given cipher text value using the WSO2 WSAS key.
     * <p>
     * IMPORTANT: Since this decrypt method is provided to force required transformation, this will not decrypt
     * self-contained ciphertexts. To decrypt self-contained ciphertext use decrypt(byte[] cipherTextBytes)
     *
     * @param cipherTextBytes      The cipher text to be decrypted
     * @param cipherTransformation The transformation that need to decrypt. If it is null, RSA is used as default.
     *                             NOTE: If symmetric encryption enabled, cipherTransformation parameter will be ignored
     * @return Decrypted bytes
     * @throws CryptoException On an error during decryption
     */
    public byte[] decrypt(byte[] cipherTextBytes, String cipherTransformation) throws CryptoException {

        byte[] decryptedValue;

        try {
            Cipher keyStoreCipher;
            KeyStore keyStore;
            PrivateKey privateKey;
            KeyStoreManager keyMan = KeyStoreManager.getInstance(
                    MultitenantConstants.SUPER_TENANT_ID,
                    ISMigrationServiceDataHolder.getServerConfigurationService(),
                    ISMigrationServiceDataHolder.getRegistryService());
            keyStore = keyMan.getPrimaryKeyStore();
            privateKey = (PrivateKey) keyStore.getKey(primaryKeyStoreAlias, primaryKeyStoreKeyPass.toCharArray());
            if (cipherTransformation != null) {
                keyStoreCipher = Cipher.getInstance(cipherTransformation, "BC");
            } else {
                keyStoreCipher = Cipher.getInstance("RSA", "BC");
            }

            keyStoreCipher.init(Cipher.DECRYPT_MODE, privateKey);

            if (cipherTextBytes.length == 0) {
                decryptedValue = "".getBytes();
                if (log.isDebugEnabled()) {
                    log.debug("Empty value for plainTextBytes null will persist to DB");
                }
            } else {
                decryptedValue = keyStoreCipher.doFinal(cipherTextBytes);
            }

        } catch (Exception e) {
            throw new CryptoException("errorDuringDecryption", e);
        }
        return decryptedValue;
    }

    /**
     * Base64 decode the given value and decrypt using the WSO2 WSAS key.
     * <p>
     * IMPORTANT: Since this decrypt method is provided to force required transformation, this will not decrypt
     * self-contained ciphertexts. To decrypt self-contained ciphertext use base64DecodeAndDecrypt(byte[] cipherTextBytes)
     *
     * @param base64CipherText Base64 encoded cipher text
     * @param transformation   The transformation used for encryption
     * @return Base64 decoded, decrypted bytes
     * @throws CryptoException On an error during decryption
     */
    public byte[] base64DecodeAndDecrypt(String base64CipherText, String transformation) throws
            CryptoException {

        return decrypt(Base64.decode(base64CipherText), transformation);
    }

    /**
     * Function to validate whether provided is self-contained ciphertext
     *
     * @param cipherBytes interested cipher text byte array
     * @return true if provided cipher is encripted using custom transformation, false if it is RSA
     */
    public boolean isSelfContainedCipherText(byte[] cipherBytes) {

        return cipherTextToCipherHolder(cipherBytes) != null;
    }

    /**
     * Function to Base64 decode the given value and validate whether provided is self-contained ciphertext
     *
     * @param base64CipherText interested cipher text byte array
     * @return true if provided cipher is self-contained cipher text
     */
    public boolean base64DecodeAndIsSelfContainedCipherText(String base64CipherText) throws CryptoException {

        return isSelfContainedCipherText(Base64.decode(base64CipherText));
    }

    /**
     * Function to convert cipher byte array to {@link CipherHolder}
     *
     * @param cipherText cipher text as a byte array
     * @return if cipher text is not a cipher with meta data
     */
    public CipherHolder cipherTextToCipherHolder(byte[] cipherText) {

        String cipherStr = new String(cipherText, Charset.defaultCharset());
        try {
            return gson.fromJson(cipherStr, CipherHolder.class);
        } catch (JsonSyntaxException e) {
            if (log.isDebugEnabled()) {
                log.debug("Deserialization failed since cipher string is not representing cipher with metadata");
            }
            return null;
        }
    }
}
