/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.integration.test.scim2.rest.api;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.dbutils.H2DataBaseManager;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.PropertyDTO;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.user.store.config.UserStoreConfigAdminServiceClient;
import org.wso2.identity.integration.test.utils.ISTestUtils;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.GROUPS_ENDPOINT;
import static org.wso2.identity.integration.test.scim2.SCIM2BaseTestCase.PERMISSIONS_ENDPOINT;

public class SCIM2GroupTest extends SCIM2BaseTest {

    private static final Log log = LogFactory.getLog(SCIM2GroupTest.class);
    private static final String USER_STORE_DOMAIN = "WSO2TESTSTORE.COM";

    protected String endpointURL;
    protected String groupId = null;
    private static final String SCIM_CONTENT_TYPE = "application/scim+json";
    private static final String USER_MGT_PERMISSION = "permission/admin/configure/security/usermgt";
    private static final String APPLICATION_NAME = "my-sp";
    private static final String APPLICATION_ROLE_NAME = "Application/" + APPLICATION_NAME;
    private static final String USERNAME_OF_THE_NEW_USER = USER_STORE_DOMAIN
            + "/sampleSecondaryUser";

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public SCIM2GroupTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.testInit(swaggerDefinition, tenant);
        SCIMUtils.createSecondaryUserStore("org.wso2.carbon.user.core.jdbc.UniqueIDJDBCUserStoreManager",
                USER_STORE_DOMAIN, populateSecondaryUserStoreProperties(), backendURL, sessionCookie);
        Thread.sleep(5000);
    }

    @AfterClass(alwaysRun = true)
    public void testFinish() throws Exception {

        super.conclude();
        SCIMUtils.deleteSecondaryUserStore(USER_STORE_DOMAIN, backendURL, sessionCookie);
        Thread.sleep(5000);
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @Test
    public void testGETGroupDetails() {

        endpointURL = GROUPS_ENDPOINT;
        getResponseOfGet(endpointURL, SCIM_CONTENT_TYPE)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .log().ifValidationFails();
    }

    @Test(dependsOnMethods = "testGETGroupDetails")
    public void testCreateGroup() throws IOException {

        String body = readResource("scim2-add-group.json");
        Response response = getResponseOfPost(GROUPS_ENDPOINT, body, SCIM_CONTENT_TYPE);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue());
        String location = response.getHeader(HttpHeaders.LOCATION);
        this.groupId = location.split(GROUPS_ENDPOINT)[1];
        log.info("groupId :" + groupId);
        Assert.assertNotNull(groupId, "The Group is not registered.");
    }

    @Test(dependsOnMethods = "testCreateGroup")
    public void putAndGetPermissionToGroup() throws IOException {

        String body = readResource("scim2-put-group-permissions.json");
        endpointURL = GROUPS_ENDPOINT + this.groupId + PERMISSIONS_ENDPOINT;
        Response response = getResponseOfPut(endpointURL, body, SCIM_CONTENT_TYPE);
        String responseString = response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .asString();
        Assert.assertNotNull(responseString);
        // PUT operation should return the same response as the requested.
        Assert.assertEquals(responseString.trim(), body.trim(), "The response for the PUT operation is incorrect.");
    }

    @Test(dependsOnMethods = "putAndGetPermissionToGroup")
    public void getPermissionsOfGroup() {

        endpointURL = GROUPS_ENDPOINT + this.groupId + PERMISSIONS_ENDPOINT;
        getResponseOfGet(endpointURL, SCIM_CONTENT_TYPE)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test(dependsOnMethods = "getPermissionsOfGroup")
    public void patchAndGetPermissionsToGroup() throws IOException {

        endpointURL = GROUPS_ENDPOINT + this.groupId + PERMISSIONS_ENDPOINT;
        // Patch - add permissions.
        String body1 = readResource("scim2-patch-add-group-permissions.json");
        Response response1 = getResponseOfPatch(endpointURL, body1, SCIM_CONTENT_TYPE);

        String responseString1 = response1.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .asString();
        Assert.assertNotNull(responseString1);
        Assert.assertTrue(responseString1.contains(USER_MGT_PERMISSION));

        // Patch remove permissions
        String body2 = readResource("scim2-patch-remove-group-permissions.json");
        Response response2 = getResponseOfPatch(endpointURL, body2, SCIM_CONTENT_TYPE);

        String responseString2 = response2.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .asString();
        Assert.assertNotNull(responseString2);
        Assert.assertFalse(responseString2.contains(USER_MGT_PERMISSION));
    }

    @Test(dependsOnMethods = "patchAndGetPermissionsToGroup")
    public void getErrorGroupIdPermissions() throws IOException {

        String errorGroupId = "/a43fe003-d90d-43ca-ae38-d2332ecc0f36";
        endpointURL = GROUPS_ENDPOINT + errorGroupId + PERMISSIONS_ENDPOINT;
        getResponseOfGet(endpointURL, "application/scim+json")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);

        getResponseOfPut(endpointURL, readResource("scim2-put-group-permissions.json"), SCIM_CONTENT_TYPE)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);

        getResponseOfPatch(endpointURL, readResource("scim2-patch-add-group-permissions.json"), SCIM_CONTENT_TYPE)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test(dependsOnMethods = "testGETGroupDetails", description = "Test whether the assigned user list of a hybrid " +
            "role created by a Service Provider is updated properly when a secondary user store is disabled/deleted " +
            "where one of the users in the respective secondary user store was assigned to the respective hybrid role.")
    public void testGetGroupsAfterRemovingHybridRoleOfAMember() throws Exception {

        ApplicationManagementServiceClient applicationManagementServiceClient =
                new ApplicationManagementServiceClient(sessionCookie, backendURL,
                        ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null));

        ServiceProvider serviceProviderApp = new ServiceProvider();
        serviceProviderApp.setApplicationName(APPLICATION_NAME);
        serviceProviderApp.setDescription("sample-description");
        serviceProviderApp.setSaasApp(true);
        applicationManagementServiceClient.createApplication(serviceProviderApp);
        serviceProviderApp = applicationManagementServiceClient.getApplication(APPLICATION_NAME);

        Assert.assertEquals(serviceProviderApp.getApplicationName(), APPLICATION_NAME,
                "Failed to create the Service Provider: " + APPLICATION_NAME);

        UserManagementClient userMgtClient = new UserManagementClient(backendURL, getSessionCookie());
        userMgtClient.addUser(USERNAME_OF_THE_NEW_USER, "newUserPassword",
                new String[]{APPLICATION_ROLE_NAME}, null);

        endpointURL = GROUPS_ENDPOINT;

        ExtractableResponse scimResponse = getResponseOfGet(endpointURL, SCIM_CONTENT_TYPE)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .and()
                .assertThat()
                .header(HttpHeaders.CONTENT_TYPE, SCIM_CONTENT_TYPE)
                .log()
                .ifValidationFails()
                .extract();

        Assert.assertNotNull(scimResponse);

        Object resourcesAttribute = scimResponse.path("Resources");

        Assert.assertTrue(resourcesAttribute instanceof ArrayList, "'Resources' attribute is not a list of " +
                "objects");

        Optional<LinkedHashMap> targetSpApplicationRole = ((ArrayList<LinkedHashMap>) resourcesAttribute).stream()
                .filter(resource -> ((String) resource.get("displayName")).contains(APPLICATION_ROLE_NAME)).findFirst();

        Assert.assertTrue(targetSpApplicationRole.isPresent(), "Application role not found for the " +
                "Service Provider: " + APPLICATION_NAME);

        groupId = (String) targetSpApplicationRole.get().get("id");

        Optional<LinkedHashMap> targetMemberAttribute = ((ArrayList<LinkedHashMap>) targetSpApplicationRole.get()
                .get("members")).stream()
                .filter(member -> StringUtils.equals((String) member.get("display"), USERNAME_OF_THE_NEW_USER))
                .findFirst();

        Assert.assertTrue(targetMemberAttribute.isPresent(), "User: " + USERNAME_OF_THE_NEW_USER + " is not " +
                "assigned to the role: " + APPLICATION_ROLE_NAME);

        String targetUserId = (String) targetMemberAttribute.get().get("value");

        UserStoreConfigAdminServiceClient userStoreConfigAdminServiceClient = new
                UserStoreConfigAdminServiceClient(backendURL, sessionCookie);

        userStoreConfigAdminServiceClient.changeUserStoreState(USER_STORE_DOMAIN, true);
        Thread.sleep(20000);

        endpointURL += "/" + groupId;

        scimResponse = getResponseOfGet(endpointURL, SCIM_CONTENT_TYPE)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .and()
                .assertThat()
                .header(HttpHeaders.CONTENT_TYPE, SCIM_CONTENT_TYPE)
                .log()
                .ifValidationFails()
                .extract();

        Assert.assertNotNull(scimResponse);

        Object membersAttribute = scimResponse.path("members");

        Assert.assertTrue(membersAttribute instanceof ArrayList, "'members' attribute is not a list of " +
                "objects");

        targetMemberAttribute = ((ArrayList<LinkedHashMap>) membersAttribute).stream().filter(member ->
                StringUtils.equals((String) member.get("value"), targetUserId)).findAny();

        Assert.assertFalse(targetMemberAttribute.isPresent(), "User: " + USERNAME_OF_THE_NEW_USER +
                " of the disabled user store: " + USER_STORE_DOMAIN +
                " is assigned to the  application role: " + APPLICATION_ROLE_NAME);

        if (ISTestUtils.nameExists(userMgtClient.listAllUsers(USERNAME_OF_THE_NEW_USER, 10),
                USERNAME_OF_THE_NEW_USER)) {
            userMgtClient.deleteUser(USERNAME_OF_THE_NEW_USER);
        }

        userStoreConfigAdminServiceClient.changeUserStoreState(USER_STORE_DOMAIN, false);
        Thread.sleep(20000);
    }

    private PropertyDTO[] populateSecondaryUserStoreProperties() throws SQLException, ClassNotFoundException,
            IOException {

        H2DataBaseManager dataBaseManager = new H2DataBaseManager("jdbc:h2:" + ServerConfigurationManager
                .getCarbonHome() + "/repository/database/JDBC_USER_STORE_DB",
                "wso2automation", "wso2automation");
        dataBaseManager.executeUpdate(new File(ServerConfigurationManager.getCarbonHome()
                + "/dbscripts/h2.sql"));
        dataBaseManager.disconnect();

        PropertyDTO[] userStoreProperties = new PropertyDTO[11];

        IntStream.range(0, userStoreProperties.length).forEach(index -> userStoreProperties[index] = new PropertyDTO());

        userStoreProperties[0].setName("driverName");
        userStoreProperties[0].setValue("org.h2.Driver");

        userStoreProperties[1].setName("url");
        userStoreProperties[1].setValue("jdbc:h2:./repository/database/JDBC_USER_STORE_DB");

        userStoreProperties[2].setName("userName");
        userStoreProperties[2].setValue("wso2automation");

        userStoreProperties[3].setName("password");
        userStoreProperties[3].setValue("wso2automation");

        userStoreProperties[4].setName("PasswordJavaRegEx");
        userStoreProperties[4].setValue("^[\\S]{5,30}$");

        userStoreProperties[5].setName("UsernameJavaRegEx");
        userStoreProperties[5].setValue("^[\\S]{5,30}$");

        userStoreProperties[6].setName("Disabled");
        userStoreProperties[6].setValue("false");

        userStoreProperties[7].setName("PasswordDigest");
        userStoreProperties[7].setValue("SHA-256");

        userStoreProperties[8].setName("StoreSaltedPassword");
        userStoreProperties[8].setValue("true");

        userStoreProperties[9].setName("SCIMEnabled");
        userStoreProperties[9].setValue("true");

        userStoreProperties[9].setName("CountRetrieverClass");
        userStoreProperties[9].setValue("org.wso2.carbon.identity.user.store.count.jdbc.JDBCUserStoreCountRetriever");

        userStoreProperties[10].setName("UserIDEnabled");
        userStoreProperties[10].setValue("true");

        return userStoreProperties;
    }
}
