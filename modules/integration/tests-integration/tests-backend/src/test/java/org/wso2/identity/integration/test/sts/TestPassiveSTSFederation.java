package org.wso2.identity.integration.test.sts;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.*;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOServiceProviderDTO;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.application.mgt.AbstractIdentityFederationTestCase;
import org.wso2.identity.integration.test.utils.CommonConstants;
import org.wso2.identity.integration.test.utils.DataExtractUtil;
import org.wso2.identity.integration.test.utils.IdentityConstants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestPassiveSTSFederation extends AbstractIdentityFederationTestCase {
    private static final String PRIMARY_IS_SERVICE_PROVIDER_NAME = "travelocity";
    private static final String SECONDARY_IS_SERVICE_PROVIDER_NAME = "secondarySP";
    private static final String IDENTITY_PROVIDER_NAME = "trustedIdP";
    private static final String PRIMARY_IS_SAML_ISSUER_NAME = "travelocity.com";
    private static final String PRIMARY_IS_SAML_ACS_URL = "http://localhost:8490/travelocity.com/home.jsp";
    private static final String SECONDARY_IS_SAML_ISSUER_NAME = "samlFedSP";
    private static final String SAML_NAME_ID_FORMAT = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
    private static final String SAML_SSO_URL = "http://localhost:8490/travelocity.com/samlsso?SAML2" +
                                               ".HTTPBinding=HTTP-Redirect";
    private static final String USER_AGENT = "Apache-HttpClient/4.2.5 (java 1.5)";
    private static final String AUTHENTICATION_TYPE = "federated";
    private static final String INBOUND_AUTH_TYPE = "samlsso";
    private static final int TOMCAT_8490 = 8490;
    private static final int PORT_OFFSET_0 = 0;
    private static final int PORT_OFFSET_1 = 1;
    private String COMMON_AUTH_URL = "https://localhost:%s/commonauth";
    private static final String PASSIVESTS_REALM = "PassiveSTSSampleApp";
    private static final String PASSIVESTS_INBOUND_AUTH_TYPE = "passivests";
    private static final String emailClaimURI = "http://wso2.org/claims/emailaddress";
    private static final String givenNameClaimURI = "http://wso2.org/claims/givenname";
    private static final String PASSIVE_STS_SAMPLE_APP_URL =
            "http://localhost:8490/PassiveSTSSampleApp";
    private static final String COMMON_AUTH_URLL =
            "https://localhost:9853/commonauth";
    private static final String HTTP_RESPONSE_HEADER_LOCATION = "location";
    private static final String PASSIVE_STS_SAMPLE_APP_NAME = "/PassiveSTSSampleApp";
    private String sessionDataKey;
    private DefaultHttpClient client;
    private String resultPage;
    private String adminUsername;
    private String adminPassword;

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {

        super.initTest();

        adminUsername = userInfo.getUserName();
        adminPassword = userInfo.getPassword();

        Map<String, String> startupParameterMap = new HashMap<String, String>();
        startupParameterMap.put("-DportOffset", String.valueOf(PORT_OFFSET_1  + CommonConstants.IS_DEFAULT_OFFSET));
        AutomationContext context = new AutomationContext("IDENTITY", "identity002", TestUserMode.SUPER_TENANT_ADMIN);

        startCarbonServer(PORT_OFFSET_1, context, startupParameterMap);

        super.startTomcat(TOMCAT_8490);
        super.addWebAppToTomcat(TOMCAT_8490, PASSIVE_STS_SAMPLE_APP_NAME,
                                getClass().getResource(ISIntegrationTest.URL_SEPARATOR + "samples" + ISIntegrationTest.URL_SEPARATOR + "PassiveSTSSampleApp.war").getPath());
        //servers getting ready...
        Thread.sleep(30000);

        super.createServiceClients(PORT_OFFSET_0, sessionCookie, new IdentityConstants
                .ServiceClientType[]{IdentityConstants.ServiceClientType.APPLICATION_MANAGEMENT, IdentityConstants.ServiceClientType.IDENTITY_PROVIDER_MGT, IdentityConstants.ServiceClientType.SAML_SSO_CONFIG});
        super.createServiceClients(PORT_OFFSET_1, null, new IdentityConstants.ServiceClientType[]{IdentityConstants.ServiceClientType.APPLICATION_MANAGEMENT, IdentityConstants.ServiceClientType.SAML_SSO_CONFIG});
        client = new DefaultHttpClient();
    }

    @AfterClass(alwaysRun = true)
    public void endTest() throws Exception {

        super.deleteSAML2WebSSOConfiguration(PORT_OFFSET_0, PRIMARY_IS_SAML_ISSUER_NAME);
        super.deleteServiceProvider(PORT_OFFSET_0, PRIMARY_IS_SERVICE_PROVIDER_NAME);
        super.deleteIdentityProvider(PORT_OFFSET_0, IDENTITY_PROVIDER_NAME);

        super.deleteSAML2WebSSOConfiguration(PORT_OFFSET_1, SECONDARY_IS_SAML_ISSUER_NAME);
        super.deleteServiceProvider(PORT_OFFSET_1, SECONDARY_IS_SERVICE_PROVIDER_NAME);

        super.stopCarbonServer(PORT_OFFSET_1);
        super.stopTomcat(TOMCAT_8490);

        super.stopHttpClient();
    }

    @Test(groups = "wso2.is", description = "Check create identity provider in primary IS")
    public void testCreateIdentityProviderInPrimaryIS() throws Exception {

        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setIdentityProviderName(IDENTITY_PROVIDER_NAME);

        FederatedAuthenticatorConfig saml2SSOAuthnConfig = new FederatedAuthenticatorConfig();
        saml2SSOAuthnConfig.setName("SAMLSSOAuthenticator");
        saml2SSOAuthnConfig.setDisplayName("samlsso");
        saml2SSOAuthnConfig.setEnabled(true);
        saml2SSOAuthnConfig.setProperties(getSAML2SSOAuthnConfigProperties());
        identityProvider.setDefaultAuthenticatorConfig(saml2SSOAuthnConfig);
        identityProvider.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{saml2SSOAuthnConfig});

        super.addIdentityProvider(PORT_OFFSET_0, identityProvider);
        Assert.assertNotNull(getIdentityProvider(PORT_OFFSET_0, IDENTITY_PROVIDER_NAME), "Failed to create Identity Provider 'trustedIdP' in primary IS");
    }

    @Test(groups = "wso2.is", description = "Check create service provider in primary IS", dependsOnMethods = {
            "testCreateIdentityProviderInPrimaryIS" })
    public void testCreateServiceProviderInPrimaryIS() throws Exception {

        super.addServiceProvider(PORT_OFFSET_0, PRIMARY_IS_SERVICE_PROVIDER_NAME);

        ServiceProvider serviceProvider = getServiceProvider(PORT_OFFSET_0, PRIMARY_IS_SERVICE_PROVIDER_NAME);
        Assert.assertNotNull(serviceProvider, "Failed to create service provider 'travelocity' in primary IS");

        updateServiceProviderWithSAMLConfigs(PORT_OFFSET_0, PRIMARY_IS_SAML_ISSUER_NAME, PRIMARY_IS_SAML_ACS_URL, serviceProvider);

        AuthenticationStep authStep = new AuthenticationStep();
        org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider idP = new org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider();
        idP.setIdentityProviderName(IDENTITY_PROVIDER_NAME);
        org.wso2.carbon.identity.application.common.model.xsd.FederatedAuthenticatorConfig saml2SSOAuthnConfig = new org.wso2.carbon.identity.application.common.model.xsd.FederatedAuthenticatorConfig();
        saml2SSOAuthnConfig.setName("SAMLSSOAuthenticator");
        saml2SSOAuthnConfig.setDisplayName("samlsso");
        idP.setFederatedAuthenticatorConfigs(new org.wso2.carbon.identity.application.common.model.xsd.FederatedAuthenticatorConfig[]{saml2SSOAuthnConfig});
        authStep.setFederatedIdentityProviders(new org.wso2.carbon.identity.application.common.model.xsd.IdentityProvider[]{idP});
        serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationSteps(new AuthenticationStep[]{authStep});
        serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationType(AUTHENTICATION_TYPE);

        updateServiceProvider(PORT_OFFSET_0, serviceProvider);
        serviceProvider = getServiceProvider(PORT_OFFSET_0, PRIMARY_IS_SERVICE_PROVIDER_NAME);

        InboundAuthenticationRequestConfig[] configs = serviceProvider.getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs();
        boolean success = false;
        if (configs != null) {
            for (InboundAuthenticationRequestConfig config : configs) {
                if (PRIMARY_IS_SAML_ISSUER_NAME.equals(config.getInboundAuthKey()) && INBOUND_AUTH_TYPE.equals(config.getInboundAuthType())) {
                    success = true;
                    break;
                }
            }
        }
        Assert.assertTrue(success, "Failed to update service provider with inbound SAML2 configs " +
                                   "in primary IS");
    }


    @Test(groups = "wso2.is", description = "Check update service provider in primary IS with " +
                                            "Passive STS configs",
          dependsOnMethods = "testCreateServiceProviderInPrimaryIS")
    public void testUpdateServiceProviderInPrimaryISWithPassiveSTSConfigs() throws Exception {
        ServiceProvider serviceProvider = getServiceProvider(PORT_OFFSET_0, PRIMARY_IS_SERVICE_PROVIDER_NAME);
        Assert.assertNotNull(serviceProvider, "Service provider in Primary IS not Exists");
        serviceProvider.setOutboundProvisioningConfig(new OutboundProvisioningConfig());
        List<InboundAuthenticationRequestConfig> authRequestList = new ArrayList<InboundAuthenticationRequestConfig>();
        String passiveSTSRealm = PASSIVESTS_REALM;
        if (passiveSTSRealm != null) {
            InboundAuthenticationRequestConfig opicAuthenticationRequest = new InboundAuthenticationRequestConfig();
            opicAuthenticationRequest.setInboundAuthKey(passiveSTSRealm);
            opicAuthenticationRequest.setInboundAuthType("passivests");
            authRequestList.add(opicAuthenticationRequest);
        }
        if (authRequestList.size() > 0) {
            serviceProvider.getInboundAuthenticationConfig()
                    .setInboundAuthenticationRequestConfigs(
                            authRequestList
                                    .toArray(new InboundAuthenticationRequestConfig[authRequestList
                                            .size()]));
        }
        updateServiceProvider(PORT_OFFSET_0, serviceProvider);
        serviceProvider = getServiceProvider(PORT_OFFSET_0, PRIMARY_IS_SERVICE_PROVIDER_NAME);
        InboundAuthenticationRequestConfig[] configs = serviceProvider.getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs();

        boolean success = false;
        if (configs != null) {
            for (InboundAuthenticationRequestConfig config : configs) {
                if (PASSIVESTS_REALM.equals(config.getInboundAuthKey()) && PASSIVESTS_INBOUND_AUTH_TYPE.equals(config
                                                                                                                       .getInboundAuthType())) {
                    success = true;
                    break;
                }
            }
        }
        Assert.assertTrue(success, "Failed to update service provider with inbound PASSIVESTS " +
                                   "configs in primary IS");

    }

    @Test(alwaysRun = true, description = "Update primary IS service provider with claim " +
                                          "configurations",
          dependsOnMethods = "testUpdateServiceProviderInPrimaryISWithPassiveSTSConfigs")
    public void testUpdateServiceProviderInPrimaryISWithClaimConfigs() throws Exception {

        ServiceProvider serviceProvider = getServiceProvider(PORT_OFFSET_0, PRIMARY_IS_SERVICE_PROVIDER_NAME);
        serviceProvider.getClaimConfig().setClaimMappings(getClaimMappings());
        updateServiceProvider(PORT_OFFSET_0, serviceProvider);
        ServiceProvider updatedServiceProvider = getServiceProvider(PORT_OFFSET_0, PRIMARY_IS_SERVICE_PROVIDER_NAME);
        ClaimConfig updatedClaimConfig = updatedServiceProvider.getClaimConfig();

        Assert.assertEquals(updatedClaimConfig.getClaimMappings()[0].getLocalClaim().getClaimUri(),
                            givenNameClaimURI, "Failed update given name claim uri");

        Assert.assertEquals(updatedClaimConfig.getClaimMappings()[1].getLocalClaim().getClaimUri(),
                            emailClaimURI, "Failed update email claim uri");
    }

    @Test(alwaysRun = true, description = "Invoke PassiveSTSSampleApp",
          dependsOnMethods = "testCreateServiceProviderInSecondaryIS")
    public void testInvokePassiveSTSSampleApp() throws IOException {
        HttpGet request = new HttpGet(PASSIVE_STS_SAMPLE_APP_URL);
        HttpResponse response = client.execute(request);
        Assert.assertNotNull(response, "PassiveSTSSampleApp invoke response is null");
        int responseCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(responseCode, 200, "Invalid Response");

        Map<String, Integer> keyPositionMap = new HashMap<String, Integer>(1);
        keyPositionMap.put("name=\"sessionDataKey\"", 1);
        List<DataExtractUtil.KeyValue> keyValues = DataExtractUtil.extractDataFromResponse(response,
                                                                                           keyPositionMap);
        Assert.assertNotNull(keyValues, "sessionDataKey key value is null");
        sessionDataKey = keyValues.get(0).getValue();
        Assert.assertNotNull(sessionDataKey, "Session data key is null.");
        EntityUtils.consume(response.getEntity());
    }

    @Test(alwaysRun = true, description = "Send login post request", dependsOnMethods =
            "testInvokePassiveSTSSampleApp")
    public void testSendLoginRequestPost() throws Exception {
        //todo: This test does not invoke the federated idp url. Need fix
        HttpPost request = new HttpPost(COMMON_AUTH_URLL);
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("username", adminUsername));
        urlParameters.add(new BasicNameValuePair("password", adminPassword));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionDataKey));
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(request);
        Assert.assertNotNull(response, "Login response is null.");
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 302, "Invalid Response");

        Header locationHeader = response.getFirstHeader(HTTP_RESPONSE_HEADER_LOCATION);
        Assert.assertNotNull(locationHeader, "Login response header is null");

        HttpGet getRequest = new HttpGet(locationHeader.getValue());
        EntityUtils.consume(response.getEntity());
        response = client.execute(getRequest);
        resultPage = DataExtractUtil.getContentData(response);
        EntityUtils.consume(response.getEntity());

    }

    @Test(groups = "wso2.is", description = "Check create service provider in secondary IS", dependsOnMethods = {
            "testUpdateServiceProviderInPrimaryISWithClaimConfigs" })
    public void testCreateServiceProviderInSecondaryIS() throws Exception {

        super.addServiceProvider(PORT_OFFSET_1, SECONDARY_IS_SERVICE_PROVIDER_NAME);

        ServiceProvider serviceProvider = getServiceProvider(PORT_OFFSET_1, SECONDARY_IS_SERVICE_PROVIDER_NAME);
        Assert.assertNotNull(serviceProvider, "Failed to create service provider 'secondarySP' in secondary IS");

        updateServiceProviderWithSAMLConfigs(PORT_OFFSET_1, SECONDARY_IS_SAML_ISSUER_NAME, String.format(COMMON_AUTH_URL, DEFAULT_PORT + PORT_OFFSET_0), serviceProvider);

        updateServiceProvider(PORT_OFFSET_1, serviceProvider);
        serviceProvider = getServiceProvider(PORT_OFFSET_1, SECONDARY_IS_SERVICE_PROVIDER_NAME);

        InboundAuthenticationRequestConfig[] configs = serviceProvider.getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs();
        boolean success = false;
        if (configs != null) {
            for (InboundAuthenticationRequestConfig config : configs) {
                if (SECONDARY_IS_SAML_ISSUER_NAME.equals(config.getInboundAuthKey()) && INBOUND_AUTH_TYPE.equals(config.getInboundAuthType())) {
                    success = true;
                    break;
                }
            }
        }

        Assert.assertTrue(success, "Failed to update service provider with inbound SAML2 configs in secondary IS");
    }


    private String sendSAMLRequestToPrimaryIS(HttpClient client) throws Exception {

        HttpGet request = new HttpGet(SAML_SSO_URL);
        request.addHeader("User-Agent", USER_AGENT);
        HttpResponse response = client.execute(request);

        return extractValueFromResponse(response, "name=\"sessionDataKey\"", 1);
    }

    private String authenticateWithSecondaryIS(HttpClient client, String sessionId)
            throws Exception {

        HttpPost request = new HttpPost(String.format(COMMON_AUTH_URL, DEFAULT_PORT + PORT_OFFSET_1));
        request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("Referer", PRIMARY_IS_SAML_ACS_URL);

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("username", "admin"));
        urlParameters.add(new BasicNameValuePair("password", "admin"));
        urlParameters.add(new BasicNameValuePair("sessionDataKey", sessionId));
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(request);
        closeHttpConnection(response);
        return getHeaderValue(response, "Location");
    }

    private Map<String, String> getSAMLResponseFromSecondaryIS(HttpClient client,
                                                               String redirectURL)
            throws Exception {

        HttpPost request = new HttpPost(redirectURL);
        request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("Referer", PRIMARY_IS_SAML_ACS_URL);
        HttpResponse response = client.execute(request);

        Map<String, Integer> searchParams = new HashMap<String, Integer>();
        searchParams.put("SAMLResponse", 5);
        searchParams.put("RelayState", 5);
        return extractValuesFromResponse(response, searchParams);
    }

    private String sendSAMLResponseToPrimaryIS(HttpClient client, Map<String, String> searchResults)
            throws Exception {

        HttpPost request = new HttpPost(String.format(COMMON_AUTH_URL, DEFAULT_PORT + PORT_OFFSET_0));
        request.setHeader("User-Agent", USER_AGENT);

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("SAMLResponse", searchResults.get("SAMLResponse")));
        urlParameters.add(new BasicNameValuePair("RelayState", searchResults.get("RelayState")));
        request.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = new DefaultHttpClient().execute(request);
        closeHttpConnection(response);
        return getHeaderValue(response, "Location");
    }

    private String getSAMLResponseFromPrimaryIS(HttpClient client, String redirectURL)
            throws Exception {

        HttpGet request = new HttpGet(redirectURL);
        request.addHeader("User-Agent", USER_AGENT);
        HttpResponse response = client.execute(request);

        return extractValueFromResponse(response, "SAMLResponse", 5);
    }

    private boolean sendSAMLResponseToWebApp(HttpClient client, String samlResponse)
            throws Exception {

        HttpPost request = new HttpPost(PRIMARY_IS_SAML_ACS_URL);
        request.setHeader("User-Agent", USER_AGENT);
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("SAMLResponse", samlResponse));
        request.setEntity(new UrlEncodedFormEntity(urlParameters));
        HttpResponse response = new DefaultHttpClient().execute(request);

        return super.validateSAMLResponse(response, "admin");
    }

    private void updateServiceProviderWithSAMLConfigs(int portOffset, String issuerName,
                                                      String acsUrl,
                                                      ServiceProvider serviceProvider)
            throws Exception {

        String attributeConsumingServiceIndex = super.createSAML2WebSSOConfiguration(portOffset, getSAMLSSOServiceProviderDTO(issuerName, acsUrl));
        Assert.assertNotNull(attributeConsumingServiceIndex, "Failed to create SAML2 Web SSO configuration for issuer '" + issuerName + "'");

        InboundAuthenticationRequestConfig samlAuthenticationRequestConfig = new InboundAuthenticationRequestConfig();
        samlAuthenticationRequestConfig.setInboundAuthKey(issuerName);
        samlAuthenticationRequestConfig.setInboundAuthType(INBOUND_AUTH_TYPE);
        org.wso2.carbon.identity.application.common.model.xsd.Property property = new org.wso2.carbon.identity.application.common.model.xsd.Property();
        property.setName("attrConsumServiceIndex");
        property.setValue(attributeConsumingServiceIndex);
        samlAuthenticationRequestConfig.setProperties(new org.wso2.carbon.identity.application.common.model.xsd.Property[]{property});

        serviceProvider.getInboundAuthenticationConfig().setInboundAuthenticationRequestConfigs(new InboundAuthenticationRequestConfig[]{samlAuthenticationRequestConfig});
    }

    private SAMLSSOServiceProviderDTO getSAMLSSOServiceProviderDTO(String issuerName,
                                                                   String acsUrl) {

        SAMLSSOServiceProviderDTO samlssoServiceProviderDTO = new SAMLSSOServiceProviderDTO();
        samlssoServiceProviderDTO.setIssuer(issuerName);
        samlssoServiceProviderDTO.setAssertionConsumerUrls(new String[]{acsUrl});
        samlssoServiceProviderDTO.setDefaultAssertionConsumerUrl(acsUrl);
        samlssoServiceProviderDTO.setNameIDFormat(SAML_NAME_ID_FORMAT);
        samlssoServiceProviderDTO.setDoSignAssertions(true);
        samlssoServiceProviderDTO.setDoSignResponse(true);
        samlssoServiceProviderDTO.setDoSingleLogout(true);
        samlssoServiceProviderDTO.setEnableAttributesByDefault(true);
        return samlssoServiceProviderDTO;
    }

    private Property[] getSAML2SSOAuthnConfigProperties() {

        Property[] properties = new Property[12];
        Property property = new Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID);
        property.setValue("samlFedIdP");
        properties[0] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.SP_ENTITY_ID);
        property.setValue("samlFedSP");
        properties[1] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.SSO_URL);
        property.setValue("https://localhost:9453/samlsso");
        properties[2] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IS_AUTHN_REQ_SIGNED);
        property.setValue("false");
        properties[3] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IS_LOGOUT_ENABLED);
        property.setValue("true");
        properties[4] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.LOGOUT_REQ_URL);
        properties[5] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IS_LOGOUT_REQ_SIGNED);
        property.setValue("false");
        properties[6] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IS_AUTHN_RESP_SIGNED);
        property.setValue("false");
        properties[7] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IS_USER_ID_IN_CLAIMS);
        property.setValue("false");
        properties[8] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IS_ENABLE_ASSERTION_ENCRYPTION);
        property.setValue("false");
        properties[9] = property;

        property = new Property();
        property.setName(IdentityConstants.Authenticator.SAML2SSO.IS_ENABLE_ASSERTION_SIGNING);
        property.setValue("false");
        properties[10] = property;

        property = new Property();
        property.setName("commonAuthQueryParams");
        properties[11] = property;

        return properties;
    }

    private ClaimMapping[] getClaimMappings() {
        List<ClaimMapping> claimMappingList = new ArrayList<ClaimMapping>();

        Claim givenNameClaim = new Claim();
        givenNameClaim.setClaimUri(givenNameClaimURI);
        ClaimMapping givenNameClaimMapping = new ClaimMapping();
        givenNameClaimMapping.setRequested(true);
        givenNameClaimMapping.setLocalClaim(givenNameClaim);
        givenNameClaimMapping.setRemoteClaim(givenNameClaim);
        claimMappingList.add(givenNameClaimMapping);

        Claim emailClaim = new Claim();
        emailClaim.setClaimUri(emailClaimURI);
        ClaimMapping emailClaimMapping = new ClaimMapping();
        emailClaimMapping.setRequested(true);
        emailClaimMapping.setLocalClaim(emailClaim);
        emailClaimMapping.setRemoteClaim(emailClaim);
        claimMappingList.add(emailClaimMapping);

        return claimMappingList.toArray(new ClaimMapping[claimMappingList.size()]);
    }


}
