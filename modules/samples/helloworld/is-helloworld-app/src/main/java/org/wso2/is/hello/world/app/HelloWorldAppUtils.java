/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.is.hello.world.app;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.ServletContextEvent;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Utility methods related to application operations.
 */
public class HelloWorldAppUtils {

    private static final Logger logger = Logger.getLogger(HelloWorldAppUtils.class.getName());
    private static Map<String, TokenData> tokenStore = new HashMap<>();
    private static final int DEFAULT_PORT = 9443;
    private static final int DEFAULT_APP_PORT = 9090;
    private static final String CLIENT_ID = "client_id";
    private static final String CONF_PATH = "conf";
    private static final String SCHEME_HTTP = "http";
    private static final String SCHEME_HTTPS = "https";
    private static final String DEFAULT_HOST = "localhost";
    private static final String CLIENT_SECRET = "client_secret";
    private static final String OAUTH2_CONTEXT = "/oauth2";
    private static final String APP_NAME_PREFIX = "HelloAuth-";
    private static final String APP_REDIRECT_PAGE = "home.jsp";
    private static final String APP_PROPERTY_FILE_NAME = "hello-world.properties";
    private static final String DEFAULT_PROPERTY_FILE_PATH = "/WEB-INF/classes/" + APP_PROPERTY_FILE_NAME;
    private static final String AUTHORIZATION_BASIC_PREFIX = "Basic ";
    private static final String OAUTH2_DCR_REGISTER_CONTEXT = "/api/identity/oauth2/dcr/v1.1/register";
    private static final String OAUTH2_AUTHZ_ENDPOINT_CONTEXT = OAUTH2_CONTEXT + "/authorize";
    private static final String OAUTH2_TOKEN_ENDPOINT_CONTEXT = OAUTH2_CONTEXT + "/token";

    static {

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            logger.log(Level.SEVERE, "Error while creating SSL context for trust manager.", e);
        }

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = (hostname, session) -> true;

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

    public static String getTokenRequestPayload(String authzCode) throws HelloWorldException {

        return "grant_type=authorization_code" +
               "&code=" + authzCode +
               "&redirect_uri=" + getRedirectUri();
    }

    public static String getAuthzRequest(HttpServletRequest request, HttpServletResponse response) throws
            HelloWorldException {

        URL url;
        try {
            url = new URL(SCHEME_HTTPS, getApplicationProperty("idp.hostname", DEFAULT_HOST),
                          Integer.parseInt(getApplicationProperty("idp.port", String.valueOf(DEFAULT_PORT))),
                          OAUTH2_AUTHZ_ENDPOINT_CONTEXT +
                          "?response_type=code" +
                          "&client_id=" + getClientId() +
                          "&redirect_uri=" + getRedirectUri() +
                          "&scope=openid");
        } catch (MalformedURLException e) {
            throw new HelloWorldException("Error while building Authorization endpoint url.", e);
        }

        Cookie appIdCookie = getAppIdCookie(request);
        if (appIdCookie != null) {
            tokenStore.remove(appIdCookie.getValue());
            appIdCookie.setMaxAge(0);
            response.addCookie(appIdCookie);
        }
        return url.toString();
    }

    private static Cookie getAppIdCookie(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();
        Cookie appIdCookie = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("AppID".equals(cookie.getName())) {
                    appIdCookie = cookie;
                    break;
                }
            }
        }
        return appIdCookie;
    }

    public static String getApplicationProperty(String propertyKey, String defaultValue) {

        Properties properties = HelloWorldDataHolder.getInstance().getProperties();
        return properties.getProperty(propertyKey, defaultValue);
    }

    private static String getClientId() throws HelloWorldException {

        String clientId = getApplicationProperty(CLIENT_ID, null);
        if (clientId == null || !isValidApp(clientId)) {
            clientId = registerApp();
        }
        return clientId;
    }

    public static boolean isValidApp(String clientId) throws HelloWorldException {

        String dcrEP = getDCRRegisterEndpoint();
        dcrEP = dcrEP.concat("/" + clientId);

        HttpsURLConnection con = getHttpsURLConnection(dcrEP);
        int responseCode;
        try {
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", buildBasicAuthHeader());
            try {
                responseCode = con.getResponseCode();
            } catch (IOException e) {
                throw new HelloWorldException("Error while reading IdP response code", e);
            }

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String response = getResponse(con);
                if (!response.isEmpty()) {
                    Map<String, Object> map = getJsonResponse(response);
                    String client_id = (String) map.get(CLIENT_ID);

                    if (client_id != null) {
                        updateAppProperties(CLIENT_ID, client_id, true);
                        String client_secret = (String) map.get(CLIENT_SECRET);
                        if (client_secret != null) {
                            updateAppProperties(CLIENT_SECRET, client_secret, false);
                        }
                    }
                    return true;
                }
            } else if (responseCode < HttpsURLConnection.HTTP_BAD_REQUEST) {
                String response = getResponse(con);
                logger.log(Level.SEVERE, "Unexpected response from IDP. Status code: " + responseCode + ", Response: " +
                                         response);
            } else {

                String response;
                if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    response = "Invalid client ID: " + clientId;
                } else {
                    response = getErrorResponse(con);
                }
                logger.log(Level.SEVERE, "Error response from IDP. Status code: " + responseCode + ", Response: " +
                                         response);
            }
        } catch (ProtocolException e) {
            logger.log(Level.SEVERE, "Error occurred while setting connection request method to: 'GET'", e);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        return false;
    }

    private static Map<String, Object> getJsonResponse(String response) {

        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        return gson.fromJson(response, type);
    }

    private static String getResponse(HttpsURLConnection con) throws HelloWorldException {

        try {
            return getResponseFromStream(con.getInputStream());
        } catch (IOException e) {
            throw new HelloWorldException("Error getting input stream.", e);
        }
    }

    private static String getErrorResponse(HttpsURLConnection con) throws HelloWorldException {

        return getResponseFromStream(con.getErrorStream());
    }

    private static String getResponseFromStream(InputStream inputStream) throws HelloWorldException {

        StringBuilder responseBuilder = new StringBuilder();
        if (inputStream != null) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    responseBuilder.append(inputLine);
                }
            } catch (IOException e) {
                throw new HelloWorldException("Error while reading server response.", e);
            }
        }
        return responseBuilder.toString();
    }

    private static HttpsURLConnection getHttpsURLConnection(String url) throws HelloWorldException {


        try {
            URL requestUrl = new URL(url);
            return (HttpsURLConnection) requestUrl.openConnection();
        } catch (IOException e) {
            throw new HelloWorldException("Error while creating connection to: " + url, e);
        }
    }

    private static String getDCRRegisterEndpoint() throws HelloWorldException {

        try {
            URL url = new URL(SCHEME_HTTPS, getApplicationProperty("idp.hostname", DEFAULT_HOST),
                          Integer.parseInt(getApplicationProperty("idp.port", String.valueOf(DEFAULT_PORT))),
                          OAUTH2_DCR_REGISTER_CONTEXT);
            return url.toString();
        } catch (MalformedURLException e) {
            throw new HelloWorldException("Error while building DCR endpoint url.", e);
        }
    }

    private static String getTokenEndpoint() throws HelloWorldException {

        try {
            URL url = new URL(SCHEME_HTTPS, getApplicationProperty("idp.hostname", DEFAULT_HOST),
                          Integer.parseInt(getApplicationProperty("idp.port", String.valueOf(DEFAULT_PORT))),
                          OAUTH2_TOKEN_ENDPOINT_CONTEXT);
            return url.toString();
        } catch (MalformedURLException e) {
            throw new HelloWorldException("Error while building Token endpoint url.", e);
        }
    }


    public static void getToken(HttpServletRequest request, HttpServletResponse response) throws HelloWorldException {

        Cookie appIdCookie = getAppIdCookie(request);
        HttpSession session = request.getSession(false);
        TokenData storedTokenData;
        if (appIdCookie != null) {
            storedTokenData = tokenStore.get(appIdCookie.getValue());
            if (storedTokenData != null) {
                setTokenDataToSession(session, storedTokenData);
                return;
            }
        }

        String authzCode = request.getParameter("code");
        if (authzCode == null) {
            return;
        }

        HttpsURLConnection con = getHttpsURLConnection(getTokenEndpoint());
        try {
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Authorization", buildBearerHeader());

            String payload = "grant_type=authorization_code" +
                             "&code=" + authzCode +
                             "&redirect_uri=" + getRedirectUri();
            // Send post request
            con.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {

                wr.writeBytes(payload);
                int responseCode = 0;
                try {
                    responseCode = con.getResponseCode();
                } catch (IOException e) {
                    throw new HelloWorldException("Error while reading IdP response code", e);
                }

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    String responseJson = getResponse(con);
                    if (!responseJson.isEmpty()) {
                        Map<String, Object> map = getJsonResponse(responseJson);

                        String accessToken = (String) map.get("access_token");
                        session = request.getSession();
                        if (accessToken != null) {
                            session.setAttribute("accessToken", accessToken);
                            String idToken = (String) map.get("id_token");
                            if (idToken != null) {
                                session.setAttribute("idToken", idToken);
                            }
                            session.setAttribute("authenticated", true);
                            TokenData tokenData = new TokenData();
                            tokenData.setAccessToken(accessToken);
                            tokenData.setIdToken(idToken);

                            String sessionId = UUID.randomUUID().toString();
                            tokenStore.put(sessionId, tokenData);
                            Cookie cookie = new Cookie("AppID", sessionId);
                            cookie.setMaxAge(-1);
                            cookie.setPath("/");
                            response.addCookie(cookie);
                        } else {
                            session.invalidate();
                        }
                    }
                } else if (responseCode < HttpsURLConnection.HTTP_BAD_REQUEST) {
                    String res = getResponse(con);
                    request.getSession().invalidate();
                    logger.log(Level.SEVERE, "Unexpected response from IDP. Status code: " + responseCode + ", " +
                                             "Response: " + res);
                    throw new HelloWorldException("Unexpected response from IDP. Status code: " + responseCode);
                } else {

                    String resp;
                    if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        resp = "Authorization failed for the token request.";
                    } else {
                        resp = getErrorResponse(con);
                    }
                    request.getSession().invalidate();
                    logger.log(Level.SEVERE, "Error response from IDP. Status code: " + responseCode + ", Response: " +
                                             resp);
                    throw new HelloWorldException("Error response from IDP. Status code: " + responseCode);
                }
            } catch (IOException e) {
                throw new HelloWorldException("Error occurred while sending the request to IDP", e);
            }
        } catch (ProtocolException e) {
            throw new HelloWorldException("Error occurred while setting connection request method to: 'POST'", e);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    private static void setTokenDataToSession(HttpSession session, TokenData storedTokenData) {

        session.setAttribute("authenticated", true);
        session.setAttribute("accessToken", storedTokenData.getAccessToken());
        session.setAttribute("idToken", storedTokenData.getIdToken());
        return;
    }

    private static String buildBearerHeader() {

        String clientId = getApplicationProperty(CLIENT_ID, null);
        String clientSecret = getApplicationProperty(CLIENT_SECRET, null);
        String encodedCredentials = new String(Base64.getEncoder().encode(String.join(":", clientId, clientSecret)
                                                                                .getBytes()));
        return AUTHORIZATION_BASIC_PREFIX + encodedCredentials;

    }

    public static String registerApp() throws HelloWorldException {

        HttpsURLConnection con = null;
        try {
            con = getHttpsURLConnection(getDCRRegisterEndpoint());
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", buildBasicAuthHeader());

            JsonObject jsonPayload = new JsonObject();
            jsonPayload.addProperty("client_name", APP_NAME_PREFIX + UUID.randomUUID().toString());
            JsonArray grantTypes = new JsonArray();
            grantTypes.add("authorization_code");
            jsonPayload.add("grant_types", grantTypes);

            JsonArray redirectUris = new JsonArray();
            redirectUris.add(getRedirectUri());
            jsonPayload.add("redirect_uris", redirectUris);

            String payload = new Gson().toJson(jsonPayload);
            // Send post request
            con.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.writeBytes(payload);
                int responseCode;
                try {
                    responseCode = con.getResponseCode();
                } catch (IOException e) {
                    throw new HelloWorldException("Error while reading IdP response code", e);
                }

                if (responseCode == HttpsURLConnection.HTTP_CREATED) {
                    String responseJson = getResponse(con);
                    if (!responseJson.isEmpty()) {
                        Map<String, Object> map = getJsonResponse(responseJson);
                        String clientId = (String) map.get(CLIENT_ID);
                        if (clientId != null) {
                            logger.info("OAuth application registered successfully. ClientID: " + clientId);
                            updateAppProperties(CLIENT_ID, clientId, true);
                            String client_secret = (String) map.get(CLIENT_SECRET);
                            if (client_secret != null) {
                                updateAppProperties(CLIENT_SECRET, client_secret, false);
                            }
                            return clientId;
                        }
                    }
                } else if (responseCode < HttpsURLConnection.HTTP_BAD_REQUEST) {
                    String response = getResponse(con);
                    logger.log(Level.SEVERE, "Unexpected response from IDP. Status code: " + responseCode + ", Response: " +
                                             response);
                    throw new HelloWorldException("Unexpected response from IDP. Status code: " + responseCode);
                } else {
                    String response;
                    if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        response = "Authorization failed for the DCR registration request.";
                    } else {
                        response = getErrorResponse(con);
                    }
                    logger.log(Level.SEVERE, "Error response from IDP. Status code: " + responseCode + ", Response: " +
                                             response);
                    throw new HelloWorldException("Error response from IDP. Status code: " + responseCode);
                }
            } catch (IOException e) {
                throw new HelloWorldException("Error occurred while sending the request to IDP", e);
            }
        } catch (ProtocolException e) {
            throw new HelloWorldException("Error occurred while setting connection request method to: 'POST'", e);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        return null;
    }

    private static String buildBasicAuthHeader() {

        String username = getApplicationProperty("username", "admin");
        String password = getApplicationProperty("password", "admin");
        String encodedCredentials = new String(Base64.getEncoder().encode(String.join(":", username, password)
                                                                                .getBytes()));
        return AUTHORIZATION_BASIC_PREFIX + encodedCredentials;
    }

    public static String getAppUriWithContext(String context) throws HelloWorldException {

        try {
            URL url = new URL(SCHEME_HTTP, getApplicationProperty("app.hostname", DEFAULT_HOST),
                          Integer.parseInt(getApplicationProperty("app.port", String.valueOf(DEFAULT_APP_PORT))),
                          getApplicationProperty("app.context", "/") + (context == null ? "" : context));
            return url.toString();
        } catch (MalformedURLException e) {
            throw new HelloWorldException("Error while building redirect url.", e);
        }
    }

    private static String getRedirectUri() throws HelloWorldException {

        return getAppUriWithContext(APP_REDIRECT_PAGE);
    }

    private static void updateAppProperties(String key, String value, boolean updateFile) {

        Properties properties = HelloWorldDataHolder.getInstance().getProperties();
        properties.put(key, value);
        if (updateFile) {
            updatePropertyFile(key, value);
        }
    }

    private static void updatePropertyFile(String key, String value) {

        Path path = HelloWorldDataHolder.getInstance().getPropertyPath();
        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(path)) {
            properties.load(inputStream);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while reading file: " + path.getFileName(), e);
        }
        properties.put(key, value);
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            properties.store(outputStream, null);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while writing property: '" + key + "' to file: " + path.getFileName(), e);
        }
    }

    public static boolean logout(HttpServletRequest request, HttpServletResponse response) {

        Cookie appIdCookie = getAppIdCookie(request);

        if (appIdCookie != null) {
            tokenStore.remove(appIdCookie.getValue());
            appIdCookie.setMaxAge(0);
            response.addCookie(appIdCookie);
            return true;
        }
        return false;
    }

    public static Path buildPropertyFilePath(ServletContextEvent servletContextEvent) {

        String appHome = System.getProperty("app.home");
        String absPath = servletContextEvent.getServletContext().getRealPath(DEFAULT_PROPERTY_FILE_PATH);
        Path propertyPath;
        if (appHome != null) {
            propertyPath = Paths.get(appHome, CONF_PATH, APP_PROPERTY_FILE_NAME);
            if (!Files.exists(propertyPath)) {
                Path path = Paths.get(absPath);
                try {
                    Files.copy(path, propertyPath);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error while copying property file to: " + propertyPath.toAbsolutePath()
                            , e);
                    propertyPath = path;
                }
            }
        } else {
            propertyPath = Paths.get(absPath);
        }
        logger.info("App property path: " + propertyPath.toAbsolutePath());
        return propertyPath;
    }
}
