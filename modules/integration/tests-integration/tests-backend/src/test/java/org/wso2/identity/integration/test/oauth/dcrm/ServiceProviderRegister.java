/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.identity.integration.test.oauth.dcrm;

import org.wso2.carbon.identity.application.common.model.xsd.*;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.oauth.OauthAdminClient;
import org.wso2.identity.integration.test.oauth.dcrm.bean.ServiceProviderDataHolder;
import org.wso2.identity.integration.test.oauth.dcrm.util.OAuthDCRMConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Register a new OAuth service provider
 */
public class ServiceProviderRegister {
    public ServiceProviderDataHolder register (ApplicationManagementServiceClient appMgtService,
                                               OauthAdminClient adminClient) throws Exception {

        ServiceProviderDataHolder serviceProviderDataHolder = new ServiceProviderDataHolder();

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(OAuthDCRMConstants.APPLICATION_NAME);
        appMgtService.createApplication(serviceProvider);

        OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
        appDTO.setApplicationName(OAuthDCRMConstants.APPLICATION_NAME);
        appDTO.setGrantTypes(OAuthDCRMConstants.GRANT_TYPE_IMPLICIT);
        appDTO.setCallbackUrl(OAuthDCRMConstants.REDIRECT_URI);
        appDTO.setOAuthVersion(OAuthDCRMConstants.OAUTH_VERSION);

        adminClient.registerOAuthApplicationData(appDTO);

        OAuthConsumerAppDTO[] appDtos = adminClient.getAllOAuthApplicationData();
        String consumerKey = null;
        String consumerSecret = null;
        for (OAuthConsumerAppDTO appDto : appDtos) {
            if (appDto.getApplicationName().equals(OAuthDCRMConstants.APPLICATION_NAME)) {
                consumerKey = appDto.getOauthConsumerKey();
                consumerSecret = appDto.getOauthConsumerSecret();
            }
        }

        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        List<InboundAuthenticationRequestConfig> inboundAuthenticationRequestConfigs =
                new ArrayList<>();

        InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig =
                new InboundAuthenticationRequestConfig();
        if (consumerKey != null) {
            inboundAuthenticationRequestConfig.setInboundAuthKey(consumerKey);
            inboundAuthenticationRequestConfig.setInboundAuthType("oauth2");
            if (consumerSecret != null && !consumerSecret.isEmpty()) {
                Property property = new Property();
                property.setName("oauthConsumerSecret");
                property.setValue(consumerSecret);
                Property[] properties = { property };
                inboundAuthenticationRequestConfig.setProperties(properties);
            }
        }

        ServiceProvider createdServiceProvider = appMgtService
                .getApplication(OAuthDCRMConstants.APPLICATION_NAME);
        inboundAuthenticationRequestConfigs.add(inboundAuthenticationRequestConfig);

        inboundAuthenticationConfig.setInboundAuthenticationRequestConfigs(inboundAuthenticationRequestConfigs
                .toArray(new InboundAuthenticationRequestConfig[inboundAuthenticationRequestConfigs.size()]));
        createdServiceProvider.setInboundAuthenticationConfig(inboundAuthenticationConfig);
        appMgtService.updateApplicationData(createdServiceProvider);

        serviceProviderDataHolder.setClientName(appDTO.getApplicationName());
        serviceProviderDataHolder.setClientID(consumerKey);
        serviceProviderDataHolder.setClientSecret(consumerSecret);
        serviceProviderDataHolder.addGrantType(appDTO.getGrantTypes());
        serviceProviderDataHolder.addRedirectUri(appDTO.getCallbackUrl());

        return serviceProviderDataHolder;
    }
}
