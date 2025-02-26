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

package org.wso2.identity.integration.test.user.mgt;


import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.wso2.identity.integration.test.restclients.ClaimManagementRestClient;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

import static org.wso2.identity.integration.test.rest.api.common.RESTTestBase.readResource;

public class ReadWriteLDAPUserStoreManagerTestCase extends UserManagementServiceAbstractTest {

    private static final String EMAIL_ADDRESSES_CLAIM_UPDATE_TO_EXCLUDE_USER_STORE =
            "email-addresses-claim-update-to-exclude-userstore.json";
    private static final String EMAIL_ADDRESSES_CLAIM_UPDATE_TO_RESET_EXCLUDED_USER_STORES =
            "email-addresses-claim-update-to-reset-excluded-userstores.json";
    private static final String MOBILE_NUMBERS_CLAIM_UPDATE_TO_EXCLUDE_USER_STORE =
            "mobile-numbers-claim-update-to-exclude-userstore.json";
    private static final String MOBILE_NUMBERS_CLAIM_UPDATE_TO_RESET_EXCLUDED_USER_STORES =
            "mobile-numbers-claim-update-to-reset-excluded-userstores.json";
    private static final String LOCAL_CLAIM_DIALECT = "local";
    private static final String EMAIL_ADDRESSES_CLAIM_ID = "aHR0cDovL3dzbzIub3JnL2NsYWltcy9lbWFpbEFkZHJlc3Nlcw";
    private static final String MOBILE_NUMBERS_CLAIM_ID = "aHR0cDovL3dzbzIub3JnL2NsYWltcy9tb2JpbGVOdW1iZXJz";

    @BeforeClass(alwaysRun = true)
    public void configureServer() throws Exception {
        super.doInit();

    }

    @AfterClass(alwaysRun = true)
    public void restoreServer() throws Exception {
        super.clean();
    }

    @Override
    protected void setUserName() {
        newUserName = "ReadWriteLDAPUserName" ;
    }

    @Override
    protected void setUserPassword() {
        newUserPassword = "ReadWriteLDAPUserName@123";
    }

    @Override
    protected void setUserRole() {
       newUserRole = "ReadWriteLDAPUserRole";
    }

    @Override
    protected void updateExcludedUserStoresClaimProperty(String userStoreName, Boolean reset,
                                                         ClaimManagementRestClient claimManagementRestClient)
            throws Exception {

        String emailAddressesClaimUpdateRequest;
        String mobileNumbersClaimUpdateRequest;
        if (reset) {
            emailAddressesClaimUpdateRequest =
                    readResource(EMAIL_ADDRESSES_CLAIM_UPDATE_TO_RESET_EXCLUDED_USER_STORES, this.getClass());
            mobileNumbersClaimUpdateRequest =
                    readResource(MOBILE_NUMBERS_CLAIM_UPDATE_TO_RESET_EXCLUDED_USER_STORES, this.getClass());
        } else {
            emailAddressesClaimUpdateRequest =
                    readResource(EMAIL_ADDRESSES_CLAIM_UPDATE_TO_EXCLUDE_USER_STORE, this.getClass());
            mobileNumbersClaimUpdateRequest =
                    readResource(MOBILE_NUMBERS_CLAIM_UPDATE_TO_EXCLUDE_USER_STORE, this.getClass());
        }

        claimManagementRestClient.updateClaim(LOCAL_CLAIM_DIALECT, EMAIL_ADDRESSES_CLAIM_ID,
                emailAddressesClaimUpdateRequest);
        claimManagementRestClient.updateClaim(LOCAL_CLAIM_DIALECT, MOBILE_NUMBERS_CLAIM_ID,
                mobileNumbersClaimUpdateRequest);
    }
}

