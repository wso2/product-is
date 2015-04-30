/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.user.mgt;

import java.io.File;
import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.utils.CarbonUtils;

public class ReadOnlyLdapBasedUserMgtTestCase extends UserMgtServiceAbstractTestCase {

	private static final Log log = LogFactory.getLog(ReadOnlyLdapBasedUserMgtTestCase.class);
	private ServerConfigurationManager scm;
	private File userMgtServerFile;

	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE})
	@BeforeClass(alwaysRun = true)
	public void testInit() throws Exception {
		super.testInit();

		String carbonHome = CarbonUtils.getCarbonHome();
		userMgtServerFile = new File(carbonHome + File.separator + "repository" + File.separator
				+ "conf" + File.separator + "user-mgt.xml");
		File userMgtConfigFile = new File(getISResourceLocation() + File.separator + "userMgt"
				+ File.separator + "readOnlyLdapUserMgtConfig.xml");

		scm = new ServerConfigurationManager(isServer);
		scm.applyConfigurationWithoutRestart(userMgtConfigFile, userMgtServerFile, true);
		scm.restartGracefully();
		super.testInit();

		userMgtClient.addUser("user1", "passWord1@", null, "default");
		userMgtClient.addRole("umRole1", null, new String[] { "/permission/admin/login" }, false);
	}

	@AfterClass(alwaysRun = true)
	public void atEnd() throws Exception {

		if (userNameExists(userMgtClient.listUsers("user1", 100), "user1")) {
			userMgtClient.deleteUser("user1");
		}

		if (nameExists(userMgtClient.getAllRolesNames("umRole1", 100), "umRole1")) {
			userMgtClient.deleteRole("umRole1");
		}
		if (nameExists(userMgtClient.getAllRolesNames("umRole2", 100), "umRole2")) {
			userMgtClient.deleteRole("umRole2");
		}

		// Reset the user-mgt.xml configuration.
		File userMgtDefaultFile = new File(getISResourceLocation() + File.separator + "userMgt"
				+ File.separator + "default-user-mgt.xml");
		scm.applyConfigurationWithoutRestart(userMgtDefaultFile, userMgtServerFile, true);
		scm.restartGracefully();

	}
	
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
	@Test(groups = "wso2.is", description = "Get all the role names")
    public void testGetAllRoleNames() throws Exception {	
//		TODO - Why read only admin role non-exists?
		Assert.assertTrue(nameExists(userMgtClient.getAllRolesNames("admin", 100), "admin"), "Getting all user role names has failed.");
    }
	
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.is", description = "Check role addition", dependsOnMethods="testGetAllRoleNames")
    public void testAddRole() throws Exception{
    	userMgtClient.addRole("umRole2", null, new String[]{"login"}, false);
    	Assert.assertFalse(nameExists(userMgtClient.listRoles("umRole2", 100), "umRole2"), "User should not be added when " +
				"user store is read only");
    }
    
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.is", description = "Check delete role", dependsOnMethods="testAddRole")
	public void testDeleteRole() throws Exception {
    	userMgtClient.deleteRole("admin");
//    	TODO - Assert for an existing user role when getAllRoleNames passes above.
    	Assert.assertFalse(nameExists(userMgtClient.getAllRolesNames("admin", 100), "admin"), "Deleting the added user role has failed");
	}
}
