package org.wso2.identity.integration.test.saml;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.identity.application.common.model.idp.xsd.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.idp.xsd.Property;
import org.wso2.carbon.identity.sso.saml.stub.IdentitySAMLSSOConfigServiceIdentityException;
import org.wso2.identity.integration.common.clients.Idp.IdentityProviderMgtServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;

/**
 * This tests adds an Identity Provider using metadata file, checks for the validity of properties
 */
public class IDPMetadataTestCase extends ISIntegrationTest {

    private IdentityProviderMgtServiceClient identityProviderMgtServiceClient;

    private static final String SPENTITYID = "spEntityId";
    private static final String ENTITYID = "loadbalancer1-3.example.com";
    private static final String SSOURL = "https://LoadBalancer-3.example.com:9443/amserver/SSORedirect/metaAlias/idp";
    private static final String IS_AUTHN_REQ_SIGNED = "false";
    private static final String IS_LOGOUT_ENABLED = "true";
    private static final String LOGOUT_REQ_URL = "https://IdentityProvider.com/SAML/SLO/SOAP";
    private static final String IS_ENABLE_ASSERTION_ENCRYPTION = "true";
    private static final String IS_ENABLE_ASSERTION_SIGNING = "true";
    private static final String SAML_SSO_CONFIG_NAME = "SAMLSSOAuthenticator";
    private static final String SAML_SSO = "samlsso";


    @BeforeClass
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        identityProviderMgtServiceClient = new IdentityProviderMgtServiceClient(sessionCookie, backendURL);
    }

    @Test
    public void addIDPMetadata() throws Exception {

        String filePath = FrameworkPathUtil.getSystemResourceLocation() +
                "artifacts" + File.separator + "IS" + File.separator + "saml" + File.separator
                + "idp-metadata.xml";

        byte[] encoded = Files.readAllBytes(Paths.get(filePath));
        String metadataXml = new String(encoded, StandardCharsets.UTF_8);
        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setIdentityProviderName(ENTITYID);
        buildSAMLAuthenticationConfiguration(identityProvider, metadataXml);

        identityProviderMgtServiceClient.addIdP(identityProvider);

        identityProvider = identityProviderMgtServiceClient.getIdPByName(ENTITYID);
        FederatedAuthenticatorConfig samlFederatedAuthenticatorConfig = null;
        FederatedAuthenticatorConfig federatedAuthenticatorConfigs[] = identityProvider
                .getFederatedAuthenticatorConfigs();

        for (int i = 0; i < federatedAuthenticatorConfigs.length; i++) {
            if ("SAMLSSOAuthenticator".equals(federatedAuthenticatorConfigs[i].getName())) {
                samlFederatedAuthenticatorConfig = federatedAuthenticatorConfigs[i];
                break;
            }
        }
        Assert.assertNotNull(identityProvider);
        Assert.assertNotNull(samlFederatedAuthenticatorConfig);

        Property[] properties = samlFederatedAuthenticatorConfig.getProperties();

        for (Property property : properties) {

            if (IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID.equals(property.getName())) {
                Assert.assertEquals(property.getValue(), ENTITYID);
            }

            if (IdentityApplicationConstants.Authenticator.SAML2SSO.SP_ENTITY_ID.equals(property.getName())) {
                Assert.assertEquals(property.getValue(), SPENTITYID);
            }

            if (IdentityApplicationConstants.Authenticator.SAML2SSO.SSO_URL.equals(property.getName())) {
                Assert.assertEquals(property.getValue(), SSOURL);
            }

            if (IdentityApplicationConstants.Authenticator.SAML2SSO.IS_AUTHN_REQ_SIGNED.equals(property.getName())) {
                Assert.assertEquals(property.getValue(), IS_AUTHN_REQ_SIGNED);
            }

            if (IdentityApplicationConstants.Authenticator.SAML2SSO.IS_LOGOUT_ENABLED.equals(property.getName())) {
                Assert.assertEquals(property.getValue(), IS_LOGOUT_ENABLED);
            }

            if (IdentityApplicationConstants.Authenticator.SAML2SSO.LOGOUT_REQ_URL.equals(property.getName())) {
                Assert.assertEquals(property.getValue(), LOGOUT_REQ_URL);
            }

            if (IdentityApplicationConstants.Authenticator.SAML2SSO.IS_ENABLE_ASSERTION_ENCRYPTION.equals(property.getName())) {
                Assert.assertEquals(property.getValue(), IS_ENABLE_ASSERTION_ENCRYPTION);
            }

            if (IdentityApplicationConstants.Authenticator.SAML2SSO.IS_ENABLE_ASSERTION_SIGNING.equals(property.getName())) {
                Assert.assertEquals(property.getValue(), IS_ENABLE_ASSERTION_SIGNING);
            }
        }

    }

    @AfterClass
    public void endTest() throws RemoteException, IdentitySAMLSSOConfigServiceIdentityException {
        try {
            identityProviderMgtServiceClient.deleteIdP(ENTITYID);
        } catch (Exception ex) {
            throw new RemoteException("Error Deleting Identity Provider", ex);
        }
    }

    private static void buildSAMLAuthenticationConfiguration(IdentityProvider fedIdp, String metadataXML) {

        FederatedAuthenticatorConfig saml2SSOAuthnConfig = new FederatedAuthenticatorConfig();
        saml2SSOAuthnConfig.setName(SAML_SSO_CONFIG_NAME);
        saml2SSOAuthnConfig.setDisplayName(SAML_SSO);
        saml2SSOAuthnConfig.setEnabled(true);
        Property[] properties = new Property[2];

        Property property = new Property();

        property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.SP_ENTITY_ID);
        property.setValue("spEntityId");
        properties[0] = property;

        property = new Property();
        property.setName("meta_data_saml");
        property.setValue(metadataXML);
        properties[1] = property;

        saml2SSOAuthnConfig.setProperties(properties);

        fedIdp.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{saml2SSOAuthnConfig});

    }
}
