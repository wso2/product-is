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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;

public class ReadWriteLdapBasedUserMgtTestCase extends UserMgtServiceAbstractTestCase {

	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL})
	@BeforeClass(alwaysRun = true)
	public void testInit() throws Exception {

		super.testInit();

		userMgtClient.addUser("user1", "passWord1@", null, "default");
		userMgtClient.addUser("user2", "passWord1@", null, "default");
        userMgtClient.addUser("user3", "passWord1@", new String[]{"admin"}, "default");
		userMgtClient.addUser("user4", "passWord1@", new String[]{"admin"}, "default");
        
		userMgtClient.addRole("umRole1", null, new String[] { "/permission/admin/login" }, false);
		userMgtClient.addRole("umRole3", new String[]{"user1"}, new String[]{"login"}, false);
		
	}

	@AfterClass(alwaysRun = true)
	public void atEnd() throws Exception {

		if (nameExists(userMgtClient.listAllUsers("user1", 100), "user1")) {
			userMgtClient.deleteUser("user1");
		}
		if (nameExists(userMgtClient.listAllUsers("user2", 100), "user2")) {
			userMgtClient.deleteUser("user2");
		}
		if (nameExists(userMgtClient.listAllUsers("user3", 100), "user3")) {
			userMgtClient.deleteUser("user3");
		}
		if (nameExists(userMgtClient.listAllUsers("user4", 100), "user4")) {
			userMgtClient.deleteUser("user4");
		}
		if (nameExists(userMgtClient.listRoles("umRole1", 100), "umRole1")) {
			userMgtClient.deleteRole("umRole1");
		}
		if (nameExists(userMgtClient.listRoles("umRole2", 100), "umRole2")) {
			userMgtClient.deleteRole("umRole2");
		}		
		if (nameExists(userMgtClient.listRoles("umRole3", 100), "umRole3")) {
			userMgtClient.deleteRole("umRole3");
		}
	}
}
