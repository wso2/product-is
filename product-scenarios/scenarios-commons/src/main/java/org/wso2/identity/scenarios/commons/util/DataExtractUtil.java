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

package org.wso2.identity.scenarios.commons.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.identity.scenarios.commons.ScenarioTestException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.wso2.identity.scenarios.commons.util.Constants.HEADER_SET_COOKIE;
import static org.wso2.identity.scenarios.commons.util.Constants.PARAM_SESSION_DATA_KEY;
import static org.wso2.identity.scenarios.commons.util.Constants.SystemProperties.TEST_USERS_LOCATION;

/**
 * Use to extract data from HttpResponse.
 */
public class DataExtractUtil {

    private static final Log log = LogFactory.getLog(DataExtractUtil.class);

    /**
     * Extract data from http response with the given keywords.
     *
     * @param response       HttpResponse for extracting data.
     * @param keyPositionMap Search keys.
     * @return Search values.
     * @throws IOException If error occurs while data extraction.
     */
    public static List<KeyValue> extractDataFromResponse(HttpResponse response, Map<String, Integer> keyPositionMap)
            throws IOException {

        //todo extracting sessionDataKey using this method required key and value to be in the same line
        //todo ex. <input type="hidden" name="sessionDataKey"  value='8a433378-6d1f-434b-b574-a143dbb1a508'/>
        //todo if the jsp page formatted and value moved to the next line this will break. nice to have this fixed

        List<KeyValue> keyValues = new ArrayList<>();
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line;
        while ((line = rd.readLine()) != null) {
            log.info("###: " + line);
            for (String key : keyPositionMap.keySet()) {
                if (line.contains(key)) {
                    String[] tokens;
                    if (line.contains("'")) {
                        tokens = line.split("'");
                        KeyValue keyValue = new KeyValue(key, tokens[keyPositionMap.get(key)]);
                        keyValues.add(keyValue);
                        return keyValues;
                    } else {
                        String regexString = Pattern.quote(key + " value=\"") + "(.*?)" + Pattern.quote("\"");
                        Pattern pattern = Pattern.compile(regexString);
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            KeyValue keyValue = new KeyValue(key, matcher.group(1));
                            keyValues.add(keyValue);
                            return keyValues;
                        }
                        return null;
                    }
                }
            }
        }
        rd.close();
        return null;
    }

    /**
     * Extract input values from http response
     *
     * @param response       HttpResponse for extracting data.
     * @param keyPositionMap Search key map.
     * @return Search values.
     * @throws IOException If error occurs while data extraction.
     */
    public static List<KeyValue> extractInputValueFromResponse(HttpResponse response, Map<String, Integer>
            keyPositionMap) throws IOException {

        List<KeyValue> keyValues = new ArrayList<>();
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line;

        while ((line = rd.readLine()) != null) {
            for (String key : keyPositionMap.keySet()) {
                if (line.contains(key)) {
                    Pattern p = Pattern.compile("value=\"(\\S+)\"");
                    Matcher m = p.matcher(line);
                    if (m.find()) {
                        KeyValue keyValue = new KeyValue(key, m.group(1));
                        keyValues.add(keyValue);
                        return keyValues;
                    }
                }
            }
        }
        rd.close();
        return null;
    }

    /**
     * Extract label values from response.
     *
     * @param response       HttpResponse for extracting data.
     * @param keyPositionMap Search key map.
     * @return Search values.
     * @throws IOException If error occurs while data extraction.
     */
    public static List<KeyValue> extractLabelValueFromResponse(HttpResponse response,
                                                               Map<String, Integer> keyPositionMap)
            throws IOException {

        List<KeyValue> keyValues = new ArrayList<>();
        BufferedReader rd =
                new BufferedReader(new InputStreamReader(response.getEntity()
                        .getContent()));

        String line;

        while ((line = rd.readLine()) != null) {
            for (String key : keyPositionMap.keySet()) {
                if (line.contains(key)) {
                    Pattern p = Pattern.compile(">(\\S+)</label>");
                    Matcher m = p.matcher(line);
                    if (m.find()) {
                        KeyValue keyValue = new KeyValue(key, m.group(1));
                        keyValues.add(keyValue);
                        return keyValues;
                    }
                }
            }
        }
        rd.close();
        return null;
    }

    /**
     * Extract table row data from http response
     *
     * @param response       HttpResponse for extracting data.
     * @param keyPositionMap Search key map.
     * @return Search values.
     * @throws IOException If error occurs while data extraction.
     */
    public static List<KeyValue> extractTableRowDataFromResponse(HttpResponse response, Map<String, Integer>
            keyPositionMap) throws IOException {

        boolean lineReached = false;
        List<KeyValue> keyValues = new ArrayList<>();
        BufferedReader rd =
                new BufferedReader(new InputStreamReader(response.getEntity()
                        .getContent()));
        String line;
        while ((line = rd.readLine()) != null) {
            for (String key : keyPositionMap.keySet()) {
                if (line.contains(key)) {
                    lineReached = true;
                    continue;
                }
                if (lineReached) {
                    if (line.contains("<td>")) {
                        Pattern p = Pattern.compile("<td>(\\S+)</td>");
                        Matcher m = p.matcher(line);
                        if (m.find()) {
                            KeyValue keyValue = new KeyValue(key, m.group(1));
                            keyValues.add(keyValue);
                            return keyValues;
                        }
                    }
                }
            }
        }
        rd.close();
        return null;
    }

    /**
     * Extract session consent data from response
     *
     * @param response       HttpResponse for extracting data.
     * @param keyPositionMap Search key map.
     * @return Session consent data.
     * @throws IOException If error occurs while data extraction.
     */
    public static List<KeyValue> extractSessionConsentDataFromResponse(HttpResponse response,
                                                                       Map<String, Integer> keyPositionMap)
            throws IOException {

        boolean lineReached = false;
        List<KeyValue> keyValues = new ArrayList<>();
        BufferedReader rd =
                new BufferedReader(new InputStreamReader(response.getEntity()
                        .getContent()));
        String line;
        while ((line = rd.readLine()) != null) {
            for (String key : keyPositionMap.keySet()) {
                if (line.contains(key)) {
                    lineReached = true;
                }
                if (lineReached) {
                    if (line.contains("value")) {
                        String[] tokens = line.split("\"");
                        KeyValue keyValue = new KeyValue(key, tokens[1]);
                        keyValues.add(keyValue);
                        return keyValues;
                    }
                }
            }
        }
        rd.close();

        return keyValues;
    }

    /**
     * Extract a value corresponds to a given key from Query String.
     *
     * @param query Query string that holds the key value pair.
     * @param key   String key to be extracted.
     * @return Extracted string value from the query.
     */
    public static String extractParamFromURIFragment(String query, String key) {

        String fragment = query.substring(query.indexOf("#") + 1);
        String[] params = fragment.split("&");
        for (String param : params) {
            String name = param.split("=")[0];
            if (name.contains(key)) {
                return param.split("=")[1];
            }
        }
        return null;
    }

    /**
     * Return a query param value of a given key, from a URI string.
     *
     * @param URIString URI as a string.
     * @param key       Key that needs to be extracted.
     * @return String value corresponds to the key.
     * @throws URISyntaxException If error occurs while parsing the URI.
     */
    public static String getParamFromURIString(String URIString, String key) throws URISyntaxException {

        String param = null;
        List<NameValuePair> params = URLEncodedUtils.parse(new URI(URIString),
                String.valueOf(StandardCharsets.UTF_8));
        for (NameValuePair param1 : params) {
            if (StringUtils.equals(param1.getName(), key)) {
                param = param1.getValue();
                break;
            }
        }
        return param;
    }

    public static class KeyValue {

        private String key;
        private String value;

        KeyValue(String key, String value) {

            this.key = key;
            this.value = value;
        }

        public String getValue() {

            return value;
        }

        public String getKey() {

            return key;
        }
    }

    /**
     * Extract the sessionDataKey value from the HTML response content
     *
     * @param response HttpResponse for extracting data.
     * @return sessionDataKey if found, null otherwise
     * @throws IOException
     */
    public static String getSessionDataKey(HttpResponse response) throws IOException {

        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put("name=\"" + PARAM_SESSION_DATA_KEY + "\"", 1);
        List<KeyValue> keyValues = extractDataFromResponse(response, keyPositionMap);
        if (keyValues != null && keyValues.size() > 0) {
            String sessionDataKey = keyValues.get(0).getValue();
            EntityUtils.consume(response.getEntity());
            return sessionDataKey;
        }
        return null;
    }

    /**
     * Check whether user consent is requested.
     *
     * @param response HttpResponse for extracting data.
     * @return true if consent page is presented.
     */
    public static boolean isConsentRequested(HttpResponse response) {

        String redirectUrl = getRedirectUrlFromResponse(response);
        return redirectUrl.contains("consent.do");

    }

    /**
     * Extract redirect URL from the response
     *
     * @param response HttpResponse for extracting data.
     * @return Location Header
     */
    public static String getRedirectUrlFromResponse(HttpResponse response) {
        Header[] headers = response.getAllHeaders();
        String url = "";
        for (Header header : headers) {
            if (HttpHeaders.LOCATION.equals(header.getName())) {
                url = header.getValue();
            }
        }
        return url;
    }

    /**
     * Extract an specific Cookie from the response
     *
     * @param response   HttpResponse for extracting data.
     * @param cookieName name of the cookie to be extracted
     * @return extracted cookie value, null if not found
     */
    public static String getCookieFromResponse(HttpResponse response, String cookieName) {

        String pastrCookie = null;
        boolean foundPastrCookie = false;
        Header[] headers = response.getHeaders(HEADER_SET_COOKIE);
        if (headers != null) {
            int i = 0;
            while (!foundPastrCookie && i < headers.length) {
                if (headers[i].getValue().contains(cookieName)) {
                    pastrCookie = headers[i].getValue().split(";")[0];
                    foundPastrCookie = true;
                }
                i++;
            }
        }
        return pastrCookie;
    }

    /**
     * Extract query parameters in a URL
     *
     * @param Url url for extracting data.
     * @return HashMap of extracted query parameters
     * @throws Exception
     */
    public static Map<String, String> getQueryParams(String Url) throws Exception {

        Map<String, String> queryParams = new HashMap<>();

        List<NameValuePair> params = URLEncodedUtils.parse(new URI(Url), StandardCharsets.UTF_8.name());
        for (NameValuePair param : params) {
            queryParams.put(param.getName(), param.getValue());
        }
        return queryParams;
    }

    /**
     * Extract a particular value from the response
     *
     * @param response HttpResponse for extracting data
     * @param Key      Key that needs to be extracted.
     * @param position position of the key is placed.
     * @return search value if found
     * @throws IOException
     */
    public static String extractValueFromResponse(HttpResponse response, String Key, Integer position)
            throws
            IOException {
        Map<String, Integer> keyPositionMap = new HashMap<>(1);
        keyPositionMap.put(Key, position);
        List<KeyValue> extracted = extractDataFromResponse(response, keyPositionMap);
        if (!extracted.isEmpty()) {
            return extracted.get(0).getValue();
        }
        return null;
    }

    /**
     * Extract the full content of the response as a String
     *
     * @param response HttpResponse for extracting data
     * @return response content
     * @throws IOException
     */
    public static String extractFullContentFromResponse(HttpResponse response) throws IOException {
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }

    public static JSONObject readJSONObject(JSONParser parser, String filePath, String fileName) throws
            ScenarioTestException {

        String pathToFile = "";
        try {
            pathToFile = getFilePath(fileName, filePath);
            return (JSONObject) parser.parse(new FileReader(pathToFile));
        } catch (IOException e) {
            throw new ScenarioTestException("Error while reading from :" + pathToFile);
        } catch (ParseException e) {
            throw new ScenarioTestException("Error while parsing to JSON :" + pathToFile);
        }
    }


    /**
     * Get file path.
     *
     * @param fileName File name.
     * @return File path.
     * @throws Exception Exception.
     */
    public static String getFilePath(String fileName, String filePath) throws ScenarioTestException {

        Path path = Paths.get(filePath + fileName);
        if (!Files.exists(path)) {
            throw new ScenarioTestException("Failed to find file: " + path.toString());
        }
        return path.toString();
    }

    /**
     * Create service provider.
     *
     * @param fileName Service provider configuration file name.
     * @return Service provider name.
     * @throws Exception If error occurs while creating service provider.
     */
    public static JSONObject getTestUser(String fileName) throws Exception {
        return readJSONObject(new JSONParser(),System.getProperty(TEST_USERS_LOCATION),fileName );
    }

}
