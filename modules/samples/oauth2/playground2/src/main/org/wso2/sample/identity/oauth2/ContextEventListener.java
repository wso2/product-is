/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.sample.identity.oauth2;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ContextEventListener implements ServletContextListener {

    private static Logger LOGGER = Logger.getLogger(ContextEventListener.class.getName());

    public void contextInitialized(ServletContextEvent servletContextEvent) {

        //Load the config file.
        Properties properties = new Properties();
        try {
            String resourcePath = "/WEB-INF/classes/playground2.properties";
            InputStream resourceStream = servletContextEvent.getServletContext().getResourceAsStream(resourcePath);
            if (resourceStream != null) {
                properties.load(servletContextEvent.getServletContext().getResourceAsStream(resourcePath));
            }
            ApplicationConfig.setProperties(properties);
        } catch (Exception e){
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

}
