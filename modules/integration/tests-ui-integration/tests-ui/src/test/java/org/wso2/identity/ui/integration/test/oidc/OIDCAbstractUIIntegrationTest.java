/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.ui.integration.test.oidc;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.WebDriver;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.identity.application.common.model.xsd.Claim;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.OutboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.oauth.OauthAdminClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationUITest;
import org.wso2.identity.ui.integration.test.utils.OIDCUITestConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This class defines basic functionality needed to initiate an OIDC UI test
 */
public class OIDCAbstractUIIntegrationTest extends ISIntegrationUITest {

    private static final Log log = LogFactory.getLog(OIDCAbstractUIIntegrationTest.class);

    private static final int TOMCAT_PORT = 8490;

    protected WebDriver driver;
    protected Tomcat tomcat;
    protected ApplicationManagementServiceClient applicationManagementServiceClient;
    protected RemoteUserStoreManagerServiceClient remoteUserStoreManagerServiceClient;
    protected OauthAdminClient oauthAdminClient;

    @Override
    protected void init(TestUserMode userMode) throws Exception {

        super.init(userMode);
        initTest();
    }

    @Override
    protected void init(String instance, String domainKey, String userKey) throws Exception {

        super.init(instance, domainKey, userKey);
        initTest();
    }

    /**
     * Starts a tomcat server instance
     *
     * @throws LifecycleException
     */
    public void startTomcat() throws LifecycleException {

        tomcat = new Tomcat();
        tomcat.getService().setContainer(tomcat.getEngine());
        tomcat.setPort(TOMCAT_PORT);
        tomcat.setBaseDir("");

        StandardHost stdHost = (StandardHost) tomcat.getHost();

        stdHost.setAppBase("");
        stdHost.setAutoDeploy(true);
        stdHost.setDeployOnStartup(true);
        stdHost.setUnpackWARs(true);
        tomcat.setHost(stdHost);

        tomcat.start();
    }

    /**
     * Stops tomcat server instance
     *
     * @throws LifecycleException
     */
    public void stopTomcat() throws LifecycleException {

        tomcat.stop();
        tomcat.destroy();
    }

    /**
     * Creates a user
     *
     * @param user user instance
     * @throws Exception
     */
    public void createUser(OIDCUser user) throws Exception {

        log.info("Creating User " + user.getUsername());

        ClaimValue[] claims = null;
        if (MapUtils.isNotEmpty(user.getUserClaims())) {
            claims = new ClaimValue[user.getUserClaims().size()];

            int i = 0;
            for (Map.Entry<String, String> entry : user.getUserClaims().entrySet()) {
                ClaimValue claimValue = new ClaimValue();
                claimValue.setClaimURI(entry.getKey());
                claimValue.setValue(entry.getValue());
                claims[i++] = claimValue;
            }
        }

        String[] roles = null;
        if (!user.getRoles().isEmpty()) {
            roles = new String[user.getRoles().size()];
            roles = user.getRoles().toArray(roles);
        }

        // creating the user
        remoteUserStoreManagerServiceClient.addUser(user.getUsername(), user.getPassword(), roles, claims,
                                                    user.getProfile(), true);
    }

    /**
     * Deletes a user
     *
     * @param user user instance
     * @throws Exception
     */
    public void deleteUser(OIDCUser user) throws Exception {

        log.info("Deleting User " + user.getUsername());
        remoteUserStoreManagerServiceClient.deleteUser(user.getUsername());
    }

    /**
     * Register an OIDC application in OP
     *
     * @param application application instance
     * @throws Exception
     */
    public void createApplication(OIDCApplication application) throws Exception {

        log.info("Creating application " + application.getApplicationName());

        OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
        appDTO.setApplicationName(application.getApplicationName());
        appDTO.setCallbackUrl(application.getCallBackURL());
        appDTO.setOAuthVersion(OIDCUITestConstants.OAUTH_VERSION_2);
        appDTO.setGrantTypes("authorization_code implicit password client_credentials refresh_token " +
                             "urn:ietf:params:oauth:grant-type:saml2-bearer iwa:ntlm");

        oauthAdminClient.registerOAuthApplicationData(appDTO);
        OAuthConsumerAppDTO[] appDtos = oauthAdminClient.getAllOAuthApplicationData();

        for (OAuthConsumerAppDTO appDto : appDtos) {
            if (appDto.getApplicationName().equals(application.getApplicationName())) {
                application.setClientId(appDto.getOauthConsumerKey());
                application.setClientSecret(appDto.getOauthConsumerSecret());
            }
        }

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(application.getApplicationName());
        serviceProvider.setDescription(application.getApplicationName());
        applicationManagementServiceClient.createApplication(serviceProvider);

        serviceProvider = applicationManagementServiceClient.getApplication(application.getApplicationName());

        ClaimConfig claimConfig = null;
        if (!application.getRequiredClaims().isEmpty()) {
            claimConfig = new ClaimConfig();
            for (String claimUri : application.getRequiredClaims()) {
                Claim claim = new Claim();
                claim.setClaimUri(claimUri);
                ClaimMapping claimMapping = new ClaimMapping();
                claimMapping.setRequested(true);
                claimMapping.setLocalClaim(claim);
                claimMapping.setRemoteClaim(claim);
                claimConfig.addClaimMappings(claimMapping);
            }
        }

        serviceProvider.setClaimConfig(claimConfig);
        serviceProvider.setOutboundProvisioningConfig(new OutboundProvisioningConfig());
        List<InboundAuthenticationRequestConfig> authRequestList = new ArrayList<>();

        if (application.getClientId() != null) {
            InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig =
                    new InboundAuthenticationRequestConfig();
            inboundAuthenticationRequestConfig.setInboundAuthKey(application.getClientId());
            inboundAuthenticationRequestConfig.setInboundAuthType(OIDCUITestConstants.OAUTH_2);
            if (StringUtils.isNotBlank(application.getClientSecret())) {
                Property property = new Property();
                property.setName(OIDCUITestConstants.OAUTH_CONSUMER_SECRET);
                property.setValue(application.getClientSecret());
                Property[] properties = { property };
                inboundAuthenticationRequestConfig.setProperties(properties);
            }
            authRequestList.add(inboundAuthenticationRequestConfig);
        }

        if (authRequestList.size() > 0) {
            serviceProvider.getInboundAuthenticationConfig().setInboundAuthenticationRequestConfigs(
                    authRequestList.toArray(new InboundAuthenticationRequestConfig[authRequestList.size()]));
        }

        applicationManagementServiceClient.updateApplicationData(serviceProvider);
    }

    /**
     * Deletes the registered OIDC application in OP
     *
     * @param application application instance
     * @throws Exception
     */
    public void deleteApplication(OIDCApplication application) throws Exception {

        log.info("Deleting application " + application.getApplicationName());
        applicationManagementServiceClient.deleteApplication(application.getApplicationName());
    }

    private void initTest() throws Exception {

        setSystemproperties();

        applicationManagementServiceClient = new ApplicationManagementServiceClient(sessionCookie, backendURL, null);
        remoteUserStoreManagerServiceClient = new RemoteUserStoreManagerServiceClient(backendURL, sessionCookie);
        oauthAdminClient = new OauthAdminClient(backendURL, sessionCookie);

        driver = BrowserManager.getWebDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

}
