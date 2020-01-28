/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.test.oidc;

import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.identity.integration.test.oidc.bean.OIDCApplication;

/**
 * This test class tests OIDC SSO functionality for two relying party applications with different subject identifiers
 */
public class OIDCAuthCodeGrantSSODifferentSubjectIDTestCase extends OIDCAuthCodeGrantSSOTestCase {


    @Override
    protected void initUser() throws Exception {
        super.initUser();
        user.setUsername("oidcsessiontestuser1");
    }

    @Override
    protected void initApplications() throws Exception {
        super.initApplications();

        applications.get(OIDCUtilTest.playgroundAppOneAppName).setSubjectClaimURI(OIDCUtilTest.emailClaimUri);
        applications.get(OIDCUtilTest.playgroundAppTwoAppName).setSubjectClaimURI(OIDCUtilTest.lastNameClaimUri);
    }

    @Override
    public void createApplication(OIDCApplication application) throws Exception {

        super.createApplication(application);

        ServiceProvider serviceProvider = appMgtclient.getApplication(application.getApplicationName());
        serviceProvider.getLocalAndOutBoundAuthenticationConfig().setSubjectClaimUri(application.getSubjectClaimURI());

        appMgtclient.updateApplicationData(serviceProvider);
    }
}
