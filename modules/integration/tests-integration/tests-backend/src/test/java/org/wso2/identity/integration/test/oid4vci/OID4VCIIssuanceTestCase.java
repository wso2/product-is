/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.identity.integration.test.oid4vci;

import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.identity.integration.test.oauth2.OAuth2ServiceAbstractIntegrationTest;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.APIResourceListItem;
import org.wso2.identity.integration.test.rest.api.server.api.resource.v1.model.ScopeGetModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthorizedAPICreationModel;
import org.wso2.identity.integration.test.rest.api.server.roles.v2.model.Permission;
import org.wso2.identity.integration.test.rest.api.server.vc.template.management.v1.model.VCTemplate;
import org.wso2.identity.integration.test.rest.api.server.vc.template.management.v1.model.VCTemplateCreateRequest;
import org.wso2.identity.integration.test.restclients.OID4VCIRestClient;
import org.wso2.identity.integration.test.restclients.VCTemplateManagementRestClient;
import org.wso2.identity.integration.test.utils.CarbonUtils;
import org.wso2.identity.integration.test.utils.OAuth2Constant;
import org.wso2.identity.integration.test.utils.OID4VCIProofJWTGenerator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Integration test for VC issuance flow through OID4VCI endpoints.
 */
public class OID4VCIIssuanceTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String CALLBACK_URL = "https://localhost/callback";
    private static final String VC_IDENTIFIER = "employee_badge";
    private static final String VC_DISPLAY_NAME = "EmployeeBadge";
    private static final String VC_TEMPLATE_FORMAT = "dc+sd-jwt";
    private static final String VC_CLAIM_GIVEN_NAME = "given_name";
    private static final String VC_CLAIM_EMAIL = "email";
    private static final String VC_POLICY_IDENTIFIER = "No Policy";
    private static final String VC_RESOURCE_TYPE_FILTER = "type+eq+VC";
    private static final String PASSWORD_GRANT_TYPE = "password";
    private static final int VC_RESOURCE_LOOKUP_RETRIES = 10;
    private static final long VC_RESOURCE_LOOKUP_RETRY_INTERVAL_MS = 1000L;

    private VCTemplateManagementRestClient vcTemplateManagementRestClient;
    private OID4VCIRestClient oid4VCIRestClient;
    private String appId;
    private String templateId;
    private String tokenUserName;
    private String tokenUserPassword;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
        setSystemproperties();
        vcTemplateManagementRestClient = new VCTemplateManagementRestClient(serverURL, tenantInfo);
        oid4VCIRestClient = new OID4VCIRestClient(serverURL, tenantInfo.getContextUser().getUserDomain());
        tokenUserName = isServer.getContextTenant().getTenantAdmin().getUserName();
        tokenUserPassword = isServer.getContextTenant().getTenantAdmin().getPassword();
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        if (templateId != null) {
            vcTemplateManagementRestClient.deleteTemplate(templateId);
        }
        if (appId != null) {
            deleteApp(appId);
        }
        if (vcTemplateManagementRestClient != null) {
            vcTemplateManagementRestClient.closeHttpClient();
        }
        if (oid4VCIRestClient != null) {
            oid4VCIRestClient.closeHttpClient();
        }
        if (restClient != null) {
            restClient.closeHttpClient();
        }
    }

    @Test(groups = "wso2.is", description = "Test verifiable credential issuance end-to-end flow.")
    public void testVCCredentialIssuanceFlow() throws Exception {

        VCTemplate vcTemplate = createVCTemplate();
        createOAuthApplication();

        if (!CarbonUtils.isLegacyAuthzRuntimeEnabled()) {
            authorizeTemplateScopeToApplication(VC_IDENTIFIER);
        }

        String tokenEndpoint = getTenantQualifiedURL(OAuth2Constant.ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain());
        String accessToken = requestAccessToken(consumerKey, consumerSecret, tokenEndpoint,
                tokenUserName, tokenUserPassword,
                Collections.singletonList(new Permission(vcTemplate.getIdentifier())));
        Assert.assertTrue(StringUtils.isNotBlank(accessToken), "Access token should not be empty.");

        JSONObject issuanceResponse = oid4VCIRestClient.requestCredential(accessToken, vcTemplate.getIdentifier());
        String issuedCredential = (String) issuanceResponse.get("credential");
        Assert.assertTrue(StringUtils.isNotBlank(issuedCredential), "Issued credential should not be empty.");
    }

    @Test(groups = "wso2.is",
            description = "Test verifiable credential issuance with nonce and JWT key binding (OID4VCI Draft 16).",
            dependsOnMethods = "testVCCredentialIssuanceFlow")
    public void testVCCredentialIssuanceWithKeyBinding() throws Exception {

        // Retrieve credential issuer URL from metadata (used as JWT audience per Draft 16 §7.2.1.1).
        JSONObject metadata = oid4VCIRestClient.getCredentialIssuerMetadata();
        String credentialIssuerUrl = (String) metadata.get("credential_issuer");
        Assert.assertTrue(StringUtils.isNotBlank(credentialIssuerUrl),
                "credential_issuer is not present in OID4VCI metadata.");

        // Obtain a fresh access token for VC issuance.
        String tokenEndpoint = getTenantQualifiedURL(OAuth2Constant.ACCESS_TOKEN_ENDPOINT, tenantInfo.getDomain());
        String accessToken = requestAccessToken(consumerKey, consumerSecret, tokenEndpoint,
                tokenUserName, tokenUserPassword,
                Collections.singletonList(new Permission(VC_IDENTIFIER)));
        Assert.assertTrue(StringUtils.isNotBlank(accessToken), "Access token should not be empty.");

        // Obtain a c_nonce from the nonce endpoint.
        String cNonce = oid4VCIRestClient.getNonce();

        // Generate a holder key pair and create a proof JWT bound to the nonce.
        ECKey holderKey = OID4VCIProofJWTGenerator.generateECKeyPair();
        String proofJwt = OID4VCIProofJWTGenerator.generateProofJWT(holderKey, credentialIssuerUrl,
                consumerKey, cNonce);

        // Request credential with key-bound proof.
        JSONObject issuanceResponse = oid4VCIRestClient.requestCredential(accessToken, VC_IDENTIFIER, proofJwt);
        String issuedCredential = (String) issuanceResponse.get("credential");
        Assert.assertTrue(StringUtils.isNotBlank(issuedCredential),
                "Issued credential with key binding should not be empty.");

        // Validate that the issued credential contains the cnf claim with the holder's public key.
        // For dc+sd-jwt format, the credential is <Issuer-JWT>~<disclosure1>~... — take the JWT part.
        String credentialJwt = issuedCredential.split("~")[0];
        SignedJWT credentialSignedJwt = SignedJWT.parse(credentialJwt);
        Map<String, Object> cnfClaim = credentialSignedJwt.getJWTClaimsSet().getJSONObjectClaim("cnf");
        Assert.assertNotNull(cnfClaim, "cnf claim should be present in the issued credential.");

        Map<String, Object> cnfJwk = (Map<String, Object>) cnfClaim.get("jwk");
        Assert.assertNotNull(cnfJwk, "cnf.jwk should be present in the issued credential.");

        JWK embeddedJwk = JWK.parse(cnfJwk);
        Assert.assertEquals(embeddedJwk.getKeyID(), holderKey.getKeyID(),
                "cnf.jwk key ID should match the holder's public key thumbprint.");
    }

    private VCTemplate createVCTemplate() throws Exception {

        VCTemplateCreateRequest request = new VCTemplateCreateRequest()
                .identifier(VC_IDENTIFIER)
                .displayName(VC_DISPLAY_NAME)
                .description(VC_DISPLAY_NAME)
                .format(VC_TEMPLATE_FORMAT)
                .claims(Arrays.asList(VC_CLAIM_GIVEN_NAME, VC_CLAIM_EMAIL))
                .expiresIn(31536000);

        VCTemplate createdTemplate = vcTemplateManagementRestClient.createTemplate(request);
        Assert.assertNotNull(createdTemplate, "VC template creation response should not be null.");
        Assert.assertTrue(StringUtils.isNotBlank(createdTemplate.getId()), "VC template id should not be empty.");
        Assert.assertEquals(createdTemplate.getIdentifier(), VC_IDENTIFIER, "Unexpected VC template identifier.");
        templateId = createdTemplate.getId();

        return createdTemplate;
    }

    private void createOAuthApplication() throws Exception {

        OAuthConsumerAppDTO appDto = getBasicOAuthApp(CALLBACK_URL);
        appDto.setApplicationName("oid4vci-issuance-app-" + UUID.randomUUID());
        appDto.setGrantTypes(PASSWORD_GRANT_TYPE);

        ServiceProvider serviceProvider = registerServiceProviderWithOAuthInboundConfigs(appDto);
        Assert.assertNotNull(serviceProvider, "Service provider should not be null.");
        appId = serviceProvider.getApplicationResourceId();
        Assert.assertTrue(StringUtils.isNotBlank(appId), "Application resource id should not be empty.");
    }

    private void authorizeTemplateScopeToApplication(String templateIdentifier) throws Exception {

        APIResourceListItem vcAPIResource = getVCAPIResource(templateIdentifier);
        Assert.assertNotNull(vcAPIResource, "Unable to find VC API resource for scope: " + templateIdentifier);

        AuthorizedAPICreationModel authorizedAPICreationModel = new AuthorizedAPICreationModel();
        authorizedAPICreationModel.setId(vcAPIResource.getId());
        authorizedAPICreationModel.setPolicyIdentifier(VC_POLICY_IDENTIFIER);
        authorizedAPICreationModel.setScopes(Collections.singletonList(templateIdentifier));

        int statusCode = restClient.addAPIAuthorizationToApplication(appId, authorizedAPICreationModel);
        Assert.assertTrue(statusCode == 200 || statusCode == 201 || statusCode == 409,
                "Authorizing VC API to the application failed. Status: " + statusCode);
    }

    private APIResourceListItem getVCAPIResource(String templateIdentifier) throws Exception {

        for (int attempt = 0; attempt < VC_RESOURCE_LOOKUP_RETRIES; attempt++) {
            List<APIResourceListItem> vcResources = restClient.getAPIResourcesWithFiltering(VC_RESOURCE_TYPE_FILTER);
            APIResourceListItem matchedResource = getMatchingVCAPIResource(vcResources, templateIdentifier);
            if (matchedResource != null) {
                return matchedResource;
            }

            List<APIResourceListItem> identifierMatchedResources = restClient.getAPIResourcesWithFiltering(
                    "identifier+eq+" + templateIdentifier);
            matchedResource = getMatchingVCAPIResource(identifierMatchedResources, templateIdentifier);
            if (matchedResource != null) {
                return matchedResource;
            }

            if (attempt < VC_RESOURCE_LOOKUP_RETRIES - 1) {
                Thread.sleep(VC_RESOURCE_LOOKUP_RETRY_INTERVAL_MS);
            }
        }
        return null;
    }

    private APIResourceListItem getMatchingVCAPIResource(List<APIResourceListItem> resources, String templateIdentifier)
            throws Exception {

        for (APIResourceListItem resource : resources) {
            List<ScopeGetModel> scopes = restClient.getAPIResourceScopes(resource.getId());
            for (ScopeGetModel scope : scopes) {
                if (templateIdentifier.equals(scope.getName())) {
                    return resource;
                }
            }
            if (templateIdentifier.equals(resource.getIdentifier())) {
                return resource;
            }
        }
        return null;
    }

}
