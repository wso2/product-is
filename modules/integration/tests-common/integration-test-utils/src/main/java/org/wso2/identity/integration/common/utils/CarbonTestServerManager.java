/*
*Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.identity.integration.common.utils;

import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.extensions.servers.carbonserver.TestServerManager;

import java.io.IOException;
import java.util.Map;

public class CarbonTestServerManager extends TestServerManager {

    public CarbonTestServerManager(AutomationContext context) {
        super(context);
    }

    public CarbonTestServerManager(AutomationContext context, String carbonZip,
                                      Map<String, String> commandMap) {
        super(context, carbonZip, commandMap);
    }

    public CarbonTestServerManager(AutomationContext context, int portOffset) {
        super(context, portOffset);
    }

    public CarbonTestServerManager(AutomationContext context, String carbonZip) {
        super(context, carbonZip);
    }

    public String startServer() throws Exception {
        String carbonHome = super.startServer();
        System.setProperty("carbon.home", carbonHome);
        return carbonHome;
    }

    public void stopServer() throws Exception {
        super.stopServer();
    }

}
