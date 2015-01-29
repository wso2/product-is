/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.identity.integration.test.utils;

import org.apache.http.HttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Use to extract data from HttpResponce
 */
public class DataExtractUtil {

	/**
	 * Extract data from http response with the given keywords
	 * 
	 * @param response
	 * @param keyPositionMap
	 * @return values
	 * @throws java.io.IOException
	 */
	public static List<KeyValue> extractDataFromResponse(HttpResponse response,
	                                                     Map<String, Integer> keyPositionMap)
	                                                                                         throws IOException {
		List<KeyValue> keyValues = new ArrayList<KeyValue>();
		BufferedReader rd =
		                    new BufferedReader(new InputStreamReader(response.getEntity()
		                                                                     .getContent()));
		String line;

		while ((line = rd.readLine()) != null) {
			for (String key : keyPositionMap.keySet()) {
				if (line.contains(key)) {
					String[] tokens = line.split("'");
					KeyValue keyValue = new KeyValue(key, tokens[keyPositionMap.get(key)]);
					keyValues.add(keyValue);
					return keyValues;
				}
			}
		}
		rd.close();
		return null;
	}

	/**
	 * Extract input values from http response
	 * 
	 * @param response
	 * @param keyPositionMap
	 * @return values
	 * @throws java.io.IOException
	 */
	public static List<KeyValue> extractInputValueFromResponse(HttpResponse response,
	                                                           Map<String, Integer> keyPositionMap)
	                                                                                               throws IOException {
		List<KeyValue> keyValues = new ArrayList<KeyValue>();
		BufferedReader rd =
		                    new BufferedReader(new InputStreamReader(response.getEntity()
		                                                                     .getContent()));
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
	 * Extract label values from response
	 * 
	 * @param response
	 * @param keyPositionMap
	 * @return values
	 * @throws java.io.IOException
	 */
	public static List<KeyValue> extractLabelValueFromResponse(HttpResponse response,
	                                                           Map<String, Integer> keyPositionMap)
	                                                                                               throws IOException {
		List<KeyValue> keyValues = new ArrayList<KeyValue>();
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
	 * @param response
	 * @param keyPositionMap
	 * @return values
	 * @throws java.io.IOException
	 */
	public static List<KeyValue> extractTableRowDataFromResponse(HttpResponse response,
	                                                             Map<String, Integer> keyPositionMap)
	                                                                                                 throws IOException {
		boolean lineReached = false;
		List<KeyValue> keyValues = new ArrayList<KeyValue>();
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
	 * @param response
	 * @param keyPositionMap
	 * @return
	 * @throws java.io.IOException
	 */
	public static List<KeyValue> extractSessionConsentDataFromResponse(HttpResponse response,
	                                                                   Map<String, Integer> keyPositionMap)
	                                                                                                       throws IOException {
		boolean lineReached = false;
		List<KeyValue> keyValues = new ArrayList<KeyValue>();
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

		return null;
	}

	/**
	 * Extract Access token from Query String
	 * 
	 * @param query
	 *            - Query String
	 * @return Access Token
	 */
	public static String extractAccessTokenFromQueryString(String query) {
		String[] params = query.split("&");
		for (String param : params) {
			String name = param.split("=")[0];
			String value = param.split("=")[1];
			if (name.contains("access_token"))
				return value;
		}
		return null;
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

    public static String getContentData(HttpResponse response) throws IOException{
        BufferedReader rd = new BufferedReader(new InputStreamReader(response
                                                                             .getEntity().getContent()));
        String line;
        StringBuffer result = new StringBuffer();
        while ((line = rd.readLine()) != null) {
            result = result.append(line);

        }
        rd.close();
        return result.toString();
    }

}