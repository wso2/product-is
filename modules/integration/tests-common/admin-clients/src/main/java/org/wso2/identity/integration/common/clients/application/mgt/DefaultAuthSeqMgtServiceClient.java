/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.identity.integration.common.clients.application.mgt;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.xsd.DefaultAuthenticationSequence;
import org.wso2.carbon.identity.application.mgt.defaultsequence.stub.IdentityDefaultSeqManagementServiceDefaultAuthSeqMgtException;
import org.wso2.carbon.identity.application.mgt.defaultsequence.stub.IdentityDefaultSeqManagementServiceStub;

import java.rmi.RemoteException;

/**
 * Client for DefaultAuthSeqMgtService.
 */
public class DefaultAuthSeqMgtServiceClient {

    private IdentityDefaultSeqManagementServiceStub stub;
    private static final Log log = LogFactory.getLog(DefaultAuthSeqMgtServiceClient.class);
    private static final String DEFAULT_AUTH_SEQ = "default_sequence";

    public DefaultAuthSeqMgtServiceClient(String cookie, String backendServerURL,
                                          ConfigurationContext configCtx) throws Exception {

        String serviceURL = backendServerURL + "IdentityDefaultSeqManagementService";
        stub = new IdentityDefaultSeqManagementServiceStub(configCtx, serviceURL);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        if (log.isDebugEnabled()) {
            log.debug("Invoking service " + serviceURL);
        }
    }

    /**
     * Create default authentication sequence.
     *
     * @param authenticationSequence default authentication sequence
     * @throws IdentityDefaultSeqManagementServiceDefaultAuthSeqMgtException
     */
    public void createDefaultAuthenticationSeq(DefaultAuthenticationSequence authenticationSequence)
            throws IdentityDefaultSeqManagementServiceDefaultAuthSeqMgtException {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Creating default authentication sequence.");
            }
            stub.createDefaultAuthenticationSeq(authenticationSequence);
        } catch (RemoteException e) {
            log.error("Error occurred when creating default authentication sequence.", e);
            throw new IdentityDefaultSeqManagementServiceDefaultAuthSeqMgtException("Server error occurred.");
        }
    }

    /**
     * Retrieve default authentication sequence in XML.
     *
     * @return default authentication sequence in XML
     * @throws IdentityDefaultSeqManagementServiceDefaultAuthSeqMgtException
     */
    public DefaultAuthenticationSequence getDefaultAuthenticationSeqInXML() throws
            IdentityDefaultSeqManagementServiceDefaultAuthSeqMgtException {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Retrieving default authentication sequence in XML format.");
            }
            return stub.getDefaultAuthenticationSeqInXML(DEFAULT_AUTH_SEQ);
        } catch (RemoteException e) {
            log.error("Error occurred when retrieving default authentication sequence in XML format.", e);
            throw new IdentityDefaultSeqManagementServiceDefaultAuthSeqMgtException("Server error occurred.");
        }
    }

    /**
     * Retrieve default authentication sequence.
     *
     * @return default authentication sequence
     * @throws IdentityDefaultSeqManagementServiceDefaultAuthSeqMgtException
     */
    public DefaultAuthenticationSequence getDefaultAuthenticationSeq() throws
            IdentityDefaultSeqManagementServiceDefaultAuthSeqMgtException {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Retrieving default authentication sequence.");
            }
            return stub.getDefaultAuthenticationSeq(DEFAULT_AUTH_SEQ);
        } catch (RemoteException e) {
            log.error("Error occurred when retrieving default authentication sequence.", e);
            throw new IdentityDefaultSeqManagementServiceDefaultAuthSeqMgtException("Server error occurred.");
        }
    }

    /**
     * Retrieve default authentication sequence basic info.
     *
     * @return default authentication sequence
     * @throws IdentityDefaultSeqManagementServiceDefaultAuthSeqMgtException
     */
    public DefaultAuthenticationSequence getDefaultAuthenticationSeqInfo()
            throws IdentityDefaultSeqManagementServiceDefaultAuthSeqMgtException {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Retrieving default authentication sequence info.");
            }
            return stub.getDefaultAuthenticationSeqInfo(DEFAULT_AUTH_SEQ);
        } catch (RemoteException e) {
            log.error("Error occurred when retrieving default authentication sequence info.", e);
            throw new IdentityDefaultSeqManagementServiceDefaultAuthSeqMgtException("Server error occurred.");
        }
    }

    /**
     * Check existence of default authentication sequence.
     *
     * @return true if a default authentication sequence exists for the tenant
     * @throws IdentityDefaultSeqManagementServiceDefaultAuthSeqMgtException
     */
    public boolean isExistingDefaultAuthenticationSequence()
            throws IdentityDefaultSeqManagementServiceDefaultAuthSeqMgtException {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Checking existence of default authentication sequence.");
            }
            return stub.isExistingDefaultAuthenticationSequence(DEFAULT_AUTH_SEQ);
        } catch (RemoteException e) {
            log.error("Error while checking existence of default authentication sequence.", e);
            throw new IdentityDefaultSeqManagementServiceDefaultAuthSeqMgtException("Server error occurred.");
        }
    }

    /**
     * Delete default authentication sequence.
     *
     * @throws IdentityDefaultSeqManagementServiceDefaultAuthSeqMgtException
     */
    public void deleteDefaultAuthenticationSeq() throws
            IdentityDefaultSeqManagementServiceDefaultAuthSeqMgtException {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Deleting default authentication sequence.");
            }
            stub.deleteDefaultAuthenticationSeq(DEFAULT_AUTH_SEQ);
        } catch (RemoteException e) {
            log.error("Error occurred when deleting default authentication sequence.", e);
            throw new IdentityDefaultSeqManagementServiceDefaultAuthSeqMgtException("Server error occurred.");
        }
    }

    /**
     * Update default authentication sequence.
     *
     * @throws IdentityDefaultSeqManagementServiceDefaultAuthSeqMgtException
     */
    public void updateDefaultAuthenticationSeq(DefaultAuthenticationSequence sequence) throws
            IdentityDefaultSeqManagementServiceDefaultAuthSeqMgtException {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Updating default authentication sequence.");
            }
            stub.updateDefaultAuthenticationSeq(DEFAULT_AUTH_SEQ, sequence);
        } catch (RemoteException e) {
            log.error("Error occurred when updating default authentication sequence.", e);
            throw new IdentityDefaultSeqManagementServiceDefaultAuthSeqMgtException("Server error occurred.");
        }
    }
}
