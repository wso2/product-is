/*
 *Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */
package org.wso2.identity.integration.test.openid;

import org.testng.Assert;
import org.testng.annotations.*;
import org.wso2.carbon.identity.provider.openid.stub.dto.OpenIDClaimDTO;
import org.wso2.carbon.identity.provider.openid.stub.dto.OpenIDParameterDTO;
import org.wso2.carbon.identity.provider.openid.stub.dto.OpenIDUserProfileDTO;
import org.wso2.identity.integration.common.clients.openid.OpenIDProviderServiceClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;

public class OpenIDUserProfileTestCase extends ISIntegrationTest {

    String userName = "suresh";
    String password = "Wso2@123";
    String[] roles = { "admin" };
    String profileName = "default";

    // claims
    String emailClaimURI = "http://axschema.org/contact/email";
    String emailClaimValue = "suresh@wso2.com";
    String firstNameClaimURI = "http://axschema.org/namePerson/first";
    String firstNameClaimValue = "Suresh";
    String lastNameClaimURI = "http://axschema.org/namePerson/last";
    String lastNameClaimValue = "Attanayake";
    String countryClaimURI = "http://axschema.org/contact/country/home";
    String countryClaimValue = "Sri Lanka";

    ClaimValue[] claimValues = new ClaimValue[4];

    private OpenIDProviderServiceClient openidServiceClient;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();

        openidServiceClient = new OpenIDProviderServiceClient(backendURL, sessionCookie);
        createUser();
    }
    
    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        super.init();
        deleteUser();

        openidServiceClient = null;
    }

    @Test(alwaysRun = true, description = "Test reading user profile and claims from the profile")
    public void testUserProfile() {

        String openId = Util.getDefaultOpenIDIdentifier(userName);
        OpenIDParameterDTO[] openidRequestParams = Util.getDummyOpenIDParameterDTOArray();

        OpenIDUserProfileDTO[] userProfiles = null;
        // reading back user profiles
        try {
            userProfiles = openidServiceClient.getUserProfiles(openId, openidRequestParams);
        } catch (Exception e) {
            Assert.fail("Error while reading user profiles", e);
        }

        Assert.assertEquals(userProfiles[0].getProfileName(), profileName);
        Assert.assertNotNull(userProfiles[0].getClaimSet());

        // we expect 4 claims : email, firstname, lastname and country
        Assert.assertEquals(userProfiles[0].getClaimSet().length, 4);

        for (OpenIDClaimDTO claimDTO: userProfiles[0].getClaimSet()){
            if (emailClaimURI.equals(claimDTO.getClaimUri())) {
                Assert.assertTrue(claimDTO.getClaimValue().contains(emailClaimValue));
            } else if (firstNameClaimURI.equals(claimDTO.getClaimUri())) {
                Assert.assertTrue(claimDTO.getClaimValue().contains(firstNameClaimValue));
            } else if (lastNameClaimURI.equals(claimDTO.getClaimUri())) {
                Assert.assertTrue(claimDTO.getClaimValue().contains(lastNameClaimValue));
            } else if (countryClaimURI.equals(claimDTO.getClaimUri())) {
                Assert.assertTrue(claimDTO.getClaimValue().contains(countryClaimValue));
            } else {
                Assert.fail("Invalid claim returned");
            }
        }

        // To access claims for the OpenID user need to have an authenticated session.
        // Login with created user
        try {
            openidServiceClient.authenticateWithOpenID(openId, password);
        } catch (Exception e) {
            Assert.fail("Error while authenticating", e);
        }
    }

    @Test(alwaysRun = true, description = "Test OpenID authentication")
    public void testOpenIDAuthentication() {

        String openId = Util.getDefaultOpenIDIdentifier(userName);

        boolean isAuthenticated = false;

        try {
            isAuthenticated = openidServiceClient.authenticateWithOpenID(openId, password);
        } catch (Exception e) {
            Assert.fail("Error while authenticating", e);
        }

        Assert.assertTrue(isAuthenticated);
    }

    @Test(alwaysRun = true, description = "Test reading claims", dependsOnMethods = { "testOpenIDAuthentication" })
    public void testClaims() {

        String openId = Util.getDefaultOpenIDIdentifier(userName);
        OpenIDParameterDTO[] openidRequestParams = Util.getDummyOpenIDParameterDTOArray();

        OpenIDClaimDTO[] claims = null;
        try {
            // reading back user claims
            claims = openidServiceClient.getClaimValues(openId, profileName, openidRequestParams);
        } catch (Exception e) {
            Assert.fail("Error while reading user claims", e);
        }

        // we expect 4 claims : email, firstname, lastname and country
        Assert.assertEquals(claims.length, 4);

        // now checking claim values
        for (OpenIDClaimDTO dto : claims) {
            if (emailClaimURI.equals(dto.getClaimUri())) {
                Assert.assertTrue(dto.getClaimValue().contains(emailClaimValue));
            } else if (firstNameClaimURI.equals(dto.getClaimUri())) {
                Assert.assertTrue(dto.getClaimValue().contains(firstNameClaimValue));
            } else if (lastNameClaimURI.equals(dto.getClaimUri())) {
                Assert.assertTrue(dto.getClaimValue().contains(lastNameClaimValue));
            } else if (countryClaimURI.equals(dto.getClaimUri())) {
                Assert.assertTrue(dto.getClaimValue().contains(countryClaimValue));
            } else {
                Assert.fail("Invalid claim returned");
            }
        }
    }

    public void createUser() throws Exception {
        ClaimValue email = new ClaimValue();
        email.setClaimURI(emailClaimURI);
        email.setValue(emailClaimValue);
        claimValues[0] = email;

        ClaimValue firstName = new ClaimValue();
        firstName.setClaimURI(firstNameClaimURI);
        firstName.setValue(firstNameClaimValue);
        claimValues[1] = firstName;

        ClaimValue lastName = new ClaimValue();
        lastName.setClaimURI(lastNameClaimURI);
        lastName.setValue(lastNameClaimValue);
        claimValues[2] = lastName;

        ClaimValue country = new ClaimValue();
        country.setClaimURI(countryClaimURI);
        country.setValue(countryClaimValue);
        claimValues[3] = country;

        // creating the user
        RemoteUserStoreManagerServiceClient remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient
                (backendURL, sessionCookie);
        remoteUSMServiceClient.addUser(userName, password, roles, claimValues, profileName, true);
    }

    public void deleteUser() throws Exception {
        RemoteUserStoreManagerServiceClient remoteUSMServiceClient = new RemoteUserStoreManagerServiceClient
                (backendURL, sessionCookie);
        remoteUSMServiceClient.deleteUser(userName);
    }
}