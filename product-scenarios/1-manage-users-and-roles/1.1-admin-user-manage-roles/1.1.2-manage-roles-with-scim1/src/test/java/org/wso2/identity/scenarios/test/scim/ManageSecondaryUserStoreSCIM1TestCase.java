/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.identity.scenarios.test.scim;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.dbutils.H2DataBaseManager;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.PropertyDTO;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO;
import org.wso2.identity.scenarios.commons.SCIM1CommonClient;
import org.wso2.identity.scenarios.commons.ScenarioTestBase;
import org.wso2.identity.scenarios.commons.clients.service.client.UserStoreConfigAdminServiceClient;
import org.wso2.identity.scenarios.commons.util.UserStoreConfigUtils;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.identity.scenarios.commons.util.Constants.IS_HTTPS_URL;
import static org.wso2.identity.scenarios.commons.util.IdentityScenarioUtil.getJSONFromResponse;

public class ManageSecondaryUserStoreSCIM1TestCase extends ScenarioTestBase {

    private static final String DOMAIN_ID = "USERSTOREDB";
    private static final String USER_STORE_DB_NAME = "JDBC_USER_STORE_DB";
    private static final String DB_USER_NAME = "wso2carbon";
    private static final String DB_USER_PASSWORD = "wso2carbon";
    private UserStoreConfigAdminServiceClient userStoreConfigAdminServiceClient;
    private UserStoreConfigUtils userStoreConfigUtils = new UserStoreConfigUtils();
    private CloseableHttpClient client;
    private String groupId;
    private SCIM1CommonClient scim1Group;
    private String username;
    private String password;
    private String inputFileName;
    private String tenantDomain;

    @DataProvider(name = "manageRolesConfigProvider")
    private static Object[][] manageRolesConfigProvider() {

        return new Object[][] {
                {
                        ADMIN_USERNAME, ADMIN_PASSWORD, SUPER_TENANT_DOMAIN, "scim1SecondaryUserStore.json"
                }
        };
    }

    @Factory(dataProvider = "manageRolesConfigProvider")
    public ManageSecondaryUserStoreSCIM1TestCase(String username, String password, String tenantDomain,
            String inputFile) {

        this.username = username;
        this.password = password;
        this.tenantDomain = tenantDomain;
        this.inputFileName = inputFile;
    }

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        scim1Group = new SCIM1CommonClient(getDeploymentProperty(IS_HTTPS_URL));
        loginAndObtainSessionCookie();
        userStoreConfigAdminServiceClient = new UserStoreConfigAdminServiceClient(backendServiceURL, sessionCookie);
        client = HttpClients.createDefault();
    }

    @Test
    private void addSecondaryUserStore() throws Exception {

        String jdbcClass = "org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager";
        H2DataBaseManager dbmanager = new H2DataBaseManager("jdbc:h2:./repository/database/" +
                USER_STORE_DB_NAME, DB_USER_NAME, DB_USER_PASSWORD);
        dbmanager.disconnect();

        PropertyDTO[] propertyDTOs = new PropertyDTO[10];
        for (int i = 0; i < 10; i++) {
            propertyDTOs[i] = new PropertyDTO();
        }

        propertyDTOs[0].setName("driverName");
        propertyDTOs[0].setValue("org.h2.Driver");

        propertyDTOs[1].setName("url");
        propertyDTOs[1].setValue("jdbc:h2:repository/database/" + USER_STORE_DB_NAME);

        propertyDTOs[2].setName("userName");
        propertyDTOs[2].setValue(DB_USER_NAME);

        propertyDTOs[3].setName("password");
        propertyDTOs[3].setValue(DB_USER_PASSWORD);

        propertyDTOs[4].setName("PasswordJavaRegEx");
        propertyDTOs[4].setValue("^[\\S]{5,30}$");

        propertyDTOs[5].setName("UsernameJavaRegEx");
        propertyDTOs[5].setValue("^[\\S]{5,30}$");

        propertyDTOs[6].setName("Disabled");
        propertyDTOs[6].setValue("false");

        propertyDTOs[7].setName("PasswordDigest");
        propertyDTOs[7].setValue("SHA-256");

        propertyDTOs[8].setName("StoreSaltedPassword");
        propertyDTOs[8].setValue("true");

        propertyDTOs[9].setName("SCIMEnabled");
        propertyDTOs[9].setValue("true");

        UserStoreDTO userStoreDTO = userStoreConfigAdminServiceClient
                .createUserStoreDTO(jdbcClass, DOMAIN_ID, propertyDTOs);
        userStoreConfigAdminServiceClient.addUserStore(userStoreDTO);
        Thread.sleep(5000);
        userStoreConfigUtils.waitForUserStoreDeployment(userStoreConfigAdminServiceClient, DOMAIN_ID);
    }

    @Test(dependsOnMethods = "addSecondaryUserStore")
    public void testSCIM1CreateGroup() throws Exception {

        JSONObject userJSON = scim1Group.getRoleJSON(inputFileName);
        HttpResponse response = scim1Group.provisionGroup(client, userJSON, username, password);

        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED,
                "Group has not been created successfully");

        JSONObject returnedUserJSON = getJSONFromResponse(response);
        groupId = returnedUserJSON.get(SCIM1Constants.ID_ATTRIBUTE).toString();

        assertNotNull(groupId, "SCIM1 group id not available in the response.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(dependsOnMethods = "testSCIM1CreateGroup")
    private void testSCIM1DeleteGroup() throws Exception {

        HttpResponse response = scim1Group.deleteGroup(client, groupId, username, password);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK, "Failed to delete the group");
        EntityUtils.consume(response.getEntity());
    }

    @Test(dependsOnMethods = "testSCIM1DeleteGroup")
    private void testSCIM1DeleteSecoundaryUserStore() throws Exception {

        userStoreConfigAdminServiceClient.deleteUserStore(DOMAIN_ID);
    }

}
