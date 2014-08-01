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
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceStub;
import org.wso2.carbon.identity.entitlement.stub.EntitlementServiceStub;
import org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 *
 */
public class WSO2IdentityAgent {

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

    /**
     * Keeps authenticated session cookie for sub sequences requests
     */
    private static String authCookie = null;

    /**
     *
     */
    private static EntitlementServiceStub entitlementStub = null;

    /**
     *
     */
    private static EntitlementPolicyAdminServiceStub entitlementPolicyServiceStub = null;

    /**
     *
     */
    private static RemoteUserStoreManagerServiceStub adminStub = null;

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
         * Axis2 configuration context
         */
        ConfigurationContext configContext;

        try {

            /**
             * Create a configuration context. A configuration context contains information for
             * axis2 environment. This is needed to create an axis2 service client
             */
            configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem( null, null);

            /**
             * Setting basic auth headers for authentication for carbon server
             */
            HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
            auth.setUsername(serverUserName);
            auth.setPassword(serverPassword);
            auth.setPreemptiveAuthentication(true);

            /**
             * create stub and service client for admin stub.
             */
            String serviceEndPoint = serverUrl + "RemoteUserStoreManagerService";
            adminStub = new RemoteUserStoreManagerServiceStub(configContext, serviceEndPoint);
            ServiceClient adminClient = adminStub._getServiceClient();
            Options adminClientOption = adminClient.getOptions();
            adminClientOption.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, auth);
            adminClientOption.setManageSession(true);

            /**
             * create stub and service client for entitlement stub.
             */
            serviceEndPoint = serverUrl + "EntitlementService";
            entitlementStub = new EntitlementServiceStub(configContext, serviceEndPoint);
            ServiceClient entitlementClient = entitlementStub._getServiceClient();
            Options entitlementClientOption = entitlementClient.getOptions();
            entitlementClientOption.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, auth);
            entitlementClientOption.setManageSession(true);


            /**
             * create stub and service client for entitlement stub.
             */
            serviceEndPoint = serverUrl + "EntitlementPolicyAdminService";
            entitlementPolicyServiceStub = new EntitlementPolicyAdminServiceStub(configContext, serviceEndPoint);
            ServiceClient entitlementPolicyClient = entitlementPolicyServiceStub._getServiceClient();
            Options entitlementPolicyClientOption = entitlementPolicyClient.getOptions();
            entitlementPolicyClientOption.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, auth);
            entitlementPolicyClientOption.setManageSession(true);
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }


    }


    public boolean authenticate(String userName, String password){

        try {

             /**
             * Setting a authenticated cookie that is received from Carbon server.
             * If you have authenticated with Carbon server earlier, you can use that cookie, if
             * it has not been expired
             */
            adminStub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, authCookie);
            return adminStub.authenticate(userName, password);
            
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
            entitlementStub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, authCookie);
            return entitlementStub.getDecision(xacmlRequest);

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
            entitlementPolicyServiceStub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, authCookie);

            PolicyDTO policyDTO = entitlementPolicyServiceStub.getPolicy(policyId);
            if(policyDTO != null){
                System.out.println("Policy is already exist in the PDP : " + policyId);
            } else {
                policyDTO = new PolicyDTO();
                policyDTO.setPolicy(policy);
                policyDTO.setPolicyId(policyId);
                policyDTO.setPromote(true);
                entitlementPolicyServiceStub.addPolicy(policyDTO);
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
            adminStub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, authCookie);
            String[] users = new String[] {"bob", "alice", "peter"};
            for(String user : users){
                if(adminStub.isExistingUser(user)){
                    System.out.println("User is already exist in the primary user store : " + user);
                } else {
                    adminStub.addUser(user, "wso2123@IS", null, null,null, false);
                    System.out.println("User " + user + " is added in to primary user store with password : wso2123@IS");
                }
            }

            String[] roles = new String[] {"gold", "sliver", "blue"};
            for(int i = 0; i < roles.length; i ++){
                if(adminStub.isExistingRole(roles[i])){
                    System.out.println("Role is already exist in the primary user store : " + roles[i]);
                } else {
                    adminStub.addRole(roles[i], new String[] {users[i]}, null);
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

}
