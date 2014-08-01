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

package org.wso2.carbon.identity.ui.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.carbon.automation.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;

public abstract class ISIntegrationUITest {

    protected Log log = LogFactory.getLog(getClass());
    protected EnvironmentVariables isServer;
    protected UserInfo userInfo;
    private EnvironmentBuilder builder;

    protected void init() throws Exception {
        init(2);

    }

    protected void init(int userId) throws Exception {
        userInfo = UserListCsvReader.getUserInfo(userId);
        builder = new EnvironmentBuilder().is(userId);
        isServer = builder.build().getIs();
    }

    protected boolean isRunningOnStratos() {
        return FrameworkFactory.getFrameworkProperties(ProductConstant.IS_SERVER_NAME)
                .getEnvironmentSettings().is_runningOnStratos();
    }

    protected String getESBResourceLocation() {
        return ProductConstant.getResourceLocations(ProductConstant.IS_SERVER_NAME);
    }

    protected boolean isBuilderEnabled() {
        return FrameworkFactory.getFrameworkProperties(ProductConstant.IS_SERVER_NAME)
                .getEnvironmentSettings().is_builderEnabled();
    }

    protected boolean isClusterEnabled() {
        return FrameworkFactory.getFrameworkProperties(ProductConstant.IS_SERVER_NAME)
                .getEnvironmentSettings().isClusterEnable();
    }

    protected String getExecutionEnvironment() {
        return FrameworkFactory.getFrameworkProperties(ProductConstant.IS_SERVER_NAME)
                .getEnvironmentSettings().executionEnvironment();
    }


    protected String getLoginURL(String productName) {
        EnvironmentBuilder environmentBuilder = new EnvironmentBuilder();
        boolean isRunningOnStratos =
                environmentBuilder.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos();

        if (isRunningOnStratos) {
            return ProductUrlGeneratorUtil.getServiceHomeURL(productName);
        } else {
            return ProductUrlGeneratorUtil.getProductHomeURL(productName);
        }
    }

    protected boolean isRunningOnCloud() {
        return FrameworkFactory.getFrameworkProperties(ProductConstant.APP_SERVER_NAME).getEnvironmentSettings().is_runningOnStratos();

    }
}