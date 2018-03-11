/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.identity.integration.test.oauth2;

import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ResourceOwnerPasswordCredentialsGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenIntrospectionRequest;
import com.nimbusds.oauth2.sdk.TokenIntrospectionResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.identity.integration.common.clients.entitlement.EntitlementPolicyServiceClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.net.URI;

/**
 * Test cases to check the functionality of the XACML based scope validator.
 */
public class OAuth2XACMLScopeValidatorTestCase extends OAuth2ServiceAbstractIntegrationTest {

    private static final String VALIDATE_SCOPE_BASED_POLICY_ID = "validate_scope_based_policy_template";
    private static final String VALID_SCOPE = "SCOPE1";
    private static final String SCOPE_VALIDATOR_NAME = "XACML Scope Validator";
    private static final String CALLBACK_URL = "https://localhost/callback";
    private static final String SCOPE_POLICY = "<Policy xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" " +
            "PolicyId=\"validate_scope_based_policy_template\"\n" +
            "        RuleCombiningAlgId=\"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable\" " +
            "Version=\"1.0\">\n" +
            "    <Description></Description>\n" +
            "    <Target>\n" +
            "        <AnyOf>\n" +
            "            <AllOf>\n" +
            "                <Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\">\n" +
            "                    <AttributeValue DataType=\"http://www" +
            ".w3.org/2001/XMLSchema#string\">" + SERVICE_PROVIDER_NAME + "</AttributeValue>\n" +
            "                    <AttributeDesignator AttributeId=\"http://wso2.org/identity/sp/sp-name\"\n" +
            "                                         Category=\"http://wso2.org/identity/sp\"\n" +
            "                                         DataType=\"http://www.w3.org/2001/XMLSchema#string\"\n" +
            "                                         MustBePresent=\"false\"></AttributeDesignator>\n" +
            "                </Match>\n" +
            "                <Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\">\n" +
            "                    <AttributeValue DataType=\"http://www" +
            ".w3.org/2001/XMLSchema#string\">token_validation</AttributeValue>\n" +
            "                    <AttributeDesignator " +
            "AttributeId=\"http://wso2.org/identity/identity-action/action-name\"\n" +
            "                                         Category=\"http://wso2.org/identity/identity-action\"\n" +
            "                                         DataType=\"http://www.w3.org/2001/XMLSchema#string\"\n" +
            "                                         MustBePresent=\"false\"></AttributeDesignator>\n" +
            "                </Match>\n" +
            "            </AllOf>\n" +
            "        </AnyOf>\n" +
            "    </Target>\n" +
            "    <Rule Effect=\"Permit\" RuleId=\"permit_by_scopes\">\n" +
            "        <Condition>\n" +
            "            <Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:or\">\n" +
            "                <Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-is-in\">\n" +
            "                    <AttributeValue DataType=\"http://www" +
            ".w3.org/2001/XMLSchema#string\">" + VALID_SCOPE + "</AttributeValue>\n" +
            "                    <AttributeDesignator " +
            "AttributeId=\"http://wso2.org/identity/oauth-scope/scope-name\"\n" +
            "                                         Category=\"http://wso2.org/identity/oauth-scope\"\n" +
            "                                         DataType=\"http://www.w3.org/2001/XMLSchema#string\"\n" +
            "                                         MustBePresent=\"true\"></AttributeDesignator>\n" +
            "                </Apply>\n" +
            "            </Apply>\n" +
            "        </Condition>\n" +
            "    </Rule>\n" +
            "    <Rule Effect=\"Deny\" RuleId=\"deny_others\"></Rule>\n" +
            "</Policy>";
    private CloseableHttpClient client;
    private EntitlementPolicyServiceClient entitlementPolicyClient;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
        entitlementPolicyClient = new EntitlementPolicyServiceClient(backendURL, sessionCookie);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApplication();
        removeOAuthApplicationData();
        consumerKey = null;
        consumerSecret = null;
        entitlementPolicyClient.removePolicy(VALIDATE_SCOPE_BASED_POLICY_ID);
        entitlementPolicyClient.publishPolicies(new String[]{VALIDATE_SCOPE_BASED_POLICY_ID}, new String[]{"PDP " +
                "Subscriber"}, "DELETE", true, null, 1);
    }

    @Test(groups = "wso2.is", description = "Check Oauth2 application registration.")
    public void testRegisterApplication() throws Exception {

        OAuthConsumerAppDTO oAuthConsumerAppDTO = getBasicOAuthApp(CALLBACK_URL);
        oAuthConsumerAppDTO.setScopeValidators(new String[]{SCOPE_VALIDATOR_NAME});
        ServiceProvider serviceProvider = registerServiceProviderWithOAuthInboundConfigs(oAuthConsumerAppDTO);
        Assert.assertNotNull(serviceProvider, "OAuth App creation failed.");
        Assert.assertNotNull(consumerKey, "Consumer Key is null.");
        Assert.assertNotNull(consumerSecret, "Consumer Secret is null.");
    }

    @Test(groups = "wso2.is", description = "Check publishing a policy", dependsOnMethods = "testRegisterApplication")
    public void testPublishPolicy() throws Exception {

        PolicyDTO policy = new PolicyDTO();
        policy.setPolicy(SCOPE_POLICY);
        policy.setVersion("3.0");
        policy.setPolicy(policy.getPolicy().replaceAll(">\\s+<", "><").trim());
        policy.setPolicyId(VALIDATE_SCOPE_BASED_POLICY_ID);
        entitlementPolicyClient.addPolicy(policy);
        entitlementPolicyClient.publishPolicies(new String[]{VALIDATE_SCOPE_BASED_POLICY_ID}, new String[]{"PDP " +
                "Subscriber"}, "CREATE", true, null, 1);
        Assert.assertNotNull(entitlementPolicyClient.getPolicy(VALIDATE_SCOPE_BASED_POLICY_ID, true), "Entitlement " +
                "service publish policy failed.");
    }

    @Test(groups = "wso2.is", description = "Request access token with invalid token and validate it.",
            dependsOnMethods = "testPublishPolicy")
    public void testValidateTokenWithInValidScope() throws Exception {

        boolean result = getTokenAndValidate(new Scope(OAuth2Constant.OAUTH2_SCOPE_EMAIL));
        Assert.assertFalse(result, "Introspection is true.");
    }

    @Test(groups = "wso2.is", description = "Request access token with valid token and validate it.",
            dependsOnMethods = "testValidateTokenWithInValidScope")
    public void testValidateTokenWithValidScope() throws Exception {

        boolean result = getTokenAndValidate(new Scope(VALID_SCOPE));
        Assert.assertTrue(result, "Introspection is false.");
    }

    @Test(groups = "wso2.is", description = "Request access token with multiple token and validate it.",
            dependsOnMethods = "testValidateTokenWithValidScope")
    public void testValidateTokenWithMultipleScope() throws Exception {

        boolean result = getTokenAndValidate(new Scope(VALID_SCOPE, OAuth2Constant.OAUTH2_SCOPE_EMAIL));
        Assert.assertTrue(result, "Introspection is false.");
    }


    /**
     * Request access token with the scope and validate the token.
     *
     * @param scope scope
     * @return whether validation success or not
     * @throws Exception exception
     */
    private boolean getTokenAndValidate(Scope scope) throws Exception {

        client = HttpClientBuilder.create().disableRedirectHandling().build();

        try {
            Secret password = new Secret(userInfo.getPassword());
            AuthorizationGrant passwordGrant = new ResourceOwnerPasswordCredentialsGrant(userInfo.getUserName(),
                    password);
            ClientID clientID = new ClientID(consumerKey);
            Secret clientSecret = new Secret(consumerSecret);
            ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);
            URI tokenEndpoint = new URI(OAuth2Constant.ACCESS_TOKEN_ENDPOINT);
            TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, passwordGrant, scope);

            HTTPResponse tokenHTTPResp = request.toHTTPRequest().send();
            Assert.assertNotNull(tokenHTTPResp, "Access token http response is null.");
            AccessTokenResponse tokenResponse = AccessTokenResponse.parse(tokenHTTPResp);
            Assert.assertNotNull(tokenResponse, "Access token response is null.");

            AccessToken accessToken = tokenResponse.getTokens().getAccessToken();
            URI introSpecEndpoint = new URI(OAuth2Constant.INTRO_SPEC_ENDPOINT);
            BearerAccessToken bearerAccessToken = new BearerAccessToken(accessToken.getValue());
            TokenIntrospectionRequest TokenIntroRequest = new TokenIntrospectionRequest(introSpecEndpoint,
                    bearerAccessToken,
                    accessToken);
            HTTPResponse introspectionHTTPResp = TokenIntroRequest.toHTTPRequest().send();
            Assert.assertNotNull(introspectionHTTPResp, "Introspection http response is null.");

            TokenIntrospectionResponse introspectionResponse = TokenIntrospectionResponse.parse(introspectionHTTPResp);
            Assert.assertNotNull(introspectionResponse, "Introspection response is null.");
            return introspectionResponse.indicatesSuccess();
        } finally {
            client.close();
        }
    }
}
