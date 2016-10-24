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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.is.portal.user.client.association;

import org.wso2.carbon.security.caas.user.core.bean.User;

import java.util.List;
import java.util.Map;

public interface FederatedAccountAssociationClientService {

    Map<String, FederatedUserAccount> listUserAssociations(User primaryUser);

    FederatedUserAccount addUserAssociation(User primaryUser, User associatedUser, String idp) throws
                                                                                               UserAccountAssociationException;

    FederatedUserAccount removeUserAssociation(User primaryUser, FederatedUserAccount federatedUserAccount) throws
                                                                                                            UserAccountAssociationException;

    User getAssociationCandidateUser(String userName, String password, String idp);

    List<String> listAvailableIdp(User primaryUser);
}
