/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.identity.integration.test.base;

import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.extensions.servers.carbonserver.MultipleServersManager;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to hold data elements that are shared among test classes.
 */
public class TestDataHolder {

    private static TestDataHolder instance = new TestDataHolder();
    private Map<Integer, AutomationContext> automationContextMap;
    private MultipleServersManager manager;
    private AutomationContext automationContext;

    private TestDataHolder(){

        automationContextMap = new HashMap<>();
        manager = new MultipleServersManager();
    }

    public static TestDataHolder getInstance() {

        return instance;
    }

    public MultipleServersManager getManager() {

        return manager;
    }

    public void setManager(MultipleServersManager manager) {

        this.manager = manager;
    }

    public AutomationContext getAutomationContext() {

        return automationContext;
    }

    public void setAutomationContext(AutomationContext context) {

        this.automationContext = context;
    }

    public Map<Integer, AutomationContext> getAutomationContextMap() {

        return automationContextMap;
    }

    public void setAutomationContextMap(Map<Integer, AutomationContext> automationContextMap) {

        this.automationContextMap = automationContextMap;
    }
}
