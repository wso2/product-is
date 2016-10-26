/*
 * Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.provider.common.model.AuthenticatorConfig;
import org.wso2.carbon.identity.provider.common.model.ClaimConfig;
import org.wso2.carbon.identity.provider.common.model.IdentityProvider;
import org.wso2.carbon.identity.provider.common.model.ProvisionerConfig;
import org.wso2.carbon.identity.provider.common.model.ResidentIdentityProvider;
import org.wso2.carbon.identity.provider.common.model.RoleConfig;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of Identity Prvider Service.
 */
public class IdentityProviderServiceImpl implements IdentityProviderService {

    private static final Logger log = LoggerFactory.getLogger(IdentityProviderServiceImpl.class);

    @Override
    public ResidentIdentityProvider getResidentIdP() throws IdentityProviderException {
        return null;
    }

    @Override
    public void createResidentIdP(ResidentIdentityProvider identityProvider) throws IdentityProviderException {

    }

    @Override
    public void updateResidentIdP(ResidentIdentityProvider identityProvider) throws IdentityProviderException {

    }

    @Override
    public void enableResidentIdP() throws IdentityProviderException {

    }

    @Override
    public void disableResidentIdP() throws IdentityProviderException {

    }

    @Override
    public void updateMetaIdentityProvider() throws IdentityProviderException {

    }

    @Override
    public void updateAuthenticatorConfig() throws IdentityProviderException {

    }

    @Override
    public void updateProvisioningConfig() throws IdentityProviderException {

    }

    @Override
    public void updateProperties() throws IdentityProviderException {

    }

    @Override
    public List<IdentityProvider> getIdPs(boolean includeResidentIdP) throws IdentityProviderException {
        return null;
    }

    @Override
    public IdentityProvider getIdP(String idPName) throws IdentityProviderException {
        return null;
    }

    @Override
    public IdentityProvider getIdP(int idPId) throws IdentityProviderException {
        return null;
    }

    @Override
    public List<IdentityProvider> getEnabledIdPs(String tenantDomain) throws IdentityProviderException {
        return null;
    }

    @Override
    public IdentityProvider getEnabledIdPByName(String idPName) throws IdentityProviderException {
        return null;
    }

    @Override
    public IdentityProvider getIdPByName(String idPName) throws IdentityProviderException {
        return null;
    }

    @Override
    public IdentityProvider getIdPByAuthenticatorPropertyValue(String name, String value)
            throws IdentityProviderException {
        return null;
    }

    @Override
    public Set<ClaimConfig> getMappedLocalClaims(String idPName, List<String> idPClaimURIs)
            throws IdentityProviderException {
        return null;
    }

    @Override
    public Map<String, String> getMappedLocalClaimsMap(String idPName, String tenantDomain, List<String> idPClaimURIs)
            throws IdentityProviderException {
        return null;
    }

    @Override
    public Set<ClaimConfig> getMappedIdPClaims(String idPName, String tenantDomain, List<String> localClaimURIs)
            throws IdentityProviderException {
        return null;
    }

    @Override
    public Map<String, String> getMappedIdPClaimsMap(String idPName, String tenantDomain, List<String> localClaimURIs)
            throws IdentityProviderException {
        return null;
    }

    @Override
    public Set<RoleConfig> getMappedLocalRoles(String idPName, String tenantDomain, String[] idPRoles)
            throws IdentityProviderException {
        return null;
    }

    @Override
    public Map<String, RoleConfig> getMappedLocalRolesMap(String idPName, String tenantDomain, String[] idPRoles)
            throws IdentityProviderException {
        return null;
    }

    @Override
    public Set<RoleConfig> getMappedIdPRoles(String idPName, String tenantDomain, RoleConfig[] localRoles)
            throws IdentityProviderException {
        return null;
    }

    @Override
    public Map<RoleConfig, String> getMappedIdPRolesMap(String idPName, String tenantDomain, RoleConfig[] localRoles)
            throws IdentityProviderException {
        return null;
    }

    @Override
    public void createIdP(IdentityProvider identityProvider) throws IdentityProviderException {

    }

    @Override
    public void deleteIdP(String idPName) throws IdentityProviderException {

    }

    @Override
    public void updateIdP(String oldIdPName, IdentityProvider newIdentityProvider) throws IdentityProviderException {

    }

    @Override
    public AuthenticatorConfig[] getAllFederatedAuthenticators() throws IdentityProviderException {
        return new AuthenticatorConfig[0];
    }

    @Override
    public ProvisionerConfig[] getAllProvisioningConnectors() throws IdentityProviderException {
        return new ProvisionerConfig[0];
    }
}
