/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.sample.inforecovery.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceStub;
import org.wso2.carbon.identity.mgt.stub.dto.ChallengeQuestionDTO;
import org.wso2.carbon.identity.mgt.stub.dto.UserChallengesDTO;
import org.wso2.sample.inforecovery.client.authenticator.AuthenticationException;
import org.wso2.sample.inforecovery.client.authenticator.ServiceAuthenticator;

/**
 *
 */
public class IdentityManagementAdminClient {

    public static final String CHALLENGE_QUESTION = "challenge.question";
    protected static Log log = LogFactory.getLog(IdentityManagementAdminClient.class);
    protected UserIdentityManagementAdminServiceStub stub = null;

    public IdentityManagementAdminClient(String cookie, String url
            , ConfigurationContext configContext)
            throws Exception {
        try {
            stub = new UserIdentityManagementAdminServiceStub(configContext, url
                    + "services/UserIdentityManagementAdminService");
            ServiceAuthenticator authenticator = ServiceAuthenticator.getInstance();
            ServiceClient client = stub._getServiceClient();
            try {
                authenticator.authenticate(client);
            } catch (AuthenticationException e) {
                e.printStackTrace();
            }
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    public ChallengeQuestionDTO[] getChallengeQuestions() throws AxisFault {

        try {
            return stub.getAllChallengeQuestions();
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }

        return null;
    }

    public void setChallengeQuestions(ChallengeQuestionDTO[] challengeQuestionDTOs)
            throws AxisFault {
        try {
            stub.setChallengeQuestions(challengeQuestionDTOs);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    public void setChallengeQuestionsOfUser(String userId, UserChallengesDTO[] userChallengesDTOs)
            throws AxisFault {
        try {
            stub.setChallengeQuestionsOfUser(userId, userChallengesDTOs);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    public UserChallengesDTO[] getChallengeQuestionsOfUser(String userId) throws AxisFault {

        try {
            return stub.getChallengeQuestionsOfUser(userId);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }

        return null;
    }

    public boolean isReadOnlyUserStore(String username, String tenantDomain) throws AxisFault {

        try {
            return stub.isReadOnlyUserStore(username, tenantDomain);

        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }

        return false;
    }

    private String[] handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }
}
