/*
 *Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */

package org.wso2.sample.identity.oauth2;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jwt.EncryptedJWT;
import net.minidev.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.ParseException;
import java.util.Base64;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet class for decrypting id tokens.
 */
@WebServlet(name = "IDTokenDecrypterServlet")
public class IDTokenDecrypterServlet extends HttpServlet {

    private static Logger LOGGER = Logger.getLogger(IDTokenDecrypterServlet.class.getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String idToken = request.getParameter("idToken");
        String privateKeyString = request.getParameter("privateKeyString");

        ServletOutputStream out = response.getOutputStream();

        if (StringUtils.isBlank(privateKeyString)) {
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
            out.print("Client private key cannot be empty!");
        } else if (StringUtils.isBlank(idToken)) {
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
            out.print("Error occurred while decrypting: Empty id token received!");
        } else {
            response.setContentType("application/json");

            EncryptedJWT encryptedJWT;
            try {
                encryptedJWT = decryptJWE(idToken, privateKeyString);
                JSONObject outJSON = new JSONObject();
                JSONObject claimsJSON = new JSONObject();

                // Get all claims set to a map and return a JSON object.
                Map<String, Object> allClaims = encryptedJWT.getJWTClaimsSet().getAllClaims();
                for (Map.Entry<String, Object> entry : allClaims.entrySet()) {
                    claimsJSON.put(entry.getKey(), entry.getValue());
                }
                outJSON.put("claims", claimsJSON);

                // Get JWT header data.
                outJSON.put("header", encryptedJWT.getHeader().toJSONObject());

                out.print(outJSON.toString());
            } catch (NoSuchAlgorithmException | ParseException | JOSEException | IllegalArgumentException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                response.setStatus(HttpStatus.SC_BAD_REQUEST);
                out.print("Error occurred while decrypting id token.");
            } catch (InvalidKeySpecException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                response.setStatus(HttpStatus.SC_BAD_REQUEST);
                out.print("Invalid client private key.");
            }
        }
    }

    /**
     * Decrypt the id token using the private key.
     *
     * @param JWE              id token to be decrypted
     * @param privateKeyString client private key as a string
     * @return decrypted id token as an EncryptedJWT object
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws ParseException
     * @throws JOSEException
     * @throws IllegalArgumentException
     */
    private EncryptedJWT decryptJWE(String JWE, String privateKeyString)
            throws NoSuchAlgorithmException, InvalidKeySpecException, ParseException, JOSEException, IllegalArgumentException {

        KeyFactory kf = KeyFactory.getInstance("RSA");
        // Remove EOF characters from key string and generate key object.
        privateKeyString = privateKeyString.replace("\n", "").replace("\r", "");
        PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyString));
        PrivateKey privateKey = kf.generatePrivate(keySpecPKCS8);

        EncryptedJWT jwt = EncryptedJWT.parse(JWE);

        // Create a decrypter with the specified private RSA key.
        RSADecrypter decrypter = new RSADecrypter((RSAPrivateKey) privateKey);

        jwt.decrypt(decrypter);

        return jwt;
    }
}
