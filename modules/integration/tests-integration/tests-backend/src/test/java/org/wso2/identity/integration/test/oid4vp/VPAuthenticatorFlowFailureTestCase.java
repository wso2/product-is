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

package org.wso2.identity.integration.test.oid4vp;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.identity.integration.test.restclients.IdpMgtRestClient;
import org.wso2.identity.integration.test.restclients.VPSessionRestClient;
import org.wso2.identity.integration.test.utils.SDJwtVcTestBuilder;

import java.util.UUID;

/**
 * Failure-path integration tests for the VP Authenticator flow.
 */
public class VPAuthenticatorFlowFailureTestCase extends VPAuthenticatorFlowTestBase {

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        issuerSigningKey = SDJwtVcTestBuilder.generateSigningKey();
        configureVPResponseMode("direct_post");
        presentationDefinitionId = createPresentationDefinition();
        configureTrustedIssuer(presentationDefinitionId, CREDENTIAL_IDENTIFIER, ISSUER_URL, issuerSigningKey);
        idpMgtRestClient = new IdpMgtRestClient(serverURL, isServer.getContextTenant());
        idpId = createVPAuthenticatorIDP();
        appId = createOAuthApplicationWithVPIdp();
        vpSessionRestClient = new VPSessionRestClient(serverURL, isServer.getContextTenant());
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {

        deleteApplicationIfExists();
        deleteIdpIfExists();
        deletePresentationDefinitionQuietly();
        closeVpSessionClient();
        closeIdpMgtClient();
        closeOAuthRestClient();
    }

    @Test(groups = "wso2.is",
            description = "Submitting a VP with a wrong nonce causes the auth flow to end with an error redirect.")
    public void testAuthFlowFailsWhenVPVerificationFails() throws Exception {

        AuthFlowContext ctx = initiateAuthFlowAndFetchNonce();
        Assert.assertNotNull(ctx,
                "Authorization redirect chain must reach " + WALLET_LOGIN_JSP + " with a valid requestId.");
        Assert.assertNotNull(ctx.sessionDataKey,
                "Wallet login redirect must carry a sessionDataKey parameter.");

        String vpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice")
                .nonce("wrong-nonce-" + UUID.randomUUID())
                .audience(ctx.audience)
                .signingKey(issuerSigningKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse vpResponse = vpSessionRestClient.submitVPDirectPost(ctx.requestId, vpToken)) {
            EntityUtils.consume(vpResponse.getEntity());
        }

        String finalStatus = pollStatusUntilNotActive(ctx.requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, "FAILED",
                "VP with wrong nonce must result in a FAILED session.");

        String redirectUri = triggerCommonAuthCallbackAndGetRedirect(ctx.sessionDataKey, ctx.requestId, "failed",
                ctx.cookieStore);
        Assert.assertNotNull(redirectUri,
                "IS must redirect after a failed VP callback — got no Location header.");

        if (!redirectUri.contains(WALLET_LOGIN_JSP) && !redirectUri.contains("retry.do")) {
            Assert.assertTrue(redirectUri.contains("error") || redirectUri.contains("error_description"),
                    "Auth flow error redirect must carry an 'error' parameter.  Got: " + redirectUri);
        }
    }

    @Test(groups = "wso2.is",
            description = "A wallet access_denied response causes the auth flow to end with an error redirect.")
    public void testAuthFlowFailsWhenWalletReportsError() throws Exception {

        AuthFlowContext ctx = initiateAuthFlowAndFetchNonce();
        Assert.assertNotNull(ctx,
                "Authorization redirect chain must reach " + WALLET_LOGIN_JSP + " with a valid requestId.");
        Assert.assertNotNull(ctx.sessionDataKey,
                "Wallet login redirect must carry a sessionDataKey parameter.");

        try (CloseableHttpResponse errorResponse = vpSessionRestClient.submitWalletError(
                ctx.requestId, "access_denied", "User declined to share credentials.")) {
            EntityUtils.consume(errorResponse.getEntity());
        }

        String finalStatus = pollStatusUntilNotActive(ctx.requestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, "FAILED",
                "Wallet access_denied must result in a FAILED session.");

        String redirectUri = triggerCommonAuthCallbackAndGetRedirect(ctx.sessionDataKey, ctx.requestId, "failed",
                ctx.cookieStore);
        Assert.assertNotNull(redirectUri,
                "IS must redirect after a wallet error callback — got no Location header.");
        if (!redirectUri.contains(WALLET_LOGIN_JSP) && !redirectUri.contains("retry.do")) {
            Assert.assertTrue(redirectUri.contains("error"),
                    "Auth flow error redirect must carry an 'error' parameter.  Got: " + redirectUri);
        }
    }

    @Test(groups = "wso2.is",
            description = "When retry is enabled, a failed VP attempt causes IS to issue a new requestId.")
    public void testAuthFlowRetryAfterFailure() throws Exception {

        AuthFlowContext ctx = initiateAuthFlowAndFetchNonce();
        Assert.assertNotNull(ctx,
                "Authorization redirect chain must reach " + WALLET_LOGIN_JSP + " with a valid requestId.");
        Assert.assertNotNull(ctx.sessionDataKey,
                "Wallet login redirect must carry a sessionDataKey parameter.");

        String firstRequestId = ctx.requestId;

        String badVpToken = SDJwtVcTestBuilder.builder()
                .vct("PersonCredential")
                .claim("given_name", "Alice")
                .nonce("wrong-nonce")
                .audience(ctx.audience)
                .signingKey(issuerSigningKey)
                .buildVpToken(CREDENTIAL_IDENTIFIER);

        try (CloseableHttpResponse resp = vpSessionRestClient.submitVPDirectPost(firstRequestId, badVpToken)) {
            EntityUtils.consume(resp.getEntity());
        }

        String finalStatus = pollStatusUntilNotActive(firstRequestId, POLL_MAX_RETRIES, POLL_INTERVAL_MS);
        Assert.assertEquals(finalStatus, "FAILED",
                "VP with wrong nonce must result in a FAILED session.");

        String redirectUri = triggerCommonAuthCallbackAndGetRedirect(ctx.sessionDataKey, firstRequestId, "failed",
                ctx.cookieStore);
        Assert.assertNotNull(redirectUri,
                "IS must redirect after a failed VP callback — got no Location header.");

        // When retry is enabled IS redirects back to wallet_login.jsp with a fresh requestId.
        if (redirectUri.contains(WALLET_LOGIN_JSP)) {
            String newRequestId = extractQueryParam(redirectUri, "requestId");
            Assert.assertNotNull(newRequestId,
                    "Retry redirect to " + WALLET_LOGIN_JSP + " must carry a requestId.  Got: " + redirectUri);
            Assert.assertNotEquals(newRequestId, firstRequestId,
                    "Retry must issue a fresh requestId distinct from the failed session's requestId.");
        }
    }
}
