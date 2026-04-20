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

package org.wso2.identity.integration.test.passkey;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPoint;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for passkey integration tests.
 */
public abstract class PasskeyClient {

    protected static final String WEBAUTHN_CREATE = "webauthn.create";
    protected static final String WEBAUTHN_GET = "webauthn.get";

    /**
     * Represents a stored passkey credential in the virtual authenticator.
     */
    public static class StoredCredential {

        private final byte[] credentialId;
        private final KeyPair keyPair;
        private final byte[] userHandle;
        private final String userName;
        private int signCount;

        StoredCredential(byte[] credentialId, KeyPair keyPair, byte[] userHandle, String userName) {

            this.credentialId = credentialId;
            this.keyPair = keyPair;
            this.userHandle = userHandle;
            this.userName = userName;
            this.signCount = 0;
        }

        public byte[] getCredentialId() { return credentialId; }
        public KeyPair getKeyPair() { return keyPair; }
        public byte[] getUserHandle() { return userHandle; }
        public String getUserName() { return userName; }
        public int getSignCount() { return signCount; }
        int incrementSignCount() { return ++signCount; }
    }

    private final Map<String, List<StoredCredential>> credentialStore = new LinkedHashMap<>();

    /**
     * Returns all stored credentials for the given rpId.
     */
    protected List<StoredCredential> getCredentialsForRpId(String rpId) {

        return Collections.unmodifiableList(credentialStore.getOrDefault(rpId, Collections.emptyList()));
    }

    /**
     * Finds a stored credential by rpId and userName.
     */
    protected StoredCredential findCredentialByUser(String rpId, String userName) {

        for (StoredCredential c : credentialStore.getOrDefault(rpId, Collections.emptyList())) {
            if (userName.equals(c.getUserName())) {
                return c;
            }
        }
        return null;
    }

    /**
     * Finds a stored credential by rpId and base64url-encoded credentialId.
     */
    protected StoredCredential findCredentialById(String rpId, String credentialId) {

        for (StoredCredential c : credentialStore.getOrDefault(rpId, Collections.emptyList())) {
            if (base64UrlEncode(c.getCredentialId()).equals(credentialId)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Removes all stored credentials. Call in test tear-down.
     */
    protected void clearAllCredentials() {

        credentialStore.clear();
    }

    /**
     * Performs the WebAuthn registration ceremony, stores the credential, and returns
     * the challengeResponse JSON for FINISH_FIDO_ENROLL.
     *
     * @param requestId   from publicKeyCredentialCreationOptions
     * @param challenge   base64url challenge from creation options
     * @param rpId        relying party ID (e.g., "localhost")
     * @param origin      RP origin (e.g., "https://localhost:9443")
     * @param userName    used to retrieve the credential during authentication
     * @param userHandle  base64url-decoded user.id from creation options
     */
    protected String registerPasskey(String requestId, String challenge, String rpId,
            String origin, String userName, byte[] userHandle) throws Exception {

        KeyPair keyPair = generateKeyPair();
        byte[] credentialId = generateCredentialId();

        credentialStore.computeIfAbsent(rpId, k -> new ArrayList<>())
                .add(new StoredCredential(credentialId, keyPair, userHandle, userName));

        byte[] clientDataJSON = buildClientDataJSON(WEBAUTHN_CREATE, challenge, origin);
        byte[] authData = buildAuthenticatorDataForRegistration(rpId, credentialId, (ECPublicKey) keyPair.getPublic());
        byte[] attestationObject = buildAttestationObject(authData);
        return buildRegistrationChallengeResponse(requestId, credentialId, attestationObject, clientDataJSON);
    }

    /**
     * Performs the WebAuthn authentication ceremony using the stored credential for the
     * given user and returns the tokenResponse JSON.
     *
     * @param requestId   from publicKeyCredentialRequestOptions
     * @param challenge   base64url challenge from request options
     * @param rpId        relying party ID
     * @param origin      RP origin
     * @param userName    identifies which stored credential to use
     */
    protected String authenticatePasskey(String requestId, String challenge, String rpId,
            String origin, String userName) throws Exception {

        StoredCredential credential = findCredentialByUser(rpId, userName);
        if (credential == null) {
            throw new IllegalStateException("No stored credential for rpId=" + rpId + ", user=" + userName);
        }
        return buildAuthenticationResponse(requestId, challenge, rpId, origin, credential);
    }

    /**
     * Builds the clientDataJSON bytes for a WebAuthn ceremony.
     */
    protected byte[] buildClientDataJSON(String type, String challenge, String origin) {

        String json = "{\"type\":\"" + type + "\","
                + "\"challenge\":\"" + challenge + "\","
                + "\"origin\":\"" + origin + "\","
                + "\"crossOrigin\":false}";
        return json.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Builds the binary authenticatorData for registration (includes attested credential data).
     *
     * Layout: rpIdHash(32) | flags(1) | signCount(4) | aaguid(16) | credIdLen(2) | credentialId | coseKey
     */
    protected byte[] buildAuthenticatorDataForRegistration(String rpId, byte[] credentialId,
            ECPublicKey publicKey) throws NoSuchAlgorithmException, IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(sha256(rpId.getBytes(StandardCharsets.UTF_8)));
        out.write(0x45);                // flags: UP | UV | AT
        out.write(new byte[]{0, 0, 0, 0}); // signCount
        out.write(new byte[16]);           // aaguid (zeros)
        out.write((byte) ((credentialId.length >> 8) & 0xff));
        out.write((byte) (credentialId.length & 0xff));
        out.write(credentialId);
        out.write(encodeCosePublicKey(publicKey));
        return out.toByteArray();
    }

    /**
     * Builds the binary authenticatorData for an assertion (no attested credential data).
     *
     * Layout: rpIdHash(32) | flags(1) | signCount(4)
     */
    protected byte[] buildAuthenticatorDataForAssertion(String rpId, int signCount)
            throws NoSuchAlgorithmException, IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(sha256(rpId.getBytes(StandardCharsets.UTF_8)));
        out.write(0x05);                // flags: UP | UV
        out.write((signCount >> 24) & 0xff);
        out.write((signCount >> 16) & 0xff);
        out.write((signCount >> 8) & 0xff);
        out.write(signCount & 0xff);
        return out.toByteArray();
    }

    /**
     * CBOR-encodes an EC P-256 public key as a COSE_Key map: {1:2, 3:-7, -1:1, -2:x, -3:y}.
     */
    protected byte[] encodeCosePublicKey(ECPublicKey publicKey) throws IOException {

        ECPoint w = publicKey.getW();
        byte[] x = toFixedLengthUnsignedBytes(w.getAffineX().toByteArray(), 32);
        byte[] y = toFixedLengthUnsignedBytes(w.getAffineY().toByteArray(), 32);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0xa5);
        out.write(0x01); out.write(0x02); // kty: EC2
        out.write(0x03); out.write(0x26); // alg: ES256
        out.write(0x20); out.write(0x01); // crv: P-256
        out.write(0x21); writeCborBytes(out, x); // x
        out.write(0x22); writeCborBytes(out, y); // y
        return out.toByteArray();
    }

    /**
     * Builds an attestationObject with fmt="none".
     *
     * CBOR map: {"fmt":"none","attStmt":{},"authData":{@code authenticatorData}}
     */
    protected byte[] buildAttestationObject(byte[] authenticatorData) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0xa3);
        writeCborText(out, "fmt");     writeCborText(out, "none");
        writeCborText(out, "attStmt"); out.write(0xa0);
        writeCborText(out, "authData"); writeCborBytes(out, authenticatorData);
        return out.toByteArray();
    }

    /**
     * Signs {@code authenticatorData || SHA-256(clientDataJSON)} with the credential's private key.
     */
    protected byte[] signAssertion(byte[] authenticatorData, byte[] clientDataJSON, PrivateKey privateKey)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        byte[] data = concat(authenticatorData, sha256(clientDataJSON));
        Signature sig = Signature.getInstance("SHA256withECDSA");
        sig.initSign(privateKey);
        sig.update(data);
        return sig.sign();
    }

    /**
     * Builds the challengeResponse JSON for scenario=FINISH_FIDO_ENROLL.
     */
    protected String buildRegistrationChallengeResponse(String requestId, byte[] credentialId,
            byte[] attestationObject, byte[] clientDataJSON) {

        return "{\"requestId\":\"" + requestId + "\","
                + "\"credential\":{"
                + "\"id\":\"" + base64UrlEncode(credentialId) + "\","
                + "\"response\":{"
                + "\"attestationObject\":\"" + base64UrlEncode(attestationObject) + "\","
                + "\"clientDataJSON\":\"" + base64UrlEncode(clientDataJSON) + "\"},"
                + "\"clientExtensionResults\":{\"credProps\":{\"rk\":true}},"
                + "\"type\":\"public-key\"}}";
    }

    /**
     * Builds the tokenResponse JSON for passkey authentication.
     */
    protected String buildLoginTokenResponse(String requestId, byte[] credentialId,
            byte[] authenticatorData, byte[] clientDataJSON, byte[] signature, byte[] userHandle) {

        return "{\"requestId\":\"" + requestId + "\","
                + "\"credential\":{"
                + "\"id\":\"" + base64UrlEncode(credentialId) + "\","
                + "\"response\":{"
                + "\"authenticatorData\":\"" + base64UrlEncode(authenticatorData) + "\","
                + "\"clientDataJSON\":\"" + base64UrlEncode(clientDataJSON) + "\","
                + "\"signature\":\"" + base64UrlEncode(signature) + "\","
                + "\"userHandle\":\"" + base64UrlEncode(userHandle) + "\"},"
                + "\"clientExtensionResults\":{},"
                + "\"type\":\"public-key\"}}";
    }

    protected KeyPair generateKeyPair() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {

        KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
        gen.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());
        return gen.generateKeyPair();
    }

    protected byte[] generateCredentialId() {

        byte[] id = new byte[16];
        new SecureRandom().nextBytes(id);
        return id;
    }

    protected String base64UrlEncode(byte[] data) {

        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    protected byte[] base64UrlDecode(String data) {

        return Base64.getUrlDecoder().decode(data);
    }

    protected byte[] sha256(byte[] data) throws NoSuchAlgorithmException {

        return MessageDigest.getInstance("SHA-256").digest(data);
    }

    private String buildAuthenticationResponse(String requestId, String challenge, String rpId,
            String origin, StoredCredential credential) throws Exception {

        int signCount = credential.incrementSignCount();
        byte[] clientDataJSON = buildClientDataJSON(WEBAUTHN_GET, challenge, origin);
        byte[] authData = buildAuthenticatorDataForAssertion(rpId, signCount);
        byte[] signature = signAssertion(authData, clientDataJSON, credential.getKeyPair().getPrivate());
        return buildLoginTokenResponse(requestId, credential.getCredentialId(),
                authData, clientDataJSON, signature, credential.getUserHandle());
    }

    private void writeCborBytes(ByteArrayOutputStream out, byte[] data) throws IOException {

        if (data.length <= 23) {
            out.write(0x40 | data.length);
        } else if (data.length <= 0xff) {
            out.write(0x58);
            out.write(data.length);
        } else {
            out.write(0x59);
            out.write((data.length >> 8) & 0xff);
            out.write(data.length & 0xff);
        }
        out.write(data);
    }

    private void writeCborText(ByteArrayOutputStream out, String text) throws IOException {

        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= 23) {
            out.write(0x60 | bytes.length);
        } else {
            out.write(0x78);
            out.write(bytes.length);
        }
        out.write(bytes);
    }

    private byte[] toFixedLengthUnsignedBytes(byte[] signed, int length) {

        byte[] result = new byte[length];
        if (signed.length <= length) {
            System.arraycopy(signed, 0, result, length - signed.length, signed.length);
        } else {
            System.arraycopy(signed, signed.length - length, result, 0, length);
        }
        return result;
    }

    private byte[] concat(byte[] a, byte[] b) {

        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
