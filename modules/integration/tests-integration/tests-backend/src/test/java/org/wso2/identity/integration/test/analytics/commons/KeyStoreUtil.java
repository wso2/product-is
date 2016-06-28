/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.integration.test.analytics.commons;

import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;

import java.io.File;

public class KeyStoreUtil {

	static File filePath = new File(FrameworkPathUtil.getCarbonHome() + File.separator +
	                                "repository" + File.separator + "resources" + File.separator + "security");

	public static void setTrustStoreParams() {
		String trustStore = filePath.getAbsolutePath();
		System.setProperty("javax.net.ssl.trustStore", trustStore + "/client-truststore.jks");
		System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");

	}

	public static void setKeyStoreParams() {
		String keyStore = filePath.getAbsolutePath();
		System.setProperty("Security.KeyStore.Location", keyStore + "/wso2carbon.jks");
		System.setProperty("Security.KeyStore.Password", "wso2carbon");

	}
}