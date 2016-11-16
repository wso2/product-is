/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.integration.common.clients.challenge.questions.mgt;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.xsd.User;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.ClaimMetadataManagementServiceStub;
import org.wso2.carbon.identity.recovery.stub.ChallengeQuestionManagementAdminServiceStub;
import org.wso2.carbon.identity.recovery.stub.model.ChallengeQuestion;
import org.wso2.carbon.identity.recovery.stub.model.UserChallengeAnswer;
import org.wso2.identity.integration.common.clients.AuthenticateStub;


/**
 *  Admin service client for ChallengeQuestionManagement Service.
 *
 */
public class ChallengeQuestionMgtAdminClient {

    private static final Log log = LogFactory.getLog(ChallengeQuestionMgtAdminClient.class);
    private static final String SERVICE_NAME = "ChallengeQuestionManagementAdminService";
    private ChallengeQuestionManagementAdminServiceStub stub = null;

    private String endPoint;

    public ChallengeQuestionMgtAdminClient(String backendUrl, String cookie) throws AxisFault {
            endPoint = backendUrl + SERVICE_NAME;
            stub = new ChallengeQuestionManagementAdminServiceStub(endPoint);
            AuthenticateStub.authenticateStub(cookie, stub);
    }

    public ChallengeQuestionMgtAdminClient(String backEndUrl, String userName, String password) throws AxisFault {
        this.endPoint = backEndUrl + SERVICE_NAME;
        stub = new ChallengeQuestionManagementAdminServiceStub(endPoint);
        AuthenticateStub.authenticateStub(userName, password, stub);
    }

    public ChallengeQuestion[] getChallengeQuestionsForTenant(String tenantDomain) throws AxisFault {

        try {
            return stub.getChallengeQuestionsOfTenant(tenantDomain);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return new ChallengeQuestion[0];
    }


    public ChallengeQuestion[] getChallengeQuestionsForUser(User user) throws AxisFault {

        try {
            return stub.getChallengeQuestionsForUser(user);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }

        return new ChallengeQuestion[0];
    }

    public ChallengeQuestion[] getChallengeQuestionsForLocale(String tenantDomain, String locale) throws AxisFault {

        try {
            return stub.getChallengeQuestionsForLocale(tenantDomain, locale);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }

        return new ChallengeQuestion[0];
    }


    public void setChallengeQuestions(ChallengeQuestion[] challengeQuestions, String tenantDomain)
            throws AxisFault {
        try {
            stub.setChallengeQuestionsOfTenant(challengeQuestions, tenantDomain);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    public void deleteChallengeQuestions(ChallengeQuestion[] challengeQuestions, String tenantDomain)
            throws AxisFault {
        try {
            stub.deleteChallengeQuestionsOfTenant(challengeQuestions, tenantDomain);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    public void setUserChallengeAnswers(User user, UserChallengeAnswer[] userChallengeAnswers)
            throws AxisFault {
        try {
            stub.setUserChallengeAnswers(user, userChallengeAnswers);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    public UserChallengeAnswer[] getUserChallengeAnswers(User user) throws AxisFault {
        try {
            return stub.getUserChallengeAnswers(user);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }

        return new UserChallengeAnswer[0];
    }

    private String[] handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }
}
