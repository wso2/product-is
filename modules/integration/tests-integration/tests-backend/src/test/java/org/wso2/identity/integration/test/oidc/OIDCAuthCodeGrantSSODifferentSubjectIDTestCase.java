/*
 * Copyright (c) 2016, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.identity.integration.test.oidc;

import org.wso2.identity.integration.test.oidc.bean.OIDCApplication;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationPatchModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.ApplicationResponseModel;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.Claim;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SubjectConfig;

/**
 * This test class tests OIDC SSO functionality for two relying party applications with different subject identifiers.
 */
public class OIDCAuthCodeGrantSSODifferentSubjectIDTestCase extends OIDCAuthCodeGrantSSOTestCase {

    @Override
    protected void initUser() throws Exception {

        super.initUser();
        user.setUserName("oidcsessiontestuser1");
    }

    @Override
    protected void initApplications() throws Exception {

        super.initApplications();

        applications.get(OIDCUtilTest.PLAYGROUND_APP_ONE_APP_NAME).setSubjectClaimURI(OIDCUtilTest.EMAIL_CLAIM_URI);
        applications.get(OIDCUtilTest.PLAYGROUND_APP_TWO_APP_NAME).setSubjectClaimURI(OIDCUtilTest.LAST_NAME_CLAIM_URI);
    }

    @Override
    public void createApplication(OIDCApplication application) throws Exception {

        super.createApplication(application);

        ApplicationResponseModel oidcApplication = getApplication(application.getApplicationId());

        SubjectConfig subjectClaimConfig = new SubjectConfig().claim(new Claim().uri(application.getSubjectClaimURI()));
        ApplicationPatchModel applicationPatch = new ApplicationPatchModel();
        applicationPatch.setClaimConfiguration(oidcApplication.getClaimConfiguration().subject(subjectClaimConfig));

        updateApplication(application.getApplicationId(), applicationPatch);
    }
}
