/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.identity.integration.test.rest.api.server.branding.preference.management.v1;

import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationSharePOSTRequest;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.InboundProtocols;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OpenIDConnectConfiguration;
import org.wso2.identity.integration.test.restclients.OAuth2RestClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base test class for the Application Branding Preference Management Rest APIs.
 */
public class AppBrandingPreferenceManagementTestBase extends BrandingPreferenceManagementTestBase{

    public static final String APPLICATION_TYPE = "APP";
    public static final String ADD_ROOT_APP_BRANDING_RESOURCE_FILE = "add-root-app-branding-preference.json";
    public static final String UPDATE_ROOT_APP_BRANDING_RESOURCE_FILE = "update-root-app-branding-preference.json";
    public static final String ADD_L1_APP_BRANDING_RESOURCE_FILE = "add-l1-app-branding-preference.json";
    public static final String UPDATE_L1_APP_BRANDING_RESOURCE_FILE = "update-l1-app-branding-preference.json";
    public static final String ADD_L2_APP_BRANDING_RESOURCE_FILE = "add-l2-app-branding-preference.json";
    public static final String UPDATE_L2_APP_BRANDING_RESOURCE_FILE = "update-l2-app-branding-preference.json";
    public static final String APP_ID_PLACEHOLDER = "app-id";
    public static final String TEST_APP_NAME = "test-app";

    protected OAuth2RestClient oAuth2RestClient;

    protected String createTestApp() throws Exception {

        List<String> grantTypes = new ArrayList<>();
        Collections.addAll(grantTypes, "authorization_code");

        OpenIDConnectConfiguration oidcConfig = new OpenIDConnectConfiguration();
        oidcConfig.setGrantTypes(grantTypes);
        oidcConfig.setCallbackURLs(Collections.singletonList("http://localhost:8490/playground2/oauth2client"));

        InboundProtocols inboundProtocolsConfig = new InboundProtocols();
        inboundProtocolsConfig.setOidc(oidcConfig);

        ApplicationModel application = new ApplicationModel();
        application.setInboundProtocolConfiguration(inboundProtocolsConfig);
        application.setName(TEST_APP_NAME);

        return oAuth2RestClient.createApplication(application);
    }

    protected void shareAppWithAllChildren(String appId) throws IOException, InterruptedException {

        ApplicationSharePOSTRequest appSharePOSTRequest = new ApplicationSharePOSTRequest();
        appSharePOSTRequest.setShareWithAllChildren(true);
        oAuth2RestClient.shareApplication(appId, appSharePOSTRequest);

        // Since application sharing is an async operation, wait for some time for it to finish.
        Thread.sleep(5000);
    }

    protected void deleteTestApp(String appId) throws Exception {

        oAuth2RestClient.deleteApplication(appId);
    }
}
