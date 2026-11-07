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

import com.nimbusds.jose.jwk.ECKey;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTCTime;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.wso2.identity.integration.test.restclients.SCIM2RestClient;

import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Shared static utilities for OID4VP integration tests.
 */
final class VPTestUtils {

    private static final Logger LOG = Logger.getLogger(VPTestUtils.class.getName());

    private VPTestUtils() {
    }

    /**
     * Reads a classpath resource into a UTF-8 string.
     *
     * @param resourcePath Classpath-relative path to the resource.
     * @return The file contents as a UTF-8 string.
     * @throws Exception If the resource cannot be found or read.
     */
    static String readResourceAsString(String resourcePath) throws Exception {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = VPTestUtils.class.getClassLoader();
        }
        try (InputStream in = cl.getResourceAsStream(resourcePath)) {
            Assert.assertNotNull(in, "Test resource not found on classpath: " + resourcePath);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Builds a minimal self-signed PEM certificate wrapping the EC public key.
     * Used as the {@code keySource} when registering a trusted issuer.
     *
     * @param ecKey The EC key whose public part to certify.
     * @return PEM-encoded certificate string.
     * @throws Exception If certificate construction or signing fails.
     */
    static String buildSelfSignedPemCertificate(ECKey ecKey) throws Exception {

        X500Name dn = new X500Name("CN=test-issuer");
        AlgorithmIdentifier sigAlgId = new AlgorithmIdentifier(X9ObjectIdentifiers.ecdsa_with_SHA256);
        SubjectPublicKeyInfo spki = SubjectPublicKeyInfo.getInstance(ecKey.toECPublicKey().getEncoded());

        Date notBefore = new Date(System.currentTimeMillis() - 86400000L);
        Date notAfter = new Date(System.currentTimeMillis() + 365L * 86400000L);
        ASN1EncodableVector validityVec = new ASN1EncodableVector();
        validityVec.add(new DERUTCTime(notBefore));
        validityVec.add(new DERUTCTime(notAfter));

        ASN1EncodableVector tbsVec = new ASN1EncodableVector();
        tbsVec.add(new DERTaggedObject(true, 0, new ASN1Integer(2)));
        tbsVec.add(new ASN1Integer(BigInteger.ONE));
        tbsVec.add(sigAlgId);
        tbsVec.add(dn);
        tbsVec.add(new DERSequence(validityVec));
        tbsVec.add(dn);
        tbsVec.add(spki);
        DERSequence tbsCert = new DERSequence(tbsVec);

        java.security.Signature sig = java.security.Signature.getInstance("SHA256withECDSA");
        sig.initSign(ecKey.toECPrivateKey());
        sig.update(tbsCert.getEncoded());
        byte[] sigBytes = sig.sign();

        ASN1EncodableVector certVec = new ASN1EncodableVector();
        certVec.add(tbsCert);
        certVec.add(sigAlgId);
        certVec.add(new DERBitString(sigBytes));
        byte[] certBytes = new DERSequence(certVec).getEncoded();

        String base64 = java.util.Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(certBytes);
        return "-----BEGIN CERTIFICATE-----\n" + base64 + "\n-----END CERTIFICATE-----";
    }

    /**
     * Deletes all local users whose {@code userName} matches the given value.
     * No-op if none exist.  Errors are logged as warnings and swallowed so that
     * pre-test cleanup never fails the test class itself.
     *
     * @param userName      The exact userName to match (SCIM filter: {@code userName eq "..."}).
     * @param scim2Client   The SCIM2 client to use.
     */
    static void deleteUserIfExists(String userName, SCIM2RestClient scim2Client) {

        try {
            String filter = java.net.URLEncoder.encode(
                    "userName eq \"" + userName + "\"", StandardCharsets.UTF_8);
            JSONObject result = new JSONObject(scim2Client.filterUsers(filter).toJSONString());
            if (result.optLong("totalResults", 0) == 0) {
                return;
            }
            JSONArray resources = result.optJSONArray("Resources");
            if (resources == null) {
                return;
            }
            for (int i = 0; i < resources.length(); i++) {
                scim2Client.deleteUser(resources.getJSONObject(i).getString("id"));
            }
        } catch (Exception e) {
            LOG.warning("Pre-cleanup: could not delete user '" + userName + "': " + e.getMessage());
        }
    }
}
