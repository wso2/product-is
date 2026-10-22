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

package org.wso2.identity.integration.test.utils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Builder for minimal, structurally valid SD-JWT-VC tokens used in integration tests.
 *
 * <p>Token format (SD-JWT):
 * <pre>{@code <issuer-JWT>.<disclosure1>~<disclosure2>~...~<kb-JWT>}</pre>
 *
 * <p>This builder intentionally keeps the implementation thin: it does not attempt to be a
 * production SD-JWT library. Its sole purpose is to produce tokens that exercise the VP
 * verification pipeline in integration tests.
 */
public class SDJwtVcTestBuilder {

    private String vct = "TestCredential";
    private String issuer = "https://test-issuer.example.com";
    private final Map<String, Object> subjectClaims = new LinkedHashMap<>();
    private String nonce;
    private String audience;
    private long expiresInSeconds = 3600L;
    private ECKey signingKey;
    private boolean omitKbJwt = false;
    private boolean corruptDisclosure = false;
    private long issuedAtOffsetSeconds = 0L;

    private SDJwtVcTestBuilder() {

    }

    /**
     * Create a new builder instance.
     *
     * @return A fresh {@link SDJwtVcTestBuilder}.
     */
    public static SDJwtVcTestBuilder builder() {

        return new SDJwtVcTestBuilder();
    }

    /** Set the Verifiable Credential Type claim ({@code vct}). */
    public SDJwtVcTestBuilder vct(String vct) {
        this.vct = vct;
        return this;
    }

    /** Set the credential issuer URI ({@code iss}). */
    public SDJwtVcTestBuilder issuer(String issuer) {
        this.issuer = issuer;
        return this;
    }

    /** Set the nonce embedded in the KB-JWT. */
    public SDJwtVcTestBuilder nonce(String nonce) {
        this.nonce = nonce;
        return this;
    }

    /** Set the audience embedded in the KB-JWT ({@code aud}). */
    public SDJwtVcTestBuilder audience(String audience) {
        this.audience = audience;
        return this;
    }

    /**
     * Set the lifetime of the credential in seconds (relative to build time).
     * Pass a negative value to produce an already-expired credential.
     */
    public SDJwtVcTestBuilder expiresInSeconds(long seconds) {
        this.expiresInSeconds = seconds;
        return this;
    }

    /**
     * Shift the {@code iat} claim by the given number of seconds.
     * A positive value makes the credential appear issued in the future.
     */
    public SDJwtVcTestBuilder issuedAtOffsetSeconds(long offsetSeconds) {
        this.issuedAtOffsetSeconds = offsetSeconds;
        return this;
    }

    /** Add a subject claim that will be selectively disclosed. */
    public SDJwtVcTestBuilder claim(String name, Object value) {
        this.subjectClaims.put(name, value);
        return this;
    }

    /**
     * Set the EC key pair used to sign the issuer JWT.
     * If not set, a fresh P-256 key is generated at {@link #build()} time.
     */
    public SDJwtVcTestBuilder signingKey(ECKey key) {
        this.signingKey = key;
        return this;
    }

    /**
     * When {@code true}, the token will be assembled without a KB-JWT tail.
     * This produces a structurally invalid SD-JWT-VC for negative testing.
     */
    public SDJwtVcTestBuilder omitKbJwt(boolean omit) {
        this.omitKbJwt = omit;
        return this;
    }

    /**
     * When {@code true}, one disclosure will have its content corrupted so the
     * SHA-256 hash in the issuer JWT no longer matches.  Useful for negative tests
     * that verify disclosure-hash validation.
     */
    public SDJwtVcTestBuilder corruptDisclosure(boolean corrupt) {
        this.corruptDisclosure = corrupt;
        return this;
    }

    /**
     * Generate a fresh P-256 EC key pair suitable for use as an issuer or holder signing key.
     *
     * @return Generated EC key pair.
     * @throws JOSEException If key generation fails.
     */
    public static ECKey generateSigningKey() throws JOSEException {

        return new ECKeyGenerator(Curve.P_256)
                .keyID(UUID.randomUUID().toString())
                .generate();
    }

    /**
     * Build the SD-JWT-VC token string.
     *
     * <p>Format:
     * <pre>{@code <issuer-JWT>.<disclosure1>~<disclosure2>~...~<kb-JWT>}</pre>
     *
     * @return Compact SD-JWT-VC token string.
     * @throws Exception If token construction fails.
     */
    public String build() throws Exception {

        if (signingKey == null) {
            signingKey = generateSigningKey();
        }

        MessageDigest sha256 = sha256Digest();

        // 1. Build disclosures for each subject claim.
        List<String> disclosures = new ArrayList<>();
        List<String> sdHashes = new ArrayList<>();

        int claimIndex = 0;
        for (Map.Entry<String, Object> entry : subjectClaims.entrySet()) {
            String salt = urlSafeBase64(randomBytes(16));
            String disclosureJson = buildDisclosureJson(salt, entry.getKey(), entry.getValue());

            // Optionally corrupt one disclosure for negative testing.
            String disclosureBase64;
            if (corruptDisclosure && claimIndex == 0) {
                // Replace with a disclosure whose hash will not match the issuer JWT.
                disclosureBase64 = urlSafeBase64(("CORRUPTED_" + disclosureJson)
                        .getBytes(StandardCharsets.UTF_8));
            } else {
                disclosureBase64 = urlSafeBase64(disclosureJson.getBytes(StandardCharsets.UTF_8));
            }
            disclosures.add(disclosureBase64);

            // Hash of the *real* (non-corrupted) disclosure is placed in issuer JWT.
            String realBase64 = urlSafeBase64(disclosureJson.getBytes(StandardCharsets.UTF_8));
            byte[] hash = sha256.digest(realBase64.getBytes(StandardCharsets.US_ASCII));
            sdHashes.add(urlSafeBase64(hash));
            claimIndex++;
        }

        // 2. Generate holder key pair and build the cnf claim.
        ECKey holderKey = generateSigningKey();
        Map<String, Object> cnf = new LinkedHashMap<>();
        cnf.put("jwk", holderKey.toPublicJWK().toJSONObject());

        // 3. Build issuer JWT.
        long nowMs = System.currentTimeMillis();
        Date iat = new Date(nowMs + issuedAtOffsetSeconds * 1000L);
        Date exp = new Date(nowMs + expiresInSeconds * 1000L);

        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .issueTime(iat)
                .expirationTime(exp)
                .claim("vct", vct)
                .claim("_sd", sdHashes)
                .claim("_sd_alg", "sha-256")
                .claim("cnf", cnf);

        JWSHeader issuerHeader = new JWSHeader.Builder(JWSAlgorithm.ES256)
                .keyID(signingKey.getKeyID())
                .type(new JOSEObjectType("dc+sd-jwt"))
                .build();

        SignedJWT issuerJwt = new SignedJWT(issuerHeader, claimsBuilder.build());
        issuerJwt.sign(new ECDSASigner(signingKey));
        String issuerJwtStr = issuerJwt.serialize();

        // 4. Assemble the disclosure string (without trailing tilde).
        String disclosureChain = String.join("~", disclosures);

        if (omitKbJwt) {
            // Return token without a KB-JWT (invalid format for negative tests).
            return issuerJwtStr + "~" + disclosureChain + "~";
        }

        // 5. Compute sd_hash for the KB-JWT.
        //    sd_hash = Base64url(SHA-256("<issuer-JWT>~<disclosures>~"))
        //    The SD-JWT separator is always '~', never '.'.
        String sdJwtWithoutKb = issuerJwtStr + "~" + disclosureChain + "~";
        byte[] sdHashBytes = sha256.digest(sdJwtWithoutKb.getBytes(StandardCharsets.US_ASCII));
        String sdHash = urlSafeBase64(sdHashBytes);

        // 6. Build KB-JWT.
        JWTClaimsSet.Builder kbBuilder = new JWTClaimsSet.Builder()
                .issueTime(new Date())
                .claim("sd_hash", sdHash);
        if (nonce != null) {
            kbBuilder.claim("nonce", nonce);
        }
        if (audience != null) {
            kbBuilder.audience(audience);
        }

        JWSHeader kbHeader = new JWSHeader.Builder(JWSAlgorithm.ES256)
                .keyID(holderKey.getKeyID())
                .type(new JOSEObjectType("kb+jwt"))
                .build();

        SignedJWT kbJwt = new SignedJWT(kbHeader, kbBuilder.build());
        kbJwt.sign(new ECDSASigner(holderKey));

        // 7. Assemble final token: <issuer-JWT>~<disclosures>~<kb-JWT>
        return issuerJwtStr + "~" + disclosureChain + "~" + kbJwt.serialize();
    }

    /**
     * Build a VP token JSON object that wraps the SD-JWT-VC.
     *
     * <p>Format per DCQL:
     * <pre>{@code {"<credentialId>": "<sd_jwt_vc_token>"}}</pre>
     *
     * @param credentialId The DCQL credential query ID used as the JSON key.
     * @return JSON string representing the VP token.
     * @throws Exception If token construction fails.
     */
    public String buildVpToken(String credentialId) throws Exception {

        String sdJwtVc = build();
        // Simple JSON construction — avoids pulling in a JSON library dependency.
        return "{\"" + credentialId + "\": \"" + sdJwtVc + "\"}";
    }

    private static String buildDisclosureJson(String salt, String claimName, Object claimValue) {

        String valueStr;
        if (claimValue instanceof Number || claimValue instanceof Boolean) {
            valueStr = claimValue.toString();
        } else {
            valueStr = "\"" + claimValue + "\"";
        }
        return "[\"" + salt + "\",\"" + claimName + "\"," + valueStr + "]";
    }

    private static String urlSafeBase64(byte[] bytes) {

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static byte[] randomBytes(int count) {

        byte[] bytes = new byte[count];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }

    private static MessageDigest sha256Digest() {

        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
