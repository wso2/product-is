/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.identity.integration.test.analytics.commons;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.databridge.core.AgentCallback;
import org.wso2.carbon.databridge.core.DataBridge;
import org.wso2.carbon.databridge.core.Utils.AgentSession;
import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.databridge.core.definitionstore.InMemoryStreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.core.internal.authentication.AuthenticationHandler;
import org.wso2.carbon.databridge.receiver.thrift.ThriftDataReceiver;
import org.wso2.carbon.user.api.UserStoreException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;

public class ThriftServer implements Runnable {
	private static Log log = LogFactory.getLog(ThriftServer.class);
	private final String FILE_STREAM_DEFINITION_EXT = ".json";
	private AbstractStreamDefinitionStore streamDefinitionStore = new InMemoryStreamDefinitionStore();
	private ThriftDataReceiver thriftDataReceiver;
	private boolean eventReceived = false;
	private AtomicLong msgCount = new AtomicLong(0);
	private String testCaseResourceFolderName;
	private int listeningPort;
	private List<Event> preservedEventList = null;
	private boolean isPreservingEvents;

	public ThriftServer(String testCaseResourceFolderName, int listeningPort, boolean isPreservingEvents) {
		this.testCaseResourceFolderName = testCaseResourceFolderName;
		this.listeningPort = listeningPort;
		this.isPreservingEvents = isPreservingEvents;
	}

	public void startServer() throws DataBridgeException, StreamDefinitionStoreException {
		msgCount.set(0);
		start(listeningPort);
	}

	public void start(int receiverPort) throws DataBridgeException, StreamDefinitionStoreException {
		KeyStoreUtil.setKeyStoreParams();
		DataBridge databridge = new DataBridge(new AuthenticationHandler() {
			@Override public boolean authenticate(String userName, String password) {
				// Always authenticate to true.
				return true;
			}

			@Override public String getTenantDomain(String userName) {
				return "admin";
			}

			@Override public int getTenantId(String s) throws UserStoreException {
				return -1234;
			}

			@Override public void initContext(AgentSession agentSession) {

			}

			@Override public void destroyContext(AgentSession agentSession) {

			}

		}, streamDefinitionStore, getResourceFilePath("StatisticTestResources", "data-bridge-config.xml"));
		thriftDataReceiver = new ThriftDataReceiver(receiverPort, databridge);

		for (StreamDefinition streamDefinition : loadStreamDefinitions()) {
			streamDefinitionStore.saveStreamDefinitionToStore(streamDefinition, -1234);
			log.info("StreamDefinition of '" + streamDefinition.getStreamId() + "' added to store");
		}

		databridge.subscribe(new AgentCallback() {

			@Override public void definedStream(StreamDefinition streamDefinition, int tenantId) {
				log.info("Added StreamDefinition " + streamDefinition);
			}

			@Override public void removeStream(StreamDefinition streamDefinition, int tenantId) {
				log.info("Removed StreamDefinition " + streamDefinition);
			}

			@Override public void receive(List<Event> eventList, Credentials credentials) {
				log.info("eventListSize=" + eventList.size() + " eventList " + eventList + " for username " +
				         credentials.getUsername());
				eventReceived = true;
				msgCount.addAndGet(eventList.size());
				if (isPreservingEvents) {
					if (preservedEventList == null) {
						preservedEventList = new ArrayList<>();
					}
					preservedEventList.addAll(eventList);
				}
			}
		});
		thriftDataReceiver.start("0.0.0.0");
		log.info("Test Server Started.");
	}

	public void stop() {
		if (!eventReceived) {
			log.warn("Events did not received.");
		}
		thriftDataReceiver.stop();
		log.info("Test Server Stopped.");
	}

	@Override public void run() {
		try {
			startServer();
		} catch (DataBridgeException e) {
			log.error("Cannot start the test server.", e);
		} catch (StreamDefinitionStoreException e) {
			log.error("StreamDefinition cannot be added to the store.", e);
		}
	}

	public long getMsgCount() {
		return msgCount.get();
	}

	public List<Event> getPreservedEventList() {
		return preservedEventList;
	}

	public void resetMsgCount() {
		msgCount = new AtomicLong(0);
	}

	public void resetPreservedEventList() {
		preservedEventList.clear();
	}

	public List<StreamDefinition> loadStreamDefinitions() {

		String relativeFilePath = FrameworkPathUtil.getSystemResourceLocation() +
		                          "artifacts" + File.separator + "ESB" + File.separator +
		                          testCaseResourceFolderName;
		String directoryPath = relativeFilePath.replaceAll("[\\\\/]", Matcher.quoteReplacement(File.separator));
		directoryPath = getResourceFilePath(testCaseResourceFolderName,"");
		GenericExtFilter filter = new GenericExtFilter(FILE_STREAM_DEFINITION_EXT);
		File directory = new File(directoryPath);
		List<StreamDefinition> streamDefinitions = new ArrayList<StreamDefinition>();
		if (!directory.exists()) {
			log.error("Cannot load stream definitions from " + directory.getAbsolutePath() + " directory not exist");
			return streamDefinitions;
		}
		if (!directory.isDirectory()) {
			log.error("Cannot load stream definitions from " + directory.getAbsolutePath() + " not a directory");
			return streamDefinitions;
		}

		// List out all the file names and filter by the extension.
		String[] listStreamDefinitionFiles = directory.list(filter);
		if (listStreamDefinitionFiles != null) {
			for (final String fileEntry : listStreamDefinitionFiles) {
				BufferedReader bufferedReader = null;
				StringBuilder stringBuilder = new StringBuilder();
				String fullPathToStreamDefinitionFile = directoryPath + File.separator + fileEntry;
				try {
					bufferedReader = new BufferedReader(new FileReader(fullPathToStreamDefinitionFile));
					String line;
					while ((line = bufferedReader.readLine()) != null) {
						stringBuilder.append(line).append("\n");
					}
					StreamDefinition streamDefinition =
							EventDefinitionConverterUtils.convertFromJson(stringBuilder.toString().trim());
					streamDefinitions.add(streamDefinition);
				} catch (IOException e) {
					log.error("Error in reading file : " + fullPathToStreamDefinitionFile, e);
				} catch (MalformedStreamDefinitionException e) {
					log.error("Error in converting Stream definition : " + e.getMessage(), e);
				} finally {
					try {
						if (bufferedReader != null) {
							bufferedReader.close();
						}
					} catch (IOException e) {
						log.error("Error occurred when reading the file : " + e.getMessage(), e);
					}
				}
			}
		}
		return streamDefinitions;
	}

	public String getResourceFilePath(String testCaseFolderName, String resourceFileName) {
		String relativeFilePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
		"IS" + File.separator + "analytics" + File.separator + testCaseFolderName + File.separator + resourceFileName;
		return relativeFilePath.replaceAll("[\\\\/]", Matcher.quoteReplacement(File.separator));
	}

	// Inner class, generic extension filter
	public class GenericExtFilter implements FilenameFilter {

		private String ext;

		public GenericExtFilter(String ext) {
			this.ext = ext;
		}

		public boolean accept(File dir, String name) {
			return (name.endsWith(ext));
		}
	}

	public void waitToReceiveEvents(int maxWaitTime, int expectedCount){
		for(int i = 0; i < maxWaitTime; i = i + 5000){
			if(msgCount.get() >= expectedCount){
				break;
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				//ignored
			}
		}
	}
}