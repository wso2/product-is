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

package org.wso2.identity.integration.common.clients;

import org.apache.axiom.om.util.Base64;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.sso.saml.stub.IdentitySAMLSSOConfigServiceStub;
import org.wso2.carbon.integration.common.admin.client.utils.AuthenticateStubUtil;
import org.wso2.carbon.security.mgt.stub.keystore.AddKeyStore;
import org.wso2.carbon.security.mgt.stub.keystore.DeleteStore;
import org.wso2.carbon.security.mgt.stub.keystore.GetKeyStoresResponse;
import org.wso2.carbon.security.mgt.stub.keystore.GetKeystoreInfo;
import org.wso2.carbon.security.mgt.stub.keystore.GetKeystoreInfoResponse;
import org.wso2.carbon.security.mgt.stub.keystore.GetPaginatedKeystoreInfo;
import org.wso2.carbon.security.mgt.stub.keystore.GetPaginatedKeystoreInfoResponse;
import org.wso2.carbon.security.mgt.stub.keystore.GetStoreEntries;
import org.wso2.carbon.security.mgt.stub.keystore.GetStoreEntriesResponse;
import org.wso2.carbon.security.mgt.stub.keystore.ImportCertToStore;
import org.wso2.carbon.security.mgt.stub.keystore.KeyStoreAdminServiceStub;
import org.wso2.carbon.security.mgt.stub.keystore.RemoveCertFromStore;
import org.wso2.carbon.security.mgt.stub.keystore.xsd.KeyStoreData;
import org.wso2.carbon.security.mgt.stub.keystore.xsd.PaginatedKeyStoreData;

import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.security.KeyStore;
import java.util.Enumeration;

public class KeyStoreAdminClient {

    private static Log log = LogFactory.getLog(KeyStoreAdminClient.class);
    private KeyStoreAdminServiceStub stub = null;
    private final String serviceName = "KeyStoreAdminService";
    private String endPoint;

    public KeyStoreAdminClient(String backEndURL, String sessionCookie)
            throws Exception {
        endPoint = backEndURL + serviceName;
        stub = new KeyStoreAdminServiceStub(endPoint);
        AuthenticateStubUtil.authenticateStub(sessionCookie, stub);

    }

    public KeyStoreAdminClient(String backEndUrl, String userName, String password)
            throws RemoteException {
        this.endPoint = backEndUrl + serviceName;
        try {
            stub = new KeyStoreAdminServiceStub(endPoint);
        } catch (AxisFault axisFault) {
            log.error("Error on initializing stub : " + axisFault.getMessage());
            throw new RemoteException("Error on initializing stub : ", axisFault);
        }
        AuthenticateStub.authenticateStub(userName, password, stub);

    }

    public KeyStoreData[] getKeyStores() throws Exception {
        try {
            GetKeyStoresResponse response = stub.getKeyStores();
            return response.get_return();
        } catch (Exception e) {
            log.error("Error in getting keystore data", e);
            throw new Exception("Error in getting keystore data");
        }
    }

    public void addKeyStore(byte[] content, String filename, String password, String provider,
                            String type, String pvtkspass) throws Exception {
        try {
            String data = Base64.encode(content);
            AddKeyStore request = new AddKeyStore();
            request.setFileData(data);
            request.setFilename(filename);
            request.setPassword(password);
            request.setProvider(provider);
            request.setType(type);
            request.setPvtkeyPass(pvtkspass);
            stub.addKeyStore(request);
        } catch (Exception e) {
            log.error("Error in adding keystore", e);
            throw new Exception("Error in adding keystore");
        }
    }

    public void deleteStore(String keyStoreName) throws Exception {
        try {
            DeleteStore request = new DeleteStore();
            request.setKeyStoreName(keyStoreName);
            stub.deleteStore(request);
        } catch (Exception e) {
            log.error("Error in deleting keystore", e);
            throw new Exception("Error in deleting keystore");
        }
    }

    public void importCertToStore(String filename, byte[] content, String keyStoreName)
            throws Exception {
        try {
            String data = Base64.encode(content);
            ImportCertToStore request = new ImportCertToStore();
            request.setFileName(filename);
            request.setFileData(data);
            request.setKeyStoreName(keyStoreName);
            stub.importCertToStore(request);
        } catch (Exception e) {
            log.error("Error in importing cert to store.", e);
            throw new Exception("Error in importing cert to store.");
        }
    }

    public String[] getStoreEntries(String keyStoreName) throws Exception {
        try {
            GetStoreEntries request = new GetStoreEntries();
            request.setKeyStoreName(keyStoreName);
            GetStoreEntriesResponse response = stub.getStoreEntries(request);
            return response.get_return();
        } catch (Exception e) {
            log.error("Error in getting store entries.", e);
            throw new Exception("Error in getting store entries.");
        }
    }

    public boolean isPrivateKeyStore(byte[] content, String password, String type)
            throws Exception {
        try {
            boolean isPrivateStore = false;
            ByteArrayInputStream stream = new ByteArrayInputStream(content);
            KeyStore store = KeyStore.getInstance(type);
            store.load(stream, password.toCharArray());
            Enumeration<String> aliases = store.aliases();
            while (aliases.hasMoreElements()) {
                String value = aliases.nextElement();
                if (store.isKeyEntry(value)) {
                    isPrivateStore = true;
                    break;
                }
            }
            return isPrivateStore;
        } catch (Exception e) {
            log.error("Error in checking private key store.", e);
            throw new Exception("Error in checking private key store.");
        }
    }

    public KeyStoreData getKeystoreInfo(String keyStoreName) throws Exception {
        try {
            GetKeystoreInfo request = new GetKeystoreInfo();
            request.setKeyStoreName(keyStoreName);
            GetKeystoreInfoResponse response = stub.getKeystoreInfo(request);
            return response.get_return();
        } catch (Exception e) {
            log.error("Error in getting keystore info.", e);
            throw new Exception("Error in getting keystore info.");
        }
    }

    public void removeCertificateFromKeyStore(String keySoreName, String CertificateAlias) throws Exception {
        RemoveCertFromStore request = new RemoveCertFromStore();
        request.setKeyStoreName(keySoreName);
        request.setAlias(CertificateAlias);
        try {
            stub.removeCertFromStore(request);
        } catch (Exception e) {
            log.error("Error in removing certificate from keystore.", e);
            throw new Exception("Error in removing certificate from keystore.");
        }
    }

    public PaginatedKeyStoreData getPaginatedKeystoreInfo(String keyStoreName, int pageNumber) throws Exception {
        try {
            GetPaginatedKeystoreInfo request = new GetPaginatedKeystoreInfo();
            request.setKeyStoreName(keyStoreName);
            request.setPageNumber(pageNumber);

            GetPaginatedKeystoreInfoResponse response = stub.getPaginatedKeystoreInfo(request);
            return response.get_return();
        } catch (Exception e) {
            log.error("Error in getting paginated keystore info.", e);
            throw new Exception("Error in getting paginated keystore info.");
        }
    }

}
