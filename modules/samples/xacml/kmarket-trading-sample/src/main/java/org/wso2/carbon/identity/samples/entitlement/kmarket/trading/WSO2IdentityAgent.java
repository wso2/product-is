/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.samples.entitlement.kmarket.trading;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceStub;
import org.wso2.carbon.identity.entitlement.stub.EntitlementServiceStub;
import org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO;
import org.wso2.carbon.identity.samples.entitlement.kmarket.trading.stub.factory.EntitlementPolicyAdminServiceStubFactory;
import org.wso2.carbon.identity.samples.entitlement.kmarket.trading.stub.factory.EntitlementServiceStubFactory;
import org.wso2.carbon.identity.samples.entitlement.kmarket.trading.stub.factory.RemoteUserStoreManagerServiceStubFactory;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

/**
 *
 */
public class WSO2IdentityAgent {

    private static Log log = LogFactory.getLog(WSO2IdentityAgent.class);
    /**
     * Server url of the WSO2 Identity Server
     */
    private static String serverUrl = "https://localhost:9443/services/";

    /**
     * User Name to access WSO2 Identity Server
     */
    private static String serverUserName = "admin";

    /**
     * Password of the User who access the WSO2 Identity Server
     */
    private static String serverPassword = "admin";

    private ConfigurationContext configurationContext = null;

    private HttpTransportProperties.Authenticator auth = null;

    private static final String REMOTE_USER_STORE_MANAGER_SERVICE = "RemoteUserStoreManagerService";

    private static final String ENTITLEMENT_SERVICE = "EntitlementService";

    private static final String ENTITLEMENT_POLICY_ADMIN_SERVICE = "EntitlementPolicyAdminService";

    private GenericObjectPool entitlementServiceStubPool;

    private GenericObjectPool entitlementPolicyAdminServiceStubPool;

    private GenericObjectPool remoteUserStoreManagerServiceStubPool;

    public WSO2IdentityAgent(Properties properties) {

        String unProcessedServerUrl = properties.getProperty(Constants.AgentConstants.SERVER_URL);
        if(unProcessedServerUrl != null){
            if(unProcessedServerUrl.endsWith("/")){
                serverUrl = unProcessedServerUrl;
            } else {
                serverUrl = unProcessedServerUrl + "/";
            }
        }

        String password = properties.getProperty(Constants.AgentConstants.SERVER_PASSWORD);
        if(password != null && password.trim().length() > 0){
            serverPassword = password;
        }

        String userName  = properties.getProperty(Constants.AgentConstants.SERVER_USER_NAME);
        if(userName != null && userName.trim().length() > 0){
            serverUserName = userName;
        }

        String trustStore = properties.getProperty(Constants.AgentConstants.TRUST_STORE_FILE);
        if(trustStore == null || trustStore.trim().length() == 0){
            try{
                trustStore =  (new File(".")).getCanonicalPath() + File.separator +
                                                 "src" + File.separator + "main" + File.separator +
                                                 "resources" + File.separator + "wso2carbon.jks";
            } catch (IOException e) {
                e.printStackTrace(); 
            }
        }

        String trustStorePassword = properties.getProperty(Constants.AgentConstants.TRUST_STORE_PASSWORD);
        if(trustStorePassword == null || trustStorePassword.trim().length() == 0){
            trustStorePassword = "wso2carbon";
        }

        /**
         * Call to https://localhost:9443/services/   uses HTTPS protocol.
         * Therefore we to validate the server certificate or CA chain. The server certificate is looked up in the
         * trust store.
         * Following code sets what trust-store to look for and its JKs password.
         */
        System.setProperty("javax.net.ssl.trustStore",  trustStore );

        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);

        /**
         * Create a configuration context. A configuration context contains information for
         * axis2 environment. This is needed to create an axis2 service client
         */
        try {
            initConfigurationContext();
        } catch (AxisFault e) {
            log.error("Error initializing Axis2 configuration context: " + e.getMessage(), e);
        }

        /**
         * Setting basic auth headers for authentication for carbon server
         */
        auth = new HttpTransportProperties.Authenticator();
        auth.setUsername(serverUserName);
        auth.setPassword(serverPassword);
        auth.setPreemptiveAuthentication(true);

    }


    public boolean authenticate(String userName, String password){

        try {

             /**
             * Setting a authenticated cookie that is received from Carbon server.
             * If you have authenticated with Carbon server earlier, you can use that cookie, if
             * it has not been expired
             */
            RemoteUserStoreManagerServiceStub stub = getRemoteUserStoreManagerServiceStub();
            stub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, null);
            return stub.authenticate(userName, password);
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    public String authorize(String xacmlRequest){

        try {

             /**
             * Setting a authenticated cookie that is received from Carbon server.
             * If you have authenticated with Carbon server earlier, you can use that cookie, if
             * it has not been expired
             */
            EntitlementServiceStub stub = getEntitlementServiceStub();
            stub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, null);
            return stub.getDecision(xacmlRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    public String uploadPolicy(String policyId, String policy){

        try {

             /**
             * Setting a authenticated cookie that is received from Carbon server.
             * If you have authenticated with Carbon server earlier, you can use that cookie, if
             * it has not been expired
             */
            EntitlementPolicyAdminServiceStub stub = getEntitlementPolicyAdminServiceStub();
            stub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, null);

            PolicyDTO policyDTO = stub.getPolicy(policyId, false);
            if(policyDTO != null){
                System.out.println("Policy is already exist in the PDP : " + policyId);
            } else {
                policyDTO = new PolicyDTO();
                policyDTO.setPolicy(policy);
                policyDTO.setPolicyId(policyId);
                policyDTO.setPromote(true);
                stub.addPolicy(policyDTO);
                System.out.println("Policy is added in to PDP successfully : " + policyId);
            }
        } catch (Exception e) {
            System.out.println("Error while adding policies. Samples may be failed");
            e.printStackTrace();
        }

        return null;
    }

    public String setUpUserAndRoles(){

        try {

             /**
             * Setting a authenticated cookie that is received from Carbon server.
             * If you have authenticated with Carbon server earlier, you can use that cookie, if
             * it has not been expired
             */
            RemoteUserStoreManagerServiceStub stub = getRemoteUserStoreManagerServiceStub();
            stub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, null);
            String[] users = new String[] {"bob", "alice", "peter"};
            for(String user : users){
                if(stub.isExistingUser(user)){
                    System.out.println("User is already exist in the primary user store : " + user);
                } else {
                    stub.addUser(user, "wso2123@IS", null, null,null, false);
                    System.out.println("User " + user + " is added in to primary user store with password : wso2123@IS");
                }
            }

            String[] roles = new String[] {"gold", "sliver", "blue"};
            for(int i = 0; i < roles.length; i ++){
                if(stub.isExistingRole(roles[i])){
                    System.out.println("Role is already exist in the primary user store : " + roles[i]);
                } else {
                    stub.addRole(roles[i], new String[] {users[i]}, null);
                    System.out.println("Role " + roles[i] + " is added in to primary user store." +
                            " And " + users[i] + " is assigned");
                }
            }

        } catch (Exception e) {
            System.out.println("Error while adding users and roles. Samples may be failed");
            e.printStackTrace();
        }

        return null;
    }

    private EntitlementServiceStub getEntitlementServiceStub() throws Exception {

        if (entitlementServiceStubPool == null) {
            entitlementServiceStubPool = new GenericObjectPool(
                    new EntitlementServiceStubFactory(configurationContext, serverUrl + ENTITLEMENT_SERVICE, auth));
        }

        return (EntitlementServiceStub) entitlementServiceStubPool.borrowObject();

    }

    private EntitlementPolicyAdminServiceStub getEntitlementPolicyAdminServiceStub() throws Exception {

        if (entitlementPolicyAdminServiceStubPool == null) {
            entitlementPolicyAdminServiceStubPool = new GenericObjectPool(
                    new EntitlementPolicyAdminServiceStubFactory(configurationContext,
                            serverUrl + ENTITLEMENT_POLICY_ADMIN_SERVICE, auth));
        }

        return (EntitlementPolicyAdminServiceStub) entitlementPolicyAdminServiceStubPool.borrowObject();

    }

    private RemoteUserStoreManagerServiceStub getRemoteUserStoreManagerServiceStub() throws Exception {

        if (remoteUserStoreManagerServiceStubPool == null) {
            remoteUserStoreManagerServiceStubPool = new GenericObjectPool(
                    new RemoteUserStoreManagerServiceStubFactory(configurationContext,
                            serverUrl + REMOTE_USER_STORE_MANAGER_SERVICE, auth));
        }

        return (RemoteUserStoreManagerServiceStub) remoteUserStoreManagerServiceStubPool.borrowObject();

    }

    private void initConfigurationContext() throws AxisFault {
        MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager();
        HttpClient httpClient = new HttpClient(multiThreadedHttpConnectionManager);
        configurationContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        configurationContext.setProperty(HTTPConstants.CACHED_HTTP_CLIENT, httpClient);
        configurationContext.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, org.apache.axis2.Constants.VALUE_TRUE);
        HashMap<String, TransportOutDescription> transportsOut = configurationContext.getAxisConfiguration()
                .getTransportsOut();
        for (TransportOutDescription transportOutDescription : transportsOut.values()) {
            transportOutDescription.getSender().init(configurationContext, transportOutDescription);
        }
    }

}
