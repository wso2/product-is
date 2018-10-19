/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.common.clients.application.mgt;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.xsd.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.xsd.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.xsd.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.xsd.SpFileContent;
import org.wso2.carbon.identity.application.common.model.xsd.SpTemplate;
import org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementClientException;
import org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException;
import org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceStub;
import org.wso2.carbon.integration.common.admin.client.utils.AuthenticateStubUtil;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class ApplicationManagementServiceClient {

    IdentityApplicationManagementServiceStub stub;
    private UserAdminStub userAdminStub;

    Log log = LogFactory.getLog(ApplicationManagementServiceClient.class);
    boolean debugEnabled = log.isErrorEnabled();

    /**
     *
     * @param cookie
     * @param backendServerURL
     * @param configCtx
     * @throws org.apache.axis2.AxisFault
     */
    public ApplicationManagementServiceClient(String cookie, String backendServerURL,
                                              ConfigurationContext configCtx) throws AxisFault {

        String serviceURL = backendServerURL + "IdentityApplicationManagementService";
        String userAdminServiceURL = backendServerURL + "UserAdmin";
        stub = new IdentityApplicationManagementServiceStub(configCtx, serviceURL);
        userAdminStub = new UserAdminStub(configCtx, userAdminServiceURL);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        ServiceClient userAdminClient = userAdminStub._getServiceClient();
        Options userAdminOptions = userAdminClient.getOptions();
        userAdminOptions.setManageSession(true);
        userAdminOptions.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                cookie);

        if (debugEnabled) {
            log.debug("Invoking service " + serviceURL);
        }

    }

    /**
     *
     * @param username
     * @param password
     * @param backendServerURL
     * @param configCtx
     * @throws org.apache.axis2.AxisFault
     */
    public ApplicationManagementServiceClient(String username, String password, String backendServerURL,
                                              ConfigurationContext configCtx) throws AxisFault {

        String serviceURL = backendServerURL + "IdentityApplicationManagementService";
        String userAdminServiceURL = backendServerURL + "UserAdmin";
        stub = new IdentityApplicationManagementServiceStub(configCtx, serviceURL);
        userAdminStub = new UserAdminStub(configCtx, userAdminServiceURL);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        AuthenticateStubUtil.authenticateStub(username, password, stub);

        ServiceClient userAdminClient = userAdminStub._getServiceClient();
        Options userAdminOptions = userAdminClient.getOptions();
        userAdminOptions.setManageSession(true);
        AuthenticateStubUtil.authenticateStub(username, password, userAdminStub);
    }

    /**
     *
     * @param serviceProvider
     * @throws Exception
     */
    public void createApplication(ServiceProvider serviceProvider) throws Exception {
        try {
            if (debugEnabled) {
                log.debug("Registering Service Provider " + serviceProvider.getApplicationName());
            }
            stub.createApplication(serviceProvider);
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        } catch (IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            log.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        }

    }

    /**
     *
     * @param serviceProvider
     * @throws Exception
     */
    public void createApplicationWithTemplate(ServiceProvider serviceProvider, String templateName) throws Exception {
        try {
            if (debugEnabled) {
                log.debug("Registering Service Provider " + serviceProvider.getApplicationName() + " with template " +
                templateName);
            }
            stub.createApplicationWithTemplate(serviceProvider, templateName);
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        } catch (IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            log.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        }

    }

    /**
     *
     * @param applicationName
     * @return
     * @throws Exception
     */
    public ServiceProvider getApplication(String applicationName) throws Exception {
        try {
            if (debugEnabled) {
                log.debug("Loading Service Provider " + applicationName);
            }
            return stub.getApplication(applicationName);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        }

    }

    /**
     *
     * @return
     * @throws Exception
     */
    public ApplicationBasicInfo[] getAllApplicationBasicInfo() throws Exception {
        try {
            return stub.getAllApplicationBasicInfo();
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        } catch (IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            log.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        }
    }

    /**
     *
     * @param serviceProvider
     * @throws Exception
     */
    public void updateApplicationData(ServiceProvider serviceProvider) throws Exception {
        try {
            stub.updateApplication(serviceProvider);
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        } catch (IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            log.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        }
    }

    /**
     *
     * @param applicationID
     * @throws Exception
     */
    public void deleteApplication(String applicationID) throws Exception {
        try {
            stub.deleteApplication(applicationID);
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        } catch (IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            log.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        }

    }

    /**
     *
     * @param identityProviderName
     * @throws Exception
     */
    public IdentityProvider getFederatedIdentityProvider(String identityProviderName)
            throws Exception {
        return stub.getIdentityProvider(identityProviderName);
    }

    /**
     *
     * @return
     * @throws Exception
     */
    public RequestPathAuthenticatorConfig[] getAllRequestPathAuthenticators() throws Exception {
        return stub.getAllRequestPathAuthenticators();
    }

    /**
     *
     * @return
     * @throws Exception
     */
    public LocalAuthenticatorConfig[] getAllLocalAuthenticators() throws Exception {
        return stub.getAllLocalAuthenticators();
    }

    /**
     *
     * @return
     * @throws Exception
     */
    public IdentityProvider[] getAllFederatedIdentityProvider() throws Exception {
        IdentityProvider[] idps = null;

        try {
            idps = stub.getAllIdentityProviders();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return idps;
    }

    /**
     *
     * @return
     * @throws Exception
     */
    public String[] getAllClaimUris() throws Exception {
        return stub.getAllLocalClaimUris();
    }

    /**
     * Get User Store Domains
     * @return
     * @throws Exception
     */
    public String[] getUserStoreDomains() throws Exception {

        try {
            List<String> readWriteDomainNames = new ArrayList<String>();
            UserStoreInfo[] storesInfo = userAdminStub.getUserRealmInfo().getUserStoresInfo();
            for(UserStoreInfo storeInfo : storesInfo){
                if(!storeInfo.getReadOnly()){
                    readWriteDomainNames.add(storeInfo.getDomainName());
                }
            }
            return readWriteDomainNames.toArray(new String[readWriteDomainNames.size()]);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new Exception(
                    "Error occurred while retrieving Read-Write User Store Domain IDs for logged-in user's tenant realm");
        }
    }

    /**
     * Import service provider from spFile.
     *
     * @param serviceProviderContent
     * @param fileName
     * @throws Exception
     */
    public void importApplication(String serviceProviderContent, String fileName) throws Exception {

        try {
            if (debugEnabled) {
                log.debug("Importing Service Provider ");
            }
            SpFileContent spFileContent = new SpFileContent();
            spFileContent.setFileName(fileName);
            spFileContent.setContent(serviceProviderContent);
            stub.importApplication(spFileContent);
        } catch (RemoteException | IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            log.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        }

    }

    /**
     * Export service provider to spFile.
     *
     * @param spName
     * @param exportSecret
     * @return
     * @throws Exception
     */
    public String exportApplication(String spName, boolean exportSecret) throws Exception {

        try {
            if (debugEnabled) {
                log.debug("Exporting Service Provider " + spName);
            }
            return stub.exportApplication(spName, exportSecret);
        } catch (RemoteException | IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            log.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        }

    }

    /**
     * Create an application template.
     *
     * @param spTemplate service provider template info
     * @throws IdentityApplicationManagementServiceIdentityApplicationManagementClientException
     */
    public void createApplicationTemplate(SpTemplate spTemplate)
            throws IdentityApplicationManagementServiceIdentityApplicationManagementClientException {

        try {
            if (debugEnabled) {
                log.debug("Registering Service Provider template: " + spTemplate.getName());
            }
            stub.createApplicationTemplate(spTemplate);
        } catch (RemoteException e) {
            handleException(e, "Error occurred when creating Service Provider template: " + spTemplate.getName());
        }
    }

    /**
     * Add configured service provider as a template.
     *
     * @param serviceProvider Service provider to be configured as a template
     * @param spTemplate service provider template basic info
     * @throws IdentityApplicationManagementServiceIdentityApplicationManagementClientException
     */
    public void createApplicationTemplateFromSP(ServiceProvider serviceProvider, SpTemplate spTemplate)
            throws IdentityApplicationManagementServiceIdentityApplicationManagementClientException {

        try {
            if (debugEnabled) {
                log.debug("Adding Service Provider:" + serviceProvider.getApplicationName() + " as a template " +
                        "with name: " + spTemplate.getName());
            }
            stub.createApplicationTemplateFromSP(serviceProvider, spTemplate);
        } catch (RemoteException e) {
            handleException(e, "Error occurred when creating Service Provider template: " + spTemplate.getName() +
                    " from service provider: " + serviceProvider.getApplicationName());
        }
    }

    /**
     * Get Service provider template.
     *
     * @param templateName template name
     * @return service provider template info
     * @throws IdentityApplicationManagementServiceIdentityApplicationManagementClientException
     */
    public SpTemplate getApplicationTemplate(String templateName)
            throws IdentityApplicationManagementServiceIdentityApplicationManagementClientException {

        try {
            if (debugEnabled) {
                log.debug("Retrieving Service Provider template: " + templateName);
            }
            return stub.getApplicationTemplate(templateName);
        } catch (RemoteException e) {
            handleException(e, "Error occurred when retrieving Service Provider template: " + templateName);
        }
        return null;
    }

    /**
     * Delete an application template.
     *
     * @param templateName name of the template
     * @throws IdentityApplicationManagementServiceIdentityApplicationManagementClientException
     */
    public void deleteApplicationTemplate(String templateName)
            throws IdentityApplicationManagementServiceIdentityApplicationManagementClientException {

        try {
            if (debugEnabled) {
                log.debug("Deleting Service Provider template: " + templateName);
            }
            stub.deleteApplicationTemplate(templateName);
        } catch (RemoteException e) {
            handleException(e, "Error occurred when deleting Service Provider template: " + templateName);
        }
    }

    /**
     * Update an application template.
     *
     * @param spTemplate SP template info to be updated
     * @throws IdentityApplicationManagementServiceIdentityApplicationManagementClientException
     */
    public void updateApplicationTemplate(String templateName, SpTemplate spTemplate)
            throws IdentityApplicationManagementServiceIdentityApplicationManagementClientException {

        try {
            if (debugEnabled) {
                log.debug("Updating Service Provider template: " + templateName);
            }
            stub.updateApplicationTemplate(templateName, spTemplate);
        } catch (RemoteException e) {
            handleException(e, "Error occurred when updating Service Provider template: " + templateName);
        }
    }

    /**
     * Check existence of a application template.
     *
     * @param templateName template name
     * @return true if a template with the specified name exists
     * @throws IdentityApplicationManagementServiceIdentityApplicationManagementClientException
     */
    public boolean isExistingApplicationTemplate(String templateName)
            throws IdentityApplicationManagementServiceIdentityApplicationManagementClientException {

        try {
            if (debugEnabled) {
                log.debug("Checking existence of application template: " + templateName);
            }
            return stub.isExistingApplicationTemplate(templateName);
        } catch (RemoteException e) {
            handleException(e, "Error occurred when checking existence of Service Provider template: " + templateName);
        }
        return false;
    }

    /**
     * Get basic info of all the service provider templates.
     *
     * @return Array of all application templates
     * @throws IdentityApplicationManagementServiceIdentityApplicationManagementClientException
     */
    public SpTemplate[] getAllApplicationTemplateInfo()
            throws IdentityApplicationManagementServiceIdentityApplicationManagementClientException {

        try {
            if (debugEnabled) {
                log.debug("Get all service provider template basic info.");
            }
            return stub.getAllApplicationTemplateInfo();
        } catch (RemoteException e) {
            handleException(e, "Error occurred when retrieving service provider template basic info");
        }
        return new SpTemplate[0];
    }

    private void handleException(RemoteException e, String msg)
            throws IdentityApplicationManagementServiceIdentityApplicationManagementClientException {
        log.error(msg, e);
        throw new IdentityApplicationManagementServiceIdentityApplicationManagementClientException(
                "Server error occurred.");
    }

    private void handleException(Exception e) throws Exception {
        String errorMessage = "Unknown error occurred.";

        if (e instanceof IdentityApplicationManagementServiceIdentityApplicationManagementException) {
            IdentityApplicationManagementServiceIdentityApplicationManagementException exception =
                    (IdentityApplicationManagementServiceIdentityApplicationManagementException) e;
            if (exception.getFaultMessage().getIdentityApplicationManagementException() != null) {
                errorMessage = exception.getFaultMessage().getIdentityApplicationManagementException().getMessage();
            }
        } else {
            errorMessage = e.getMessage();
        }

        throw new Exception(errorMessage, e);
    }

}
