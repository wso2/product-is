/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.is.portal.user.client.api.unit.test;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.claim.mapping.profile.ClaimConfigEntry;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.claim.MetaClaim;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.is.portal.user.client.api.IdentityStoreClientService;
import org.wso2.is.portal.user.client.api.bean.UUFUser;
import org.wso2.is.portal.user.client.api.bean.UserListBean;
import org.wso2.is.portal.user.client.api.exception.UserPortalUIException;
import org.wso2.is.portal.user.client.api.unit.test.util.UserPortalOSGiTestUtils;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.systemProperty;

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class IdentityStoreClientServiceTest {

    private static List<UUFUser> users = new ArrayList<>();
    private static Set<String> domainNames;
    private static final String PRIMARY_DOMAIN = "PRIMARY";
    private static final String SECONDARY_DOMAIN = "SECONDARY";
    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityStoreClientServiceTest.class);

    @Inject
    private BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Inject
    private IdentityStoreClientService identityStoreClientService;


    @Configuration
    public Option[] createConfiguration() {

        List<Option> optionList = UserPortalOSGiTestUtils.getDefaultSecurityPAXOptions();

        optionList.add(systemProperty("java.security.auth.login.config")
                .value(Paths.get(UserPortalOSGiTestUtils.getCarbonHome(), "conf", "security", "carbon-jaas.config")
                        .toString()));

        return optionList.toArray(new Option[optionList.size()]);
    }

    @Test(groups = "addUsers")
    public void testAddUser() throws UserPortalUIException {
        IdentityStoreClientService identityStoreClientService =
                bundleContext.getService(bundleContext.getServiceReference(IdentityStoreClientService.class));
        Assert.assertNotNull(identityStoreClientService, "Failed to get IdentityStoreClientService instance");

        Map<String, String> userClaims = new HashMap<>();
        Map<String, String> credentials = new HashMap<>();
        userClaims.put("http://wso2.org/claims/username", "user1");
        userClaims.put("http://wso2.org/claims/givenname", "user1_firstName");
        userClaims.put("http://wso2.org/claims/lastName", "user1_lastName");
        userClaims.put("http://wso2.org/claims/email", "user1@wso2.com");

        credentials.put("password", "admin");

        UUFUser user = identityStoreClientService.addUser(userClaims, credentials);

        Assert.assertNotNull(user, "Failed to add the user.");
        Assert.assertNotNull(user.getUserId(), "Invalid user unique id.");

        users.add(user);
    }

    @Test(groups = "addUsers")
    public void testAddUserToDomain() throws UserPortalUIException {
        IdentityStoreClientService identityStoreClientService =
                bundleContext.getService(bundleContext.getServiceReference(IdentityStoreClientService.class));
        Assert.assertNotNull(identityStoreClientService, "Failed to get IdentityStoreClientService instance");

        Map<String, String> userClaims = new HashMap<>();
        Map<String, String> credentials = new HashMap<>();
        userClaims.put("http://wso2.org/claims/username", "user2");
        userClaims.put("http://wso2.org/claims/givenname", "user2_firstName");
        userClaims.put("http://wso2.org/claims/lastName", "user2_lastName");
        userClaims.put("http://wso2.org/claims/email", "user2@wso2.com");

        credentials.put("password", "admin");

        UUFUser user = identityStoreClientService.addUser(userClaims, credentials, PRIMARY_DOMAIN);

        Assert.assertNotNull(user, "Failed to add the user.");
        Assert.assertNotNull(user.getUserId(), "Invalid user unique id.");

        users.add(user);

        boolean isUserExists = identityStoreClientService.isUserExist(userClaims, PRIMARY_DOMAIN);
        Assert.assertTrue(isUserExists, "User does not exist in the given domain");
    }

    @Test(groups = "addUsers")
    public void testAddUserWithoutClaimsAndCredentials() {
        IdentityStoreClientService identityStoreClientService =
                bundleContext.getService(bundleContext.getServiceReference(IdentityStoreClientService.class));
        Assert.assertNotNull(identityStoreClientService, "Failed to get IdentityStoreClientService instance");

        UUFUser user = null;
        try {
            user = identityStoreClientService.addUser(new HashMap<>(), new HashMap<>());
        } catch (UserPortalUIException e) {
            LOGGER.info("Test passed. Add user failure without password credentials.");
            return;
        }

        Assert.assertNull(user, "Test Failed. Add user successfully without password credentials.");
    }

    @Test(groups = "addUsers")
    public void testAddUserWithoutUsernameClaim() {
        IdentityStoreClientService identityStoreClientService =
                bundleContext.getService(bundleContext.getServiceReference(IdentityStoreClientService.class));
        Assert.assertNotNull(identityStoreClientService, "Failed to get IdentityStoreClientService instance");

        Map<String, String> userClaims = new HashMap<>();
        Map<String, String> credentials = new HashMap<>();
        userClaims.put("http://wso2.org/claims/givenname", "user3_firstName");
        userClaims.put("http://wso2.org/claims/lastName", "user3_lastName");
        userClaims.put("http://wso2.org/claims/email", "user3@wso2.com");

        credentials.put("password", "admin");

        UUFUser user = null;
        try {
            user = identityStoreClientService.addUser(userClaims, credentials, PRIMARY_DOMAIN);
        } catch (UserPortalUIException e) {
            LOGGER.info("Test passed. Add user failure without a valid username claim.");
            return;
        }

        Assert.assertNull(user, "Test Failed. Add user successfully without a valid username claim.");
    }


    @Test(groups = "authentication", dependsOnGroups = {"addUsers"})
    public void testAuthenticate() throws UserPortalUIException {

        IdentityStoreClientService identityStoreClientService =
                bundleContext.getService(bundleContext.getServiceReference(IdentityStoreClientService.class));
        Assert.assertNotNull(identityStoreClientService, "Failed to get IdentityStoreClientService instance");

        UUFUser user = identityStoreClientService.authenticate("user1", "admin".toCharArray(), PRIMARY_DOMAIN);

        Assert.assertNotNull(user, "Failed to authenticate the user.");
        Assert.assertNotNull(user.getUserId(), "Invalid user unique id.");
        Assert.assertNotNull(user.getDomainName(), "Invalid domain name.");
    }

    @Test(groups = "authentication", dependsOnGroups = {"addUsers"})
    public void testAuthenticateWithInvalidCredentials() {

        IdentityStoreClientService identityStoreClientService =
                bundleContext.getService(bundleContext.getServiceReference(IdentityStoreClientService.class));
        Assert.assertNotNull(identityStoreClientService, "Failed to get IdentityStoreClientService instance");

        UUFUser user = null;
        try {
            user = identityStoreClientService.authenticate("user1", "admin2".toCharArray(), PRIMARY_DOMAIN);
        } catch (UserPortalUIException e) {
            LOGGER.info("Test passed. Authentication failure for the user with invalid credentials.");
            return;
        }

        Assert.assertNull(user, "Test Failure." +
                "Successfully authenticated the user with invalid password credential.");
    }

    @Test(groups = "authentication", dependsOnGroups = {"addUsers"})
    public void testAuthenticateWithInvalidDomain() {

        IdentityStoreClientService identityStoreClientService =
                bundleContext.getService(bundleContext.getServiceReference(IdentityStoreClientService.class));
        Assert.assertNotNull(identityStoreClientService, "Failed to get IdentityStoreClientService instance");

        UUFUser user = null;
        try {
            user = identityStoreClientService.authenticate("user1", "admin2".toCharArray(), SECONDARY_DOMAIN);
        } catch (UserPortalUIException e) {
            LOGGER.info("Test passed. Authentication failure for invalid domain.");
            return;
        }

        Assert.assertNull(user, "Test Failure." +
                "Successfully authenticated the user with invalid domain.");
    }

    @Test(groups = "update", dependsOnGroups = {"addUsers"})
    public void testUpdateUserProfile() throws UserPortalUIException {

        IdentityStoreClientService identityStoreClientService =
                bundleContext.getService(bundleContext.getServiceReference(IdentityStoreClientService.class));
        Assert.assertNotNull(identityStoreClientService, "Failed to get IdentityStoreClientService instance");

        Map<String, String> updatedClaims = new HashMap<>();
        updatedClaims.put("http://wso2.org/claims/givenname", "user1_firstNameUpdated");

        identityStoreClientService.updateUserProfile(users.get(0).getUserId(), updatedClaims);

        List<MetaClaim> metaClaims = new ArrayList<>();
        List<Claim> userClaims;
        MetaClaim metaClaim1 = new MetaClaim("http://wso2.org/claims", "http://wso2.org/claims/givenname");
        metaClaims.add(metaClaim1);

        userClaims = identityStoreClientService.getClaimsOfUser(users.get(0).getUserId(), metaClaims);
        Assert.assertNotNull(userClaims, "Failed to get the user claims.");
        Assert.assertEquals(userClaims.get(0).getValue(), "user1_firstNameUpdated", "Fail to update the user profile");
    }

    @Test(groups = "update", dependsOnGroups = {"addUsers"})
    public void testUpdateUserProfileWithInvalidUserId() throws UserPortalUIException {

        IdentityStoreClientService identityStoreClientService =
                bundleContext.getService(bundleContext.getServiceReference(IdentityStoreClientService.class));
        Assert.assertNotNull(identityStoreClientService, "Failed to get IdentityStoreClientService instance");

        Map<String, String> updatedClaims = new HashMap<>();
        updatedClaims.put("http://wso2.org/claims/givenname", "user1_firstNameUpdated2");

        try {
            identityStoreClientService.updateUserProfile(null, updatedClaims);
        } catch (UserPortalUIException e) {
            LOGGER.info("Test passed. Update profile failure for the invalid user id.");
            return;
        }
        throw new UserPortalUIException("Test Failure. Successfully updated the profile for an invalid user id.");
    }

    @Test(groups = "updatePassword", dependsOnGroups = {"addUsers", "update", "authentication"})
    public void testUpdatePassword() throws UserPortalUIException, UserNotFoundException {

        IdentityStoreClientService identityStoreClientService =
                bundleContext.getService(bundleContext.getServiceReference(IdentityStoreClientService.class));
        Assert.assertNotNull(identityStoreClientService, "Failed to get IdentityStoreClientService instance");

        identityStoreClientService.updatePassword("user1", "admin".toCharArray(), "password_updated".toCharArray(),
                PRIMARY_DOMAIN);
        UUFUser user = identityStoreClientService.authenticate("user1", "password_updated".toCharArray(),
                PRIMARY_DOMAIN);

        Assert.assertNotNull(user, "Failed to authenticate the user after updating the password.");
        Assert.assertNotNull(user.getUserId(), "Invalid user unique id.");
    }

    @Test(dependsOnGroups = {"addUsers", "update", "authentication"})
    public void testUpdatePasswordWithInvalidUsername() throws UserPortalUIException {

        IdentityStoreClientService identityStoreClientService =
                bundleContext.getService(bundleContext.getServiceReference(IdentityStoreClientService.class));
        Assert.assertNotNull(identityStoreClientService, "Failed to get IdentityStoreClientService instance");

        try {
            identityStoreClientService.updatePassword(null, "admin".toCharArray(), "password_updated".toCharArray(),
                    PRIMARY_DOMAIN);
        } catch (UserNotFoundException e) {
            LOGGER.info("Test passed. Failure for update password with invalid username.");
            return;
        } catch (UserPortalUIException e) {
            LOGGER.info("Test passed. Failure for update password with invalid username.");
            return;
        }
        throw new UserPortalUIException("Test Failure. Successfully updated the password for an invalid user name.");
    }

    @Test(dependsOnGroups = {"addUsers", "update"})
    public void testGetClaimsOfUser() throws UserPortalUIException {
        IdentityStoreClientService identityStoreClientService =
                bundleContext.getService(bundleContext.getServiceReference(IdentityStoreClientService.class));
        Assert.assertNotNull(identityStoreClientService, "Failed to get IdentityStoreClientService instance");

        List<MetaClaim> metaClaims = new ArrayList<>();
        List<Claim> userClaims;
        MetaClaim metaClaim1 = new MetaClaim("http://wso2.org/claims", "http://wso2.org/claims/givenname");
        metaClaims.add(metaClaim1);

        userClaims = identityStoreClientService.getClaimsOfUser(users.get(0).getUserId(), metaClaims);
        Assert.assertNotNull(userClaims, "Failed to get the user claims.");
        Assert.assertEquals(userClaims.get(0).getValue(), "user1_firstNameUpdated", "Fail to update the user profile");
    }

    @Test(dependsOnGroups = {"addUsers", "update"})
    public void testGetClaimsOfUserWithInvalidUserId() {
        IdentityStoreClientService identityStoreClientService =
                bundleContext.getService(bundleContext.getServiceReference(IdentityStoreClientService.class));
        Assert.assertNotNull(identityStoreClientService, "Failed to get IdentityStoreClientService instance");

        List<MetaClaim> metaClaims = new ArrayList<>();
        List<Claim> userClaims = null;
        MetaClaim metaClaim1 = new MetaClaim("http://wso2.org/claims", "http://wso2.org/claims/givenname");
        metaClaims.add(metaClaim1);

        try {
            userClaims = identityStoreClientService.getClaimsOfUser(null, metaClaims);
        } catch (UserPortalUIException e) {
            LOGGER.info("Test passed. Get claims failure for the invalid user id.");
            return;
        }
        Assert.assertNull(userClaims, "Test Failure. Get claims failure for the invalid user id.");
    }

    @Test(groups = "domainList")
    public void testGetDomainNames() throws UserPortalUIException, UserNotFoundException {
        IdentityStoreClientService identityStoreClientService =
                bundleContext.getService(bundleContext.getServiceReference(IdentityStoreClientService.class));
        Assert.assertNotNull(identityStoreClientService, "Failed to get IdentityStoreClientService instance");

        Set<String> domainNames = identityStoreClientService.getDomainNames();

        Assert.assertNotNull(domainNames, "Failed to retrieve the domain name list.");

        this.domainNames = domainNames;
    }

    //Due to issue :https://wso2.org/jira/browse/IDENTITY-5824 we have to put a depends on for updatePassword method.
    //Once that issue is solved we need to update
    // product-is/tests/osgi-tests/src/test/resources/dbscripts/identity-mgt/test-data.sql file with same change
    @Test(groups = "addUsersByAdmin", dependsOnGroups = {"updatePassword"})
    public void testAddUserToSecondaryDomain() throws UserPortalUIException {
        IdentityStoreClientService identityStoreClientService =
                bundleContext.getService(bundleContext.getServiceReference(IdentityStoreClientService.class));
        Assert.assertNotNull(identityStoreClientService, "Failed to get IdentityStoreClientService instance");

        Map<String, String> userClaims = new HashMap<>();
        Map<String, String> credentials = new HashMap<>();
        userClaims.put("http://wso2.org/claims/username", "secondaryuser1");
        userClaims.put("http://wso2.org/claims/givenname", "secondaryuser1_firstName");
        userClaims.put("http://wso2.org/claims/lastName", "secondaryuser1_lastName");
        userClaims.put("http://wso2.org/claims/email", "secondaryuser1@wso2.com");

        credentials.put("password", "secondaryuser1");
        String validSecodaryDomain = "SECONDARY_VALID";
        UUFUser user = identityStoreClientService.addUser(userClaims, credentials, validSecodaryDomain);

        Assert.assertNotNull(user, "Failed to add the user.");
        Assert.assertNotNull(user.getUserId(), "Invalid user unique id.");

        boolean isUserExists = identityStoreClientService.isUserExist(userClaims, validSecodaryDomain);
        Assert.assertTrue(isUserExists, "User does not exist in the given domain");
    }

    @Test(dependsOnGroups = {"addUsers"})
    public void testListUsersByOffsetAndLength() throws UserPortalUIException {
        IdentityStoreClientService identityStoreClientService =
                bundleContext.getService(bundleContext.getServiceReference(IdentityStoreClientService.class));
        Assert.assertNotNull(identityStoreClientService, "Failed to get IdentityStoreClientService instance");

        ClaimConfigEntry claimConfig = new ClaimConfigEntry();
        claimConfig.setClaimURI("http://wso2.org/claims/username");
        claimConfig.setDisplayName("Username");
        claimConfig.setDataType("text");

        List<ClaimConfigEntry> requestedClaims = new ArrayList<>(Arrays.asList(claimConfig));
        List<UserListBean> users = identityStoreClientService.listUsers(1, 2, PRIMARY_DOMAIN, requestedClaims);

        Assert.assertNotNull(users, "Failed to list the users.");
        Assert.assertTrue(!users.isEmpty() && users.size() == 1, "Number of users received in the response " +
                "is invalid.");

        Boolean isUsernameRetrieved = users.get(0).getClaims().containsKey("Username");

        Assert.assertTrue(isUsernameRetrieved, "Failed to retrieve requested claim");
    }

    @Test(dependsOnGroups = {"addUsers"})
    public void testFilterUserListByClaim() throws UserPortalUIException {
        IdentityStoreClientService identityStoreClientService =
                bundleContext.getService(bundleContext.getServiceReference(IdentityStoreClientService.class));
        Assert.assertNotNull(identityStoreClientService, "Failed to get IdentityStoreClientService instance");

        String claimURI = "http://wso2.org/claims/givenname";
        String claimValue = "user2*";

        ClaimConfigEntry claimConfig = new ClaimConfigEntry();
        claimConfig.setClaimURI("http://wso2.org/claims/givenname");
        claimConfig.setDisplayName("GivenName");
        claimConfig.setDataType("text");

        List<ClaimConfigEntry> requestedClaims = new ArrayList<>(Arrays.asList(claimConfig));

        List<UserListBean> users = identityStoreClientService.listUsersWithFilter(0, 3,
                claimURI, claimValue, PRIMARY_DOMAIN, requestedClaims);

        Assert.assertNotNull(users, "Failed to list the users.");

        List<String> givenNames = users.stream()
                .map(UserListBean -> new String(UserListBean.getClaims().get("GivenName")))
                .collect(Collectors.toList());

        Boolean filterApplied = true;

        for (String givenName : givenNames) {
            if (!givenName.contains("user2")) {
                filterApplied = false;
            }
        }

        Assert.assertTrue(filterApplied, "Filter is not properly applied when listing users");
    }

}
