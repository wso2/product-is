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
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.is.portal.user.client.api.IdentityStoreClientService;
import org.wso2.is.portal.user.client.api.bean.UUFUser;
import org.wso2.is.portal.user.client.api.exception.UserPortalUIException;
import org.wso2.is.portal.user.client.api.unit.test.util.UserPortalOSGiTestUtils;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.systemProperty;

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class AccountLockHandlerTest {

    private static List<UUFUser> users = new ArrayList<>();

    private static final String PRIMARY_DOMAIN = "PRIMARY";
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountLockHandlerTest.class);

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

    @Test(groups = "accountLockWithInvalidCredential")
    public void testLockUserInvalidCredential() throws UserPortalUIException {
        UUFUser user = addUser("user1234");
        Assert.assertNotNull(user, "Failed to add the user.");
        Assert.assertNotNull(user.getUserId(), "Invalid user unique id.");

        //Success attempt
        UUFUser authenticatedUser = identityStoreClientService.authenticate("user1234", "admin".toCharArray(),
                PRIMARY_DOMAIN);

        Assert.assertNotNull(authenticatedUser, "Failed to authenticate the user.");
        Assert.assertNotNull(authenticatedUser.getUserId(), "Invalid user unique id.");

        //Failed attempt 1
        UUFUser failedUser = null;
        try {
            failedUser = identityStoreClientService.authenticate("user1234", "admin2".toCharArray(), PRIMARY_DOMAIN);
        } catch (UserPortalUIException e) {
            LOGGER.info("Test passed. Authentication failure for the user with invalid credentials.");
        }
        Assert.assertNull(failedUser, "Test Failure." +
                "Successfully authenticated the user with invalid password credential.");

        //Failed attempt 2

        try {
            failedUser = identityStoreClientService.authenticate("user1234", "admin2".toCharArray(), PRIMARY_DOMAIN);
        } catch (UserPortalUIException e) {
            LOGGER.info("Test passed. Authentication failure for the user with invalid credentials.");
        }
        Assert.assertNull(failedUser, "Test Failure." +
                "Successfully authenticated the user with invalid password credential.");

        //Failed attempt 3, Now the account should be locked
        try {
            failedUser = identityStoreClientService.authenticate("user1234", "admin2".toCharArray(), PRIMARY_DOMAIN);
        } catch (UserPortalUIException e) {
            LOGGER.info("Test passed. Authentication failure for the user with invalid credentials.");
        }

        Assert.assertNull(failedUser, "Test Failure." +
                "Successfully authenticated the user with invalid password credential.");


        //Success attempt, but the account is locked, So, user should not be able to authenticate
        try {
            failedUser = identityStoreClientService.authenticate("user1234", "admin".toCharArray(), PRIMARY_DOMAIN);
        } catch (UserPortalUIException e) {
            LOGGER.info("Test passed. Authentication failure for the user with invalid credentials.");
        }

        Assert.assertNull(failedUser, "Test Failure. User account is not locked.");
    }


    @Test(groups = "adminLockUserAccount")
    public void testLockUserByAdmin() throws UserPortalUIException {

        UUFUser user = addUser("user12345");
        users.add(user);
        UUFUser failedUser = null;

        Map<String, String> userClaims = new HashMap<>();
        userClaims.put("http://wso2.org/claims/username", "user12345");
        userClaims.put("http://wso2.org/claims/givenname", "user1_firstName");
        userClaims.put("http://wso2.org/claims/lastName", "user1_lastName");
        userClaims.put("http://wso2.org/claims/email", "user1@wso2.com");
        userClaims.put("http://wso2.org/claims/accountLocked", "true");

        //Success attempt
        UUFUser authenticatedUser = identityStoreClientService.authenticate("user12345", "admin".toCharArray(),
                PRIMARY_DOMAIN);

        Assert.assertNotNull(authenticatedUser, "Failed to authenticate the user.");
        Assert.assertNotNull(authenticatedUser.getUserId(), "Invalid user unique id.");

        identityStoreClientService.updateUserProfile(user.getUserId(), userClaims);

        //Success attempt, but the account is locked, So, user should not be able to authenticate
        try {
            failedUser = identityStoreClientService.authenticate("user12345", "admin".toCharArray(), PRIMARY_DOMAIN);
        } catch (UserPortalUIException e) {
            LOGGER.info("Test passed. Authentication failure for the user with invalid credentials.");
        }

        Assert.assertNull(failedUser, "Test Failure. User account is not locked.");
    }


    @Test(groups = "adminUnlockUserAccount", dependsOnGroups = {"adminLockUserAccount"})
    public void testUnlockUserByAdmin() throws UserPortalUIException {

        UUFUser failedUser = null;
        //Success attempt, but the account is locked, So, user should not be able to authenticate
        try {
            failedUser = identityStoreClientService.authenticate("user12345", "admin".toCharArray(), PRIMARY_DOMAIN);
        } catch (UserPortalUIException e) {
            LOGGER.info("Test passed. Authentication failure for the user with invalid credentials.");
        }

        Assert.assertNull(failedUser, "Test Failure. User account is not locked.");

        Map<String, String> userClaims = new HashMap<>();
        userClaims.put("http://wso2.org/claims/username", "user12345");
        userClaims.put("http://wso2.org/claims/givenname", "user1_firstName");
        userClaims.put("http://wso2.org/claims/lastName", "user1_lastName");
        userClaims.put("http://wso2.org/claims/email", "user1@wso2.com");
        userClaims.put("http://wso2.org/claims/accountLocked", "false");

        identityStoreClientService.updateUserProfile(users.get(0).getUserId(), userClaims);

        //Success attempt
        UUFUser authenticatedUser = identityStoreClientService.authenticate("user12345", "admin".toCharArray(),
                PRIMARY_DOMAIN);

        Assert.assertNotNull(authenticatedUser, "Failed to authenticate the user.");
        Assert.assertNotNull(authenticatedUser.getUserId(), "Invalid user unique id.");

    }


    private UUFUser addUser(String username) throws UserPortalUIException {
        IdentityStoreClientService identityStoreClientService =
                bundleContext.getService(bundleContext.getServiceReference(IdentityStoreClientService.class));
        Assert.assertNotNull(identityStoreClientService, "Failed to get IdentityStoreClientService instance");

        Map<String, String> userClaims = new HashMap<>();
        Map<String, String> credentials = new HashMap<>();
        userClaims.put("http://wso2.org/claims/username", username);
        userClaims.put("http://wso2.org/claims/givenname", "user1_firstName");
        userClaims.put("http://wso2.org/claims/lastName", "user1_lastName");
        userClaims.put("http://wso2.org/claims/email", "user1@wso2.com");

        credentials.put("password", "admin");

        UUFUser user = identityStoreClientService.addUser(userClaims, credentials);
        return user;
    }
}
