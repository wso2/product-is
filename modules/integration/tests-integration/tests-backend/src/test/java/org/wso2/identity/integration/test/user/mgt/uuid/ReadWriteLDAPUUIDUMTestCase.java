package org.wso2.identity.integration.test.user.mgt.uuid;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.test.integration.service.stub.AuthenticationResultDTO;
import org.wso2.carbon.identity.test.integration.service.stub.ClaimDTO;
import org.wso2.carbon.identity.test.integration.service.stub.ConditionDTO;
import org.wso2.carbon.identity.test.integration.service.stub.UserDTO;
import org.wso2.carbon.user.core.model.xsd.UniqueIDUserClaimSearchEntry;

import java.util.ArrayList;
import java.util.List;

public class ReadWriteLDAPUUIDUMTestCase extends AbstractUUIDUMTestCase {

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
    }

    @Test
    public void testAddUser() throws Exception {

        super.testAddUser();
    }

    @Test(dependsOnMethods = "testAddUser")
    public void testGetUser() throws Exception {

        super.testGetUser();
    }

    @Test(dependsOnMethods = "testAddUser")
    public void testDeleteUser() throws Exception {

        super.testDeleteUser();
    }

    @Test(dependsOnMethods = "testAddUser")
    public void testAuthenticateWithIDLoginIdentifier() throws Exception {

        super.testAuthenticateWithIDLoginIdentifier();
    }

    @Test(dependsOnMethods = "testAddUser")
    public void testAuthenticateWithIDUserId() throws Exception {

        super.testAuthenticateWithIDUserId();
    }

    @Test(dependsOnMethods = "testAddUser")
    public void testGetProfileNamesWithID() throws Exception {

        super.testGetProfileNamesWithID();
    }

    @Test(dependsOnMethods = "testAddUser")
    public void testAuthenticateWithIDUsernameClaim() throws Exception {

        super.testAuthenticateWithIDUsernameClaim();
    }

    @Test(dependsOnMethods = "testAddUser")
    public void testGetPasswordExpirationTimeWithID() throws Exception {

        super.testGetPasswordExpirationTimeWithID();
    }

    @Test(dependsOnMethods = "testAddUser")
    public void testListAllUsers() throws Exception {

        super.testListAllUsers();
    }

    @Test(dependsOnMethods = "testAddUser")
    public void testIsExistingUserWithID() throws Exception {

        super.testIsExistingUserWithID();
    }

    @Test(dependsOnMethods = "testAddUser")
    public void testGetUserClaimValueWithID() throws Exception {

        super.testGetUserClaimValueWithID();
    }

    @Test(dependsOnMethods = "testAddUser")
    public void testGetUserClaimValuesWithID() throws Exception {

        super.testGetUserClaimValuesWithID();
    }

    @Test(dependsOnMethods = "testAddUser")
    public void testGetUserListOfRoleWithID() throws Exception {

        super.testGetUserListOfRoleWithID();
    }

    @Test(dependsOnMethods = "testAddUser")
    public void testAddRoleWithId() throws Exception {

        super.testAddRoleWithId();
    }

    @Test(dependsOnMethods = "testAddRoleWithId")
    public void testIsUserInRoleWithID() throws Exception {

        super.testIsUserInRoleWithID();
    }

    @Test(dependsOnMethods = "testAddUser")
    public void testGetRoleListOfUsersWithID() throws Exception {

        super.testGetRoleListOfUsersWithID();
    }

    @Test(dependsOnMethods = "testAddUser")
    public void testListUsers() throws Exception {

        super.testListUsers();
    }

    @Test(dependsOnMethods = "testAddUser")
    public void testAddRemoveUsersOfRole() throws Exception {

        super.testAddRemoveUsersOfRole();
    }

    @Test(dependsOnMethods = "testAddUser")
    public void testAddRemoveRolesOfUser() throws Exception {

        super.testAddRemoveRolesOfUser();
    }

    @Test(dependsOnMethods = "testAddRemoveRolesOfUser")
    public void testUpdateUsersOfRole() throws Exception {

        super.testUpdateUsersOfRole();
    }

    @Test(dependsOnMethods = "testAddRemoveRolesOfUser")
    public void testGetRolesOfCurrentUser() throws Exception {

        super.testGetRolesOfCurrentUser();
    }

    @Test(dependsOnMethods = "testAddUser")
    public void testSetUserClaimValues() throws Exception {

        super.testSetUserClaimValues();
    }

    @Test(dependsOnMethods = "testAddUser")
    public void testListUserByClaim() throws Exception {

        super.testListUserByClaim();
    }

    @Test(dependsOnMethods = "testAddUser")
    public void testDeleteUserClaimValuesWithID() throws Exception {

        super.testDeleteUserClaimValuesWithID();
    }

    @Test(dependsOnMethods = "testAddUser")
    public void testGetUserListConditionWithID() throws Exception {

    }

    @Test(dependsOnMethods = "testAddUser")
    public void testGetUsersClaimValuesWithID() throws Exception {

        super.testGetUsersClaimValuesWithID();
    }

    @Test(dependsOnMethods = "testAddUser")
    public void testDeleteUserClaimValueWithID() throws Exception {

        super.testDeleteUserClaimValueWithID();
    }

    @Test(dependsOnMethods = "testAddUser")
    public void testUpdateCredentialWithID() throws Exception {

        super.testUpdateCredentialWithID();
    }

    @Test(dependsOnMethods = "testAddUser")
    public void testUpdateCredentialByAdminWithID() throws Exception {

        super.testUpdateCredentialByAdminWithID();
    }
}
