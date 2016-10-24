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

import org.apache.axiom.om.util.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.application.common.ApplicationAuthenticatorService;
import org.wso2.carbon.identity.application.common.ProvisioningConnectorService;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.IdentityProviderProperty;
import org.wso2.carbon.identity.application.common.model.LocalRole;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ProvisioningConnectorConfig;
import org.wso2.carbon.identity.application.common.model.RoleMapping;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.internal.IdPManagementServiceComponent;
import org.wso2.carbon.idp.mgt.listener.IdentityProviderMgtListener;
import org.wso2.carbon.idp.mgt.util.IdPManagementConstants;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.security.KeyStore;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of Identity Prvider Service.
 */
public class IdentityProviderServiceImpl implements IdentityProviderService {

    private static final Log log = LogFactory.getLog(IdentityProviderServiceImpl.class);

    private static CacheBackedIdPMgtDAO dao = new CacheBackedIdPMgtDAO(
            new org.wso2.carbon.idp.mgt.dao.IdentityProviderDAO());

    private static volatile IdentityProviderServiceImpl instance = new IdentityProviderServiceImpl();

    private static final String OPENID_IDP_ENTITY_ID = "IdPEntityId";

    private IdentityProviderServiceImpl() {

    }

    public static IdentityProviderServiceImpl getInstance() {
        return instance;
    }

    /**
     * Retrieves resident Identity provider for a given tenant
     *
     * @param tenantDomain Tenant domain whose resident IdP is requested
     * @return <code>LocalIdentityProvider</code>
     * @throws IdentityProviderManagementException Error when getting Resident Identity Providers
     */
    @Override
    public IdentityProvider getResidentIdP(String tenantDomain) throws IdentityProviderManagementException {

        String tenantContext = "";
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(tenantDomain)) {
            tenantContext = MultitenantConstants.TENANT_AWARE_URL_PREFIX + "/" + tenantDomain + "/";
        }

        String openIdUrl = null;
        String samlSSOUrl = null;
        String samlLogoutUrl = null;
        String oauth1RequestTokenUrl = null;
        String oauth1AuthorizeUrl = null;
        String oauth1AccessTokenUrl = null;
        String oauth2AuthzEPUrl = null;
        String oauth2TokenEPUrl = null;
        String oauth2RevokeEPUrl = null;
        String oauth2UserInfoEPUrl = null;
        String oidcCheckSessionEPUrl = null;
        String oidcLogoutEPUrl = null;
        String passiveStsUrl = null;
        String stsUrl = null;
        String scimUserEndpoint = null;
        String scimGroupsEndpoint = null;

        openIdUrl = IdentityUtil.getProperty(IdentityConstants.ServerConfig.OPENID_SERVER_URL);
        samlSSOUrl = IdentityUtil.getProperty(IdentityConstants.ServerConfig.SSO_IDP_URL);
        samlLogoutUrl = samlSSOUrl;
        oauth1RequestTokenUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH1_REQUEST_TOKEN_URL);
        oauth1AuthorizeUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH1_AUTHORIZE_URL);
        oauth1AccessTokenUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH1_ACCESSTOKEN_URL);
        oauth2AuthzEPUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH2_AUTHZ_EP_URL);
        oauth2TokenEPUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH2_TOKEN_EP_URL);
        oauth2UserInfoEPUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH2_USERINFO_EP_URL);
        oidcCheckSessionEPUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OIDC_CHECK_SESSION_EP_URL);
        oidcLogoutEPUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OIDC_LOGOUT_EP_URL);
        passiveStsUrl = IdentityUtil.getProperty(IdentityConstants.STS.PSTS_IDENTITY_PROVIDER_URL);
        stsUrl = IdentityUtil.getProperty(IdentityConstants.STS.STS_IDENTITY_PROVIDER_URL);
        scimUserEndpoint = IdentityUtil.getProperty(IdentityConstants.SCIM.USER_EP_URL);
        scimGroupsEndpoint = IdentityUtil.getProperty(IdentityConstants.SCIM.GROUP_EP_URL);
        oauth2RevokeEPUrl = IdentityUtil.getProperty(IdentityConstants.OAuth.OAUTH2_REVOKE_EP_URL);

        if (StringUtils.isBlank(openIdUrl)) {
            openIdUrl = IdentityUtil.getServerURL(IdentityConstants.OpenId.OPENID, true, true);
        }

        if (StringUtils.isBlank(samlSSOUrl)) {
            samlSSOUrl = IdentityUtil.getServerURL(IdentityConstants.ServerConfig.SAMLSSO, true, true);
        }

        if (StringUtils.isBlank(samlLogoutUrl)) {
            samlLogoutUrl = IdentityUtil.getServerURL(IdentityConstants.ServerConfig.SAMLSSO, true, true);
        }

        if (StringUtils.isBlank(oauth1RequestTokenUrl)) {
            oauth1RequestTokenUrl = IdentityUtil.getServerURL(IdentityConstants.OAuth.REQUEST_TOKEN, true, true);
        }

        if (StringUtils.isBlank(oauth1AuthorizeUrl)) {
            oauth1AuthorizeUrl = IdentityUtil.getServerURL(IdentityConstants.OAuth.AUTHORIZE_URL, true, true);
        }

        if (StringUtils.isBlank(oauth1AccessTokenUrl)) {
            oauth1AccessTokenUrl = IdentityUtil.getServerURL(IdentityConstants.OAuth.ACCESS_TOKEN, true, true);
        }

        if (StringUtils.isBlank(oauth2AuthzEPUrl)) {
            oauth2AuthzEPUrl = IdentityUtil.getServerURL(IdentityConstants.OAuth.AUTHORIZE, true, false);
        }

        if (StringUtils.isBlank(oauth2TokenEPUrl)) {
            oauth2TokenEPUrl = IdentityUtil.getServerURL(IdentityConstants.OAuth.TOKEN, true, false);
        }

        if (StringUtils.isBlank(oauth2RevokeEPUrl)) {
            oauth2RevokeEPUrl = IdentityUtil.getServerURL(IdentityConstants.OAuth.REVOKE, true, false);
        }

        if (StringUtils.isBlank(oauth2UserInfoEPUrl)) {
            oauth2UserInfoEPUrl = IdentityUtil.getServerURL(IdentityConstants.OAuth.USERINFO, true, false);
        }

        if (StringUtils.isBlank(oidcCheckSessionEPUrl)) {
            oidcCheckSessionEPUrl = IdentityUtil.getServerURL(IdentityConstants.OAuth.CHECK_SESSION, true, false);
        }

        if (StringUtils.isBlank(oidcLogoutEPUrl)) {
            oidcLogoutEPUrl = IdentityUtil.getServerURL(IdentityConstants.OAuth.LOGOUT, true, false);
        }

        if (StringUtils.isBlank(passiveStsUrl)) {
            passiveStsUrl = IdentityUtil.getServerURL(IdentityConstants.STS.PASSIVE_STS, true, true);
        }

        // If sts url is configured in file, change it according to tenant domain. If not configured, add a default url
        if (StringUtils.isNotBlank(stsUrl)) {
            stsUrl = stsUrl.replace(IdentityConstants.STS.WSO2_CARBON_STS,
                    tenantContext + IdentityConstants.STS.WSO2_CARBON_STS);
        } else {
            stsUrl = IdentityUtil
                    .getServerURL("services/" + tenantContext + IdentityConstants.STS.WSO2_CARBON_STS, true, true);
        }

        if (StringUtils.isBlank(scimUserEndpoint)) {
            scimUserEndpoint = IdentityUtil.getServerURL(IdentityConstants.SCIM.USER_EP, true, false);
        }

        if (StringUtils.isBlank(scimGroupsEndpoint)) {
            scimGroupsEndpoint = IdentityUtil.getServerURL(IdentityConstants.SCIM.GROUP_EP, true, false);
        }
        IdentityProvider identityProvider = dao
                .getIdPByName(null, IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME,
                        IdentityTenantUtil.getTenantId(tenantDomain), tenantDomain);

        if (identityProvider == null) {
            String message = "Could not find Resident Identity Provider for tenant " + tenantDomain;
            throw new IdentityProviderManagementException(message);
        }

        int tenantId = -1;
        try {
            tenantId = IdPManagementServiceComponent.getRealmService().getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            throw new IdentityProviderManagementException(
                    "Exception occurred while retrieving Tenant ID from Tenant Domain " + tenantDomain, e);
        }
        X509Certificate cert = null;
        try {
            IdentityTenantUtil.initializeRegistry(tenantId, tenantDomain);
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(tenantDomain, true);
            KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantId);
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                // derive key store name
                String ksName = tenantDomain.trim().replace(".", "-");
                // derive JKS name
                String jksName = ksName + ".jks";
                KeyStore keyStore = keyStoreManager.getKeyStore(jksName);
                cert = (X509Certificate) keyStore.getCertificate(tenantDomain);
            } else {
                cert = keyStoreManager.getDefaultPrimaryCertificate();
            }
        } catch (Exception e) {
            String msg = "Error retrieving primary certificate for tenant : " + tenantDomain;
            throw new IdentityProviderManagementException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

        if (cert == null) {
            throw new IdentityProviderManagementException(
                    "Cannot find the primary certificate for tenant " + tenantDomain);
        }
        try {
            identityProvider.setCertificate(Base64.encode(cert.getEncoded()));
        } catch (CertificateEncodingException e) {
            String msg = "Error occurred while encoding primary certificate for tenant domain " + tenantDomain;
            throw new IdentityProviderManagementException(msg, e);
        }

        List<FederatedAuthenticatorConfig> fedAuthnCofigs = new ArrayList<FederatedAuthenticatorConfig>();
        List<Property> propertiesList = null;

        FederatedAuthenticatorConfig openIdFedAuthn = IdentityApplicationManagementUtil
                .getFederatedAuthenticator(identityProvider.getFederatedAuthenticatorConfigs(),
                        IdentityApplicationConstants.Authenticator.OpenID.NAME);
        if (openIdFedAuthn == null) {
            openIdFedAuthn = new FederatedAuthenticatorConfig();
            openIdFedAuthn.setName(IdentityApplicationConstants.Authenticator.OpenID.NAME);
        }
        propertiesList = new ArrayList<Property>(Arrays.asList(openIdFedAuthn.getProperties()));
        if (IdentityApplicationManagementUtil.getProperty(openIdFedAuthn.getProperties(),
                IdentityApplicationConstants.Authenticator.OpenID.OPEN_ID_URL) == null) {
            Property openIdUrlProp = new Property();
            openIdUrlProp.setName(IdentityApplicationConstants.Authenticator.OpenID.OPEN_ID_URL);
            openIdUrlProp.setValue(openIdUrl);
            propertiesList.add(openIdUrlProp);
        }
        openIdFedAuthn.setProperties(propertiesList.toArray(new Property[propertiesList.size()]));
        fedAuthnCofigs.add(openIdFedAuthn);

        FederatedAuthenticatorConfig saml2SSOFedAuthn = IdentityApplicationManagementUtil
                .getFederatedAuthenticator(identityProvider.getFederatedAuthenticatorConfigs(),
                        IdentityApplicationConstants.Authenticator.SAML2SSO.NAME);
        if (saml2SSOFedAuthn == null) {
            saml2SSOFedAuthn = new FederatedAuthenticatorConfig();
            saml2SSOFedAuthn.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.NAME);
        }
        propertiesList = new ArrayList<Property>(Arrays.asList(saml2SSOFedAuthn.getProperties()));
        if (IdentityApplicationManagementUtil.getProperty(saml2SSOFedAuthn.getProperties(),
                IdentityApplicationConstants.Authenticator.SAML2SSO.SSO_URL) == null) {
            Property ssoUrlProp = new Property();
            ssoUrlProp.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.SSO_URL);
            ssoUrlProp.setValue(samlSSOUrl);
            propertiesList.add(ssoUrlProp);
        }
        if (IdentityApplicationManagementUtil.getProperty(saml2SSOFedAuthn.getProperties(),
                IdentityApplicationConstants.Authenticator.SAML2SSO.LOGOUT_REQ_URL) == null) {
            Property logoutReqUrlProp = new Property();
            logoutReqUrlProp.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.LOGOUT_REQ_URL);
            logoutReqUrlProp.setValue(samlLogoutUrl);
            propertiesList.add(logoutReqUrlProp);
        }
        if (IdentityApplicationManagementUtil.getProperty(saml2SSOFedAuthn.getProperties(),
                IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID) == null) {
            Property idPEntityIdProp = new Property();
            idPEntityIdProp.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID);
            idPEntityIdProp.setValue(org.wso2.carbon.idp.mgt.util.IdentityProviderUtil.getResidentIdPEntityId());
            propertiesList.add(idPEntityIdProp);
        }
        saml2SSOFedAuthn.setProperties(propertiesList.toArray(new Property[propertiesList.size()]));
        fedAuthnCofigs.add(saml2SSOFedAuthn);

        FederatedAuthenticatorConfig oauth1FedAuthn = IdentityApplicationManagementUtil
                .getFederatedAuthenticator(identityProvider.getFederatedAuthenticatorConfigs(),
                        IdentityApplicationConstants.OAuth10A.NAME);
        if (oauth1FedAuthn == null) {
            oauth1FedAuthn = new FederatedAuthenticatorConfig();
            oauth1FedAuthn.setName(IdentityApplicationConstants.OAuth10A.NAME);
        }
        propertiesList = new ArrayList<Property>(Arrays.asList(oauth1FedAuthn.getProperties()));
        if (IdentityApplicationManagementUtil.getProperty(oauth1FedAuthn.getProperties(),
                IdentityApplicationConstants.OAuth10A.OAUTH1_REQUEST_TOKEN_URL) == null) {
            Property oauth1ReqTokUrlProp = new Property();
            oauth1ReqTokUrlProp.setName(IdentityApplicationConstants.OAuth10A.OAUTH1_REQUEST_TOKEN_URL);
            oauth1ReqTokUrlProp.setValue(oauth1RequestTokenUrl);
            propertiesList.add(oauth1ReqTokUrlProp);
        }
        if (IdentityApplicationManagementUtil
                .getProperty(oauth1FedAuthn.getProperties(), IdentityApplicationConstants.OAuth10A.OAUTH1_AUTHORIZE_URL)
                == null) {
            Property oauth1AuthzUrlProp = new Property();
            oauth1AuthzUrlProp.setName(IdentityApplicationConstants.OAuth10A.OAUTH1_AUTHORIZE_URL);
            oauth1AuthzUrlProp.setValue(oauth1AuthorizeUrl);
            propertiesList.add(oauth1AuthzUrlProp);
        }
        if (IdentityApplicationManagementUtil.getProperty(oauth1FedAuthn.getProperties(),
                IdentityApplicationConstants.OAuth10A.OAUTH1_ACCESS_TOKEN_URL) == null) {
            Property oauth1AccessTokUrlProp = new Property();
            oauth1AccessTokUrlProp.setName(IdentityApplicationConstants.OAuth10A.OAUTH1_ACCESS_TOKEN_URL);
            oauth1AccessTokUrlProp.setValue(oauth1AccessTokenUrl);
            propertiesList.add(oauth1AccessTokUrlProp);
        }
        oauth1FedAuthn.setProperties(propertiesList.toArray(new Property[propertiesList.size()]));
        fedAuthnCofigs.add(oauth1FedAuthn);

        FederatedAuthenticatorConfig oidcFedAuthn = IdentityApplicationManagementUtil
                .getFederatedAuthenticator(identityProvider.getFederatedAuthenticatorConfigs(),
                        IdentityApplicationConstants.Authenticator.OIDC.NAME);
        if (oidcFedAuthn == null) {
            oidcFedAuthn = new FederatedAuthenticatorConfig();
            oidcFedAuthn.setName(IdentityApplicationConstants.Authenticator.OIDC.NAME);
        }
        propertiesList = new ArrayList<Property>(Arrays.asList(oidcFedAuthn.getProperties()));
        if (IdentityApplicationManagementUtil.getProperty(oidcFedAuthn.getProperties(), OPENID_IDP_ENTITY_ID) == null) {
            Property idPEntityIdProp = new Property();
            idPEntityIdProp.setName(OPENID_IDP_ENTITY_ID);
            idPEntityIdProp.setValue(getOIDCResidentIdPEntityId());
            propertiesList.add(idPEntityIdProp);
        }
        if (IdentityApplicationManagementUtil.getProperty(oidcFedAuthn.getProperties(),
                IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_AUTHZ_URL) == null) {
            Property authzUrlProp = new Property();
            authzUrlProp.setName(IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_AUTHZ_URL);
            authzUrlProp.setValue(oauth2AuthzEPUrl);
            propertiesList.add(authzUrlProp);
        }
        if (IdentityApplicationManagementUtil.getProperty(oidcFedAuthn.getProperties(),
                IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_TOKEN_URL) == null) {
            Property tokenUrlProp = new Property();
            tokenUrlProp.setName(IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_TOKEN_URL);
            tokenUrlProp.setValue(oauth2TokenEPUrl);
            propertiesList.add(tokenUrlProp);
        }
        if (IdentityApplicationManagementUtil.getProperty(oidcFedAuthn.getProperties(),
                IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_REVOKE_URL) == null) {
            Property revokeUrlProp = new Property();
            revokeUrlProp.setName(IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_REVOKE_URL);
            revokeUrlProp.setValue(oauth2RevokeEPUrl);
            propertiesList.add(revokeUrlProp);
        }
        if (IdentityApplicationManagementUtil.getProperty(oidcFedAuthn.getProperties(),
                IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_USER_INFO_EP_URL) == null) {
            Property userInfoUrlProp = new Property();
            userInfoUrlProp.setName(IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_USER_INFO_EP_URL);
            userInfoUrlProp.setValue(oauth2UserInfoEPUrl);
            propertiesList.add(userInfoUrlProp);
        }
        if (IdentityApplicationManagementUtil.getProperty(oidcFedAuthn.getProperties(),
                IdentityApplicationConstants.Authenticator.OIDC.OIDC_CHECK_SESSION_URL) == null) {
            Property checkSessionUrlProp = new Property();
            checkSessionUrlProp.setName(IdentityApplicationConstants.Authenticator.OIDC.OIDC_CHECK_SESSION_URL);
            checkSessionUrlProp.setValue(oidcCheckSessionEPUrl);
            propertiesList.add(checkSessionUrlProp);
        }
        if (IdentityApplicationManagementUtil.getProperty(oidcFedAuthn.getProperties(),
                IdentityApplicationConstants.Authenticator.OIDC.OIDC_LOGOUT_URL) == null) {
            Property logoutUrlProp = new Property();
            logoutUrlProp.setName(IdentityApplicationConstants.Authenticator.OIDC.OIDC_LOGOUT_URL);
            logoutUrlProp.setValue(oidcLogoutEPUrl);
            propertiesList.add(logoutUrlProp);
        }
        oidcFedAuthn.setProperties(propertiesList.toArray(new Property[propertiesList.size()]));
        fedAuthnCofigs.add(oidcFedAuthn);

        FederatedAuthenticatorConfig passiveSTSFedAuthn = IdentityApplicationManagementUtil
                .getFederatedAuthenticator(identityProvider.getFederatedAuthenticatorConfigs(),
                        IdentityApplicationConstants.Authenticator.PassiveSTS.NAME);
        if (passiveSTSFedAuthn == null) {
            passiveSTSFedAuthn = new FederatedAuthenticatorConfig();
            passiveSTSFedAuthn.setName(IdentityApplicationConstants.Authenticator.PassiveSTS.NAME);
        }
        propertiesList = new ArrayList<Property>(Arrays.asList(passiveSTSFedAuthn.getProperties()));
        if (IdentityApplicationManagementUtil.getProperty(passiveSTSFedAuthn.getProperties(),
                IdentityApplicationConstants.Authenticator.PassiveSTS.IDENTITY_PROVIDER_URL) == null) {
            Property passiveSTSUrlProp = new Property();
            passiveSTSUrlProp.setName(IdentityApplicationConstants.Authenticator.PassiveSTS.IDENTITY_PROVIDER_URL);
            passiveSTSUrlProp.setValue(passiveStsUrl);
            propertiesList.add(passiveSTSUrlProp);
        }

        if (IdentityApplicationManagementUtil.getProperty(passiveSTSFedAuthn.getProperties(),
                IdentityApplicationConstants.Authenticator.PassiveSTS.IDENTITY_PROVIDER_ENTITY_ID) == null) {
            Property idPEntityIdProp = new Property();
            idPEntityIdProp.setName(IdentityApplicationConstants.Authenticator.PassiveSTS.IDENTITY_PROVIDER_ENTITY_ID);
            idPEntityIdProp.setValue(org.wso2.carbon.idp.mgt.util.IdentityProviderUtil.getResidentIdPEntityId());
            propertiesList.add(idPEntityIdProp);
        }
        passiveSTSFedAuthn.setProperties(propertiesList.toArray(new Property[propertiesList.size()]));
        fedAuthnCofigs.add(passiveSTSFedAuthn);

        FederatedAuthenticatorConfig stsFedAuthn = IdentityApplicationManagementUtil
                .getFederatedAuthenticator(identityProvider.getFederatedAuthenticatorConfigs(),
                        IdentityApplicationConstants.Authenticator.WSTrust.NAME);
        if (stsFedAuthn == null) {
            stsFedAuthn = new FederatedAuthenticatorConfig();
            stsFedAuthn.setName(IdentityApplicationConstants.Authenticator.WSTrust.NAME);
        }
        propertiesList = new ArrayList<Property>(Arrays.asList(stsFedAuthn.getProperties()));
        if (IdentityApplicationManagementUtil.getProperty(stsFedAuthn.getProperties(),
                IdentityApplicationConstants.Authenticator.WSTrust.IDENTITY_PROVIDER_URL) == null) {
            Property stsUrlProp = new Property();
            stsUrlProp.setName(IdentityApplicationConstants.Authenticator.WSTrust.IDENTITY_PROVIDER_URL);
            stsUrlProp.setValue(stsUrl);
            propertiesList.add(stsUrlProp);
        }
        stsFedAuthn.setProperties(propertiesList.toArray(new Property[propertiesList.size()]));
        fedAuthnCofigs.add(stsFedAuthn);

        List<IdentityProviderProperty> identityProviderProperties = new ArrayList<IdentityProviderProperty>();

        FederatedAuthenticatorConfig sessionTimeoutConfig = IdentityApplicationManagementUtil
                .getFederatedAuthenticator(identityProvider.getFederatedAuthenticatorConfigs(),
                        IdentityApplicationConstants.NAME);
        if (sessionTimeoutConfig == null) {
            sessionTimeoutConfig = new FederatedAuthenticatorConfig();
            sessionTimeoutConfig.setName(IdentityApplicationConstants.NAME);
        }
        propertiesList = new ArrayList<Property>(Arrays.asList(sessionTimeoutConfig.getProperties()));
        if (IdentityApplicationManagementUtil
                .getProperty(sessionTimeoutConfig.getProperties(), IdentityApplicationConstants.CLEAN_UP_PERIOD)
                == null) {
            Property cleanUpPeriodProp = new Property();
            cleanUpPeriodProp.setName(IdentityApplicationConstants.CLEAN_UP_PERIOD);
            String cleanUpPeriod = IdentityUtil.getProperty(IdentityConstants.ServerConfig.CLEAN_UP_PERIOD);
            if (StringUtils.isBlank(cleanUpPeriod)) {
                cleanUpPeriod = IdentityApplicationConstants.CLEAN_UP_PERIOD_DEFAULT;
            } else if (!StringUtils.isNumeric(cleanUpPeriod)) {
                log.warn("PersistanceCleanUpPeriod in identity.xml should be a numeric value");
                cleanUpPeriod = IdentityApplicationConstants.CLEAN_UP_PERIOD_DEFAULT;
            }
            cleanUpPeriodProp.setValue(cleanUpPeriod);
            propertiesList.add(cleanUpPeriodProp);
        }
        sessionTimeoutConfig.setProperties(propertiesList.toArray(new Property[propertiesList.size()]));
        fedAuthnCofigs.add(sessionTimeoutConfig);

        identityProvider.setFederatedAuthenticatorConfigs(
                fedAuthnCofigs.toArray(new FederatedAuthenticatorConfig[fedAuthnCofigs.size()]));

        ProvisioningConnectorConfig scimProvConn = IdentityApplicationManagementUtil
                .getProvisioningConnector(identityProvider.getProvisioningConnectorConfigs(), "scim");
        if (scimProvConn == null) {
            scimProvConn = new ProvisioningConnectorConfig();
            scimProvConn.setName("scim");
        }
        propertiesList = new ArrayList<Property>(Arrays.asList(scimProvConn.getProvisioningProperties()));
        if (IdentityApplicationManagementUtil.getProperty(scimProvConn.getProvisioningProperties(), "scimUserEndpoint")
                == null) {
            Property property = new Property();
            property.setName("scimUserEndpoint");
            property.setValue(scimUserEndpoint);
            propertiesList.add(property);
        }
        if (IdentityApplicationManagementUtil.getProperty(scimProvConn.getProvisioningProperties(), "scimUserEndpoint")
                == null) {
            Property property = new Property();
            property.setName("scimGroupEndpoint");
            property.setValue(scimGroupsEndpoint);
            propertiesList.add(property);
        }
        scimProvConn.setProvisioningProperties(propertiesList.toArray(new Property[propertiesList.size()]));
        identityProvider.setProvisioningConnectorConfigs(new ProvisioningConnectorConfig[] { scimProvConn });

        return identityProvider;
    }

    /**
     * Add Resident Identity provider for a given tenant
     *
     * @param identityProvider <code>IdentityProvider</code>
     * @param tenantDomain     Tenant domain whose resident IdP is requested
     * @throws IdentityProviderManagementException Error when adding Resident Identity Provider
     */
    @Override
    public void addResidentIdP(IdentityProvider identityProvider, String tenantDomain)
            throws IdentityProviderManagementException {

        // invoking the pre listeners
        Collection<IdentityProviderMgtListener> listeners = IdPManagementServiceComponent.getIdpMgtListeners();
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreAddResidentIdP(identityProvider, tenantDomain)) {
                return;
            }
        }

        if (identityProvider.getFederatedAuthenticatorConfigs() == null) {
            identityProvider.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[0]);
        }
        FederatedAuthenticatorConfig saml2SSOResidentAuthenticatorConfig = IdentityApplicationManagementUtil
                .getFederatedAuthenticator(identityProvider.getFederatedAuthenticatorConfigs(),
                        IdentityApplicationConstants.Authenticator.SAML2SSO.NAME);
        if (saml2SSOResidentAuthenticatorConfig == null) {
            saml2SSOResidentAuthenticatorConfig = new FederatedAuthenticatorConfig();
            saml2SSOResidentAuthenticatorConfig.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.NAME);
        }
        if (saml2SSOResidentAuthenticatorConfig.getProperties() == null) {
            saml2SSOResidentAuthenticatorConfig.setProperties(new Property[0]);
        }

        boolean idPEntityIdAvailable = false;
        for (Property property : saml2SSOResidentAuthenticatorConfig.getProperties()) {
            if (IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID.equals(property.getName())) {
                idPEntityIdAvailable = true;
            }
        }
        if (!idPEntityIdAvailable) {
            Property property = new Property();
            property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID);
            property.setValue(org.wso2.carbon.idp.mgt.util.IdentityProviderUtil.getResidentIdPEntityId());
            if (saml2SSOResidentAuthenticatorConfig.getProperties().length > 0) {
                List<Property> properties = Arrays.asList(saml2SSOResidentAuthenticatorConfig.getProperties());
                properties.add(property);
                saml2SSOResidentAuthenticatorConfig.setProperties((Property[]) properties.toArray());
            } else {
                saml2SSOResidentAuthenticatorConfig.setProperties(new Property[] { property });
            }
        }

        FederatedAuthenticatorConfig idpPropertiesResidentAuthenticatorConfig = IdentityApplicationManagementUtil
                .getFederatedAuthenticator(identityProvider.getFederatedAuthenticatorConfigs(),
                        IdentityApplicationConstants.NAME);
        if (idpPropertiesResidentAuthenticatorConfig == null) {
            idpPropertiesResidentAuthenticatorConfig = new FederatedAuthenticatorConfig();
            idpPropertiesResidentAuthenticatorConfig.setName(IdentityApplicationConstants.NAME);
        }
        List<Property> propertiesList = new ArrayList<Property>(
                Arrays.asList(idpPropertiesResidentAuthenticatorConfig.getProperties()));
        if (IdentityApplicationManagementUtil.getProperty(idpPropertiesResidentAuthenticatorConfig.getProperties(),
                IdentityApplicationConstants.CLEAN_UP_PERIOD) == null) {
            Property cleanUpPeriodProp = new Property();
            cleanUpPeriodProp.setName(IdentityApplicationConstants.CLEAN_UP_PERIOD);
            String cleanUpPeriod = IdentityUtil.getProperty(IdentityConstants.ServerConfig.CLEAN_UP_PERIOD);
            if (StringUtils.isBlank(cleanUpPeriod)) {
                cleanUpPeriod = IdentityApplicationConstants.CLEAN_UP_PERIOD_DEFAULT;
            } else if (!StringUtils.isNumeric(cleanUpPeriod)) {
                log.warn("PersistanceCleanUpPeriod in identity.xml should be a numeric value");
                cleanUpPeriod = IdentityApplicationConstants.CLEAN_UP_PERIOD_DEFAULT;
            }
            cleanUpPeriodProp.setValue(cleanUpPeriod);
            propertiesList.add(cleanUpPeriodProp);
        }
        idpPropertiesResidentAuthenticatorConfig
                .setProperties(propertiesList.toArray(new Property[propertiesList.size()]));

        Property oidcProperty = new Property();
        oidcProperty.setName(OPENID_IDP_ENTITY_ID);
        oidcProperty.setValue(getOIDCResidentIdPEntityId());

        FederatedAuthenticatorConfig oidcAuthenticationConfig = new FederatedAuthenticatorConfig();
        oidcAuthenticationConfig.setProperties(new Property[] { oidcProperty });
        oidcAuthenticationConfig.setName(IdentityApplicationConstants.Authenticator.OIDC.NAME);

        Property passiveStsProperty = new Property();
        passiveStsProperty.setName(IdentityApplicationConstants.Authenticator.PassiveSTS.IDENTITY_PROVIDER_ENTITY_ID);
        passiveStsProperty.setValue(org.wso2.carbon.idp.mgt.util.IdentityProviderUtil.getResidentIdPEntityId());

        FederatedAuthenticatorConfig passiveStsAuthenticationConfig = new FederatedAuthenticatorConfig();
        passiveStsAuthenticationConfig.setProperties(new Property[] { passiveStsProperty });
        passiveStsAuthenticationConfig.setName(IdentityApplicationConstants.Authenticator.PassiveSTS.NAME);

        FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs = {
                saml2SSOResidentAuthenticatorConfig, idpPropertiesResidentAuthenticatorConfig,
                passiveStsAuthenticationConfig, oidcAuthenticationConfig
        };
        identityProvider.setFederatedAuthenticatorConfigs(IdentityApplicationManagementUtil
                .concatArrays(identityProvider.getFederatedAuthenticatorConfigs(), federatedAuthenticatorConfigs));

        IdentityProviderProperty[] idpProperties = new IdentityProviderProperty[2];

        IdentityProviderProperty rememberMeTimeoutProperty = new IdentityProviderProperty();
        String rememberMeTimeout = IdentityUtil.getProperty(IdentityConstants.ServerConfig.REMEMBER_ME_TIME_OUT);
        if (StringUtils.isBlank(rememberMeTimeout) || !StringUtils.isNumeric(rememberMeTimeout) ||
                Integer.parseInt(rememberMeTimeout) <= 0) {
            log.warn("RememberMeTimeout in identity.xml should be a numeric value");
            rememberMeTimeout = IdentityApplicationConstants.REMEMBER_ME_TIME_OUT_DEFAULT;
        }
        rememberMeTimeoutProperty.setName(IdentityApplicationConstants.REMEMBER_ME_TIME_OUT);
        rememberMeTimeoutProperty.setValue(rememberMeTimeout);

        IdentityProviderProperty sessionIdletimeOutProperty = new IdentityProviderProperty();
        String idleTimeout = IdentityUtil.getProperty(IdentityConstants.ServerConfig.SESSION_IDLE_TIMEOUT);
        if (StringUtils.isBlank(idleTimeout) || !StringUtils.isNumeric(idleTimeout) ||
                Integer.parseInt(idleTimeout) <= 0) {
            log.warn("SessionIdleTimeout in identity.xml should be a numeric value");
            idleTimeout = IdentityApplicationConstants.SESSION_IDLE_TIME_OUT_DEFAULT;
        }
        sessionIdletimeOutProperty.setName(IdentityApplicationConstants.SESSION_IDLE_TIME_OUT);
        sessionIdletimeOutProperty.setValue(idleTimeout);

        idpProperties[0] = rememberMeTimeoutProperty;
        idpProperties[1] = sessionIdletimeOutProperty;
        identityProvider.setIdpProperties(idpProperties);

        dao.addIdP(identityProvider, IdentityTenantUtil.getTenantId(tenantDomain), tenantDomain);

        // invoking the post listeners
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostAddResidentIdP(identityProvider, tenantDomain)) {
                return;
            }
        }
    }

    /**
     * Update Resident Identity provider for a given tenant
     *
     * @param identityProvider <code>IdentityProvider</code>
     * @param tenantDomain     Tenant domain whose resident IdP is requested
     * @throws IdentityProviderManagementException Error when updating Resident Identity Provider
     */
    @Override
    public void updateResidentIdP(IdentityProvider identityProvider, String tenantDomain)
            throws IdentityProviderManagementException {

        for (IdentityProviderProperty idpProp : identityProvider.getIdpProperties()) {
            if (StringUtils.equals(idpProp.getName(), IdentityApplicationConstants.SESSION_IDLE_TIME_OUT)) {
                if (StringUtils.isBlank(idpProp.getValue()) || !StringUtils.isNumeric(idpProp.getValue()) ||
                        Integer.parseInt(idpProp.getValue().trim()) <= 0) {
                    throw new IdentityProviderManagementException(IdentityApplicationConstants.SESSION_IDLE_TIME_OUT
                            + " of ResidentIdP should be a numeric value greater than 0 ");
                }
            } else if (StringUtils.equals(idpProp.getName(), IdentityApplicationConstants.REMEMBER_ME_TIME_OUT)) {
                if (StringUtils.isBlank(idpProp.getValue()) || !StringUtils.isNumeric(idpProp.getValue()) ||
                        Integer.parseInt(idpProp.getValue().trim()) <= 0) {
                    throw new IdentityProviderManagementException(IdentityApplicationConstants.REMEMBER_ME_TIME_OUT
                            + " of ResidentIdP should be a numeric value greater than 0 ");
                }
            }
        }
        // invoking the pre listeners
        Collection<IdentityProviderMgtListener> listeners = IdPManagementServiceComponent.getIdpMgtListeners();
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreUpdateResidentIdP(identityProvider, tenantDomain)) {
                return;
            }
        }

        if (identityProvider.getFederatedAuthenticatorConfigs() == null) {
            identityProvider.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[0]);
        }

        IdentityProvider currentIdP = org.wso2.carbon.idp.mgt.IdentityProviderServiceImpl.getInstance()
                .getIdPByName(IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME, tenantDomain, true);

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        validateUpdateOfIdPEntityId(currentIdP.getFederatedAuthenticatorConfigs(),
                identityProvider.getFederatedAuthenticatorConfigs(), tenantId, tenantDomain);

        dao.updateIdP(identityProvider, currentIdP, tenantId, tenantDomain);

        // invoking the post listeners
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostUpdateResidentIdP(identityProvider, tenantDomain)) {
                return;
            }
        }
    }

    /**
     * Retrieves registered Identity providers for a given tenant
     *
     * @param tenantDomain Tenant domain whose IdP names are requested
     * @return Set of <code>IdentityProvider</code>. IdP names, primary IdP and home realm
     * identifiers of each IdP
     * @throws IdentityProviderManagementException Error when getting list of Identity Providers
     */
    @Override
    public List<IdentityProvider> getIdPs(String tenantDomain) throws IdentityProviderManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        return dao.getIdPs(null, tenantId, tenantDomain);

    }

    /**
     * Retrieves registered Enabled Identity providers for a given tenant
     *
     * @param tenantDomain Tenant domain whose IdP names are requested
     * @return Set of <code>IdentityProvider</code>. IdP names, primary IdP and home realm
     * identifiers of each IdP
     * @throws IdentityProviderManagementException Error when getting list of Identity Providers
     */
    @Override
    public List<IdentityProvider> getEnabledIdPs(String tenantDomain) throws IdentityProviderManagementException {
        List<IdentityProvider> enabledIdentityProviders = new ArrayList<IdentityProvider>();
        List<IdentityProvider> identityProviers = getIdPs(tenantDomain);

        for (IdentityProvider idp : identityProviers) {
            if (idp.isEnable()) {
                enabledIdentityProviders.add(idp);
            }
        }
        return enabledIdentityProviders;

    }

    /**
     * @param idPName
     * @param tenantDomain
     * @param ignoreFileBasedIdps
     * @return
     * @throws IdentityProviderManagementException
     */
    @Override
    public IdentityProvider getIdPByName(String idPName, String tenantDomain, boolean ignoreFileBasedIdps)
            throws IdentityProviderManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (StringUtils.isEmpty(idPName)) {
            String msg = "Invalid argument: Identity Provider Name value is empty";
            throw new IdentityProviderManagementException(msg);
        }

        IdentityProvider identityProvider = dao.getIdPByName(null, idPName, tenantId, tenantDomain);

        if (!ignoreFileBasedIdps) {

            if (identityProvider == null) {
                identityProvider = new FileBasedIdPMgtDAO().getIdPByName(idPName, tenantDomain);
            }

            if (identityProvider == null) {
                identityProvider = IdPManagementServiceComponent.getFileBasedIdPs()
                        .get(IdentityApplicationConstants.DEFAULT_IDP_CONFIG);
            }
        }

        return identityProvider;
    }

    /**
     * @param idPName
     * @param tenantDomain
     * @param ignoreFileBasedIdps
     * @return
     * @throws IdentityProviderManagementException
     */
    @Override
    public IdentityProvider getEnabledIdPByName(String idPName, String tenantDomain, boolean ignoreFileBasedIdps)
            throws IdentityProviderManagementException {

        IdentityProvider idp = getIdPByName(idPName, tenantDomain, ignoreFileBasedIdps);
        if (idp != null && idp.isEnable()) {
            return idp;
        }
        return null;
    }

    /**
     * Retrieves Identity provider information about a given tenant by Identity Provider name
     *
     * @param idPName      Unique name of the Identity provider of whose information is requested
     * @param tenantDomain Tenant domain whose information is requested
     * @return <code>IdentityProvider</code> Identity Provider information
     * @throws IdentityProviderManagementException Error when getting Identity Provider
     *                                             information by IdP name
     */
    @Override
    public IdentityProvider getIdPByName(String idPName, String tenantDomain)
            throws IdentityProviderManagementException {
        return getIdPByName(idPName, tenantDomain, false);
    }

    /**
     * @param property     IDP authenticator property (E.g.: IdPEntityId)
     * @param value        Value associated with given Property
     * @param tenantDomain
     * @return <code>IdentityProvider</code> Identity Provider information
     * @throws IdentityProviderManagementException Error when getting Identity Provider
     *                                             information by authenticator property value
     */
    @Override
    public IdentityProvider getIdPByAuthenticatorPropertyValue(String property, String value, String tenantDomain,
            boolean ignoreFileBasedIdps) throws IdentityProviderManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        if (StringUtils.isEmpty(property) || StringUtils.isEmpty(value)) {
            String msg = "Invalid argument: Authenticator property or property value is empty";
            throw new IdentityProviderManagementException(msg);
        }

        IdentityProvider identityProvider = dao
                .getIdPByAuthenticatorPropertyValue(null, property, value, tenantId, tenantDomain);

        if (identityProvider == null && !ignoreFileBasedIdps) {
            identityProvider = new FileBasedIdPMgtDAO()
                    .getIdPByAuthenticatorPropertyValue(property, value, tenantDomain);
        }

        return identityProvider;
    }

    /**
     * Retrieves Enabled Identity provider information about a given tenant by Identity Provider name
     *
     * @param idPName      Unique name of the Identity provider of whose information is requested
     * @param tenantDomain Tenant domain whose information is requested
     * @return <code>IdentityProvider</code> Identity Provider information
     * @throws IdentityProviderManagementException Error when getting Identity Provider
     *                                             information by IdP name
     */
    @Override
    public IdentityProvider getEnabledIdPByName(String idPName, String tenantDomain)
            throws IdentityProviderManagementException {

        IdentityProvider idp = getIdPByName(idPName, tenantDomain);
        if (idp != null && idp.isEnable()) {
            return idp;
        }
        return null;
    }

    /**
     * Retrieves Identity provider information about a given tenant by realm identifier
     *
     * @param realmId      Unique realm identifier of the Identity provider of whose information is
     *                     requested
     * @param tenantDomain Tenant domain whose information is requested
     * @throws IdentityProviderManagementException Error when getting Identity Provider
     *                                             information by IdP home realm identifier
     */
    @Override
    public IdentityProvider getIdPByRealmId(String realmId, String tenantDomain)
            throws IdentityProviderManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (StringUtils.isEmpty(realmId)) {
            String msg = "Invalid argument: Identity Provider Home Realm Identifier value is empty";
            throw new IdentityProviderManagementException(msg);
        }
        IdentityProvider identityProvider = dao.getIdPByRealmId(realmId, tenantId, tenantDomain);

        if (identityProvider == null) {
            identityProvider = new FileBasedIdPMgtDAO().getIdPByRealmId(realmId, tenantDomain);
        }

        return identityProvider;
    }

    /**
     * Retrieves Enabled Identity provider information about a given tenant by realm identifier
     *
     * @param realmId      Unique realm identifier of the Identity provider of whose information is
     *                     requested
     * @param tenantDomain Tenant domain whose information is requested
     * @throws IdentityProviderManagementException Error when getting Identity Provider
     *                                             information by IdP home realm identifier
     */
    @Override
    public IdentityProvider getEnabledIdPByRealmId(String realmId, String tenantDomain)
            throws IdentityProviderManagementException {

        IdentityProvider idp = getIdPByRealmId(realmId, tenantDomain);
        if (idp != null && idp.isEnable()) {
            return idp;
        }
        return null;
    }

    /**
     * Retrieves Identity provider information about a given tenant
     *
     * @param idPName      Unique Name of the IdP to which the given IdP claim URIs need to be mapped
     * @param tenantDomain The tenant domain of whose local claim URIs to be mapped
     * @param idPClaimURIs IdP claim URIs which need to be mapped to tenant's local claim URIs
     * @throws IdentityProviderManagementException Error when getting claim mappings
     */
    @Override
    public Set<ClaimMapping> getMappedLocalClaims(String idPName, String tenantDomain, List<String> idPClaimURIs)
            throws IdentityProviderManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (StringUtils.isEmpty(idPName)) {
            String msg = "Invalid argument: Identity Provider Name value is empty";
            throw new IdentityProviderManagementException(msg);
        }

        IdentityProvider identityProvider = dao.getIdPByName(null, idPName, tenantId, tenantDomain);

        if (identityProvider == null) {
            identityProvider = new FileBasedIdPMgtDAO().getIdPByName(idPName, tenantDomain);
        }

        if (identityProvider == null) {
            identityProvider = IdPManagementServiceComponent.getFileBasedIdPs()
                    .get(IdentityApplicationConstants.DEFAULT_IDP_CONFIG);
        }

        ClaimConfig claimConfiguration = identityProvider.getClaimConfig();

        if (claimConfiguration != null) {

            ClaimMapping[] claimMappings = claimConfiguration.getClaimMappings();

            if (claimMappings != null && claimMappings.length > 0 && idPClaimURIs != null) {
                Set<ClaimMapping> returnSet = new HashSet<ClaimMapping>();
                for (String idpClaim : idPClaimURIs) {
                    for (ClaimMapping claimMapping : claimMappings) {
                        if (claimMapping.getRemoteClaim().getClaimUri().equals(idpClaim)) {
                            returnSet.add(claimMapping);
                            break;
                        }
                    }
                }
                return returnSet;
            }
        }

        return new HashSet<ClaimMapping>();
    }

    /**
     * Retrieves Identity provider information about a given tenant
     *
     * @param idPName      Unique Name of the IdP to which the given IdP claim URIs need to be mapped
     * @param tenantDomain The tenant domain of whose local claim URIs to be mapped
     * @param idPClaimURIs IdP claim URIs which need to be mapped to tenant's local claim URIs
     * @throws IdentityProviderManagementException Error when getting claim mappings
     */
    @Override
    public Map<String, String> getMappedLocalClaimsMap(String idPName, String tenantDomain, List<String> idPClaimURIs)
            throws IdentityProviderManagementException {

        Set<ClaimMapping> claimMappings = getMappedLocalClaims(idPName, tenantDomain, idPClaimURIs);
        Map<String, String> returnMap = new HashMap<String, String>();
        for (ClaimMapping claimMapping : claimMappings) {
            returnMap.put(claimMapping.getRemoteClaim().getClaimUri(), claimMapping.getLocalClaim().getClaimUri());
        }
        return returnMap;
    }

    /**
     * Retrieves Identity provider information about a given tenant
     *
     * @param idPName        Unique Name of the IdP to which the given local claim URIs need to be mapped
     * @param tenantDomain   The tenant domain of whose local claim URIs to be mapped
     * @param localClaimURIs Local claim URIs which need to be mapped to IdP's claim URIs
     * @throws IdentityProviderManagementException Error when getting claim mappings
     */
    @Override
    public Set<ClaimMapping> getMappedIdPClaims(String idPName, String tenantDomain, List<String> localClaimURIs)
            throws IdentityProviderManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (StringUtils.isEmpty(idPName)) {
            String msg = "Invalid argument: Identity Provider Name value is empty";
            throw new IdentityProviderManagementException(msg);
        }

        IdentityProvider identityProvider = dao.getIdPByName(null, idPName, tenantId, tenantDomain);

        if (identityProvider == null) {
            identityProvider = new FileBasedIdPMgtDAO().getIdPByName(idPName, tenantDomain);
        }

        if (identityProvider == null) {
            identityProvider = IdPManagementServiceComponent.getFileBasedIdPs()
                    .get(IdentityApplicationConstants.DEFAULT_IDP_CONFIG);
        }

        ClaimConfig claimConfiguration = identityProvider.getClaimConfig();

        if (claimConfiguration != null) {

            ClaimMapping[] claimMappings = claimConfiguration.getClaimMappings();

            if (claimMappings != null && claimMappings.length > 0 && localClaimURIs != null) {
                Set<ClaimMapping> returnSet = new HashSet<ClaimMapping>();
                for (String localClaimURI : localClaimURIs) {
                    for (ClaimMapping claimMapping : claimMappings) {
                        if (claimMapping.equals(localClaimURI)) {
                            returnSet.add(claimMapping);
                            break;
                        }
                    }
                }
                return returnSet;
            }
        }
        return new HashSet<ClaimMapping>();
    }

    /**
     * Retrieves Identity provider information about a given tenant
     *
     * @param idPName        Unique Name of the IdP to which the given local claim URIs need to be mapped
     * @param tenantDomain   The tenant domain of whose local claim URIs to be mapped
     * @param localClaimURIs Local claim URIs which need to be mapped to IdP's claim URIs
     * @throws IdentityProviderManagementException Error when getting claim mappings
     */
    @Override
    public Map<String, String> getMappedIdPClaimsMap(String idPName, String tenantDomain, List<String> localClaimURIs)
            throws IdentityProviderManagementException {

        Set<ClaimMapping> claimMappings = getMappedIdPClaims(idPName, tenantDomain, localClaimURIs);
        Map<String, String> returnMap = new HashMap<String, String>();
        for (ClaimMapping claimMapping : claimMappings) {
            returnMap.put(claimMapping.getLocalClaim().getClaimUri(), claimMapping.getRemoteClaim().getClaimUri());
        }
        return returnMap;
    }

    /**
     * Retrieves Identity provider information about a given tenant
     *
     * @param idPName      Unique name of the IdP to which the given IdP roles need to be mapped
     * @param tenantDomain The tenant domain of whose local roles to be mapped
     * @param idPRoles     IdP roles which need to be mapped to local roles
     * @throws IdentityProviderManagementException Error when getting role mappings
     */
    @Override
    public Set<RoleMapping> getMappedLocalRoles(String idPName, String tenantDomain, String[] idPRoles)
            throws IdentityProviderManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        if (StringUtils.isEmpty(idPName)) {
            String msg = "Invalid argument: Identity Provider Name value is empty";
            throw new IdentityProviderManagementException(msg);
        }

        IdentityProvider identityProvider = dao.getIdPByName(null, idPName, tenantId, tenantDomain);

        if (identityProvider == null) {
            identityProvider = new FileBasedIdPMgtDAO().getIdPByName(idPName, tenantDomain);
        }

        if (identityProvider == null) {
            identityProvider = IdPManagementServiceComponent.getFileBasedIdPs()
                    .get(IdentityApplicationConstants.DEFAULT_IDP_CONFIG);
        }

        PermissionsAndRoleConfig roleConfiguration = identityProvider.getPermissionAndRoleConfig();

        if (roleConfiguration != null) {
            RoleMapping[] roleMappings = roleConfiguration.getRoleMappings();

            if (roleMappings != null && roleMappings.length > 0 && idPRoles != null) {
                Set<RoleMapping> returnSet = new HashSet<RoleMapping>();
                for (String idPRole : idPRoles) {
                    for (RoleMapping roleMapping : roleMappings) {
                        if (roleMapping.getRemoteRole().equals(idPRole)) {
                            returnSet.add(roleMapping);
                            break;
                        }
                    }
                }
                return returnSet;
            }
        }
        return new HashSet<RoleMapping>();
    }

    /**
     * Retrieves Identity provider information about a given tenant
     *
     * @param idPName      Unique name of the IdP to which the given IdP roles need to be mapped
     * @param tenantDomain The tenant domain of whose local roles to be mapped
     * @param idPRoles     IdP roles which need to be mapped to local roles
     * @throws IdentityProviderManagementException Error when getting role mappings
     */
    @Override
    public Map<String, LocalRole> getMappedLocalRolesMap(String idPName, String tenantDomain, String[] idPRoles)
            throws IdentityProviderManagementException {

        Set<RoleMapping> roleMappings = getMappedLocalRoles(idPName, tenantDomain, idPRoles);
        Map<String, LocalRole> returnMap = new HashMap<String, LocalRole>();
        for (RoleMapping roleMapping : roleMappings) {
            returnMap.put(roleMapping.getRemoteRole(), roleMapping.getLocalRole());
        }
        return returnMap;
    }

    /**
     * Retrieves Identity provider information about a given tenant
     *
     * @param idPName      Unique name of the IdP to which the given local roles need to be mapped
     * @param tenantDomain The tenant domain of whose local roles need to be mapped
     * @param localRoles   Local roles which need to be mapped to IdP roles
     * @throws IdentityProviderManagementException Error when getting role mappings
     */
    @Override
    public Set<RoleMapping> getMappedIdPRoles(String idPName, String tenantDomain, LocalRole[] localRoles)
            throws IdentityProviderManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (StringUtils.isEmpty(idPName)) {
            String msg = "Invalid argument: Identity Provider Name value is empty";
            throw new IdentityProviderManagementException(msg);
        }
        IdentityProvider identityProvider = dao.getIdPByName(null, idPName, tenantId, tenantDomain);

        if (identityProvider == null) {
            identityProvider = new FileBasedIdPMgtDAO().getIdPByName(idPName, tenantDomain);
        }

        if (identityProvider == null) {
            identityProvider = IdPManagementServiceComponent.getFileBasedIdPs()
                    .get(IdentityApplicationConstants.DEFAULT_IDP_CONFIG);
        }

        PermissionsAndRoleConfig roleConfiguration = identityProvider.getPermissionAndRoleConfig();

        if (roleConfiguration != null) {
            RoleMapping[] roleMappings = roleConfiguration.getRoleMappings();

            if (roleMappings != null && roleMappings.length > 0 && localRoles != null) {
                Set<RoleMapping> returnSet = new HashSet<RoleMapping>();
                for (LocalRole localRole : localRoles) {
                    for (RoleMapping roleMapping : roleMappings) {
                        if (roleMapping.getLocalRole().equals(localRole)) {
                            returnSet.add(roleMapping);
                            break;
                        }
                    }
                }
                return returnSet;
            }
        }
        return new HashSet<RoleMapping>();
    }

    /**
     * Retrieves Identity provider information about a given tenant
     *
     * @param idPName      Unique name of the IdP to which the given local roles need to be mapped
     * @param tenantDomain The tenant domain of whose local roles need to be mapped
     * @param localRoles   Local roles which need to be mapped to IdP roles
     * @throws IdentityProviderManagementException Error when getting role mappings
     */
    @Override
    public Map<LocalRole, String> getMappedIdPRolesMap(String idPName, String tenantDomain, LocalRole[] localRoles)
            throws IdentityProviderManagementException {

        Set<RoleMapping> roleMappings = getMappedIdPRoles(idPName, tenantDomain, localRoles);
        Map<LocalRole, String> returnMap = new HashMap<LocalRole, String>();
        for (RoleMapping roleMapping : roleMappings) {
            returnMap.put(roleMapping.getLocalRole(), roleMapping.getRemoteRole());
        }
        return returnMap;
    }

    /**
     * Adds an Identity Provider to the given tenant
     *
     * @param identityProvider new Identity Provider information
     * @throws IdentityProviderManagementException Error when adding Identity Provider
     *                                             information
     */
    @Override
    public void addIdP(IdentityProvider identityProvider, String tenantDomain)
            throws IdentityProviderManagementException {

        // invoking the pre listeners
        Collection<IdentityProviderMgtListener> listeners = IdPManagementServiceComponent.getIdpMgtListeners();
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreAddIdP(identityProvider, tenantDomain)) {
                return;
            }
        }

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        if (IdPManagementServiceComponent.getFileBasedIdPs().containsKey(identityProvider.getIdentityProviderName())
                && !identityProvider.getIdentityProviderName().startsWith(IdPManagementConstants.SHARED_IDP_PREFIX)) {
            //If an IDP with name starting with "SHARED_" is added from UI, It's blocked at the service class
            // before calling this method
            throw new IdentityProviderManagementException(
                    "Identity provider with the name" + identityProvider.getIdentityProviderName()
                            + "exists in the file system.");
        }

        PermissionsAndRoleConfig roleConfiguration = identityProvider.getPermissionAndRoleConfig();

        if (roleConfiguration != null && roleConfiguration.getRoleMappings() != null) {
            for (RoleMapping mapping : roleConfiguration.getRoleMappings()) {
                UserStoreManager usm = null;
                try {
                    usm = IdPManagementServiceComponent.getRealmService().getTenantUserRealm(tenantId)
                            .getUserStoreManager();
                    String role = null;
                    if (mapping.getLocalRole().getUserStoreId() != null) {
                        role = mapping.getLocalRole().getUserStoreId() + CarbonConstants.DOMAIN_SEPARATOR + mapping
                                .getLocalRole().getLocalRoleName();
                    }
                    if (usm.isExistingRole(role)) {
                        // perfect
                    } else {
                        String msg = "Cannot find tenant role " + role + " for tenant " + tenantDomain;
                        throw new IdentityProviderManagementException(msg);
                    }
                } catch (UserStoreException e) {
                    String msg = "Error occurred while retrieving UserStoreManager for tenant " + tenantDomain;
                    throw new IdentityProviderManagementException(msg, e);
                }
            }
        }

        if (org.wso2.carbon.idp.mgt.IdentityProviderServiceImpl.getInstance()
                .getIdPByName(identityProvider.getIdentityProviderName(), tenantDomain, true) != null) {
            String msg = "An Identity Provider has already been registered with the name " + identityProvider
                    .getIdentityProviderName() + " for tenant " + tenantDomain;
            throw new IdentityProviderManagementException(msg);
        }

        validateIdPEntityId(identityProvider.getFederatedAuthenticatorConfigs(), tenantId, tenantDomain);

        dao.addIdP(identityProvider, tenantId, tenantDomain);

        // invoking the post listeners
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostAddIdP(identityProvider, tenantDomain)) {
                return;
            }
        }
    }

    /**
     * Deletes an Identity Provider from a given tenant
     *
     * @param idPName Name of the IdP to be deleted
     * @throws IdentityProviderManagementException Error when deleting Identity Provider
     *                                             information
     */
    @Override
    public void deleteIdP(String idPName, String tenantDomain) throws IdentityProviderManagementException {

        // invoking the pre listeners
        Collection<IdentityProviderMgtListener> listeners = IdPManagementServiceComponent.getIdpMgtListeners();
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreDeleteIdP(idPName, tenantDomain)) {
                return;
            }
        }

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        dao.deleteIdP(idPName, tenantId, tenantDomain);

        // invoking the post listeners
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostDeleteIdP(idPName, tenantDomain)) {
                return;
            }
        }
    }

    /**
     * Updates a given Identity Provider information
     *
     * @param oldIdPName          existing Identity Provider name
     * @param newIdentityProvider new IdP information
     * @throws IdentityProviderManagementException Error when updating Identity Provider
     *                                             information
     */
    @Override
    public void updateIdP(String oldIdPName, IdentityProvider newIdentityProvider, String tenantDomain)
            throws IdentityProviderManagementException {

        // invoking the pre listeners
        Collection<IdentityProviderMgtListener> listeners = IdPManagementServiceComponent.getIdpMgtListeners();
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreUpdateIdP(oldIdPName, newIdentityProvider, tenantDomain)) {
                return;
            }
        }

        if (IdPManagementServiceComponent.getFileBasedIdPs()
                .containsKey(newIdentityProvider.getIdentityProviderName())) {
            throw new IdentityProviderManagementException(
                    "Identity provider with the same name exists in the file system.");
        }

        IdentityProvider currentIdentityProvider = this.getIdPByName(oldIdPName, tenantDomain, true);
        if (currentIdentityProvider == null) {
            String msg = "Identity Provider with name " + oldIdPName + " does not exist";
            throw new IdentityProviderManagementException(msg);
        }

        if (currentIdentityProvider.isPrimary() == true && newIdentityProvider.isPrimary() == false) {
            String msg = "Invalid argument: Cannot unset Identity Provider from primary. "
                    + "Alternatively set new Identity Provider to primary";
            throw new IdentityProviderManagementException(msg);
        }

        if (newIdentityProvider.getPermissionAndRoleConfig() != null
                && newIdentityProvider.getPermissionAndRoleConfig().getRoleMappings() != null) {
            for (RoleMapping mapping : newIdentityProvider.getPermissionAndRoleConfig().getRoleMappings()) {
                UserStoreManager usm = null;
                try {
                    usm = CarbonContext.getThreadLocalCarbonContext().getUserRealm().getUserStoreManager();
                    String role = null;
                    if (mapping.getLocalRole().getUserStoreId() != null) {
                        role = mapping.getLocalRole().getUserStoreId() + CarbonConstants.DOMAIN_SEPARATOR + mapping
                                .getLocalRole().getLocalRoleName();
                    } else {
                        role = mapping.getLocalRole().getLocalRoleName();
                    }
                    if (usm.isExistingRole(role)) {
                        // perfect
                    } else {
                        String msg = "Cannot find tenant role " + role + " for tenant " + tenantDomain;
                        throw new IdentityProviderManagementException(msg);
                    }
                } catch (UserStoreException e) {
                    String msg = "Error occurred while retrieving UserStoreManager for tenant " + tenantDomain;
                    throw new IdentityProviderManagementException(msg, e);
                }
            }
        }

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        validateUpdateOfIdPEntityId(currentIdentityProvider.getFederatedAuthenticatorConfigs(),
                newIdentityProvider.getFederatedAuthenticatorConfigs(), tenantId, tenantDomain);

        dao.updateIdP(newIdentityProvider, currentIdentityProvider, tenantId, tenantDomain);

        // invoking the post listeners
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostUpdateIdP(oldIdPName, newIdentityProvider, tenantDomain)) {
                return;
            }
        }
    }

    /**
     * Get the authenticators registered in the system.
     *
     * @return <code>FederatedAuthenticatorConfig</code> array.
     * @throws IdentityProviderManagementException Error when getting authenticators registered
     *                                             in the system
     */
    @Override
    public FederatedAuthenticatorConfig[] getAllFederatedAuthenticators() throws IdentityProviderManagementException {
        List<FederatedAuthenticatorConfig> appConfig = ApplicationAuthenticatorService.getInstance()
                .getFederatedAuthenticators();
        if (CollectionUtils.isNotEmpty(appConfig)) {
            return appConfig.toArray(new FederatedAuthenticatorConfig[appConfig.size()]);
        }
        return new FederatedAuthenticatorConfig[0];
    }

    /**
     * Get the Provisioning Connectors registered in the system.
     *
     * @return <code>ProvisioningConnectorConfig</code> array.
     * @throws IdentityProviderManagementException
     */
    @Override
    public ProvisioningConnectorConfig[] getAllProvisioningConnectors() throws IdentityProviderManagementException {
        List<ProvisioningConnectorConfig> connectorConfigs = ProvisioningConnectorService.getInstance()
                .getProvisioningConnectorConfigs();
        if (connectorConfigs != null && connectorConfigs.size() > 0) {
            return connectorConfigs.toArray(new ProvisioningConnectorConfig[connectorConfigs.size()]);
        }
        return null;
    }

    private boolean validateIdPEntityId(FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs, int tenantId,
            String tenantDomain) throws IdentityProviderManagementException {
        if (federatedAuthenticatorConfigs != null) {
            for (FederatedAuthenticatorConfig authConfig : federatedAuthenticatorConfigs) {
                if (IdentityApplicationConstants.Authenticator.SAML2SSO.FED_AUTH_NAME.equals(authConfig.getName())
                        || IdentityApplicationConstants.Authenticator.SAML2SSO.NAME.equals(authConfig.getName())) {
                    Property[] properties = authConfig.getProperties();
                    if (properties != null) {
                        for (Property property : properties) {
                            if (IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID
                                    .equals(property.getName())) {
                                if (dao.isIdPAvailableForAuthenticatorProperty(authConfig.getName(),
                                        IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID,
                                        property.getValue(), tenantId)) {
                                    String msg =
                                            "An Identity Provider Entity Id has already been registered with the " +
                                                    "name '" + property.getValue() + "' for tenant '" + tenantDomain
                                                    + "'";
                                    throw new IdentityProviderManagementException(msg);
                                }
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean validateUpdateOfIdPEntityId(FederatedAuthenticatorConfig[] currentFederatedAuthConfigs,
            FederatedAuthenticatorConfig[] newFederatedAuthConfigs, int tenantId, String tenantDomain)
            throws IdentityProviderManagementException {
        String currentIdentityProviderEntityId = null;
        if (currentFederatedAuthConfigs != null) {
            for (FederatedAuthenticatorConfig fedAuthnConfig : currentFederatedAuthConfigs) {
                if (IdentityApplicationConstants.Authenticator.SAML2SSO.FED_AUTH_NAME.equals(fedAuthnConfig.getName())
                        || IdentityApplicationConstants.Authenticator.SAML2SSO.NAME.equals(fedAuthnConfig.getName())) {
                    Property[] properties = fedAuthnConfig.getProperties();
                    if (properties != null) {
                        for (Property property : properties) {
                            if (IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID
                                    .equals(property.getName())) {
                                currentIdentityProviderEntityId = property.getValue();
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }

        if (newFederatedAuthConfigs != null) {
            for (FederatedAuthenticatorConfig fedAuthnConfig : newFederatedAuthConfigs) {
                if (IdentityApplicationConstants.Authenticator.SAML2SSO.FED_AUTH_NAME.equals(fedAuthnConfig.getName())
                        || IdentityApplicationConstants.Authenticator.SAML2SSO.NAME.equals(fedAuthnConfig.getName())) {
                    Property[] properties = fedAuthnConfig.getProperties();
                    if (properties != null) {
                        for (Property property : properties) {
                            if (IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID.equals(property.
                                    getName())) {
                                if (currentIdentityProviderEntityId != null && currentIdentityProviderEntityId
                                        .equals(property.getValue())) {
                                    return true;
                                } else {
                                    if (dao.isIdPAvailableForAuthenticatorProperty(fedAuthnConfig.getName(),
                                            IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID,
                                            property.getValue(), tenantId)) {
                                        String msg = "An Identity Provider Entity Id has already been registered " +
                                                "with the name '" +
                                                property.getValue() + "' for tenant '" + tenantDomain + "'";
                                        throw new IdentityProviderManagementException(msg);
                                    }
                                    return true;
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }

        return true;
    }

    private String getOIDCResidentIdPEntityId() {
        String oidcEntityId = IdentityUtil.getProperty("OAuth.OpenIDConnect.IDTokenIssuerID");
        if (StringUtils.isBlank(oidcEntityId)) {
            oidcEntityId = "localhost";
        }
        return oidcEntityId;
    }

}
