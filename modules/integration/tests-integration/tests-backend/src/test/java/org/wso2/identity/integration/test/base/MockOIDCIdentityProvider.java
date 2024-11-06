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

package org.wso2.identity.integration.test.base;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.ResponseTransformerV2;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.wso2.carbon.utils.security.KeystoreUtils;
import org.wso2.identity.integration.test.util.Utils;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.interfaces.RSAPrivateKey;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.notContaining;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

/**
 * Mock OIDC Identity Provider for testing OIDC flows.
 */
public class MockOIDCIdentityProvider {

    public static final String MOCK_IDP_AUTHORIZE_ENDPOINT = "https://localhost:8089/authorize";
    public static final String MOCK_IDP_TOKEN_ENDPOINT = "https://localhost:8089/token";
    public static final String MOCK_IDP_LOGOUT_ENDPOINT = "https://localhost:8089/oidc/logout";
    public static final String MOCK_IDP_CLIENT_ID = "mockIdPClientID";
    public static final String MOCK_IDP_CLIENT_SECRET = "mockIdPClientSecret";

    private WireMockServer wireMockServer;
    private final AtomicReference<String> authorizationCode = new AtomicReference<>();

    public void start() {

        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .httpsPort(8089)
                .keystorePath(Paths.get(Utils.getResidentCarbonHome(), "repository", "resources", "security",
                        "wso2carbon.p12").toAbsolutePath().toString())
                .keystorePassword("wso2carbon")
                .keyManagerPassword("wso2carbon")
                .extensions(
                        new ResponseTemplateTransformer(null, true, null, null),
                        new ResponseTransformerV2() {
                            @Override
                            public Response transform(Response response, ServeEvent serveEvent) {
                                // Extract the code parameter from the redirect URL
                                String locationHeader = response.getHeaders().getHeader("Location").firstValue();
                                String codeParam = locationHeader.split("code=")[1].split("&")[0];

                                // Store the authorization code
                                authorizationCode.set(codeParam);
                                return response;
                            }

                            @Override
                            public boolean applyGlobally() {
                                return false;
                            }

                            @Override
                            public String getName() {
                                return "authz-code-transformer";
                            }
                        }));

        wireMockServer.start();

        // Configure the mock OIDC endpoints
        configureMockEndpoints();
    }

    public void stop() {

        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    private void configureMockEndpoints() {

        wireMockServer.stubFor(post(urlEqualTo("/token"))
                .withRequestBody(notContaining("grant_type=")
                        .or(notContaining("code="))
                        .or(notContaining("redirect_uri=")))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ \"error\": \"invalid_request\", \"error_description\": " +
                                "\"Missing required parameter\" }")));

        try {
            wireMockServer.stubFor(post(urlEqualTo("/token"))
                    .withRequestBody(containing("grant_type=authorization_code"))
                    .withRequestBody(containing("code="))
                    .withRequestBody(containing("redirect_uri="))
                    .withRequestBody(containing("client_secret="+ MOCK_IDP_CLIENT_SECRET))
                    .withRequestBody(containing("client_id=" + MOCK_IDP_CLIENT_ID))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody("{\"access_token\": \"mock_access_token\", \"token_type\": \"Bearer\", " +
                                    "\"expires_in\": 3600, \"id_token\": \"" + buildIdToken() + "\" }")));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        wireMockServer.stubFor(get(urlPathEqualTo("/authorize"))
                .withQueryParam("response_type", matching(".*"))
                .withQueryParam("redirect_uri", matching(".*"))
                .withQueryParam("state", matching(".*"))
                .withQueryParam("nonce", matching(".*"))
                .withQueryParam("client_id", matching(MOCK_IDP_CLIENT_ID))
                .withQueryParam("scope", matching(".*"))
                .willReturn(aResponse()
                        .withTransformers("response-template", "authz-code-transformer")
                        .withStatus(302)
                        .withHeader("Location",
                                "{{request.query.redirect_uri}}?session_state=mockid&code="
                                        + java.util.UUID.randomUUID() + "&state={{request.query.state}}")));

        wireMockServer.stubFor(get(urlPathEqualTo("/oidc/logout"))
                .withQueryParam("state", matching(".*"))
                .withQueryParam("post_logout_redirect_uri", matching(".*"))
                .withQueryParam("id_token_hint", matching(".*"))
                .willReturn(aResponse()
                        .withTransformers("response-template")
                        .withStatus(302)
                        .withHeader("Location",
                                "{{request.query.post_logout_redirect_uri}}?state={{request.query.state}}")));
    }

    public void verifyForAuthzCodeFlow() {

        wireMockServer.verify(postRequestedFor(urlPathEqualTo("/token"))
                .withRequestBody(containing("grant_type=authorization_code"))
                .withRequestBody(containing("code=" + authorizationCode.get())));
        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/authorize")));
    }

    public void verifyForLogoutFlow() {

        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/oidc/logout")));
    }

    private String buildIdToken() throws Exception {

        KeyStore wso2KeyStore = getKeyStoreFromFile("wso2carbon.p12", "wso2carbon",
                Utils.getResidentCarbonHome());
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) wso2KeyStore.getKey("wso2carbon", "wso2carbon".toCharArray());

        JWSSigner signer = new RSASSASigner(rsaPrivateKey);

        // Prepare JWT with claims set
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer("https://localhost:8089/token")
                .subject("61b935a1-1915-4792-8916-99c59d03c54a")
                .audience("LzWfxDK_7LSGxfuL3BlRdXUGEJYa")
                .claim("azp", "LzWfxDK_7LSGxfuL3BlRdXUGEJYa")
                .claim("org_id", "10084a8d-113f-4211-a0d5-efe36b082211")
                .claim("org_name", "Super")
                .claim("amr", new String[]{"BasicAuthenticator"})
                .claim("c_hash", "3eh6RwdVWxGQEljI7l9K3g")
                .claim("at_hash", "zZ5nLASTkVRWrcCelPOHw")
                .claim("sid", "05759c14-d0bc-414a-931c-b7ffba55b2c3")
                .claim("jti", "37803fb8-f1f1-4eac-8ed2-5067349664fc")
                .claim("isk", "9ab97ab343161334c9432d117e8da73211949aacce8c5d1c0ba8d6c75e0782c4")
                .issueTime(new Date())
                .notBeforeTime(new Date())
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(getKeyId(rsaPrivateKey)).build(), claimsSet);
        signedJWT.sign(signer);
        return signedJWT.serialize();
    }

    private KeyStore getKeyStoreFromFile(String keystoreName, String password, String home) throws Exception {

        Path tenantKeystorePath = Paths.get(home, "repository", "resources", "security", keystoreName);
        FileInputStream file = new FileInputStream(tenantKeystorePath.toString());
        KeyStore keystore = KeystoreUtils.getKeystoreInstance(KeyStore.getDefaultType());
        keystore.load(file, password.toCharArray());
        return keystore;
    }

    private String getKeyId(RSAPrivateKey privateKey) throws Exception {

        java.security.MessageDigest sha256 = java.security.MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = privateKey.getEncoded();
        byte[] hash = sha256.digest(keyBytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }
}
