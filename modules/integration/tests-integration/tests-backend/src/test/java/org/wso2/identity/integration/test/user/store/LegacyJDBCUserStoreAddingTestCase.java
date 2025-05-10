/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.identity.integration.test.user.store;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.automation.test.utils.dbutils.H2DataBaseManager;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.PropertyDTO;
import org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.clients.user.store.config.UserStoreConfigAdminServiceClient;
import org.wso2.identity.integration.common.clients.user.store.count.UserStoreCountServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.common.utils.UserStoreConfigUtils;
import org.wso2.identity.integration.test.util.Utils;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;

import javax.xml.xpath.XPathExpressionException;

/**
 * This class contains test cases for adding legacy user stores with non-unique ID
 */
public class LegacyJDBCUserStoreAddingTestCase extends ISIntegrationTest {

	private static final Log LOG = LogFactory.getLog(LegacyJDBCUserStoreAddingTestCase.class);
	private static final String PERMISSION_LOGIN = "/permission/admin/login";
	private UserStoreConfigAdminServiceClient userStoreConfigAdminServiceClient;
	private UserStoreCountServiceClient userStoreCountServiceClient;
	private UserStoreConfigUtils userStoreConfigUtils = new UserStoreConfigUtils();
	private static final String LEGACY_JDBC_CLASS = "org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager";
	private static final String DOMAIN_ID = "LEGACY.WSO2TEST.COM";
	private static final String NEW_USER_NAME = DOMAIN_ID + "/legacyUserStoreUser";
	private static final String NEW_USER_ROLE = DOMAIN_ID + "/jdbcLegacyUserStoreRole";
	private static final String NEW_USER_PASSWORD = "Wso2@test";
	private static final String USER_STORE_DB_NAME = "LEGACY_JDBC_USER_STORE_ADDING_DB";
	private static final String DB_USER_NAME = "wso2automation";
	private static final String DB_USER_PASSWORD = "wso2automation";
	private UserManagementClient userMgtClient;
	private AuthenticatorClient authenticatorClient;

	@BeforeClass(alwaysRun = true)
	public void init() throws Exception {

		super.init();
		userStoreConfigAdminServiceClient = new UserStoreConfigAdminServiceClient(backendURL, sessionCookie);
		userStoreCountServiceClient = new UserStoreCountServiceClient(backendURL, sessionCookie);
	}

	@AfterClass(alwaysRun = true)
	public void atEnd() throws Exception {

		userStoreConfigAdminServiceClient.deleteUserStore(DOMAIN_ID);
		Assert.assertTrue(
				userStoreConfigUtils.waitForUserStoreUnDeployment(userStoreConfigAdminServiceClient, DOMAIN_ID),
				"Deletion of user store has failed");
	}

	@Test(groups = "wso2.is", description = "Check add user store via DTO")
	private void testAddJDBCUserStore() {

		// Create database with non-unique ID user store.
		createDatabase();

		PropertyDTO[] propertyDTOs = getJDBCUserStoreProperties();
		UserStoreDTO userStoreDTO =
				userStoreConfigAdminServiceClient.createUserStoreDTO(LEGACY_JDBC_CLASS, DOMAIN_ID, propertyDTOs);
		try {
			userStoreConfigAdminServiceClient.addUserStore(userStoreDTO);
			Assert.assertTrue(
					userStoreConfigUtils.waitForUserStoreDeployment(userStoreConfigAdminServiceClient, DOMAIN_ID),
					"Domain addition via DTO has failed.");
		} catch (Exception e) {
			Assert.fail("Error while adding user store via DTO.", e);
		}
	}

	@Test(groups = "wso2.is", dependsOnMethods = "testAddJDBCUserStore", description = "Check add user into JDBC user store")
	public void addUserIntoJDBCUserStore() throws XPathExpressionException, AxisFault {

		userMgtClient = new UserManagementClient(backendURL, getSessionCookie());
		authenticatorClient = new AuthenticatorClient(backendURL);

		try {
			userMgtClient.addRole(NEW_USER_ROLE, null, new String[]{PERMISSION_LOGIN});
			Assert.assertTrue(userMgtClient.roleNameExists(NEW_USER_ROLE), "Role name doesn't exists");

			userMgtClient.addUser(NEW_USER_NAME, NEW_USER_PASSWORD, new String[]{NEW_USER_ROLE}, null);
			Assert.assertTrue(userMgtClient.userNameExists(NEW_USER_ROLE, NEW_USER_NAME), "User name doesn't exists");

			String sessionCookie = authenticatorClient.login(NEW_USER_NAME, NEW_USER_PASSWORD,
					isServer.getInstance().getHosts().get("default"));
			Assert.assertTrue(sessionCookie.contains("JSESSIONID"), "Session Cookie not found. Login failed");
			authenticatorClient.logOut();
		} catch (UserAdminUserAdminException | RemoteException e) {
			Assert.fail("Error while adding user/role into JDBC user store.", e);
		} catch (LoginAuthenticationExceptionException e) {
			Assert.fail("Error while login to the server.", e);
		} catch (LogoutAuthenticationExceptionException e) {
			Assert.fail("Error while logout from the server.", e);
		}
	}

	@Test(groups = "wso2.is", dependsOnMethods = "testAddJDBCUserStore", description = "Check user role cache with secondary user store")
	public void testUserRoleCacheWithSecondary() throws AxisFault, XPathExpressionException {

		String loginRole = "login";
		String secondaryUsername = DOMAIN_ID + "/user4219";
		String secondaryUserPassword = "Password@2";
		String primaryUsername = "user4219";
		String primaryUserPassword = "Password@1";

		userMgtClient = new UserManagementClient(backendURL, getSessionCookie());
		authenticatorClient = new AuthenticatorClient(backendURL);
		try {
			userMgtClient.addRole(loginRole, null, new String[]{PERMISSION_LOGIN});
			userMgtClient.addUser(secondaryUsername, secondaryUserPassword, new String[]{}, null);
			userMgtClient.addUser(primaryUsername, primaryUserPassword, new String[]{loginRole}, null);
			authenticatorClient.unsuccessfulLogin(primaryUsername, primaryUserPassword,
					isServer.getInstance().getHosts().get("default"));
			Assert.assertFalse(authenticatorClient.unsuccessfulLogin(secondaryUsername, secondaryUserPassword,
							isServer.getInstance().getHosts().get("default")),
					"User from secondary user store logged in without login permissions.");
			userMgtClient.deleteUser(primaryUsername);
			userMgtClient.deleteUser(secondaryUsername);
			userMgtClient.deleteRole(loginRole);
			authenticatorClient.logOut();
		} catch (UserAdminUserAdminException | RemoteException e) {
			Assert.fail("Error while adding user/role into JDBC user store.", e);
		} catch (LoginAuthenticationExceptionException e) {
			Assert.fail("Error while login to the server.", e);
		} catch (LogoutAuthenticationExceptionException e) {
			Assert.fail("Error while logout from the server.", e);
		}
	}

	@Test(groups = "wso2.is", dependsOnMethods = "addUserIntoJDBCUserStore", description = "Check change password by user")
	public void changePassWordByUserTest() {

		try {
			userMgtClient.changePasswordByUser(NEW_USER_NAME, NEW_USER_PASSWORD, "Wso2@test2");
		} catch (UserAdminUserAdminException | RemoteException e) {
			Assert.fail("password change by user for secondary User Store failed");
		}

	}

	@Test(groups = "wso2.is", dependsOnMethods = "addUserIntoJDBCUserStore", description = "Check count enabled by user stores")
	public void getCountEnabledUserStores() throws Exception {

		Assert.assertTrue(userStoreCountServiceClient.getCountableUserStores().contains(DOMAIN_ID),
				"no count enabled user stores");
	}

	@Test(groups = "wso2.is", dependsOnMethods = "getCountEnabledUserStores", description = "Check delete user store via DTO")
	public void deleteUserFromJDBCUserStore() {

		try {
			userMgtClient.deleteUser(NEW_USER_NAME);
			Assert.assertFalse(Utils.nameExists(userMgtClient.listAllUsers(NEW_USER_NAME, 10), NEW_USER_NAME),
					"User Deletion failed");

			userMgtClient.deleteRole(NEW_USER_ROLE);
			Assert.assertFalse(Utils.nameExists(userMgtClient.getAllRolesNames(NEW_USER_ROLE, 100), NEW_USER_ROLE),
					"User Role still exist");
		} catch (RemoteException | UserAdminUserAdminException e) {
			Assert.fail("Error while deleting user/role from JDBC user store.", e);
		}
	}

	private void createDatabase() {

		try {
			//creating database
			H2DataBaseManager dbmanager = new H2DataBaseManager(
					"jdbc:h2:" + ServerConfigurationManager.getCarbonHome() + "/repository/database/" +
							USER_STORE_DB_NAME, DB_USER_NAME, DB_USER_PASSWORD);
			dbmanager.executeUpdate(new File(getISResourceLocation() + "/dbscripts/h2.sql"));
			dbmanager.disconnect();
			LOG.info("H2 database created successfully.");
		} catch (IOException e) {
			Assert.fail("Error while setting JDBC userstore properties", e);
		} catch (SQLException e) {
			Assert.fail("Error while when performing a db action", e);
		} catch (ClassNotFoundException e) {
			Assert.fail("Error trying to find the relevant Java class", e);
		}
	}

	/**
	 * Creates an H2 database and gets its properties.
	 *
	 * @return PropertyDTO[] of H2 database properties.
	 */
	private PropertyDTO[] getJDBCUserStoreProperties() {

		PropertyDTO[] propertyDTOs = new PropertyDTO[10];
		for (int i = 0; i < 10; i++) {
			propertyDTOs[i] = new PropertyDTO();
		}

		propertyDTOs[0].setName("driverName");
		propertyDTOs[0].setValue("org.h2.Driver");

		propertyDTOs[1].setName("url");
		propertyDTOs[1].setValue("jdbc:h2:./repository/database/" + USER_STORE_DB_NAME);

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

		propertyDTOs[9].setName("CountRetrieverClass");
		propertyDTOs[9].setValue("org.wso2.carbon.identity.user.store.count.jdbc.JDBCUserStoreCountRetriever");

		return propertyDTOs;
	}
}
