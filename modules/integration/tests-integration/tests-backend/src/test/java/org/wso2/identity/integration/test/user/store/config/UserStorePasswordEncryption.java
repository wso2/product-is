/*
 *     Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *     WSO2 Inc. licenses this file to you under the Apache License,
 *     Version 2.0 (the "License"); you may not use this file except
 *     in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing,
 *    software distributed under the License is distributed on an
 *    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *    KIND, either express or implied.  See the License for the
 *    specific language governing permissions and limitations
 *    under the License.
 */

package org.wso2.identity.integration.test.user.store.config;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.PropertyDTO;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.utils.FileUtil;
import org.wso2.identity.integration.common.clients.user.store.config.UserStoreConfigAdminServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.common.utils.UserStoreConfigUtils;
import org.wso2.identity.integration.test.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.crypto.Cipher;
import javax.xml.namespace.QName;

/**
 * Test class to test password encryption
 */
public class UserStorePasswordEncryption extends ISIntegrationTest {

    private static Log log = LogFactory.getLog(UserStorePasswordEncryption.class);
    private static final String JDBC_CLASS = "org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager";
    private static final String USER_STORE_DOMAIN_NAME = "integrationTest.com";

    private UserStoreConfigAdminServiceClient userStoreConfigurationClient;
    private ServerConfigurationManager serverConfigurationManager;
    private UserStoreConfigUtils userStoreConfigUtils = new UserStoreConfigUtils();
    private String keyStoreFilePath;
    private String userstoreDeploymentDir;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        userStoreConfigurationClient = new UserStoreConfigAdminServiceClient(backendURL, sessionCookie);
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        keyStoreFilePath =
                getTestArtifactLocation() + File.separator + "keystores" + File.separator + "products" + File.separator
                        + "wso2carbon.p12";
        userstoreDeploymentDir = Utils.getResidentCarbonHome() + File.separator + "repository" + File.separator +
                "deployment" + File.separator + "server" + File.separator + "userstores";
    }

    @Test(groups = "wso2.is",
          description = "Test encryption of password in user store configuration xml",
          priority = 1)
    public void testCustomPasswordEncryptionOfUserStoreXML() throws Exception {

        String userStoreConfigFileName = "integrationTest_com.xml";
        String userStorePassword = "testAdminPassword";

        userStoreConfigurationClient = new UserStoreConfigAdminServiceClient(backendURL, sessionCookie);

        Map<String, String> properties = new HashMap<>();
        properties.put("DomainName", USER_STORE_DOMAIN_NAME);
        properties.put("url", "jdbc:h2:./repository/database/UserStorePasswordEncryptionDB2");
        properties.put("password", userStorePassword);
        properties.put("userName", "testUserName");
        properties.put("driverName", "org.h2.Driver");

        Set<String> keys = properties.keySet();
        PropertyDTO[] propertyDTOs = new PropertyDTO[keys.toArray().length];
        int i = 0;
        for (String key : keys) {
            PropertyDTO propertyDTO = new PropertyDTO();
            propertyDTO.setName(key);
            propertyDTO.setValue(properties.get(key));
            propertyDTOs[i] = propertyDTO;
            i++;
        }

        UserStoreDTO userStoreDTO = userStoreConfigurationClient
                .createUserStoreDTO(JDBC_CLASS, USER_STORE_DOMAIN_NAME, propertyDTOs);
        //Add user store.
        userStoreConfigurationClient.addUserStore(userStoreDTO);
        Assert.assertTrue(
                userStoreConfigUtils.waitForUserStoreDeployment(userStoreConfigurationClient, USER_STORE_DOMAIN_NAME),
                "Domain addition via DTO has failed.");
        OMElement userStoreOM = AXIOMUtil.stringToOM(
                FileUtil.readFileToString(userstoreDeploymentDir + File.separator + userStoreConfigFileName));

        String password = null;
        Iterator propertyIterator = userStoreOM.getChildrenWithLocalName("Property");
        while (propertyIterator.hasNext()) {
            Object resultObject = propertyIterator.next();
            if (resultObject instanceof OMElement) {
                OMElement propertyOM = (OMElement) resultObject;
                if (propertyOM.getAttribute(new QName("name")).getAttributeValue().equals("password")) {
                    password = propertyOM.getText();
                    break;
                }
            }
        }

        Assert.assertNotNull(password, "Password property not available in the userstore configuration");
        //the cipher text is a self contained cipher.
        JSONObject jsonObject = (JSONObject) JSONValue
                .parse(new String(Base64.decode(password), Charset.defaultCharset()));
        String cipherText = jsonObject.get("c").toString();
        String decryptedPassword = new String(decrypt(Base64.decode(cipherText), "RSA/ECB/OAEPwithSHA1andMGF1Padding"),
                Charset.defaultCharset());
        log.info("decrypted Password : " + decryptedPassword);
        Assert.assertEquals(decryptedPassword, userStorePassword, "Password encryption failed when adding userstore");
    }


    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        userStoreConfigurationClient.deleteUserStore(USER_STORE_DOMAIN_NAME);
        Assert.assertTrue(
                userStoreConfigUtils.waitForUserStoreUnDeployment(userStoreConfigurationClient, USER_STORE_DOMAIN_NAME),
                "Deletion of user store has failed");
        serverConfigurationManager.restoreToLastConfiguration();
    }

    private byte[] decrypt(byte[] cipherTextBytes, String cipherTransformation) throws CryptoException {
        BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
        try {
            Security.addProvider(bouncyCastleProvider);
            Cipher keyStoreCipher;
            PrivateKey privateKey = (PrivateKey) getKeyStore(keyStoreFilePath, "wso2carbon", "wso2carbon")
                    .getKey("wso2carbon", "wso2carbon".toCharArray());
            keyStoreCipher = Cipher.getInstance(cipherTransformation, "BC");
            keyStoreCipher.init(Cipher.DECRYPT_MODE, privateKey);
            return keyStoreCipher.doFinal(cipherTextBytes);
        } catch (Exception e) {
            throw new CryptoException("Error occurred while decryption", e);
        } finally {
            Security.removeProvider(bouncyCastleProvider.getName());
        }
    }

    private KeyStore getKeyStore(String keyStoreFilePath, String password, String keyAlias)
            throws UserStoreException, KeyStoreException, CertificateException, NoSuchAlgorithmException {

        KeyStore store;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(keyStoreFilePath).getAbsolutePath());
            store = KeyStore.getInstance("PKCS12");
            store.load(inputStream, password.toCharArray());
            return store;
        } catch (FileNotFoundException e) {
            String errorMsg = "Keystore File Not Found in configured location";
            throw new UserStoreException(errorMsg, e);
        } catch (IOException e) {
            String errorMsg = "Keystore File IO operation failed";
            throw new UserStoreException(errorMsg, e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("Key store file closing failed");
                }
            }
        }
    }
}
