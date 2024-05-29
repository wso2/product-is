/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.identity.integration.test.cache;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

public class JMSCacheSyncTestCase extends ISIntegrationTest {

    private static final Log log = LogFactory.getLog(JMSCacheSyncTestCase.class);
    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String TOPIC_NAME = "CacheTopic";
    private static final String SERVICE_STARTED_MESSAGE =
            "Cache Sync JMS Manager Service bundle activated successfully.";
    private static final String RECEIVED_MESSAGE = "Received cache invalidation message from other cluster nodes";
    private static final String SENT_MESSAGE = "Sending cache invalidation message to other cluster nodes";
    public static final String CACHE_SYNC_JMS_MANAGER_JAR = "org.wso2.carbon.cache.sync.jms.manager-2.0.14-SNAPSHOT.jar";
    public static final String JMS_API_JAR = "jms-api_2-2.0.1.wso2v1.jar";

    private static Connection connection;
    private static Session session;
    private static MessageProducer producer;
    private static BrokerService broker;

    private ServerConfigurationManager scm;
    private File defaultConfigFile;
    private File defaultLog4j2File;
    private File carbonLogfile;
    private String jarPathString;

    @BeforeTest(alwaysRun = true)
    public void initCacheSyncConfig() throws Exception {

        startBroker();
        String carbonHome = CarbonUtils.getCarbonHome();
        carbonLogfile = new File(carbonHome + File.separator + "repository"
                + File.separator + "logs" + File.separator + "wso2carbon.log");
        removeExistingCarbonLog();

        super.init();
        applyDropinJar(CACHE_SYNC_JMS_MANAGER_JAR);
        applyDropinJar(JMS_API_JAR);
        applyAllActiveMQJars();

        scm = new ServerConfigurationManager(isServer);

        applyCacheSyncDeploymentConfigurations(carbonHome);
        applyCacheSyncDebugLogConfigurations(carbonHome);
    }

    @AfterClass(alwaysRun = true)
    public void testClear() throws Exception {

        stopBroker();
//        removeDropinsJar(CACHE_SYNC_JMS_MANAGER_JAR);
//        removeDropinsJar(JMS_API_JAR);
//        removeAllActiveMQJars();
        resetISConfiguration();
    }

    @Test
    public void confirmAllRelatedArtifactsPresence() {

        Assert.assertTrue(Files.exists(Paths.get(getDropinsFilePath(CACHE_SYNC_JMS_MANAGER_JAR).toURI())),
                "Cache sync manager jar is not copied successfully.");
        Assert.assertTrue(Files.exists(Paths.get(getDropinsFilePath(JMS_API_JAR).toURI())),
                "JMS api jar is not copied successfully.");
    }

    @Test
    public void isServiceStarted() {

        Assert.assertTrue(
                checkLogForMessage(SERVICE_STARTED_MESSAGE),
                "Error while starting JMS cache sync service.");
    }

    @Test
    public void isMessageReceived() throws InterruptedException {

        Thread.sleep(50000);
        sendMessage();
        System.out.println("Message send successfully");
        Thread.sleep(50000);

        Assert.assertTrue(
                checkLogForMessage(RECEIVED_MESSAGE),
                "Error while receiving message from jms broker.");
    }

    @Test
    public void isMessageSent() {

        Assert.assertTrue(
                checkLogForMessage(SENT_MESSAGE),
                "Error while sending message to jms broker.");
    }

    private enum ActiveMQJars {

        GERONIMO_J2EE_MANAGEMENT("geronimo-j2ee-management_1.1_spec-1.0.1.jar"),
        ACTIVEMQ_BROKER("activemq-broker-5.16.3.jar"),
        ACTIVEMQ_CLIENT("activemq-client-5.16.3.jar"),
        GERONIMO_JMS("geronimo-jms_1.1_spec-1.1.1.jar"),
        HAWTBUF("hawtbuf-1.11.jar"),
        SLF4J_API("slf4j-api-1.7.31.jar");

        private final String jarName;

        ActiveMQJars(String jarName) {
            this.jarName = jarName;
        }

        public String getJarName() {
            return jarName;
        }
    }

    private void applyCacheSyncDebugLogConfigurations(String carbonHome) throws IOException {

        log.info("Replacing default log4j2.properties & enabling cache sync debug logs");
        defaultLog4j2File = new File(carbonHome + File.separator + "repository" + File.separator
                + "conf" + File.separator + "log4j2.properties");
        File configuredLo4j2File = new File(getISResourceLocation() + File.separator + "cache"
                + File.separator + "cache_sync_log4j2.properties");
        scm.applyConfigurationWithoutRestart(configuredLo4j2File, defaultLog4j2File, true);
    }

    private void applyCacheSyncDeploymentConfigurations(String carbonHome) throws AutomationUtilException, IOException {

        log.info("Replacing default deployment.toml & enabling cache sync manager.");
        defaultConfigFile = getDeploymentTomlFile(carbonHome);
        File cacheSyncConfigFile = new File(
                getISResourceLocation() + File.separator + "cache" + File.separator +
                        "cache_sync_config.toml");
        scm.applyConfiguration(cacheSyncConfigFile, defaultConfigFile, true, true);
    }

    private void applyDropinJar(String jarName) throws IOException {

        InputStream jarUrl = getClass()
                .getResourceAsStream(ISIntegrationTest.URL_SEPARATOR + "samples" + ISIntegrationTest.URL_SEPARATOR +
                        "cache" + ISIntegrationTest.URL_SEPARATOR + jarName);
        jarPathString = Utils.getResidentCarbonHome()
                + File.separator + "repository"
                + File.separator + "components" + File.separator
                + "dropins" + File.separator + jarName;
        File jarDestFile = new File(jarPathString);
        FileOutputStream jarDest = new FileOutputStream(jarDestFile);
        copyFileUsingStream(jarUrl, jarDest);
        log.info("Copied the cache sync manager jar file to " + jarPathString);
    }

    private void applyAllActiveMQJars() throws IOException {

        for (ActiveMQJars jar : ActiveMQJars.values()) {
            applyLibJar(jar.getJarName());
        }
    }

    private void applyLibJar(String jarName) throws IOException {

        InputStream jarUrl = getClass().getResourceAsStream(ISIntegrationTest.URL_SEPARATOR
                + "samples" + ISIntegrationTest.URL_SEPARATOR
                + "cache" + ISIntegrationTest.URL_SEPARATOR + jarName);
        String jarPathString = Utils.getResidentCarbonHome()
                + File.separator + "repository"
                + File.separator + "components" + File.separator
                + "lib" + File.separator + jarName;
        File jarDestFile = new File(jarPathString);
        FileOutputStream jarDest = new FileOutputStream(jarDestFile);
        copyFileUsingStream(jarUrl, jarDest);
        log.info("Copied the activemq jar file to " + jarPathString);
    }

    private void removeAllActiveMQJars() {

        for (ActiveMQJars jar : ActiveMQJars.values()) {
            removeLibJar(jar.getJarName());
            removeDropinsJar(transformJarName(jar.getJarName()));
        }
    }

    private String transformJarName(String originalName) {
        return originalName.replace("-", "_").replaceAll("(\\.\\w+)$", "_1.0.0$1");
    }

    private void removeExistingCarbonLog() {

        if (carbonLogfile.exists()) {
            carbonLogfile.delete();
        }
    }

    private File getLibFilePath(String jarName) {

        File jarDestFile = new File(Utils.getResidentCarbonHome()
                + File.separator + File.separator + "repository"
                + File.separator + "components" + File.separator
                + "lib" + File.separator + jarName);
        return jarDestFile;
    }

    private void removeLibJar(String jarName) {

        File jarDestFile = getLibFilePath(jarName);
        if (jarDestFile.exists()) {
            jarDestFile.delete();;
        }
    }

    private void removeDropinsJar(String jarName) {

        File jarDestFile = getDropinsFilePath(jarName);
        if (jarDestFile.exists()) {
            jarDestFile.delete();;
        }
    }

    private File getDropinsFilePath(String jarName) {

        File jarDestFile = new File(Utils.getResidentCarbonHome()
                + File.separator + File.separator + "repository"
                + File.separator + "components" + File.separator
                + "dropins" + File.separator + jarName);
        return jarDestFile;
    }

    private void resetISConfiguration() throws Exception {

        log.info("Replacing log4j2.properties with default configurations");
        scm.restoreToLastConfiguration(false);
    }

    private boolean checkLogForMessage(String message) {

        try (Scanner scanner = new Scanner(carbonLogfile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains(message)) {
                    log.info("Found related log message: " + message);
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            log.error("Exception occurred while reading the log file: ", e);
        }
        return false;
    }

    private void startBroker() {

        try {
            // Create a new instance of the broker service
            if (broker != null) {
                broker.stop();
            }
            broker = new BrokerService();
            broker.setBrokerName("embeddedBroker");
            broker.addConnector("tcp://localhost:61616");
            broker.setPersistent(false);
            broker.setPlugins(new BrokerPlugin[0]);
            broker.start();
            log.info("ActiveMQ broker started successfully.");
        } catch (Exception e) {
            log.error("Error while starting the ActiveMQ broker." + e);
        }
    }

    private void stopBroker() {

        try {
            broker.stop();
            System.out.println("ActiveMQ broker stopped successfully.");
        } catch (Exception e) {
            log.error("Error while stopping the ActiveMQ broker.");;
        }
    }

    private void sendMessage() {

        try {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(TOPIC_NAME);
            producer = session.createProducer(topic);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            String invalidationMsg = "ClusterCacheInvalidationRequest{" +
                    "tenantId=-1234, " +
                    "tenantDomain='example.com', " +
                    "messageId=message-id-1234, " +
                    "cacheManager=IdentityApplicationManagementCacheManager, " +
                    "cache=$__local__$.AppAuthFrameworkSessionContextCache, " +
                    "cacheKey=org.wso2.carbon.identity.application.authentication.framework.cache." +
                    "SessionContextCacheKey@543af693}";
            TextMessage message = session.createTextMessage(invalidationMsg);
            producer.send(message);
            log.info("Sending cache invalidation message to other cluster nodes:" + invalidationMsg);

        } catch (Exception e) {
            log.error("Exception while sending message to ActiveMQ: ", e);
        } finally {
            try {
                if (producer != null) producer.close();
                if (session != null) session.close();
                if (connection != null) connection.close();
            } catch (JMSException e) {
                log.error("Exception while closing JMS resources: ", e);
            }
        }
    }

    private void copyFileUsingStream(InputStream source, OutputStream dest) throws IOException {

        try {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = source.read(buffer)) > 0) {
                dest.write(buffer, 0, length);
            }
        } finally {
            source.close();
            dest.close();
        }
    }
}
