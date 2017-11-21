/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.identity.integration.test.application.authz;

import org.apache.catalina.startup.Tomcat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceEntitlementException;
import org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO;
import org.wso2.carbon.identity.sso.saml.stub.IdentitySAMLSSOConfigServiceIdentityException;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.identity.integration.common.clients.application.mgt.ApplicationManagementServiceClient;
import org.wso2.identity.integration.common.clients.entitlement.EntitlementPolicyServiceClient;
import org.wso2.identity.integration.common.clients.sso.saml.SAMLSSOConfigServiceClient;
import org.wso2.identity.integration.common.clients.usermgt.remote.RemoteUserStoreManagerServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.utils.CommonConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract test class for application authorization based on XACML policy.
 */
public class AbstractApplicationAuthzTestCase extends ISIntegrationTest {

    // SAML Application attributes
    protected static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    protected static final String INBOUND_AUTH_TYPE = "samlsso";
    protected static final String ACS_URL = "http://localhost:" + CommonConstants.DEFAULT_TOMCAT_PORT + "/%s/home.jsp";
    protected static final String COMMON_AUTH_URL = "https://localhost:" + CommonConstants.IS_DEFAULT_HTTPS_PORT + "/commonauth";
    protected static final String SAML_SSO_LOGIN_URL = "http://localhost:" + CommonConstants.DEFAULT_TOMCAT_PORT + "/%s/samlsso?SAML2.HTTPBinding=%s";
    protected static final String NAMEID_FORMAT = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
    protected static final String LOGIN_URL = "/carbon/admin/login.jsp";
    private static final Log log = LogFactory.getLog(AbstractApplicationAuthzTestCase.class);

    protected ApplicationManagementServiceClient applicationManagementServiceClient;
    protected SAMLSSOConfigServiceClient ssoConfigServiceClient;
    protected RemoteUserStoreManagerServiceClient remoteUSMServiceClient;
    protected EntitlementPolicyServiceClient entitlementPolicyClient;

    protected HttpClient httpClientAzUser;
    protected HttpClient httpClientNonAzUser;
    protected Tomcat tomcatServer;

    protected HttpResponse sendSAMLMessage(String url, String samlMsgKey, String samlMsgValue) throws IOException {

        List<NameValuePair> urlParameters = new ArrayList<>();
        HttpPost post = new HttpPost(url);
        post.setHeader("User-Agent", USER_AGENT);
        urlParameters.add(new BasicNameValuePair(samlMsgKey, samlMsgValue));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return httpClientAzUser.execute(post);
    }

    protected String extractDataFromResponse(HttpResponse response) throws IOException {

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }

    protected void createApplication(String applicationName) throws Exception {

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(applicationName);
        serviceProvider.setDescription("This is a test Service Provider for AZ test");
        applicationManagementServiceClient.createApplication(serviceProvider);

        serviceProvider = applicationManagementServiceClient.getApplication(applicationName);

        InboundAuthenticationRequestConfig requestConfig = new InboundAuthenticationRequestConfig();
        requestConfig.setInboundAuthType(INBOUND_AUTH_TYPE);
        requestConfig.setInboundAuthKey(applicationName);


        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        inboundAuthenticationConfig.setInboundAuthenticationRequestConfigs(
                new InboundAuthenticationRequestConfig[]{requestConfig});

        serviceProvider.setInboundAuthenticationConfig(inboundAuthenticationConfig);

        LocalAndOutboundAuthenticationConfig outboundAuthConfig = new LocalAndOutboundAuthenticationConfig();
        outboundAuthConfig.setEnableAuthorization(true);
        serviceProvider.setLocalAndOutBoundAuthenticationConfig(outboundAuthConfig);
        applicationManagementServiceClient.updateApplicationData(serviceProvider);
    }

    protected void deleteApplication(String applicationName) throws Exception {

        applicationManagementServiceClient.deleteApplication(applicationName);
        ssoConfigServiceClient.removeServiceProvider(applicationName);
    }

    protected void createRole(String roleName) throws Exception {

        log.info("Creating role " + roleName);
        remoteUSMServiceClient.addRole(roleName, new String[0], null);
    }

    protected void deleteRole(String roleName) throws Exception {

        log.info("Deleting role " + roleName);
        remoteUSMServiceClient.deleteRole(roleName);
    }

    protected void createUser(String username, String password, String[] roles) throws Exception {

        log.info("Creating User " + username);
        remoteUSMServiceClient.addUser(username, password, roles, null, null, true);
    }

    protected void deleteUser(String username) throws Exception {

        log.info("Deleting User " + username);
        remoteUSMServiceClient.deleteUser(username);
    }

    protected void createSAMLApp(String applicationName, boolean singleLogout, boolean signResponse, boolean signAssertion)
            throws RemoteException, IdentitySAMLSSOConfigServiceIdentityException {

        SAMLSSOServiceProviderDTO samlssoServiceProviderDTO = new SAMLSSOServiceProviderDTO();
        samlssoServiceProviderDTO.setIssuer(applicationName);
        samlssoServiceProviderDTO.setAssertionConsumerUrls(new String[]{String.format(ACS_URL,
                applicationName)});
        samlssoServiceProviderDTO.setDefaultAssertionConsumerUrl(String.format(ACS_URL, applicationName));
        samlssoServiceProviderDTO.setNameIDFormat(NAMEID_FORMAT);
        samlssoServiceProviderDTO.setDoSingleLogout(singleLogout);
        samlssoServiceProviderDTO.setLoginPageURL(LOGIN_URL);
        samlssoServiceProviderDTO.setDoSignResponse(signResponse);
        samlssoServiceProviderDTO.setDoSignAssertions(signAssertion);
        ssoConfigServiceClient.addServiceProvider(samlssoServiceProviderDTO);
    }

    protected void setupXACMLPolicy(String policyId, String xacmlPolicy)
            throws InterruptedException, RemoteException, EntitlementPolicyAdminServiceEntitlementException {

        PolicyDTO policy = new PolicyDTO();
        policy.setPolicy(xacmlPolicy);
        policy.setPolicy(policy.getPolicy().replaceAll(">\\s+<", "><").trim());
        policy.setVersion("3.0");
        policy.setPolicyId(policyId);
        entitlementPolicyClient.addPolicy(policy);
        Thread.sleep(5000); // waiting for the policy to deploy
        entitlementPolicyClient
                .publishPolicies(new String[]{policyId}, new String[]{"PDP Subscriber"}, "CREATE", true, null, 1);
    }
}
