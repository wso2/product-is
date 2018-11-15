/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.scenarios.commons;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.identity.scenarios.commons.clients.login.AuthenticatorClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Properties;

/**
 * Base test case for IS scenario tests.
 */
public class ScenarioTestBase {

    private static final String INPUTS_LOCATION = System.getenv("DATA_BUCKET_LOCATION");
    private static final String INFRASTRUCTURE_PROPERTIES = "infrastructure.properties";
    private static final String DEPLOYMENT_PROPERTIES = "deployment.properties";
    private static final String JOB_PROPERTIES = "testplan-props.properties";
    private static final Logger LOG = LoggerFactory.getLogger(ScenarioTestBase.class);

    public static final String MGT_CONSOLE_URL = "MgtConsoleUrl";
    public static final String CARBON_SERVER_URL = "CarbonServerUrl";
    public static final String IS_HTTP_URL = "ISHttpUrl";
    public static final String IS_HTTPS_URL = "ISHttpsUrl";
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin";

    protected static final int ARTIFACT_DEPLOYMENT_WAIT_TIME_MS = 120000;
    protected static final String RESOURCE_LOCATION = System.getProperty("common.resource.location");

    protected String backendURL;
    protected String backendServiceURL;
    protected AuthenticatorClient loginClient;
    protected String sessionCookie;
    protected static final String SERVICES = "/services/";
    protected ConfigurationContext configContext;

    /**
     * This is a utility method to load the deployment details.
     * The deployment details are available as key-value pairs in {@link #INFRASTRUCTURE_PROPERTIES},
     * {@link #DEPLOYMENT_PROPERTIES}, and {@link #JOB_PROPERTIES} under the
     * {@link #INPUTS_LOCATION}.
     * <p>
     * This method loads these files into one single properties, and return it.
     *
     * @return properties the deployment properties.
     */
    public static Properties getDeploymentProperties() {

        Path infraPropsFile = Paths.get(INPUTS_LOCATION + File.separator + INFRASTRUCTURE_PROPERTIES);
        Path deployPropsFile = Paths.get(INPUTS_LOCATION + File.separator + DEPLOYMENT_PROPERTIES);
        Path jobPropsFile = Paths.get(INPUTS_LOCATION + File.separator + JOB_PROPERTIES);

        Properties props = new Properties();
        loadProperties(infraPropsFile, props);
        loadProperties(deployPropsFile, props);
        loadProperties(jobPropsFile, props);
        return props;
    }

    private static void loadProperties(Path propsFile, Properties props) {

        if (!Files.exists(propsFile)) {
            LOG.warn("Deployment property file not found: " + propsFile);
            return;
        }
        try (InputStream propsIS = Files.newInputStream(propsFile)) {
            props.load(propsIS);
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    public static void setKeyStoreProperties() {

        System.setProperty("javax.net.ssl.trustStore", RESOURCE_LOCATION + "keystores/products/wso2carbon.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
    }

    public String getAuthzHeader() {

        Base64.Encoder encoder = java.util.Base64.getEncoder();
        String encodedHeader = encoder.encodeToString(String.join(":", ADMIN_USERNAME, ADMIN_PASSWORD).getBytes());
        return String.join(" ", "Basic", encodedHeader);
    }

    public void init() throws Exception {

        backendURL = getDeploymentProperties().getProperty(IS_HTTPS_URL);
        backendServiceURL = backendURL + SERVICES;
        configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        loginClient = new AuthenticatorClient(backendServiceURL);
        sessionCookie = loginClient.login(ADMIN_USERNAME, ADMIN_PASSWORD, null);
    }
}
