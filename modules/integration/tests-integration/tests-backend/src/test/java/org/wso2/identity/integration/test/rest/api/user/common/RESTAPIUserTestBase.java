/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.rest.api.user.common;

import java.rmi.RemoteException;
import java.util.Arrays;
import javax.xml.xpath.XPathExpressionException;
import org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.rest.api.common.RESTTestBase;

/**
 * Base Test Class for user based REST API test cases
 * ex: /t/{tenant-domain}/api/users/{version}
 */
public class RESTAPIUserTestBase extends RESTTestBase {

    protected static final String API_USERS_BASE_PATH = "/api/users/%s";
    protected static final String API_USERS_BASE_PATH_IN_SWAGGER = "/t/\\{tenant-domain\\}" + API_USERS_BASE_PATH;
    private static final String ADMIN = "admin";
    private IdentityProviderMgtServiceClient superTenantIDPMgtClient;
    private IdentityProviderMgtServiceClient tenantIDPMgtClient;
    private AuthenticatorClient authenticatorClient;
    private IdentityProvider superTenantResidentIDP;

    protected void testInit(String apiVersion, String apiDefinition, String tenantDomain)
            throws XPathExpressionException, RemoteException {

        String basePathInSwagger = String.format(API_USERS_BASE_PATH_IN_SWAGGER, apiVersion);
        String basePath = ISIntegrationTest.getTenantedRelativePath(String.format(API_USERS_BASE_PATH, apiVersion),
                tenantDomain);
        super.init(apiDefinition, basePathInSwagger, basePath);
    }

    /**
     * @param apiVersion                       api version
     * @param apiDefinition                    swagger definition of api
     * @param tenantDomain                     tenant
     * @param apiUserBasePathInSwagger         base path of endpoint in swagger
     * @param apiUserBasePathWithTenantContext base path of endpoint with tenant context
     * @throws XPathExpressionException
     * @throws RemoteException
     */
    protected void testInit(String apiVersion, String apiDefinition, String tenantDomain,
                            String apiUserBasePathInSwagger, String apiUserBasePathWithTenantContext)
            throws XPathExpressionException, RemoteException {

        String basePathInSwagger = String.format(apiUserBasePathInSwagger, apiVersion);
        String basePath = ISIntegrationTest.getTenantedRelativePath(String.format(apiUserBasePathWithTenantContext,
                apiVersion), tenantDomain);
        super.init(apiDefinition, basePathInSwagger, basePath);
    }

    protected void initUpdateIDPProperty() throws Exception {

        this.authenticatorClient = new AuthenticatorClient(backendURL);
        String tenantCookie = this.authenticatorClient.login(ADMIN, ADMIN, isServer.getInstance()
                .getHosts().get("default"));
        superTenantIDPMgtClient = new IdentityProviderMgtServiceClient(sessionCookie, backendURL);
        tenantIDPMgtClient = new IdentityProviderMgtServiceClient(tenantCookie, backendURL);
        superTenantResidentIDP = superTenantIDPMgtClient.getResidentIdP();
    }

    protected void updateResidentIDPProperty(String propertyKey, String value, boolean isSuperTenant) throws Exception {

        IdentityProviderProperty[] idpProperties = superTenantResidentIDP.getIdpProperties();
        for (IdentityProviderProperty providerProperty : idpProperties) {
            if (propertyKey.equalsIgnoreCase(providerProperty.getName())) {
                providerProperty.setValue(value);
            }
        }
        updateResidentIDP(superTenantResidentIDP, isSuperTenant);
    }

    private void updateResidentIDP(IdentityProvider residentIdentityProvider, boolean isSuperTenant) throws Exception {

        FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs =
                residentIdentityProvider.getFederatedAuthenticatorConfigs();
        federatedAuthenticatorConfigs = Arrays.stream(federatedAuthenticatorConfigs)
                .filter(config -> config.getName().equalsIgnoreCase("samlsso"))
                .toArray(FederatedAuthenticatorConfig[]::new);
        residentIdentityProvider.setFederatedAuthenticatorConfigs(federatedAuthenticatorConfigs);
        if (isSuperTenant) {
            superTenantIDPMgtClient.updateResidentIdP(residentIdentityProvider);
        } else {
            tenantIDPMgtClient.updateResidentIdP(residentIdentityProvider);
        }
    }
}
