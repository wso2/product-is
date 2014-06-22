/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.tests;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Scanner;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.testng.Assert;

public class IdentityXmlParserTestCase {

	private static final Log log = LogFactory
			.getLog(IdentityXmlParserTestCase.class);

	@BeforeTest
	public void copyFileContents() {
		String changedFile = (System.getProperty("basedir", "."))
				+ File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator
				+ "identity.xml";
		String backupFile = (System.getProperty("basedir", "."))
				+ File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator
				+ "backupIdentity.xml";
		String actualFile = (System.getProperty("carbon.home"))
				+ File.separator + "repository" + File.separator + "conf"
				+ File.separator + "identity.xml";
		
		try {
			copyFiles(actualFile, backupFile);
			copyFiles(changedFile, actualFile);
		} catch (IOException e) {
			log.error("IOException occured while copying file content...");
		}
	}

	@Test
	public void restartServer() {
		XmlParserTestUtils utils = new XmlParserTestUtils();
		try {
			utils.initAuthenticatorClient();
			utils.initServerAdminClient(utils.login("admin", "admin",
					"localhost"));
			log.info("Restarting gracefully..........");
			utils.restartGracefully();
			Thread.sleep(50000);

/*			utils.initAuthenticatorClient();
			utils.initServerAdminClient(utils.login("admin", "admin",
					"localhost"));
			log.info("Shuting down gracefully..........");
			utils.shutdownGracefully();
			Thread.sleep(30000);*/

			Assert.assertFalse(
					isExceptionOccured(),
					"Error occured while reading the identity.xml after doing some changes to identity.xml...");

		} catch (AxisFault e) {
			log.error("Axis Fault occured while testing...");
		} catch (RemoteException e) {
			log.error("RemoteException occured while testing...");
		} catch (Exception e) {
			log.error("Exception occured while testing...");
		}
	}

	@AfterClass
	public void revertChanges() {
		String backupFile = (System.getProperty("basedir", "."))
				+ File.separator + "src" + File.separator + "test"
				+ File.separator + "resources" + File.separator
				+ "backupIdentity.xml";
		String actualFile = (System.getProperty("carbon.home"))
				+ File.separator + "repository" + File.separator + "conf"
				+ File.separator + "identity.xml";

		try {
			copyFiles(backupFile, actualFile);
		} catch (IOException e) {
			log.error("IOException occured while copying file content...");
		}
	}

	private void copyFiles(String sourceFile, String targetFile)
			throws IOException {
		InputStream in = new FileInputStream(sourceFile);
		OutputStream out = new FileOutputStream(targetFile);
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	private boolean isExceptionOccured() {
		File file = new File((System.getProperty("carbon.home"))
				+ File.separator + "repository" + File.separator + "logs"
				+ File.separator + "wso2carbon.log");
		try {
			Scanner scanner = new Scanner(file);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.contains((CharSequence) "org.apache.axiom.om.OMException: com.ctc.wstx.exc.WstxIOException: Bad file descriptor")) {
					return true;
				}
			}
		} catch (FileNotFoundException e) {
			log.error("FileNotFoundException occured while testing...");
		}
		return false;
	}

}
