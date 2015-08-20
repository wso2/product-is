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
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean;
import org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityExceptionException;
import org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException;
import org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceStub;
import org.wso2.carbon.identity.mgt.stub.beans.VerificationBean;
import org.wso2.carbon.identity.mgt.stub.dto.ChallengeQuestionIdsDTO;
import org.wso2.carbon.identity.mgt.stub.dto.UserChallengesDTO;
import org.wso2.carbon.identity.mgt.stub.dto.UserIdentityClaimDTO;
import org.wso2.sample.inforecovery.client.authenticator.ServiceAuthenticator;

import java.rmi.RemoteException;

public class UserInformationRecoveryClient {

    private UserInformationRecoveryServiceStub stub;

    public UserInformationRecoveryClient(String url, ConfigurationContext configContext)
            throws Exception {

        stub = new UserInformationRecoveryServiceStub(configContext, url
                + "services/UserInformationRecoveryService");

        ServiceAuthenticator authenticator = ServiceAuthenticator.getInstance();
        authenticator.authenticate(stub._getServiceClient());
    }

    public UserInformationRecoveryClient() throws Exception {
        stub = new UserInformationRecoveryServiceStub();

        ServiceAuthenticator authenticator = ServiceAuthenticator.getInstance();
        authenticator.authenticate(stub._getServiceClient());
    }

    public CaptchaInfoBean generateCaptcha() throws AxisFault {

        CaptchaInfoBean bean = null;

        try {
            bean = stub.getCaptcha();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bean;
    }

    public VerificationBean VerifyUser(String username, CaptchaInfoBean captcha) {

        VerificationBean bean = null;

        try {
            bean = stub.verifyUser(username, captcha);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return bean;
    }

    public VerificationBean sendRecoveryNotification(String username, String key, String notificationType) {

        VerificationBean bean = null;

        try {
            bean = stub.sendRecoveryNotification(username, key, "EMAIL");
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bean;
    }

    public VerificationBean verifyConfirmationCode(String username, String code,
                                                   CaptchaInfoBean captcha) {

        VerificationBean bean = null;

        try {
            bean = stub.verifyConfirmationCode(username, code, captcha);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return bean;
    }

    public VerificationBean resetPassword(String username, String confirmationCode,
                                          String newPassword) {

        VerificationBean bean = null;

        try {
            bean = stub.updatePassword(username, confirmationCode, newPassword);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return bean;
    }

    public ChallengeQuestionIdsDTO getChallengeQuestionIds(String username,
                                                           String confirmationCode) {

        ChallengeQuestionIdsDTO bean = null;

        try {
            bean = stub.getUserChallengeQuestionIds(username, confirmationCode);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return bean;
    }

    public UserChallengesDTO getChallengeQuestion(String username, String code, String id) {

        UserChallengesDTO bean = null;

        try {
            bean = stub.getUserChallengeQuestion(username, code, id);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return bean;
    }

    public VerificationBean checkAnswer(String username, String code, String id, String answer) {

        VerificationBean bean = null;

        try {
            bean = stub.verifyUserChallengeAnswer(username, code, id, answer);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return bean;
    }

    public UserIdentityClaimDTO[] getUserIdentitySupportedClaims(String dialect) {
        UserIdentityClaimDTO[] cliams = null;
        try {
            cliams = stub.getUserIdentitySupportedClaims(dialect);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UserInformationRecoveryServiceIdentityExceptionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return cliams;
    }

    public VerificationBean verifyAccount(UserIdentityClaimDTO[] claims, CaptchaInfoBean captcha,
                                          String tenantDomain) throws RemoteException {
        VerificationBean bean = null;
        try {
            bean = stub.verifyAccount(claims, captcha, tenantDomain);
        } catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bean;
    }

    public VerificationBean registerUser(String userName, String password,
                                         UserIdentityClaimDTO[] claims, String profileName,
                                         String tenantDomain) {
        VerificationBean bean = null;
        try {
            bean = stub.registerUser(userName, password, claims, profileName, tenantDomain);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bean;
    }

    public VerificationBean confirmUserSelfRegistration
            (String userName, String code, CaptchaInfoBean captcha, String tenantDomain)
            throws RemoteException {

        VerificationBean bean = null;
        try {
            bean = stub.confirmUserSelfRegistration(userName, code, captcha, tenantDomain);
        } catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bean;
    }
}
