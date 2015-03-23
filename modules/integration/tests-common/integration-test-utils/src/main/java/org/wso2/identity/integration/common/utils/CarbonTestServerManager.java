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
