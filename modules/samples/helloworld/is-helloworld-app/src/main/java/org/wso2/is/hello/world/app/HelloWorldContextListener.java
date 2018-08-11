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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import static org.wso2.is.hello.world.app.HelloWorldAppUtils.buildPropertyFilePath;
import static org.wso2.is.hello.world.app.HelloWorldAppUtils.getAppUriWithContext;
import static org.wso2.is.hello.world.app.HelloWorldAppUtils.getApplicationProperty;
import static org.wso2.is.hello.world.app.HelloWorldAppUtils.isValidApp;
import static org.wso2.is.hello.world.app.HelloWorldAppUtils.registerApp;

/**
 * This is an extension of {@link ServletContextListener} which is used to do the startup app configurations.
 */
public class HelloWorldContextListener implements ServletContextListener {

    private static final Logger logger = Logger.getLogger(HelloWorldContextListener.class.getName());
    private static final String[] APP_PROPERTY_KEYS = new String[]{"idp.hostname", "idp.port", "app.hostname",
            "app.port", "app.context", "username", "password"};

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        Path path = buildPropertyFilePath(servletContextEvent);
        HelloWorldDataHolder.getInstance().setPropertyFilePath(path);
        HelloWorldDataHolder.getInstance().setProperties(loadProperties(path));

        String clientId = getApplicationProperty("client_id", null);
        try {
            if (clientId == null || !isValidApp(clientId)) {
                logger.info("Registering OAuth application.");
                registerApp();
            }

            String message = "\n*********************************************************" +
                             "\n*                                                       *" +
                             "\n*    Application starting on: " + getAppUriWithContext("") + "    *" +
                             "\n*                                                       *" +
                             "\n*********************************************************";

            logger.info(message);
        } catch (HelloWorldException e) {
            logger.log(Level.SEVERE, "Error while registering application.", e);
        }
    }

    private Properties loadProperties(Path path) {

        Properties defaultProperties = new Properties();
        try {
            defaultProperties.load(Files.newInputStream(path));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while reading file: " + path.getFileName(), e);
        }
        return getFromSystemProperties(defaultProperties);
    }

    private Properties getFromSystemProperties(Properties defaultProperties) {

        Properties properties = new Properties(defaultProperties);
        for (String propertyKey : APP_PROPERTY_KEYS) {
            String propertyValue = System.getProperty(propertyKey);
            if (propertyValue != null && !propertyValue.isEmpty()) {
                properties.put(propertyKey, propertyValue);
            }
        }
        return properties;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

        logger.info("Shutting down!");
    }
}
