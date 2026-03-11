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

import java.util.Date;

/**
 * Utility class for generating OID4VCI proof JWTs as defined in OID4VCI Draft 16 §7.2.1.1.
 */
public class OID4VCIProofJWTGenerator {

    private OID4VCIProofJWTGenerator() {

    }

    /**
     * Generate a fresh P-256 EC key pair to use as the holder binding key.
     * The key ID is set to the SHA-256 thumbprint of the JWK.
     *
     * @return Generated EC key pair.
     * @throws JOSEException If key generation fails.
     */
    public static ECKey generateECKeyPair() throws JOSEException {

        return new ECKeyGenerator(Curve.P_256)
                .keyIDFromThumbprint(true)
                .generate();
    }

    /**
     * Generate a signed OID4VCI proof JWT per Draft 16 §7.2.1.1.
     *
     * @param ecKey               EC key pair for signing (holder key).
     * @param credentialIssuerUrl Credential issuer URL — used as the JWT audience.
     * @param clientId            OAuth2 client ID — used as the JWT issuer.
     * @param cNonce              Server-issued nonce from the nonce endpoint.
     * @return Serialized compact JWT string.
     * @throws JOSEException If signing fails.
     */
    public static String generateProofJWT(ECKey ecKey, String credentialIssuerUrl, String clientId, String cNonce)
            throws JOSEException {

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256)
                .type(new JOSEObjectType("openid4vci-proof+jwt"))
                .jwk(ecKey.toPublicJWK())
                .build();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(clientId)
                .audience(credentialIssuerUrl)
                .issueTime(new Date())
                .claim("nonce", cNonce)
                .build();

        SignedJWT signedJWT = new SignedJWT(header, claims);
        signedJWT.sign(new ECDSASigner(ecKey));
        return signedJWT.serialize();
    }
}
