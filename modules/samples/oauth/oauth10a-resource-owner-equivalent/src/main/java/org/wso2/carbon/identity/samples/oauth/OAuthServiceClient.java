/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.identity.samples.oauth;

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.Service;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.client.authn.oauth.OAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthUtil;
import org.apache.axiom.om.util.Base64;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceStub;
import org.wso2.carbon.identity.oauth.stub.OAuthServiceStub;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.oauth.stub.types.Parameters;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLDecoder;
import java.util.UUID;


public class OAuthServiceClient {

    private String username;
    private String password;
    private String backendServerURL;
    private ConfigurationContext configCtx;

    private Parameters params = new Parameters();
    private String consumerSecret;

    public OAuthServiceClient(String backendServerURL, ConfigurationContext configCtx,
                                       String username, String password) {

        this.backendServerURL = backendServerURL;
        this.configCtx = configCtx;
        this.username = username;
        this.password = password;
    }

    public void registerOAuthApplicationData (String applicationName, String oauthVersion,
                                          String consumerKey, String consumerSecret) throws Exception {

        if(consumerKey == null){
            consumerKey = getRandomNumber();
        }
        if(consumerSecret == null){
            consumerSecret = getRandomNumber();
        }
        if(oauthVersion == null){
            oauthVersion = "OAuth-1.0a";
        }

        params.setAppName(applicationName);
        params.setVersion(oauthVersion);
        params.setOauthConsumerKey(consumerKey);
        this.consumerSecret = consumerSecret;

        String serviceURL = null;
        ServiceClient client = null;
        Options option = null;
        OAuthAdminServiceStub oauth = null;

        serviceURL = backendServerURL + "services/OAuthAdminService";
        oauth = new OAuthAdminServiceStub(configCtx, serviceURL);
        client = oauth._getServiceClient();
        option = client.getOptions();
        option.setManageSession(true);
        HttpTransportProperties.Authenticator authenticator = new HttpTransportProperties.Authenticator();
        authenticator.setUsername(username);
        authenticator.setPassword(password);
        authenticator.setPreemptiveAuthentication(true);
        option.setProperty(HTTPConstants.AUTHENTICATE, authenticator);
        OAuthConsumerAppDTO consumerApp = new OAuthConsumerAppDTO();
        consumerApp.setApplicationName(applicationName);
        consumerApp.setOAuthVersion(oauthVersion);
        consumerApp.setOauthConsumerKey(consumerKey);
        consumerApp.setOauthConsumerSecret(consumerSecret);
        oauth.registerOAuthApplicationData(consumerApp);
        System.out.println(applicationName + " registered as OAuth-1.0a application. " +
                "Consumer Key: " + consumerKey + " and Consumer Secret " + consumerSecret + ".\n");
    }

    public void getRequestToken(String scope) throws Exception {

        if(scope != null){
            params.setScope(scope);
        }

        Service.GDataRequest request = null;
        GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
        oauthParameters.setOAuthConsumerKey(params.getOauthConsumerKey());
        oauthParameters.setOAuthConsumerSecret(consumerSecret);
        oauthParameters.setOAuthType(OAuthParameters.OAuthType.TWO_LEGGED_OAUTH);
        OAuthHmacSha1Signer signer = new OAuthHmacSha1Signer();
        GoogleService service = new GoogleService(params.getAppName(), params.getAppName());
        service.setOAuthCredentials(oauthParameters, signer);
        String baseString = backendServerURL + "oauth/request-token";
        if(scope != null){
            baseString += "?scope=" + scope;
        }
        URL feedUrl = new URL(baseString);
        request = service.createFeedRequest(feedUrl);
        request.execute();
        Parameters params = populateOauthConsumerData(convertStreamToString(request.getResponseStream()));
        if(params.getOauthToken() != null && params.getOauthTokenSecret() != null){
            this.params.setOauthToken(params.getOauthToken());
            this.params.setOauthTokenSecret(params.getOauthTokenSecret());
            System.out.println("Request token granted for " + this.params.getAppName() + ". " +
                    "Request token: " + params.getOauthToken() +
                    " and Request token secret: " + params.getOauthTokenSecret() + ".\n");
        } else {
            throw new Exception("");
        }
    }

    public void authorizeRequestToken(String authorizedUser, String authorizedUserPassword) throws Exception {

        if(authorizedUser == null){
            authorizedUser = this.username;
        }
        if(authorizedUserPassword == null){
            authorizedUserPassword = this.password;
        }
        this.params.setAuthorizedbyUserName(authorizedUser);
        this.params.setAuthorizedbyUserPassword(authorizedUserPassword);

        String serviceURL = null;
        ServiceClient client = null;
        Options option = null;
        OAuthServiceStub oauth = null;

        serviceURL = backendServerURL + "services/OAuthService";
        oauth = new OAuthServiceStub(configCtx, serviceURL);
        client = oauth._getServiceClient();
        option = client.getOptions();
        option.setManageSession(true);
        HttpTransportProperties.Authenticator authenticator = new HttpTransportProperties.Authenticator();
        authenticator.setUsername(username);
        authenticator.setPassword(password);
        authenticator.setPreemptiveAuthentication(true);
        option.setProperty(HTTPConstants.AUTHENTICATE, authenticator);

        Parameters params = new Parameters();
        params.setOauthConsumerKey(this.params.getOauthConsumerKey());
        params.setOauthToken(this.params.getOauthToken());
        params.setAuthorizedbyUserName(authorizedUser);
        params.setAuthorizedbyUserPassword(authorizedUserPassword);
        Parameters respParams = oauth.authorizeOauthRequestToken(params);
        if(respParams.getOauthTokenVerifier() != null){
            this.params.setOauthTokenVerifier(respParams.getOauthTokenVerifier());
            System.out.println(this.params.getAppName() + " with request token " + this.params.getOauthToken() +
                    " has been authorized by " + authorizedUser + "." +
                    " Token verifier : " + respParams.getOauthTokenVerifier() + ".\n");
        } else {
            throw new Exception("");
        }
    }

    public void getAccessToken() throws Exception {

        Service.GDataRequest request = null;
        GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
        oauthParameters.setOAuthConsumerKey(params.getOauthConsumerKey());
        oauthParameters.setOAuthConsumerSecret(consumerSecret);
        oauthParameters.setOAuthToken(params.getOauthToken());
        oauthParameters.setOAuthTokenSecret(params.getOauthTokenSecret());
        oauthParameters.setOAuthVerifier(params.getOauthTokenVerifier());
        OAuthHmacSha1Signer signer = new OAuthHmacSha1Signer();
        GoogleService service = new GoogleService(params.getAppName(), params.getAppName());
        service.setOAuthCredentials(oauthParameters, signer);
        String baseString = backendServerURL + "oauth/access-token";
        URL feedUrl = new URL(baseString);
        request = service.createFeedRequest(feedUrl);
        request.execute();
        Parameters params = populateOauthConsumerData(convertStreamToString(request.getResponseStream()));
        if(params.getOauthToken() != null && params.getOauthTokenSecret() != null){
            this.params.setOauthToken(params.getOauthToken());
            this.params.setOauthTokenSecret(params.getOauthTokenSecret());
            System.out.println("Access token granted for " + this.params.getAppName() +
                    " with authorized request token " + this.params.getOauthToken() +
                    " and token verifier " + this.params.getOauthTokenVerifier() + "." +
                    " Access token: " + params.getOauthToken() +
                    " and access token secret " + params.getOauthTokenSecret() + ".\n");
        } else {
            throw new Exception("");
        }
    }

    public boolean validateAuthenticationRequest(String resourceURL)
            throws Exception {

        String serviceURL = null;
        ServiceClient client = null;
        Options option = null;
        OAuthServiceStub oauth = null;

        serviceURL = backendServerURL + "services/OAuthService";
        oauth = new OAuthServiceStub(configCtx, serviceURL);
        client = oauth._getServiceClient();
        option = client.getOptions();
        option.setManageSession(true);
        HttpTransportProperties.Authenticator authenticator = new HttpTransportProperties.Authenticator();
        authenticator.setUsername(username);
        authenticator.setPassword(password);
        authenticator.setPreemptiveAuthentication(true);
        option.setProperty(HTTPConstants.AUTHENTICATE, authenticator);

        OAuthHmacSha1Signer signer = new OAuthHmacSha1Signer();
        GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
        oauthParameters.setOAuthConsumerKey(params.getOauthConsumerKey());
        oauthParameters.setOAuthConsumerSecret(consumerSecret);
        oauthParameters.setOAuthToken(params.getOauthToken());
        oauthParameters.setOAuthTokenSecret(params.getOauthTokenSecret());
        oauthParameters.setOAuthVerifier(params.getOauthTokenVerifier());
//        if(params.getScope() != null){
//            oauthParameters.setScope(params.getScope());
//        }
        oauthParameters.setOAuthTimestamp(OAuthUtil.getTimestamp());
        oauthParameters.setOAuthNonce(OAuthUtil.getNonce());
        oauthParameters.setOAuthSignatureMethod("HMAC-SHA1");
        String baseString = OAuthUtil.
                getSignatureBaseString(resourceURL, "GET", oauthParameters.getBaseParameters());
        String signature = signer.getSignature(baseString, oauthParameters);

        Parameters params = new Parameters();
        params.setOauthConsumerKey(this.params.getOauthConsumerKey());
        params.setOauthToken(this.params.getOauthToken());
        params.setOauthTokenVerifier(this.params.getOauthTokenVerifier());
        params.setScope(this.params.getScope());
        params.setOauthTimeStamp(oauthParameters.getOAuthTimestamp());
        params.setOauthNonce(oauthParameters.getOAuthNonce());
        params.setVersion(this.params.getVersion());
        params.setOauthSignatureMethod(oauthParameters.getOAuthSignatureMethod());
        params.setBaseString(resourceURL);
        params.setHttpMethod("GET");
        params.setOauthSignature(signature);
        oauth.validateAuthenticationRequest(params);
        System.out.println("Access token " + this.params.getOauthToken() +
                " and access token secret " + this.params.getOauthTokenSecret() + " are valid.\n");
        return true;
    }

    private static String convertStreamToString(InputStream is) throws IOException {
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } finally {
                is.close();
            }
            return sb.toString();
        } else {
            return "";
        }
    }

    /**
     * Generates a random number using two UUIDs and HMAC-SHA1
     *
     * @return generated secure random number
     * @throws Exception Invalid Algorithm or Invalid Key
     */
    private String getRandomNumber() throws Exception {
        try {
            String secretKey = UUID.randomUUID().toString();
            String baseString = UUID.randomUUID().toString();

            SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(key);
            byte[] rawHmac = mac.doFinal(baseString.getBytes());
            String random = Base64.encode(rawHmac);
            // Unsupported characters
            random = random.replace("/", "_");
            random = random.replace("=", "a");
            random = random.replace("+", "f");
            return random;
        } catch (Exception e) {
            throw new Exception("Error when generating a random number.", e);
        }
    }

    private Parameters populateOauthConsumerData(String authHeader) {

        Parameters params = new Parameters();
        String splitChar = "&";

        if (authHeader != null) {
            if (authHeader.startsWith("OAuth ") || authHeader.startsWith("oauth ")) {
                authHeader = authHeader.substring(authHeader.indexOf("o"));
            }
            String[] headers = authHeader.split(splitChar);
            if (headers != null && headers.length > 0) {
                for (int i = 0; i < headers.length; i++) {
                    String[] elements = headers[i].split("=");
                    if (elements != null && elements.length > 0) {
                        if ("oauth_consumer_key".equals(elements[0].trim())) {
                            params.setOauthConsumerKey(removeLeadingAndTrailingQuatation(elements[1]
                                    .trim()));
                        } else if ("oauth_nonce".equals(elements[0].trim())) {
                            params.setOauthNonce(removeLeadingAndTrailingQuatation(elements[1]
                                    .trim()));
                        } else if ("oauth_signature".equals(elements[0].trim())) {
                            params.setOauthSignature(removeLeadingAndTrailingQuatation(elements[1]
                                    .trim()));
                        } else if ("oauth_signature_method".equals(elements[0].trim())) {
                            params.setOauthSignatureMethod(removeLeadingAndTrailingQuatation(elements[1]
                                    .trim()));
                        } else if ("oauth_timestamp".equals(elements[0].trim())) {
                            params.setOauthTimeStamp(removeLeadingAndTrailingQuatation(elements[1]
                                    .trim()));
                        } else if ("oauth_callback".equals(elements[0].trim())) {
                            params.setOauthCallback(removeLeadingAndTrailingQuatation(elements[1]
                                    .trim()));
                        } else if ("scope".equals(elements[0].trim())) {
                            params.setScope(removeLeadingAndTrailingQuatation(elements[1].trim()));
                        } else if ("xoauth_displayname".equals(elements[0].trim())) {
                            params.setDisplayName(removeLeadingAndTrailingQuatation(elements[1]
                                    .trim()));
                        } else if ("oauth_token".equals(elements[0].trim())) {
                            params.setOauthToken(removeLeadingAndTrailingQuatation(elements[1]
                                    .trim()));
                        } else if ("oauth_verifier".equals(elements[0].trim())) {
                            params.setOauthTokenVerifier(removeLeadingAndTrailingQuatation(elements[1]
                                    .trim()));
                        } else if ("oauth_token_secret".equals(elements[0].trim())) {
                            params.setOauthTokenSecret(removeLeadingAndTrailingQuatation(elements[1]
                                    .trim()));
                        } else if ("oauth_version".equals(elements[0].trim())) {
                            params.setVersion(removeLeadingAndTrailingQuatation(elements[1].trim()));
                        } else if ("oauth_callback_confirmed".equals(elements[0]
                                .trim())) {
                            params.setCallbackConfirmed(removeLeadingAndTrailingQuatation(elements[1]
                                    .trim()));
                        }
                    }
                }
            }
        }

        return params;
    }

    private String removeLeadingAndTrailingQuatation(String base) {
        String result = base;

        if (base.startsWith("\"") || base.endsWith("\"")) {
            result = base.replace("\"", "");
        }
        return URLDecoder.decode(result.trim());
    }

}
