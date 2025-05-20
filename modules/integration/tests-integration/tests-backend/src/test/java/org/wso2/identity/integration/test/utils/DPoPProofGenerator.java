/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import org.apache.commons.lang.StringUtils;

import static com.nimbusds.jose.JWSAlgorithm.ES256;
import static com.nimbusds.jose.JWSAlgorithm.RS384;

public class DPoPProofGenerator {


    private static final String ECDSA_ENCRYPTION = "EC";
    private static final String RSA_ENCRYPTION = "RSA";
    private static final String DUMMY_JTI = "dummyJti";
    private static final String DUMMY_HTTP_METHOD = "POST";
    private static final String DUMMY_HTTP_URL = "https://localhost:9443/oauth2/token";

    public static final String ACCESS_TOKEN =
            "eyJ4NXQiOiJyVDVGYi1Cd1doWUZYWE9JQjlnaG9NZDhfSWsiLCJraWQiOiJPV1JpTXpa" +
                    "aVlURXhZVEl4WkdGa05UVTJOVE0zTWpkaFltTmxNVFZrTnpRMU56a3paVGc1TVRrNE0y" +
                    "WmxOMkZoWkdaalpURmlNemxsTTJJM1l6ZzJNZ19SUzI1NiIsInR5cCI6ImF0K2p3dCIs" +
                    "ImFsZyI6IlJTMjU2In0.eyJzdWIiOiI5MTAwMjEwZC1mOWRlLTQyMGYtYmQzMy0wZDJl" +
                    "NDdhMzBmMWEiLCJhdXQiOiJBUFBMSUNBVElPTl9VU0VSIiwiYmluZGluZ190eXBlIjoi" +
                    "RFBvUCIsImlzcyI6Imh0dHBzOlwvXC9sb2NhbGhvc3Q6OTQ0M1wvb2F1dGgyXC90b2tl" +
                    "biIsImNsaWVudF9pZCI6InFNeFZKcXFRZGUzYTJQQTR2ZjdYUFJ0dHVRZ2EiLCJhdWQi" +
                    "OiJxTXhWSnFxUWRlM2EyUEE0dmY3WFBSdHR1UWdhIiwibmJmIjoxNzEzMTY2ODk0LCJh" +
                    "enAiOiJxTXhWSnFxUWRlM2EyUEE0dmY3WFBSdHR1UWdhIiwib3JnX2lkIjoiMTAwODRh" +
                    "OGQtMTEzZi00MjExLWEwZDUtZWZlMzZiMDgyMjExIiwic2NvcGUiOiJvcGVuaWQiLCJj" +
                    "bmYiOnsiamt0IjoiUERmaEM3Tmd5LXRjTU1qNUtaLVI1QU9ESnJySTJ5NmNOTXVPRE81" +
                    "VWlLQSJ9LCJleHAiOjE3MTMxNzA0OTQsIm9yZ19uYW1lIjoiU3VwZXIiLCJpYXQiOjE3" +
                    "MTMxNjY4OTQsImJpbmRpbmdfcmVmIjoiNzcwNGFkNmRlMThjZDFmNmRjZjBiNDI4Yzc0" +
                    "MjNlNTQiLCJqdGkiOiJkNTAyMTZjOC04OWE1LTQ0OGItODYwOC1lZmMwY2E2MmM3Yjki" +
                    "fQ.eFLih6yMjruvded38eGb9Sopr_a3lJKKLIalZkp2QTChZTtba67Gue_yZ2OkK-0Ki" +
                    "TsFx9MU0wi1dVzEr-2UQSj6Mt7pyp5y_bQp4kP2OY7RgNgMIXTDnHh6PQ4Ve5W0UmcNF" +
                    "so4Uc3uQPvbQuLoYomTxDqnWebXMbWFqdu1Df_gIbaYJEjaDPkj91x-86ajU41wDKb1S" +
                    "4sGzh4HE_f5akMWVb5D0p6szJ-9ieM-HEYcv0zs-0OiwgVPxdpT_uIy2GL9ca6eIeHSB" +
                    "Ime_l_8fqNnkYB0LQD9hIzvdNDOpQhKallPucchkjUF3tXXEnFQPe6xT-Qc1y-OhTWk-" +
                    "ActXw";

    public static final String ACCESS_TOKEN_HASH = "AGYGxGwNMSqZMpwTtCJsKPP42Q8paPyfMWshrnoZFe0";

    private static final String rsaPublicKey =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAt9x8A/JZb313HsuwnUNMat52cNQSo" +
                    "I7HfHtv2IwM7QFtuq/HzMLwlYajYPIkaCiIhG67vGStNQAYPUG+z7fW6uXI3cLX+9ws2moPwj" +
                    "SnPhCf/UFmwRUXSSXNBUthVWTFJeUIYQ/WldeZyOD4LGpc+OhxHkj4PQvz2nZUhYM0vu163a8" +
                    "NbKvC3IQ+pbFOmW9mnGCSO2YqPN/zS1G1X76CdGxtJzVIpdjj4/HgoKCo+RAysMnnKDQz3+lm" +
                    "d+kQBqXzvVx0ZNuPY/B7nBzT6kvKqNBRwduPwzEgkH3rBpIBv0Ve+pHdI6Tm/2c6bC1NRlu+b" +
                    "/g8CeZDE0tZ4IyhTVsAIQIDAQAB";

    private static final String rsaPrivateKey =
            "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC33HwD8llvfXcey7CdQ0xq3" +
                    "nZw1BKgjsd8e2/YjAztAW26r8fMwvCVhqNg8iRoKIiEbru8ZK01ABg9Qb7Pt9bq5cjdwtf73C" +
                    "zaag/CNKc+EJ/9QWbBFRdJJc0FS2FVZMUl5QhhD9aV15nI4Pgsalz46HEeSPg9C/PadlSFgzS" +
                    "+7Xrdrw1sq8LchD6lsU6Zb2acYJI7Zio83/NLUbVfvoJ0bG0nNUil2OPj8eCgoKj5EDKwyeco" +
                    "NDPf6WZ36RAGpfO9XHRk249j8HucHNPqS8qo0FHB24/DMSCQfesGkgG/RV76kd0jpOb/ZzpsL" +
                    "U1GW75v+DwJ5kMTS1ngjKFNWwAhAgMBAAECggEADrgFa5F2rHC4XQxEZsqQ7wtBIxYvKZBUkv" +
                    "gUw5qunDidjrDsx00h0m6VXLj1xirchvGQcOwEW7ZWumyteFaIy4Q6uNoUzVJaet+7xDnP262" +
                    "cCTu3nKRyGUZ/67kVoS7wg3Ca455PeO5qHsU3yOJ47+o3yAtiaAyxaF9Js+iFi/U3JCM54S9s" +
                    "OiTP7j62O72CZhqZcQcmZcbxXzJl/4F9pIJvMaj5IAWt7KNZGlZ62aa1G+cXWghcCcgQf7k7I" +
                    "eWAPbHl1eviXDxGo9mIG41NMCklZOpdmQkTStsEVzALI+jx9miqv1Beenb+hHoK7oiKTFCl9f" +
                    "vB1yFunJl1zHEWeQKBgQDIODvXMz34i74kzVZzyUkBve10J2xjTqSTB0XZVUEJeWmsWWLUbvy" +
                    "xmeLrVOMMPwLQi5JmloAcUdfXSUkU73fOGDJ8K1Z0Tm8NHIl2UBgWmZfIpg/rZDqb195cqWZz" +
                    "/nf1Nko8WJXFGwBmeLPR4HVvIa7HeSglUjCGY0QKBefiLQKBgQDrFY/N9HezKMjtaj5JYjRLL" +
                    "FNf3wE/CqmYa9+w6U96AVNswD/DlCPnCGRo7fpmobm2brEDKVJm78ZBfMIL3p76O6OjFlQT+o" +
                    "P/2dAE6hU4MlYmr+w2Mqxut+BSNv6AHgtdKRUxAY6Ld5EocKdabwaL9t/TiY8pAcS9rT63DI9" +
                    "yRQKBgQC+S/xMOG7RGXian/NoT0qtdigHOyUgafGvsLzpqMcMyzHt1nNBd0+DOcDcbSzzSbxS" +
                    "HCYEjUysHfmorAXi+QuEfakWLVaZaqbP7myUX+HVMRx7X6JH11aBIrY8meE/o/+9t2DtZEDNO" +
                    "zGxM02tz8mt23S0MGpAtpJaWGSlpiFT7QKBgDoEUj8z7C6tDBl7tO+Lavh6cgEhGj+itARH6y" +
                    "bQDatAlIQsVhBAiTPFYHJ8+OVHWHvriYgMNKfu2PDkh0dCo92BxnrDUfC0TMthx/LOinoaAiT" +
                    "+Gb+uddvFSXlA1UJtJ8TQFMjJZ5KH6a0fUE4DRIxaWxbrxgcKxrFBBk9KrEQ5AoGBAKcFnoup" +
                    "LWTebgLlQ2ox80sXTdCdweSdHv8tIAZGZUs97BcPpTujyVldx7bRgLpcV93FpBafPIN5FjU1H" +
                    "uihfak3h0SQi2WxyCJpZiH+XNK/9tabN2MKQji7wjbrQRN06jNuUXeo6X18vcBVVVj2TogFJL" +
                    "fQqNUgIMZN35pyBtja";


    private static final String ecPublicKey =
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE+6F4irv76jwSiLHebVzksLfjtXYplS9RwmvJF" +
                    "dRp+rcZtUIbQLcscH1SjsIigl4Ha80CG14Y0OofBVwwS7IAjQ==";

    private static final String ecPrivateKey =
            "MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCAfBD6WwWEgj5FQ01/2zqR4NNu/i" +
                    "MEB/6fwhaMMh3GQFw==";

    private static final String DPOP_JWT_TYPE = "dpop+jwt";

    public static final String EC_DPOP_JWK_THUMBPRINT = "C07a9MZgz5wYywPc39Tw81gE8QzhkpC14sjx-2pAwbI";

    public static final String RSA_DPOP_JWK_THUMBPRINT = "_Z3DHS03lCZVeRs-J9fO7JHuTE0BmVYuBF6Rdc5qjII";

    /*
     * Generate a DPoP proof with the default values.
     * @return DPoP proof.
     * @throws NoSuchAlgorithmException
     * @throws JOSEException
     * @throws InvalidKeySpecException
     */
    public static String genarateDPoPProof()
            throws NoSuchAlgorithmException, JOSEException, InvalidKeySpecException {

        return genarateDPoPProof("RSA", DUMMY_JTI, DUMMY_HTTP_METHOD, DUMMY_HTTP_URL,
                new Date(System.currentTimeMillis()),  ACCESS_TOKEN_HASH, DPOP_JWT_TYPE);
    }

    /*
     * Generate a DPoP proof by passing keyPairType, jti, httpMethod, httpUrl.
     * @param keyPairType
     * @param jti
     * @param httpMethod
     * @param httpUrl
     * @return DPoP proof.
     * @throws NoSuchAlgorithmException
     * @throws JOSEException
     * @throws InvalidKeySpecException
     */
    public static String genarateDPoPProof(String keyPairType, String jti, String httpMethod, String httpUrl)
            throws NoSuchAlgorithmException, JOSEException, InvalidKeySpecException {

        return genarateDPoPProof(keyPairType, jti, httpMethod, httpUrl, new Date(System.currentTimeMillis()),
                ACCESS_TOKEN_HASH, DPOP_JWT_TYPE);
    }

    /*
     * Generate a DPoP proof by passing keyPairType, jti, httpMethod, httpUrl, iat.
     * @param keyPairType
     * @param jti
     * @param httpMethod
     * @param httpUrl
     * @param iat
     * @return DPoP proof.
     * @throws NoSuchAlgorithmException
     * @throws JOSEException
     * @throws InvalidKeySpecException
     */
    public static String genarateDPoPProof(String keyPairType, String jti, String httpMethod, String httpUrl, Date iat)
            throws NoSuchAlgorithmException, JOSEException, InvalidKeySpecException {

        return genarateDPoPProof(keyPairType, jti, httpMethod, httpUrl, iat, ACCESS_TOKEN_HASH,
                DPOP_JWT_TYPE);
    }

    public static String genarateDPoPProof(String keyPairType, String jti, String httpMethod, String httpUrl, Date iat,
                                           String dpopJwtType)
            throws NoSuchAlgorithmException, JOSEException, InvalidKeySpecException {

        return genarateDPoPProof(keyPairType, jti, httpMethod, httpUrl, iat, null, dpopJwtType);
    }

    /*
     * Generate a DPoP proof by passing keyPairType, jti, httpMethod, httpUrl, iat, accessTokenHash, jwtType.
     * @param keyPairType
     * @param jti
     * @param httpMethod
     * @param httpUrl
     * @param iat
     * @param accessTokenHash
     * @param jwtType
     * @return DPoP proof.
     * @throws NoSuchAlgorithmException
     * @throws JOSEException
     * @throws InvalidKeySpecException
     */
    public static String genarateDPoPProof(String keyPairType, String jti, String httpMethod, String httpUrl, Date iat,
                                           String accessToken, String jwtType)
            throws NoSuchAlgorithmException, JOSEException, InvalidKeySpecException {

        /* Read all bytes from the private key file */
        String privateKeyString = keyPairType.equals("RSA") ? rsaPrivateKey : ecPrivateKey;
        byte[] bytes = Base64.getDecoder().decode(privateKeyString);

        /* Generate private key. */
        PKCS8EncodedKeySpec privateKs = new PKCS8EncodedKeySpec(bytes);
        KeyFactory privateKf = KeyFactory.getInstance(keyPairType);
        PrivateKey privateKey = privateKf.generatePrivate(privateKs);

        /* Read all the public key bytes */
        String publicKeyString = keyPairType.equals("RSA") ? rsaPublicKey : ecPublicKey;
        byte[] pubBytes = Base64.getDecoder().decode(publicKeyString);

        /* Generate public key. */
        X509EncodedKeySpec ks = new X509EncodedKeySpec(pubBytes);
        KeyFactory kf = KeyFactory.getInstance(keyPairType);
        PublicKey publicCert = kf.generatePublic(ks);

        JWK jwk;
        if ("EC".equals(keyPairType)) {
            jwk = new ECKey.Builder(Curve.P_256, (ECPublicKey) publicCert).build();
        } else {
            jwk = new RSAKey.Builder((RSAPublicKey) publicCert).build();
        }

        JWTClaimsSet.Builder jwtClaimsSetBuilder = new JWTClaimsSet.Builder();
        jwtClaimsSetBuilder.issueTime(iat);
        jwtClaimsSetBuilder.jwtID(jti);
        jwtClaimsSetBuilder.claim("htm", httpMethod);
        jwtClaimsSetBuilder.claim("htu", httpUrl);


        if (accessToken != null && !accessToken.isEmpty()) {
            jwtClaimsSetBuilder.claim("ath", generateAccessTokenHash(accessToken));
        }

        JWSHeader.Builder headerBuilder;
        if ("EC".equals(keyPairType)) {
            headerBuilder = new JWSHeader.Builder(ES256);
        } else {
            headerBuilder = new JWSHeader.Builder(RS384);
        }
        headerBuilder.type(new JOSEObjectType(jwtType));
        headerBuilder.jwk(jwk);
        SignedJWT signedJWT = new SignedJWT(headerBuilder.build(), jwtClaimsSetBuilder.build());

        if ("EC".equals(keyPairType)) {
            ECDSASigner ecdsaSigner = new ECDSASigner(privateKey, Curve.P_256);
            signedJWT.sign(ecdsaSigner);
        } else {
            RSASSASigner rsassaSigner = new RSASSASigner(privateKey);
            signedJWT.sign(rsassaSigner);
        }
        return signedJWT.serialize();
    }

    private static String generateAccessTokenHash(String accessToken) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(accessToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found.", e);
        }
    }

    public static String getThumbprintOfKeyFromDpopProof(String dPopProof) throws Exception {

        try {
            SignedJWT signedJwt = SignedJWT.parse(dPopProof);
            JWSHeader header = signedJwt.getHeader();
            return getKeyThumbprintOfKey(header.getJWK().toString(), signedJwt);
        } catch (Exception e) {
            throw new Exception("Error while getting the thumbprint value.", e);
        }
    }

    private static String getKeyThumbprintOfKey(String jwk, SignedJWT signedJwt)
            throws ParseException, JOSEException {

        JWK parseJwk = JWK.parse(jwk);
        boolean validSignature;
        if (ECDSA_ENCRYPTION.equalsIgnoreCase(String.valueOf(parseJwk.getKeyType()))) {
            ECKey ecKey = (ECKey) parseJwk;
            ECPublicKey ecPublicKey = ecKey.toECPublicKey();
            validSignature = verifySignatureWithPublicKey(new ECDSAVerifier(ecPublicKey), signedJwt);
            if (validSignature) {
                return computeThumbprintOfECKey(ecKey);
            }
        } else if (RSA_ENCRYPTION.equalsIgnoreCase(String.valueOf(parseJwk.getKeyType()))) {
            RSAKey rsaKey = (RSAKey) parseJwk;
            RSAPublicKey rsaPublicKey = rsaKey.toRSAPublicKey();
            validSignature = verifySignatureWithPublicKey(new RSASSAVerifier(rsaPublicKey), signedJwt);
            if (validSignature) {
                return computeThumbprintOfRSAKey(rsaKey);
            }
        }
        return StringUtils.EMPTY;
    }

    private static String computeThumbprintOfRSAKey(RSAKey rsaKey) throws JOSEException {

        return rsaKey.computeThumbprint().toString();
    }

    private static String computeThumbprintOfECKey(ECKey ecKey) throws JOSEException {

        return ecKey.computeThumbprint().toString();
    }

    private static boolean verifySignatureWithPublicKey(JWSVerifier jwsVerifier, SignedJWT signedJwt)
            throws JOSEException {

        return signedJwt.verify(jwsVerifier);
    }
}