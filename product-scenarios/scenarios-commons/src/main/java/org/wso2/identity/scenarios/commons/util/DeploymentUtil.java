/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class DeploymentUtil {

    private static final String INFRASTRUCTURE_PROPERTIES = "infrastructure.properties";
    private static final String DEPLOYMENT_PROPERTIES = "deployment.properties";
    private static final String JOB_PROPERTIES = "testplan-props.properties";
    private static final Logger LOG = LoggerFactory.getLogger(DeploymentUtil.class);
    private static final String PATH_TO_WSO2CARBON_JKS = "keystores/products/wso2carbon.jks";
    private static final String DATA_BUCKET_LOCATION = "DATA_BUCKET_LOCATION";


    public static void setKeyStoreProperties() {

        String RESOURCE_LOCATION = System.getProperty("common.resource.location");
        System.setProperty("javax.net.ssl.trustStore", RESOURCE_LOCATION + PATH_TO_WSO2CARBON_JKS);
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
    }

    /**
     * This is a utility method to load the deployment details.
     * The deployment details are available as key-value pairs in {@link #INFRASTRUCTURE_PROPERTIES},
     * {@link #DEPLOYMENT_PROPERTIES}, and {@link #JOB_PROPERTIES} under the
     * <p>
     * This method loads these files into one single properties, and return it.
     *
     * @return properties the deployment properties.
     */
    public static Properties getDeploymentProperties() {

        String INPUTS_LOCATION = System.getenv(DATA_BUCKET_LOCATION);
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
}
