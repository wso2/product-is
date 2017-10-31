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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.listeners;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.extensions.FrameworkExtensionUtils;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * An Extension to provide the capability to deploy and destroy tomcat container at the beginning of
 * and at the end of a test suite.
 */
public class IdentityTomcatDeployer implements ISuiteListener{

    private static final Log log = LogFactory.getLog(IdentityTomcatDeployer.class);

    private Map<String, String> tomcatParameters = new HashMap<>();
    private Tomcat tomcatServer;

    @Override
    public void onStart(ISuite iSuite) {

        initParameters(iSuite);

        // Configured tomcat port parameter needs to be a number.
        int tomcatPort;
        try{
            tomcatPort = Integer.valueOf(tomcatParameters.get(IdentityListenerConstants.TOMCAT_PORT_PARAMETER));
        } catch (NumberFormatException ex) {
            log.warn("Tomcat port given in suite parameter is not a valid port number. Defaulting to " +
                    IdentityListenerConstants.DEFAULT_TOMCAT_PORT, ex);
            tomcatPort = IdentityListenerConstants.DEFAULT_TOMCAT_PORT;
        }

        String tomcatAppUrls = tomcatParameters.get(IdentityListenerConstants.TOMCAT_APPLICATIONS_URLS_PARAMETER);
        String tomcatAppLocations = tomcatParameters.get(
                IdentityListenerConstants.TOMCAT_APPLICATIONS_LOCATIONS_PARAMETER);
        if(StringUtils.isNotEmpty(tomcatAppUrls) && StringUtils.isNotEmpty(tomcatAppLocations)){
            tomcatServer = getTomcat(tomcatPort);
            // Start tomcat and deploy the given applications.
            try {
                startTomcat(tomcatServer, tomcatAppUrls.split(IdentityListenerConstants.PARAMETER_SEPARATOR),
                        tomcatAppLocations.split(IdentityListenerConstants.PARAMETER_SEPARATOR));
            } catch (AutomationFrameworkException ex) {
                handleException("Error while starting the tomcat server.", ex);
            }
        }
    }

    @Override
    public void onFinish(ISuite iSuite) {
        try {
            tomcatServer.stop();
            tomcatServer.destroy();
        } catch (LifecycleException ex) {
            handleException("Error while stopping tomcat server.", ex);
        }
    }

    /**
     * Extract tomcat deployment related configuration parameters.
     *
     * @param iSuite
     */
    private void initParameters(ISuite iSuite) {

        tomcatParameters.put(IdentityListenerConstants.TOMCAT_PORT_PARAMETER, iSuite.getParameter(
                IdentityListenerConstants.TOMCAT_PORT_PARAMETER));
        tomcatParameters.put(IdentityListenerConstants.TOMCAT_APPLICATIONS_URLS_PARAMETER, iSuite.getParameter(
                IdentityListenerConstants.TOMCAT_APPLICATIONS_URLS_PARAMETER));
        tomcatParameters.put(IdentityListenerConstants.TOMCAT_APPLICATIONS_LOCATIONS_PARAMETER, iSuite.getParameter(
                IdentityListenerConstants.TOMCAT_APPLICATIONS_LOCATIONS_PARAMETER));
    }

    private Tomcat getTomcat(int tomcatPort) {
        Tomcat tomcat = new Tomcat();
        tomcat.getService().setContainer(tomcat.getEngine());
        tomcat.setPort(tomcatPort);
        tomcat.setBaseDir("");

        StandardHost stdHost = (StandardHost) tomcat.getHost();

        stdHost.setAppBase("");
        stdHost.setAutoDeploy(true);
        stdHost.setDeployOnStartup(true);
        stdHost.setUnpackWARs(true);
        tomcat.setHost(stdHost);

        return tomcat;
    }

    private void startTomcat(Tomcat tomcat, String[] webAppUrls, String[] webAppPaths)
            throws AutomationFrameworkException {

        // Number of configured webApppaths and webAppUrls should be equal.
        // Otherwise should throw an Exception.
        if (webAppUrls.length == webAppPaths.length) {
            for (int index = 0; index < webAppPaths.length; index++) {
                String webAppPath = webAppPaths[index];
                String webAppUrl = webAppUrls[index];
                // Ignore the if any of webAppPath or webAppUrl is blank.
                if(StringUtils.isNotBlank(webAppPath) && StringUtils.isNotBlank(webAppUrl)){
                    URL resourceUrl = getClass().getResource(File.separator +
                            FrameworkExtensionUtils.getOSSensitivePath(webAppPath));
                    tomcat.addWebapp(tomcat.getHost(), "/" + webAppUrl, resourceUrl.getPath());
                }
            }
        } else {
            throw new AutomationFrameworkException("Number of " + IdentityListenerConstants
                    .ADDITIONAL_IS_PARAMETER_VALUE_SEPARATOR +
                    " separated " + IdentityListenerConstants.TOMCAT_APPLICATIONS_LOCATIONS_PARAMETER + " and "
                    + IdentityListenerConstants.TOMCAT_APPLICATIONS_URLS_PARAMETER + "should be equal.");
        }

        try {
            tomcat.start();
        } catch (LifecycleException ex) {
            throw new AutomationFrameworkException("Error occurred while starting tomcat.", ex);
        }
    }

    private static void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new RuntimeException(msg, e);
    }
}

