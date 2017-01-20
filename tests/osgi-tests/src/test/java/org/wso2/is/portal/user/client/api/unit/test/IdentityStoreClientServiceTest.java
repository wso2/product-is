/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.is.portal.user.client.api.unit.test;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.claim.MetaClaim;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.is.portal.user.client.api.IdentityStoreClientService;
import org.wso2.is.portal.user.client.api.bean.UUFUser;
import org.wso2.is.portal.user.client.api.exception.UserPortalUIException;
import org.wso2.is.portal.user.client.api.unit.test.util.UserPortalOSGiTestUtils;

import javax.inject.Inject;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ops4j.pax.exam.CoreOptions.systemProperty;

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class IdentityStoreClientServiceTest {

    private static List<UUFUser> users = new ArrayList<>();
    private static String PRIMARY_DOMAIN = "PRIMARY";

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
    }

    @Test(groups = "authentication", dependsOnGroups = {"addUsers"})
    public void testAuthenticate() throws UserPortalUIException {

        IdentityStoreClientService identityStoreClientService =
                bundleContext.getService(bundleContext.getServiceReference(IdentityStoreClientService.class));
        Assert.assertNotNull(identityStoreClientService, "Failed to get IdentityStoreClientService instance");

        UUFUser user = identityStoreClientService.authenticate("user1", "admin".toCharArray());

        Assert.assertNotNull(user, "Failed to authenticate the user.");
        Assert.assertNotNull(user.getUserId(), "Invalid user unique id.");
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

    @Test(dependsOnGroups = {"addUsers", "update", "authentication"})
    public void testUpdatePassword() throws UserPortalUIException, UserNotFoundException {

        IdentityStoreClientService identityStoreClientService =
                bundleContext.getService(bundleContext.getServiceReference(IdentityStoreClientService.class));
        Assert.assertNotNull(identityStoreClientService, "Failed to get IdentityStoreClientService instance");

        identityStoreClientService.updatePassword("user1", "admin".toCharArray(), "password_updated".toCharArray());
        UUFUser user = identityStoreClientService.authenticate("user1", "password_updated".toCharArray());

        Assert.assertNotNull(user, "Failed to authenticate the user after updating the password.");
        Assert.assertNotNull(user.getUserId(), "Invalid user unique id.");
    }
}
