/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.ContextUrls;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URISyntaxException;

public class ISIntegrationTest {

    protected Log log = LogFactory.getLog(getClass());
    protected AutomationContext automationContext;
    protected String backendURL;
    protected String sessionCookie;
    protected Tenant tenantInfo;
    protected User userInfo;
    protected ContextUrls identityContextUrls;

    protected void init() throws Exception {
        init(TestUserMode.SUPER_TENANT_ADMIN);

    }

    protected void init(TestUserMode userMode) throws Exception {
        automationContext = new AutomationContext("IDENTITY", userMode);
        backendURL = automationContext.getContextUrls().getBackEndUrl();
        sessionCookie = login();
        identityContextUrls = automationContext.getContextUrls();
        tenantInfo = automationContext.getContextTenant();
        userInfo = tenantInfo.getContextUser();
    }

    protected String login() throws Exception{
        return  new LoginLogoutClient(automationContext).login();
    }

    protected String getSessionCookie() {
        return sessionCookie;
    }

    protected String getISResourceLocation() {
        return TestConfigurationProvider.getResourceLocation("IS");
    }

}
