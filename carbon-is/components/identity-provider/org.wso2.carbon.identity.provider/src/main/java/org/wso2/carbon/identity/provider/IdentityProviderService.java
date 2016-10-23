/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.provider;


import org.wso2.carbon.identity.provider.common.model.IdentityProvider;
import org.wso2.carbon.identity.provider.common.model.ResidentIdentityProvider;

import java.util.List;
import java.util.Map;

public interface IdentityProviderService {

    ResidentIdentityProvider getResidentIdP() throws IdentityProviderException;

    void createResidentIdP(ResidentIdentityProvider identityProvider) throws IdentityProviderException;

    void updateResidentIdP(ResidentIdentityProvider identityProvider) throws IdentityProviderException;

    void enableResidentIdP() throws IdentityProviderException;

    void disableResidentIdP() throws IdentityProviderException;

    void updateMetaIdentityProvider() throws IdentityProviderException;

    void updateAuthenticatorConfig() throws IdentityProviderException;

    void updateProvisioningConfig() throws IdentityProviderException;

    void updateProperties() throws IdentityProviderException;

    List<IdentityProvider> getIdPs(boolean includeResidentIdP) throws IdentityProviderException;

    IdentityProvider getIdP(String idPName) throws IdentityProviderException;

    IdentityProvider getIdP(int idPId) throws IdentityProviderException;

    List<IdentityProvider> getEnabledIdPs(String tenantDomain) throws IdentityProviderException;

    IdentityProvider getEnabledIdPByName(String idPName) throws IdentityProviderException;

    IdentityProvider getIdPByName(String idPName) throws IdentityProviderException;

    IdentityProvider getIdPByAuthenticatorPropertyValue(String name, String value) throws IdentityProviderException;

    IdentityProvider getEnabledIdPByName(String idPName) throws IdentityProviderException;

    Set<ClaimMapping> getMappedLocalClaims(String idPName, List<String> idPClaimURIs) throws
                                                                                                           IdentityProviderException;

    Map<String, String> getMappedLocalClaimsMap(String idPName, String tenantDomain, List<String> idPClaimURIs) throws
                                                                                                                IdentityProviderException;

    Set<ClaimMapping> getMappedIdPClaims(String idPName, String tenantDomain, List<String> localClaimURIs) throws
                                                                                                           IdentityProviderException;
    Map<String, String> getMappedIdPClaimsMap(String idPName, String tenantDomain, List<String> localClaimURIs) throws
                                                                                                                IdentityProviderException;

    Set<RoleMapping> getMappedLocalRoles(String idPName, String tenantDomain, String[] idPRoles) throws
                                                                                                 IdentityProviderException;

    Map<String, LocalRole> getMappedLocalRolesMap(String idPName, String tenantDomain, String[] idPRoles) throws
                                                                                                          IdentityProviderException;

    Set<RoleMapping> getMappedIdPRoles(String idPName, String tenantDomain, LocalRole[] localRoles) throws
                                                                                                    IdentityProviderException;

    Map<LocalRole, String> getMappedIdPRolesMap(String idPName, String tenantDomain, LocalRole[] localRoles) throws
                                                                                                             IdentityProviderException;

    void createIdP(IdentityProvider identityProvider) throws IdentityProviderException;


    void deleteIdP(String idPName) throws IdentityProviderException;


    void updateIdP(String oldIdPName, IdentityProvider newIdentityProvider) throws IdentityProviderException;

    FederatedAuthenticatorConfig[] getAllFederatedAuthenticators() throws IdentityProviderException;

    ProvisioningConnectorConfig[] getAllProvisioningConnectors() throws IdentityProviderException;

}
