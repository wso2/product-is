/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.identity.integration.test.oauth2.dcrm.api.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utils for preparing DCR payloads
 */
public class DCRUtils {

    private static JSONParser parser = new JSONParser();
    private static final String REGISTER_REQUESTS_LOCATION = "registration.requests.location";

    /**
     * Get register request JSON object.
     *
     * @param fileName File name.
     * @return Register request JSON object.
     * @throws Exception Exception.
     */
    public static JSONObject getRegisterRequestJSON(String fileName) throws Exception {

        return (JSONObject) parser.parse(new FileReader(getFilePath(REGISTER_REQUESTS_LOCATION, fileName)));
    }

    /**
     * Get file path.
     *
     * @param folderPath Folder path.
     * @param fileName   File name.
     * @return File path.
     * @throws Exception Exception.
     */
    public static String getFilePath(String folderPath, String fileName) throws Exception {

        Path path = Paths.get(System.getProperty(folderPath) + fileName);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("Failed to find file: " + path.toString());
        }
        return path.toString();
    }

    public static void setRequestHeaders(HttpPost request, String username, String password) {

        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader(username, password));
        request.addHeader(HttpHeaders.CONTENT_TYPE, OAuthDCRMConstants.CONTENT_TYPE);
    }

    public static String getAuthzHeader(String username, String password) {

        return OAuth2Constant.BASIC_HEADER + Base64.encodeBase64String((username + ":" + password).getBytes()).trim();
    }

    public static JSONObject getPayload(HttpResponse response) throws IOException {

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        Object responseObj = JSONValue.parse(rd);
        EntityUtils.consume(response.getEntity());
        return (JSONObject) responseObj;
    }
}
