/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;

import java.io.File;

/**
 *
 * This client demonstrates an OAuth1.0a equivalent flow to the Resource Owner Password grant type flow of OAuth2.0.
 * This client will first register a OAuth1.0a client in Identity Server using the OAuthAdminService SOAP API.
 * Then it will request for OAuth1.0a request token credentials (request token + request token secret)
 * from Identity Server using the OAuth1.0a request token endpoint.
 * Then using the OAuthService SOAP API it will authorize the OAuth1.0a request token issued in the previous step.
 * NOTE: The OAuth1.0a authorization endpoint is not being used to authorize the request token since it is assumed
 * that the client does not have capability to invoke a browser and redirect the user.
 * The third step is to request for OAuth1.0a access token credentials (access token + access token secret)
 * from Identity Server using the OAuth1.0a access token endpoint.
 * Finally the client will validate the access token credentials received from Identity Server using the OAuthService
 * SOAP API.
 *
 */
public class Main {

    // Identity server service URL
    private static final String IDENTITY_SERVER = "https://localhost:9443/";

    // Resource Server URL
    private static final String RESOURCE_SERVER_URL = "http://localhost:8280/services/echo";

    // User invoking the service
    // This user has to be registered in the system
    private static final String ADMIN_USER_NAME = "admin";

    //User password
    private static final  String ADMIN_PASSWORD = "admin";

    // Resource owner user name
    private static final String RESOURCE_OWNER_USER_NAME = "admin";

    // Resource owner password
    private static final String RESOURCE_OWNER_PASSWORD = "admin";

    // Application name to be used when registering the client
    private static final String APPLICATION_NAME = "DemoApp1";

    public static void main(String[] args) {

        OAuthServiceClient client = null;

        //Axis2 client needs a configuration context
        ConfigurationContext configContext = null;

        try {

            //Create a configuration context. A configuration context contains information for a
            //axis2 environment. This is needed to create an axis2 client
            configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);

            /**
             * Call to Identity Server uses HTTPS protocol.
             * Therefore we to validate the server certificate. The server certificate is looked up in the
             * trust store. Following code sets what trust-store to look for and its JKs password.
             * Note : The trust store should have server's certificate.
             */
            System.setProperty("javax.net.ssl.trustStore",   new File("src/main/resources/wso2carbon.jks").getAbsolutePath());
            System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");

            client = new OAuthServiceClient(IDENTITY_SERVER , configContext, ADMIN_USER_NAME, ADMIN_PASSWORD);

			client.registerOAuthApplicationData(APPLICATION_NAME, null, null, null);

            client.getRequestToken("test");

            client.authorizeRequestToken(RESOURCE_OWNER_USER_NAME, RESOURCE_OWNER_PASSWORD);

            client.getAccessToken();

            client.validateAuthenticationRequest(RESOURCE_SERVER_URL);

            // Code to invoke a service in ESB which is protected using OAuth1.0a using OAuth mediator

//            // We are using Google oauth API to call the service
//            GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
//
//            // Setting user name as consumer key
//            oauthParameters.setOAuthConsumerKey(CONSUMER_KEY);
//            // Setting above assigned consumer secret
//            oauthParameters.setOAuthConsumerSecret(CONSUMER_SECRET);
//
//            // setting 2-legged OAuth flag
//            oauthParameters.setOAuthType(OAuthType.TWO_LEGGED_OAUTH);
//
//            // We will be using HMAC-SHA1 signature. Google API has a class to do that
//            OAuthHmacSha1Signer signer = new OAuthHmacSha1Signer();
//
//            // Create a Google service. The name of the current application given here
//            // Names are only for reference purpose
//            GoogleService service = new GoogleService("oauthclient", "sampleapp");
//            service.setOAuthCredentials(oauthParameters, signer);
//
//            /**
//             * We will be calling test service's echoString method. As parameter we are sending "Hello World"
//             * The parameter name is "in".
//             */
//            String param = "WSO2";
//            String baseString = RESOURCE_SERVER_URL + "services/OAuthTest/greet?name="+ param;
//
//            /**
//             * Invoking the request. And writing the response output.
//             */
//            URL feedUrl = new URL(baseString);
//            request = service.createFeedRequest(feedUrl);
//            request.execute();
//
//            System.out.println(convertStreamToString(request.getResponseStream()));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
