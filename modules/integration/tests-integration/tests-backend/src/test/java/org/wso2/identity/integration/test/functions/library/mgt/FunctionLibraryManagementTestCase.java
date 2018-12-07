/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.functions.library.mgt;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.functions.library.mgt.model.xsd.FunctionLibrary;
import org.wso2.identity.integration.common.clients.functions.library.mgt.FunctionLibraryManagementServiceClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;

/**
 * Function library management test cases.
 */
public class FunctionLibraryManagementTestCase extends ISIntegrationTest {

    private ConfigurationContext configContext;
    private FunctionLibraryManagementServiceClient functionLibraryManagementServiceClient;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();
        configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null
                , null);
        functionLibraryManagementServiceClient = new FunctionLibraryManagementServiceClient(sessionCookie, backendURL,
                configContext);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        functionLibraryManagementServiceClient = null;
    }

    @Test(alwaysRun = true, description = "Testing create Function Library")
    public void testCreateFunctionLibrary() {

        String functionLibraryName = "TestFunctionLibrary1";
        try {
            createFunctionLibrary(functionLibraryName);
            Assert.assertEquals(functionLibraryManagementServiceClient.getFunctionLibrary(
                    functionLibraryName).getFunctionLibraryName(), functionLibraryName,
                    "Failed to create a Function Library");
        } catch (AxisFault axisFault) {
            Assert.fail("Error while trying to create a Function Library", axisFault);
        }
    }

    @Test(alwaysRun = true, description = "Test listing Function Libraries")
    public void testListFunctionLibraries() {

        String functionLibraryName = "TestFunctionLibrary";

        try {
            FunctionLibrary[] functionLibraries
                    = functionLibraryManagementServiceClient.listFunctionLibraries();
            boolean functionLibraryExists = false;

            for (FunctionLibrary functionLibrary : functionLibraries) {
                if (functionLibrary.getFunctionLibraryName().equals(functionLibraryName)) {
                    Assert.assertEquals(functionLibrary.getDescription(), "This is a Test Function Library",
                            "Reading description failed");
                    functionLibraryExists = true;
                }
            }
            if (!functionLibraryExists) {
                Assert.fail("Could not find function library " + functionLibraryName);
            }

        } catch (AxisFault axisFault) {
            Assert.fail("Error while trying to retrieve a list of function libraries", axisFault);
        }
    }

    @Test(alwaysRun = true, description = "Test getting a Function Library")
    public void testGetFunctionLibrary() {

        String functionLibraryName = "TestFunctionLibrary";

        try {
            FunctionLibrary functionLibrary =
                    functionLibraryManagementServiceClient.getFunctionLibrary(functionLibraryName);
            Assert.assertEquals(functionLibrary.getFunctionLibraryName(), functionLibraryName,
                    "Failed to retrieve a function library");
        } catch (AxisFault axisFault) {
            Assert.fail("Error while trying to retrieve a function library", axisFault);
        }
    }

    @Test(alwaysRun = true, description = "Test updating a Function Library")
    public void testUpdateFunctionLibrary() {

        String functionLibraryName = "TestFunctionLibrary";

        try {
            FunctionLibrary functionLibrary =
                    functionLibraryManagementServiceClient.getFunctionLibrary(functionLibraryName);
            functionLibrary.setDescription("updated description");
            functionLibrary.setFunctionLibraryScript("function updatedTest(name){};module.exports.updatedTet = updatedTest;");
            functionLibraryManagementServiceClient.updateFunctionLibrary(functionLibraryName, functionLibrary);
            FunctionLibrary updatedFunctionLibrary = null;
            updatedFunctionLibrary = functionLibraryManagementServiceClient.getFunctionLibrary(functionLibraryName);
            Assert.assertEquals(updatedFunctionLibrary.getDescription(), "updated description",
                    "Failed update function library description");
            Assert.assertEquals(updatedFunctionLibrary.getFunctionLibraryScript(),
                    "function updatedTest(name){};module.exports.updatedTet = updatedTest;",
                    "Failed update function library script");
        } catch (AxisFault axisFault) {
            Assert.fail("Error while trying to update FunctionLibrary", axisFault);
        }
    }

    @Test(alwaysRun = true, description = "Test deleting a Function Library")
    public void testDeleteFunctionLibrary() {

        String functionLibraryName = "TestFunctionLibrary";

        try {
            deleteFunctionLibrary(functionLibraryName);
            FunctionLibrary[] functionLibraries = functionLibraryManagementServiceClient.listFunctionLibraries();
            boolean functionLibraryExists = false;
            for (FunctionLibrary functionLibrary : functionLibraries) {
                if (functionLibrary.getFunctionLibraryName().equals(functionLibraryName)) {
                    functionLibraryExists = true;
                }
            }

            Assert.assertFalse(functionLibraryExists, functionLibraryName + " has not been deleted.");
        } catch (AxisFault axisFault) {
            Assert.fail("Error while trying to delete a function library", axisFault);
        }
    }

    @BeforeMethod
    public void setUp() {

        createFunctionLibrary("TestFunctionLibrary");
    }

    @AfterMethod
    public void tearDown() {

        deleteFunctionLibrary("TestFunctionLibrary");
    }

    public void deleteFunctionLibrary(String functionLibraryName) {

        try {
            functionLibraryManagementServiceClient.deleteFunctionLibrary(functionLibraryName);
        } catch (AxisFault axisFault) {
            Assert.fail("Error while deleting Function Library", axisFault);
        }
    }

    private void createFunctionLibrary(String functionLibraryName) {

        try {
            FunctionLibrary functionLibrary = new FunctionLibrary();
            functionLibrary.setFunctionLibraryName(functionLibraryName);
            functionLibrary.setDescription("This is a Test Function Library");
            functionLibrary.setFunctionLibraryScript("function test(name){}; module.exports.test=test;");
            functionLibraryManagementServiceClient.createFunctionLibrary(functionLibrary);
        } catch (AxisFault axisFault) {
            Assert.fail("Error while trying to create Function Library", axisFault);
        }
    }
}
