/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.scenarios.test.user.mgt.remote;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.apache.http.impl.client.HttpClients;
import org.wso2.identity.scenarios.test.common.RemoteUserStoreManagerServiceClient;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;
import java.rmi.RemoteException;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.testng.annotations.Test;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.apache.axis2.transport.http.HttpTransportProperties;

import static org.testng.Assert.assertTrue;

import org.wso2.carbon.um.ws.api.stub.PermissionDTO;
import org.wso2.carbon.CarbonConstants;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Options;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import java.util.Properties;

public class ProvisionUserSOAPServiceTestCase extends ScenarioTestBase {

    private static Log log = LogFactory.getLog(ProvisionUserSOAPServiceTestCase.class);
    private RemoteUserStoreManagerServiceClient remoteUserStoreManagerClient;
    private RemoteUserStoreManagerServiceStub stub = null;

    private String ROLE_NAME = "internal/testRole124";
    private String USER_NAME = "shavantha";
    private String USER_PASSWORD = "welcome";

    private final String SERVICE_NAME = "services/RemoteUserStoreManagerService?wsdl";
    private String userName = "admin";
    private String passWord = "admin";
    private String sessionCookie="";
    private String endPoint;
    private String[] users = {"admin"};

    private CloseableHttpClient client;

    private String claimURI = "http://wso2.org/claims/givenname";
    private String claimVal = USER_NAME;
    private String claimURL2 = "http://wso2.org/claims/lastname";
    private String claimVal2 = USER_NAME;
    private String claimURL3 = "http://wso2.org/claims/emailaddress";
    private String claimVal3 = "user@wso2.com";


    private String service="RemoteUserStoreManagerService";
    private String extension="?wsdl";

    private String trustStore;
    ConfigurationContext configContext;
    RemoteUserStoreManagerServiceStub adminService;
    boolean  authenticate;



    @BeforeClass(alwaysRun = true)

    public void testInit() throws Exception {

        String protocol=getDeploymentProperties().getProperty("PROTOCOL");
        String host=getDeploymentProperties().getProperty("HOST");
        String port=getDeploymentProperties().getProperty("PORT");

        endPoint = protocol +"://"+host+":"+port+"/services/"+service+""+extension;
        setKeyStoreProperties();
        client = HttpClients.createDefault();
        remoteUserStoreManagerClient = new RemoteUserStoreManagerServiceClient(endPoint, sessionCookie);
    }

    @AfterClass(alwaysRun = true)
    public void finishing() {
    }


    public void addUserRole()throws RemoteException,RemoteUserStoreManagerServiceUserStoreExceptionException,java.lang.NullPointerException {

        String CARBON_HOME = getDeploymentProperties().getProperty("CARBON_HOME_PATH");
        String protocol=getDeploymentProperties().getProperty("PROTOCOL");
        String host=getDeploymentProperties().getProperty("HOST");
        String port=getDeploymentProperties().getProperty("PORT");

        endPoint = protocol +"://"+host+":"+port+"/services/"+service+""+extension;

        configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem( null, null);
        trustStore = CARBON_HOME + "/repository/resources/security/wso2carbon.jks";
        System.setProperty("javax.net.ssl.trustStore",  trustStore );
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");

        adminService = new RemoteUserStoreManagerServiceStub(configContext, endPoint);
        ServiceClient client = adminService._getServiceClient();
        Options option = client.getOptions();

        option.setProperty(HTTPConstants.COOKIE_STRING, null);

        HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
        auth.setUsername(userName);
        auth.setPassword(passWord);
        auth.setPreemptiveAuthentication(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, auth);
        option.setManageSession(true);


         authenticate = false;

        try{
            authenticate = adminService.authenticate(userName, passWord);
        } catch (Exception e){
            e.printStackTrace();
        }

        if(authenticate){
           log.debug("User is authenticated successfully");
        } else {
            log.error("User is authentication failed");
        }

        String[] userList = new String[2];
        userList[0] = new String("admin");

        String[] permissions = {
                "/permission/admin/login",
                "/permission/admin/manage",
                "/permission/admin/configure/security/usermgt/profiles"};

        PermissionDTO permissionDTO;
        PermissionDTO[] permissionDTOs = new PermissionDTO[permissions.length];

        for (int i = 0; i < permissions.length; i++)
        {
            permissionDTO = new PermissionDTO();
            permissionDTO.setAction(CarbonConstants.UI_PERMISSION_ACTION);
            permissionDTO.setResourceId(permissions[i]);
            permissionDTOs[i] = permissionDTO;

        }
        adminService.addRole(ROLE_NAME,null,permissionDTOs);
    }

    @Test(description = "1.1.3.2 adding a user ", enabled = true)
    public void addUser()throws RemoteException,RemoteUserStoreManagerServiceUserStoreExceptionException,java.lang.NullPointerException {

        String CARBON_HOME = getDeploymentProperties().getProperty("CARBON_HOME_PATH");
        String protocol=getDeploymentProperties().getProperty("PROTOCOL");
        String host=getDeploymentProperties().getProperty("HOST");
        String port=getDeploymentProperties().getProperty("PORT");

        endPoint = protocol +"://"+host+":"+port+"/services/"+service+""+extension;

        configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem( null, null);
        trustStore = CARBON_HOME + "/repository/resources/security/wso2carbon.jks";
        System.setProperty("javax.net.ssl.trustStore",  trustStore );
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");

        adminService = new RemoteUserStoreManagerServiceStub(configContext, endPoint);
        ServiceClient client = adminService._getServiceClient();
        Options option = client.getOptions();

        option.setProperty(HTTPConstants.COOKIE_STRING, null);

        HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
        auth.setUsername(userName);
        auth.setPassword(passWord);
        auth.setPreemptiveAuthentication(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, auth);
        option.setManageSession(true);

        authenticate = false;

        try{
            authenticate = adminService.authenticate(userName, passWord);
        } catch (Exception e){
            e.printStackTrace();
        }

        if(authenticate){
            log.debug("User is authenticated successfully");
        } else {
            log.error("User is authentication failed");
        }

        //Add the user roles
        String[] userRoleList = new String[2];
        userRoleList[1] = new String("admin");
        userRoleList[0] = new String(ROLE_NAME);

        //Configure the claims
        ClaimValue[] claims = new ClaimValue[3];

        ClaimValue claim1 = new ClaimValue();
        claim1.setClaimURI(claimURI);
        claim1.setValue(claimVal);
        claims[0] = claim1;

        ClaimValue claim2 = new ClaimValue();
        claim2.setClaimURI(claimURL2);
        claim2.setValue(claimVal2);
        claims[1] = claim2;

        ClaimValue claim3 = new ClaimValue();
        claim3.setClaimURI(claimURL3);
        claim3.setValue(claimVal3);
        claims[2] = claim3;

        //Call the add user role becuase we are going to assign that user role to the user
        addUserRole();
        adminService.addUser(USER_NAME, USER_PASSWORD, userRoleList, claims, "default", false);
        assertTrue(adminService.isExistingUser(USER_NAME), "Adding new user successful : " + USER_NAME);

    }

}
