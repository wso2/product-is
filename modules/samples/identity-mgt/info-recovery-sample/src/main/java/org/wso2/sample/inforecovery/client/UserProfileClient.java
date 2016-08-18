/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.sample.inforecovery.client;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceStub;
import org.wso2.carbon.identity.user.profile.stub.types.UserFieldDTO;
import org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO;
import org.wso2.sample.inforecovery.client.authenticator.ServiceAuthenticator;

import java.util.Arrays;
import java.util.Comparator;

public class UserProfileClient {

    private static Log log = LogFactory.getLog(UserProfileClient.class);
    private UserProfileMgtServiceStub stub = null;
    private String serviceEndPoint = null;

    public UserProfileClient(String url, ConfigurationContext configContext) throws Exception {
        try {
            this.serviceEndPoint = url + "services/UserProfileMgtService";

            stub = new UserProfileMgtServiceStub(configContext, serviceEndPoint);

            ServiceAuthenticator authenticator = ServiceAuthenticator.getInstance();
            authenticator.authenticate(stub._getServiceClient());

        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

    public static String extractDomainFromName(String nameWithDomain) {
        int index;
        if ((index = nameWithDomain.indexOf("/")) > 0) {
            // extract the domain name if exist
            String names[] = nameWithDomain.split("/");
            return names[0];
        }
        return null;
    }

    public void setUserProfile(String username, UserProfileDTO profile)
            throws Exception {
        try {
            stub.setUserProfile(username, profile);
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

    public void deleteUserProfile(String username, String profileName)
            throws Exception {
        try {
            stub.deleteUserProfile(username, profileName);
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

    public UserProfileDTO[] getUserProfiles(String userName)
            throws Exception {
        try {
            return stub.getUserProfiles(userName);
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

    public UserProfileDTO getProfileFieldsForInternalStore() throws Exception {
        try {
            return stub.getProfileFieldsForInternalStore();
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

    public boolean isReadOnlyUserStore() throws Exception {
        try {
            return stub.isReadOnlyUserStore();
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

    public UserProfileDTO getUserProfile(String username,
                                         String profile) throws Exception {
        try {
            return stub.getUserProfile(username, profile);
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

    public UserFieldDTO[] getOrderedUserFields(UserFieldDTO[] userFields)
            throws Exception {
        Arrays.sort(userFields, new UserFieldComparator());
        return userFields;
    }

    public boolean isAddProfileEnabled() throws Exception {
        try {
            return stub.isAddProfileEnabled();
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

    public boolean isAddProfileEnabledForDomain(String domain) throws Exception {
        try {
            return stub.isAddProfileEnabledForDomain(domain);
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

    class UserFieldComparator implements Comparator<UserFieldDTO> {

        public int compare(UserFieldDTO filed1, UserFieldDTO filed2) {
            if (filed1.getDisplayOrder() == 0) {
                filed1.setDisplayOrder(Integer.MAX_VALUE);
            }

            if (filed2.getDisplayOrder() == 0) {
                filed2.setDisplayOrder(Integer.MAX_VALUE);
            }

            if (filed1.getDisplayOrder() < filed2.getDisplayOrder()) {
                return -1;
            }
            if (filed1.getDisplayOrder() == filed2.getDisplayOrder()) {
                return 0;
            }
            if (filed1.getDisplayOrder() > filed2.getDisplayOrder()) {
                return 1;
            }
            return 0;
        }

    }
}
