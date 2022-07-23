/*
* Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.identity.integration.test.idp.mgt;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProviderProperty;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.JustInTimeProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.Property;
import org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdentityProviderMgtServiceTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(IdentityProviderMgtServiceTestCase.class);
    private IdentityProviderMgtServiceClient idpMgtServiceClient;
    private String sampleCertificate;
    private String testIdpName = "TestIDPProvider";
    private String testIdpNameSearch = "SearchTestIDPProviderTest";
    private String updatedTestIdpName = "UpdatedTestIDPProvider";
    private String testFedAuthName = "OpenIDAuthenticator";

    //Resident idp default values
    private boolean residentIdpEnable;
    private boolean residentIdpPrimary;
    private String residentIdpName;
    private String residentIDPDefaultRealm;
    private IdentityProviderProperty[] idpProperties;

    private String defaultSamlSSOEntityID = "localhost";
    private final String SAML2SSO_NAME = "samlsso";
    private final String SAML2SSO_IDP_ENTITY_ID = "IdPEntityId";

    private static final String RANDOM_PASSWORD_GENERATED = "random-password-generated";
    private static final String invalidPageNumberErrorMessage = "Error while getting the Identity Provider: " +
            "Invalid page number requested. The page number should be a value greater than 0.";
    private static final String invalidFilterErrorMessage = "Error while getting the Identity Provider: Error " +
            "occurred while validate filter, filter: names sp \"Search\" and namees ew \"Test\".";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        super.init();
        ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        idpMgtServiceClient = new IdentityProviderMgtServiceClient(sessionCookie, backendURL, configContext);
    }


    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        // Delete the Idp tested.
        idpMgtServiceClient.deleteIdP(testIdpNameSearch);

        //Restore default values for changes made to resident IDP
        IdentityProvider residentProvider = idpMgtServiceClient.getResidentIdP();

        Assert.assertNotNull(residentProvider, "Resident idp retrieval failed to restoring");

        //restore resident identity provider
        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setEnable(residentIdpEnable);
        identityProvider.setPrimary(residentIdpPrimary);
        identityProvider.setIdentityProviderName(residentIdpName);
        identityProvider.setHomeRealmId(residentIDPDefaultRealm);
        identityProvider.setIdpProperties(idpProperties);

        FederatedAuthenticatorConfig samlFedAuthn = new FederatedAuthenticatorConfig();
        samlFedAuthn.setName(SAML2SSO_NAME);

        Property[] properties = new Property[1];
        Property property = new Property();
        property.setName(SAML2SSO_IDP_ENTITY_ID);
        property.setValue(defaultSamlSSOEntityID);
        properties[0] = property;

        samlFedAuthn.setProperties(properties);
        FederatedAuthenticatorConfig[] federatedAuthenticators = new FederatedAuthenticatorConfig[1];
        federatedAuthenticators[0] = samlFedAuthn;
        identityProvider.setFederatedAuthenticatorConfigs(federatedAuthenticators);
        idpMgtServiceClient.updateResidentIdP(identityProvider);

        log.info("resident idp restored");
    }


    @Test(priority = 1, groups = "wso2.is", description = "Test getResidentIdP operation")
    public void testGetResidentIdP() throws Exception {
        log.info("Retrieving resident identity provide");
        IdentityProvider idProvider = idpMgtServiceClient.getResidentIdP();

        Assert.assertNotNull(idProvider, "Resident identity provider retrieval failed");

        sampleCertificate = idProvider.getCertificate();

        //Extract authenticator configurations
        FederatedAuthenticatorConfig[] authConfigs = idProvider.getFederatedAuthenticatorConfigs();

        log.info("Authenticator configs : " + authConfigs.length);

        //Extract provisioning configurations
        ProvisioningConnectorConfig[] provisioningConfigs = idProvider.getProvisioningConnectorConfigs();

        log.info("Provisioning configs : " + provisioningConfigs.length);

        //check default identity provider name
        Assert.assertEquals(idProvider.getIdentityProviderName(), "LOCAL", "Default resident identity provider name changed");

        //check Default number of authentication configurations
        Map<String, FederatedAuthenticatorConfig> fedAuthConfigMap = new HashMap<String, FederatedAuthenticatorConfig>();
        for (FederatedAuthenticatorConfig config : authConfigs) {
            fedAuthConfigMap.put(config.getName(), config);
        }

        Assert.assertEquals(fedAuthConfigMap.containsKey("openidconnect"), true, "Default auth config not found");
        Assert.assertEquals(fedAuthConfigMap.containsKey("samlsso"), true, "Default auth config not found");
        Assert.assertEquals(fedAuthConfigMap.containsKey("openid"), true, "Default auth config not found");
        Assert.assertEquals(fedAuthConfigMap.containsKey("passivests"), true, "Default auth config not found");

        //check Default number of provisioning configurations
        Map<String, ProvisioningConnectorConfig> provisioningConfigMap = new HashMap<String, ProvisioningConnectorConfig>();
        for (ProvisioningConnectorConfig config : provisioningConfigs) {
            provisioningConfigMap.put(config.getName(), config);
        }

        Assert.assertEquals(provisioningConfigMap.containsKey("scim"), true, "Default provisioning config not found");
    }


    @Test(priority = 2, groups = "wso2.is", description = "Test addIdp operation")
    public void testAddIdp() throws Exception {
        String testIdpDescription = "This is test identity provider";
        String testIdpRealmId = "localhost";
        String testFedAuthDispName = "openid";

        String testFedAuthPropName = "OpenIdUrl";
        String testFedAuthPropValue = "https://testDomain:9853/openid";
        String testFedAuthPropName2 = "IsUserIdInClaims";
        String testFedAuthPropValue2 = "false";
        String testFedAuthPropName3 = "RealmId";
        String testFedAuthPropValue3 = "localhost";

        String testProvisionConfName = "scim";

        String testProvisionPropName = "scim-user-ep";
        String testProvisionPropDisplayName = "userEndPoint";
        String testProvisionPropValue = "https://localhost:9853/testProvisionLink";

        String testProvisionPropName2 = "scim-username";
        String testProvisionPropDisplayName2 = "userName";
        String testProvisionPropValue2 = "admin";

        String testProvisionPropName3 = "scim-password";
        String testProvisionPropDisplayName3 = "userPassword";
        String testProvisionPropValue3 = "admin";

        IdentityProvider idProvider = new IdentityProvider();
        FederatedAuthenticatorConfig[] fedAuthConfigs = new FederatedAuthenticatorConfig[1];

        //set idp information
        idProvider.setHomeRealmId(testIdpRealmId);
        idProvider.setEnable(true);
        idProvider.setIdentityProviderDescription(testIdpDescription);
        idProvider.setIdentityProviderName(testIdpName);
        idProvider.setCertificate(sampleCertificate);
        idProvider.setFederationHub(false);
        idProvider.setPrimary(false);

        //Add federated authentication configuration
        FederatedAuthenticatorConfig authConfig = new FederatedAuthenticatorConfig();

        authConfig.setDisplayName(testFedAuthDispName);
        authConfig.setEnabled(true);
        authConfig.setName(testFedAuthName);
        //set properties
        //property 1
        Property fedProp = new Property();
        fedProp.setName(testFedAuthPropName);
        fedProp.setValue(testFedAuthPropValue);
        //property 2
        Property fedProp2 = new Property();
        fedProp2.setName(testFedAuthPropName2);
        fedProp2.setValue(testFedAuthPropValue2);
        //property 3
        Property fedProp3 = new Property();
        fedProp3.setName(testFedAuthPropName3);
        fedProp3.setValue(testFedAuthPropValue3);

        Property[] props = new Property[3];
        props[0] = fedProp;
        props[1] = fedProp2;
        props[2] = fedProp3;

        authConfig.setProperties(props);
        fedAuthConfigs[0] = authConfig;
        idProvider.setFederatedAuthenticatorConfigs(fedAuthConfigs);

        //Set JIT config
        JustInTimeProvisioningConfig jitConfig = new JustInTimeProvisioningConfig();
        jitConfig.setProvisioningEnabled(true);
        idProvider.setJustInTimeProvisioningConfig(jitConfig);

        ProvisioningConnectorConfig provisioningConfig = new ProvisioningConnectorConfig();
        provisioningConfig.setName(testProvisionConfName);
        provisioningConfig.setValid(false);
        provisioningConfig.setBlocking(false);
        provisioningConfig.setEnabled(true);

        //set provisioning properties
        Property provisionProp = new Property();
        provisionProp.setName(testProvisionPropName);
        provisionProp.setDisplayName(testProvisionPropDisplayName);
        provisionProp.setValue(testProvisionPropValue);

        Property provisionProp2 = new Property();
        provisionProp2.setName(testProvisionPropName2);
        provisionProp2.setDisplayName(testProvisionPropDisplayName2);
        provisionProp2.setValue(testProvisionPropValue2);

        Property provisionProp3 = new Property();
        provisionProp3.setName(testProvisionPropName3);
        provisionProp3.setDisplayName(testProvisionPropDisplayName3);
        provisionProp3.setValue(testProvisionPropValue3);

        Property[] provisionProps = new Property[3];
        provisionProps[0] = provisionProp;
        provisionProps[1] = provisionProp2;
        provisionProps[2] = provisionProp3;

        provisioningConfig.setProvisioningProperties(provisionProps);

        ProvisioningConnectorConfig[] provisionConfigs = new ProvisioningConnectorConfig[1];
        provisionConfigs[0] = provisioningConfig;
        idProvider.setProvisioningConnectorConfigs(provisionConfigs);

        //add new identity provider
        idpMgtServiceClient.addIdP(idProvider);

        //check adding idp success
        IdentityProvider addedIdp = idpMgtServiceClient.getIdPByName(testIdpName);

        Assert.assertNotNull(addedIdp, "addIdP or getIdPByName failed");
        Assert.assertEquals(addedIdp.getHomeRealmId(), testIdpRealmId, "addIdP : setting home realm failed");
        Assert.assertEquals(addedIdp.getCertificate(), sampleCertificate, "addIdP : setting certificate failed");

        //idp auto enabled
        Assert.assertEquals(addedIdp.getEnable(), true, "addIdP : idp enable failed");

        Assert.assertEquals(addedIdp.getIdentityProviderDescription(), testIdpDescription, "addIdP : setting description failed");
        Assert.assertEquals(addedIdp.getFederationHub(), false, "addIdP : setting federation hub status failed");
        Assert.assertEquals(addedIdp.getPrimary(), false, "addIdP : setting primary status failed");

        //Check added federated authenticator configs
        FederatedAuthenticatorConfig[] addedFedAuth = addedIdp.getFederatedAuthenticatorConfigs();

        Assert.assertNotNull(addedFedAuth, "federated authenticator not found");
        Assert.assertEquals(addedFedAuth.length, 1, "addIdP : deviation from expected number of federated authenticators");
        Assert.assertEquals(addedFedAuth[0].getName(), testFedAuthName, "addIdP : federated authenticator name setting failed");

        Property[] fedAuthProps = addedFedAuth[0].getProperties();

        Assert.assertNotNull(fedAuthProps, "addIdP : federated authenticator properties not found");
        Assert.assertEquals(fedAuthProps.length, 3, "addIdP : Deviation of expected number of authenticator properties");

        Map<String, Property> propertyMap = new HashMap<String, Property>();

        for (Property fedAuthProp : fedAuthProps) {
            propertyMap.put(fedAuthProp.getName(), fedAuthProp);
        }

        Assert.assertEquals(propertyMap.containsKey(testFedAuthPropName), true,
                            "addIdP : federated authenticator property not found");
        Assert.assertEquals(propertyMap.get(testFedAuthPropName).getValue(), testFedAuthPropValue,
                            "Deviation of federated authenticator property value");
        Assert.assertEquals(propertyMap.containsKey(testFedAuthPropName2), true,
                            "addIdP : federated authenticator property not found");
        Assert.assertEquals(propertyMap.get(testFedAuthPropName2).getValue(), testFedAuthPropValue2,
                            "Deviation of federated authenticator property value");
        Assert.assertEquals(propertyMap.containsKey(testFedAuthPropName3), true,
                            "addIdP : federated authenticator property not found");
        Assert.assertEquals(propertyMap.get(testFedAuthPropName3).getValue(), testFedAuthPropValue3,
                            "Deviation of federated authenticator property value");
        propertyMap.clear();

        //check provisioning connector configs
        ProvisioningConnectorConfig[] provisioningConfigs = addedIdp.getProvisioningConnectorConfigs();

        Assert.assertNotNull(provisioningConfigs, "addIdP : provisioning connector not found");
        Assert.assertEquals(provisioningConfigs.length, 1, "addIdP : Provisioning configuration property adding failed");
        Assert.assertEquals(provisioningConfigs[0].getName(), testProvisionConfName,
                            "addIdP : Provisioning configuration name setting failed");

        Property[] provisioningProps = provisioningConfigs[0].getProvisioningProperties();

        Assert.assertNotNull(provisioningProps, "addIdP : provisioning property not found");
        Assert.assertEquals(provisioningProps.length, 4, "addIdP :Provisioning configuration property setting failed");

        for (Property provisioningProp : provisioningProps) {
            propertyMap.put(provisioningProp.getName(), provisioningProp);
        }

        Assert.assertEquals(propertyMap.containsKey(testProvisionPropName), true,
                            "addIdP : Provisioning configuration property not found : " + testProvisionPropName);
        Assert.assertEquals(propertyMap.get(testProvisionPropName).getValue(), testProvisionPropValue,
                            "addIdP : Provisioning configuration property value failed : " + testProvisionPropName);
        Assert.assertEquals(propertyMap.containsKey(testProvisionPropName2), true,
                            "addIdP : Provisioning configuration property not found : " + testProvisionPropName2);
        Assert.assertEquals(propertyMap.get(testProvisionPropName2).getValue(), testProvisionPropValue2,
                            "addIdP : Provisioning configuration property value failed : " + testProvisionPropName2);
        Assert.assertEquals(propertyMap.containsKey(testProvisionPropName3), true,
                            "addIdP : Provisioning configuration property not found : " + testProvisionPropName3);
        Assert.assertTrue(propertyMap.get(testProvisionPropName3).getValue().contains(RANDOM_PASSWORD_GENERATED),
                            "addIdP : Provisioning configuration property value failed : " + testProvisionPropName3);

        //check jit
        Assert.assertEquals(addedIdp.getJustInTimeProvisioningConfig().getProvisioningEnabled(), true, "addIdP : JIT enabling failed");

    }


    @Test(priority = 3, groups = "wso2.is", description = "test getAllIdPs operation")
    public void testGetAllIdPs() throws Exception {
        List<IdentityProvider> providers = idpMgtServiceClient.getIdPs();

        Assert.assertNotNull(providers);

        log.info("All idp list : " + providers.size());

        //added test IDP included in the list
        if (providers.size() > 0) {
            IdentityProvider addedProvider = null;
            for (IdentityProvider provider : providers) {
                if (provider.getIdentityProviderName().equals(testIdpName)) {
                    addedProvider = provider;
                }
            }
            Assert.assertNotNull(addedProvider, "Added new test idp not found in the idp list");
        } else {
            Assert.fail("Unable to find added identity provider");
        }
    }

    @Test(priority = 4, groups = "wso2.is", description = "test getAllPaginatedIdPsInfo operation")
    public void testGetPaginatedIdPs() throws Exception {

        addIdpForPagination();
        // Check adding idp success.
        IdentityProvider addedIdp = idpMgtServiceClient.getIdPByName(testIdpName);
        Assert.assertNotNull(addedIdp, "addIdP or getIdPByName failed");
        int pageNumber = 1;
        List<IdentityProvider> idpList = idpMgtServiceClient.getAllPaginatedIdPsInfo(pageNumber);
        Assert.assertNotNull(idpList);
        if (idpList.size() > 0) {
            IdentityProvider filteredIdp = null;
            for (IdentityProvider idp : idpList) {
                if (testIdpName.equals(idp.getIdentityProviderName()) || testIdpNameSearch
                        .equals(idp.getIdentityProviderName())) {
                    filteredIdp = idp;
                }
            }
            Assert.assertNotNull(filteredIdp, "Paginated IDP list does not contain added identity provider");
        } else {
            Assert.fail("Unable to find added identity provider. Paginated IDP list is empty.");
        }
    }

    @Test(priority = 5, groups = "wso2.is", description = "test getAllPaginatedIdPsInfo operation with invalid page number")
    public void testGetPaginatedIdPsWithInvalidPageNumber() {

        try {
            int pageNumber = -1;
            idpMgtServiceClient.getAllPaginatedIdPsInfo(pageNumber);
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(), invalidPageNumberErrorMessage, "Error message of invoking " +
                    "getAllPaginatedIdPsInfo with invalid page number did not match with the expected error message: " +
                    invalidPageNumberErrorMessage);
        }
    }

    @Test(priority = 6, groups = "wso2.is", description = "test getAllPaginatedIdPsInfo operation with wrong page number")
    public void testGetPaginatedIdPsWithIncorrectPageNumber() throws Exception {

        int pageNumber = 2;
        List<IdentityProvider> idpList = idpMgtServiceClient.getAllPaginatedIdPsInfo(pageNumber);
        if (idpList.size() > 0) {
            Assert.fail("List of Idps found while calling getAllPaginatedIdPsInfo() with wrong page number.");
        }
    }

    @Test(priority = 7, groups = "wso2.is", description = "test getPaginatedIdPsInfo operation")
    public void testGetAllPaginatedIdPsWithFilter() throws Exception {

        String filter = "name sw \"Search\" and name ew \"Test\"";
        int pageNumber = 1;
        List<IdentityProvider> idpList = idpMgtServiceClient.getPaginatedIdPsInfo(filter, pageNumber);
        Assert.assertNotNull(idpList);
        if (idpList.size() > 0) {
            IdentityProvider filteredIdp = null;
            for (IdentityProvider idp : idpList) {
                if (testIdpNameSearch.equals(idp.getIdentityProviderName())) {
                    filteredIdp = idp;
                }
            }
            Assert.assertNotNull(filteredIdp, "Searched Idp not found");
        } else {
            Assert.fail("Unable to find filtered identity provider. Paginated IDP list is empty.");
        }
    }

    @Test(priority = 8, groups = "wso2.is", description = "test getPaginatedIdPsInfo operation")
    public void testGetAllPaginatedIdPsWithEmptyFilter() throws Exception {

        String filter = "";
        int pageNumber = 1;
        List<IdentityProvider> idpList = idpMgtServiceClient.getPaginatedIdPsInfo(filter, pageNumber);
        Assert.assertNotNull(idpList);
        if (idpList.size() > 0) {
            Assert.assertEquals(idpList.size(), 2, "filtered identity provider size not matched" +
                    " with the expected while testing getPaginatedIdPsInfo() with empty filter");
        } else {
            Assert.fail("Unable to find filtered identity provider while testing getPaginatedIdPsInfo(). Paginated " +
                    "IDP list is empty.");
        }
    }

    @Test(priority = 9, groups = "wso2.is", description = "test getPaginatedIdPsInfo operation with invalid filter")
    public void testGetPaginatedIdPsInfoWithInvalidFilter() {

        try {
            String filter = "names sp \"Search\" and namees ew \"Test\"";
            int pageNumber = 1;
            idpMgtServiceClient.getPaginatedIdPsInfo(filter, pageNumber);
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(), invalidFilterErrorMessage,
                    "Error message of invoking getPaginatedIdPsInfo with invalid filter did not match with the " +
                            "expected error message: " + invalidFilterErrorMessage);
        }
    }

    @Test(priority = 10, groups = "wso2.is", description = "test getPaginatedIdPsInfo operation with invalid page number")
    public void testGetPaginatedIdPsInfoWithInvalidPageNumber() {

        try {
            String filter = "name sw \"Search\" and name ew \"Test\"";
            int pageNumber = -1;
            idpMgtServiceClient.getPaginatedIdPsInfo(filter, pageNumber);
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(), invalidPageNumberErrorMessage, "Error message of invoking " +
                    "getPaginatedIdPsInfo with invalid page number did not match with the expected error message: " +
                    invalidPageNumberErrorMessage);
        }
    }

    @Test(priority = 11, groups = "wso2.is", description = "test getAllPaginatedIdPsInfo operation with incorrect " +
            "page number")
    public void testGetPaginatedIdPsInfoWithIncorrectPageNumber() throws Exception {

        String filter = "name sw \"Search\" and name ew \"Test\"";
        int pageNumber = 2;
        List<IdentityProvider> idpList = idpMgtServiceClient.getPaginatedIdPsInfo(filter, pageNumber);
        if (idpList.size() > 0) {
            Assert.fail("Invalid idp list found while calling getPaginatedIdPsInfo() with incorrect page number.");
        }
    }

    @Test(priority = 12, groups = "wso2.is", description = "test getAllIdpCount operation")
    public void testGetAllIdPsCount() throws Exception {

        int idpCount = idpMgtServiceClient.getAllIdpCount();
        Assert.assertEquals(idpCount, 2, "Total idp count did not match with the expected.");
    }

    @Test(priority = 13, groups = "wso2.is", description = "test getFilteredIdpCount operation with incorrect filter")
    public void testGetFilteredIdpCountWIthIncorrectFilter() {

        try {
            String filter = "names sp \"Search\" and namees ew \"Test\"";
            idpMgtServiceClient.getFilteredIdpCount(filter);
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(), invalidFilterErrorMessage, "Error message of invoking " +
                    "getFilteredIdpCount with invalid filter did not match with the expected error message: " +
                    invalidFilterErrorMessage);
        }
    }

    @Test(priority = 14, groups = "wso2.is", description = "test getFilteredIdpCount operation with incorrect filter " +
            "attribute value")
    public void testGetFilteredIdpCountWithIncorrectFilterAttributeValue() throws Exception {

        String filter = "name sw \"Admin\"";
        int idpCount = idpMgtServiceClient.getFilteredIdpCount(filter);
        if (idpCount > 0) {
            Assert.fail("Invalid idp count found while calling getFilteredIdpCount() with incorrect filter attribute " +
                    "value. Idp Count: " + idpCount);
        }
    }

    @Test(priority = 14, groups = "wso2.is", description = "test getFilteredIdpCount operation with empty filter")
    public void testGetFilteredIdpCountWithEmptyFilterValue() throws Exception {

        String filter = "";
        int idpCount = idpMgtServiceClient.getFilteredIdpCount(filter);
        if (idpCount != 2) {
            Assert.fail("Invalid Idp count found while calling getFilteredIdpCount() with empty filter value ");
        }
    }

    @Test(priority = 15, groups = "wso2.is", description = "test getFilteredIdpCount operation")
    public void testGetAllFilteredIdPsCount() throws Exception {

        String filter = "name sw \"Search\" and name ew \"Test\"";
        int idpCount = idpMgtServiceClient.getFilteredIdpCount(filter);
        Assert.assertEquals(idpCount, 1, "Filtered Idp Count not matched with the expected");
    }

    @Test(priority = 16, groups = "wso2.is", description = "test getEnabledAllIdPs operation")
    public void testGetEnabledAllIdPs() throws Exception {
        List<IdentityProvider> idpList = idpMgtServiceClient.getEnabledIdPs();

        Assert.assertNotNull(idpList, "Enabled idp retrieval failed");

        if (idpList.size() > 0) {
            IdentityProvider addedProvider = null;
            for (IdentityProvider provider : idpList) {
                if (provider.getIdentityProviderName().equals(testIdpName)) {
                    addedProvider = provider;
                }
            }
            Assert.assertNotNull(addedProvider, "Added new test idp not found in the idp list : " + testIdpName);
        } else {
            Assert.fail("Unable to find added identity provider");
        }
    }


    @Test(priority = 17, groups = "wso2.is", description = "test UpdateIdP operation")
    public void testUpdateIdP() throws Exception {

        String updatedTestIdpDescription = "This is Updated test identity provider";

        IdentityProvider idProvider = idpMgtServiceClient.getIdPByName(testIdpName);

        Assert.assertNotNull(idProvider, "Idp retrieval failed");

        //update description
        idProvider.setIdentityProviderDescription(updatedTestIdpDescription);
        //update idp name
        idProvider.setIdentityProviderName(updatedTestIdpName);
        //disable idp
        idProvider.setEnable(false);

        //update federated auth configurations
        idProvider.getFederatedAuthenticatorConfigs()[0].setEnabled(false);
        idProvider.getFederatedAuthenticatorConfigs()[0].setValid(true);

        idpMgtServiceClient.updateIdP(testIdpName, idProvider);

        //Check update
        IdentityProvider updatedProvider = idpMgtServiceClient.getIdPByName(updatedTestIdpName);

        Assert.assertNotNull(updatedProvider, "Idp update failed");
        Assert.assertEquals(updatedProvider.getIdentityProviderDescription(), updatedTestIdpDescription, "IDP description update failed");
        Assert.assertEquals(updatedProvider.getIdentityProviderName(), updatedTestIdpName, "IDP name update failed");
        Assert.assertEquals(updatedProvider.getEnable(), false, "idp disabling failed");

        Assert.assertNotNull(updatedProvider.getFederatedAuthenticatorConfigs(), "Federated authenticator retrieval failed");
        Assert.assertEquals(updatedProvider.getFederatedAuthenticatorConfigs().length, 1, "Deviation of expected number of federated authenticators");
        Assert.assertEquals(updatedProvider.getFederatedAuthenticatorConfigs()[0].getName(), testFedAuthName, "Incorrect federated authenticated received");
        Assert.assertEquals(updatedProvider.getFederatedAuthenticatorConfigs()[0].getEnabled(), false, "federated authenticator enabling failed");
        Assert.assertEquals(updatedProvider.getFederatedAuthenticatorConfigs()[0].getValid(), true, "Set validate status failed");

    }


    @Test(priority = 18, groups = "wso2.is", description = "test getAllProvisioningConnectors operation")
    public void testGetAllProvisioningConnectors() throws Exception {
        Map<String, ProvisioningConnectorConfig> provisioningCons = idpMgtServiceClient.getAllProvisioningConnectors();
        Assert.assertNotNull(provisioningCons, "getAllProvisioningConnectors retrieval failed");

        log.info("Available provisioning connectors : " + provisioningCons.size());
        if (provisioningCons.size() < 1) {
            Assert.fail("Default provisioning connectors not available");
        }
        //check current default provisioning connectors
        Assert.assertEquals(provisioningCons.containsKey("googleapps"), true, "Default provisioning connector googleapps not found");
        Assert.assertEquals(provisioningCons.containsKey("salesforce"), true, "Default provisioning connector salesforce not found");
        Assert.assertEquals(provisioningCons.containsKey("scim"), true, "Default provisioning connector scim not found");
        Assert.assertEquals(provisioningCons.containsKey("SCIM2"), true, "Default provisioning connector scim2 not found");
    }


    @Test(priority = 19, groups = "wso2.is", description = "test getAllFederatedAuthenticators operation")
    public void testGetAllFederatedAuthenticators() throws Exception {
        Map<String, FederatedAuthenticatorConfig> allFedAuthenticators = idpMgtServiceClient.getAllAvailableFederatedAuthenticators();
        Assert.assertNotNull(allFedAuthenticators, "getAllFederatedAuthenticators retrieval failed");

        log.info("Available federated authenticators : " + allFedAuthenticators.size());

        //check current default federated authenticators
        Assert.assertEquals(allFedAuthenticators.containsKey("FacebookAuthenticator"), true,
                            "Default federated authenticator FacebookAuthenticator not found");
        Assert.assertEquals(allFedAuthenticators.containsKey("OpenIDConnectAuthenticator"), true,
                            "Default federated authenticator OpenIDConnectAuthenticator not found");
        Assert.assertEquals(allFedAuthenticators.containsKey("MicrosoftWindowsLiveAuthenticator"), true,
                            "Default federated authenticator MicrosoftWindowsLiveAuthenticator not found");
        Assert.assertEquals(allFedAuthenticators.containsKey("SAMLSSOAuthenticator"), true,
                            "Default federated authenticator SAMLSSOAuthenticator not found");
        Assert.assertEquals(allFedAuthenticators.containsKey("GoogleOIDCAuthenticator"), true,
                            "Default federated authenticator GoogleOIDCAuthenticator     not found");
    }

    @Test(priority = 20, groups = "wso2.is", description = "test getAllLocalClaimUris operation")
    public void testGetAllLocalClaimUris() throws Exception {
        String[] claimUris = idpMgtServiceClient.getAllLocalClaimUris();
        Assert.assertNotNull(claimUris, "claim uri retrieval failed");

        log.info("Local claim uris:");
        //check for default claim uris
        if (claimUris.length < 28) {
            Assert.fail("Claim uri retrieval failed");
        }

    }


    @Test(priority = 21, groups = "wso2.is", description = "test updateResidentIdP operation")
    public void testUpdateResidentIdP() throws Exception {

        String samlEntityId = "samlssoIdp";
        String residentIdpRealm = "testHomeRealm";

        IdentityProvider residentProvider = idpMgtServiceClient.getResidentIdP();
        //get default value
        residentIdpEnable = residentProvider.getEnable();
        residentIdpPrimary = residentProvider.getPrimary();
        residentIdpName = residentProvider.getIdentityProviderName();
        residentIDPDefaultRealm = residentProvider.getHomeRealmId();

        idpProperties = residentProvider.getIdpProperties();

        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setEnable(true);
        identityProvider.setPrimary(true);
        identityProvider.setIdentityProviderName("LOCAL");
        identityProvider.setHomeRealmId("testHomeRealm");

        FederatedAuthenticatorConfig samlFedAuthn = new FederatedAuthenticatorConfig();
        samlFedAuthn.setName(SAML2SSO_NAME);

        Property[] properties = new Property[1];
        Property property = new Property();
        property.setName(SAML2SSO_IDP_ENTITY_ID);
        property.setValue(samlEntityId);
        properties[0] = property;

        samlFedAuthn.setProperties(properties);
        FederatedAuthenticatorConfig[] federatedAuthenticators = new FederatedAuthenticatorConfig[1];
        federatedAuthenticators[0] = samlFedAuthn;
        identityProvider.setFederatedAuthenticatorConfigs(federatedAuthenticators);
        idpMgtServiceClient.updateResidentIdP(identityProvider);

        //check changes
        IdentityProvider changedResidentIdp = idpMgtServiceClient.getResidentIdP();

        Assert.assertNotNull(changedResidentIdp, "Resident idp retrieval failed");
        Assert.assertEquals(changedResidentIdp.getHomeRealmId(), residentIdpRealm);
        Assert.assertEquals(changedResidentIdp.getEnable(), true, "Resident idp enable failed");
        Assert.assertEquals(changedResidentIdp.getPrimary(), true, "Resident idp primary failed");

        boolean found = false;
        for (FederatedAuthenticatorConfig fedConfig : changedResidentIdp.getFederatedAuthenticatorConfigs()) {
            if (fedConfig.getName().equals(SAML2SSO_NAME)) {
                for (Property prop : fedConfig.getProperties()) {
                    if (prop.getName().equals(SAML2SSO_IDP_ENTITY_ID)) {
                        found = true;
                        Assert.assertEquals(prop.getValue(), samlEntityId, "Updating federated authenticator property failed");
                        break;
                    }
                }
                break;
            }
        }

        Assert.assertTrue(found, "Resident idp saml sso properties not found");
    }


    @Test(priority = 22, groups = "wso2.is", description = "test deleteIdP operation")
    public void testDeleteIdP() throws Exception {
        idpMgtServiceClient.deleteIdP(updatedTestIdpName);

        IdentityProvider idp = idpMgtServiceClient.getIdPByName(updatedTestIdpName);

        Assert.assertNull(idp, "Deleting idp failed");
    }

    private void addIdpForPagination() throws Exception {

        String testIdpDescription = "This is second identity provider ";
        String testIdpRealmId = "localhost";
        String testFedAuthDispName = "openid";
        IdentityProvider idProvider = new IdentityProvider();
        // Set idp information.
        idProvider.setHomeRealmId(testIdpRealmId);
        idProvider.setEnable(true);
        idProvider.setIdentityProviderDescription(testIdpDescription);
        idProvider.setIdentityProviderName(testIdpNameSearch);
        idProvider.setCertificate(sampleCertificate);
        idProvider.setFederationHub(false);
        idProvider.setPrimary(false);
        // Add federated authentication configuration.
        FederatedAuthenticatorConfig authConfig = new FederatedAuthenticatorConfig();
        authConfig.setDisplayName(testFedAuthDispName);
        authConfig.setEnabled(true);
        authConfig.setName(testFedAuthName);
        // Add new identity provider.
        idpMgtServiceClient.addIdP(idProvider);
    }
}
