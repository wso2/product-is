/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.identity.sso.agent.exception.SSOAgentException;
import org.wso2.carbon.identity.sso.agent.util.SSOAgentConfigs;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SampleContextEventListener implements ServletContextListener {

    private static Logger LOGGER = Logger.getLogger("InfoLogging");

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        Properties properties = new Properties();
        try {
            properties.load(servletContextEvent.getServletContext().getClassLoader()
                    .getResourceAsStream("sso.properties"));
            SSOAgentConfigs.initConfig(properties);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (SSOAgentException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        SSOAgentConfigs.setKeyStoreStream(servletContextEvent.getServletContext().getClassLoader()
                .getResourceAsStream("wso2carbon.jks"));
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
