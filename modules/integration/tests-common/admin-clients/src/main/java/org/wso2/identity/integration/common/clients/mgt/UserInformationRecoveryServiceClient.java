/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.identity.integration.common.clients.mgt;

import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean;
import org.wso2.carbon.identity.mgt.stub.dto.UserIdentityClaimDTO;
import org.wso2.carbon.identity.mgt.stub.dto.ChallengeQuestionDTO;
import org.wso2.carbon.identity.mgt.stub.dto.UserChallengesDTO;
import org.wso2.carbon.identity.mgt.stub.dto.ChallengeQuestionIdsDTO;
import org.wso2.carbon.identity.mgt.stub.beans.VerificationBean;
import org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityExceptionException;
import org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException;
import org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceStub;
import org.wso2.identity.integration.common.clients.AuthenticateStub;


public class UserInformationRecoveryServiceClient {

	private UserInformationRecoveryServiceStub infoRecoveryStub;
    private final String serviceName = "UserInformationRecoveryService";
	
    public UserInformationRecoveryServiceClient(String backendURL, String sessionCookie)
            throws AxisFault {
        String endPoint = backendURL + serviceName;
        infoRecoveryStub = new UserInformationRecoveryServiceStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, infoRecoveryStub);
    }

    public UserInformationRecoveryServiceClient(String backendURL, String userName, String password)
            throws AxisFault {
        String endPoint = backendURL + serviceName;
        infoRecoveryStub = new UserInformationRecoveryServiceStub(endPoint);
        AuthenticateStub.authenticateStub(userName, password, infoRecoveryStub);
    }
    
	public CaptchaInfoBean getCaptcha() throws RemoteException {
		CaptchaInfoBean bean = null;
		try {
			bean = infoRecoveryStub.getCaptcha();
		} catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
			e.printStackTrace();
		}
		return bean;
	}
    
    public VerificationBean verifyUser(String username, CaptchaInfoBean captcha) throws RemoteException {
    	VerificationBean bean = null;
    	try {
    		bean = infoRecoveryStub.verifyUser(username, captcha);
		} catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
			e.printStackTrace();
		}
    	return bean;
    }
    
    public VerificationBean sendRecoveryNotification(String username, String key, String notificationType) throws RemoteException {
    	VerificationBean bean = null;
    	try {
    		bean = infoRecoveryStub.sendRecoveryNotification(username, key, notificationType);
		} catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
			e.printStackTrace();
		}
    	return bean;
    }
    
    public VerificationBean verifyConfirmationCode(String username, String code,
			CaptchaInfoBean captcha) throws RemoteException {
    	VerificationBean bean = null;
    	try {
    		bean = infoRecoveryStub.verifyConfirmationCode(username, code, captcha);
		} catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
			e.printStackTrace();
		}
    	return bean;
    }
    
    public VerificationBean updatePassword(String username, String confirmationCode,
			String newPassword) throws RemoteException {
    	VerificationBean bean = null;
    	try {
    		bean = infoRecoveryStub.updatePassword(username, confirmationCode, newPassword);
		} catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
			e.printStackTrace();
		}
    	return bean;
    }
    
    public ChallengeQuestionIdsDTO getUserChallengeQuestionIds(String username, String confirmation) throws RemoteException {
    	ChallengeQuestionIdsDTO bean = null;
    	try {
    		bean = infoRecoveryStub.getUserChallengeQuestionIds(username, confirmation);
		} catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
			e.printStackTrace();
		}
    	return bean;
    }
    
    public UserChallengesDTO getUserChallengeQuestion(String userName, String confirmation,
			String questionId) throws RemoteException {
    	UserChallengesDTO bean = null;
    	try {
    		bean = infoRecoveryStub.getUserChallengeQuestion(userName, confirmation, questionId);
		} catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
			e.printStackTrace();
		}
    	return bean;
    }
    
    public VerificationBean verifyUserChallengeAnswer(String userName, String confirmation,
			String questionId, String answer) throws RemoteException {
    	VerificationBean bean = null;
    	try {
			bean = infoRecoveryStub.verifyUserChallengeAnswer(userName, confirmation, questionId, answer);
		} catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
			e.printStackTrace();
		}
    	return bean;
    }
    
    public ChallengeQuestionDTO[] getAllChallengeQuestions() throws RemoteException {
    	ChallengeQuestionDTO[] questions = null;
    	try {
			questions = infoRecoveryStub.getAllChallengeQuestions();
		} catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
			e.printStackTrace();
		}
    	return questions;
    }
    
    public UserIdentityClaimDTO[] getUserIdentitySupportedClaims(String dialect) throws RemoteException {
    	UserIdentityClaimDTO[] claims = null;
    	try {
			claims = infoRecoveryStub.getUserIdentitySupportedClaims(dialect);
		} catch (UserInformationRecoveryServiceIdentityExceptionException e) {
			e.printStackTrace();
		}
    	return claims;
    }
    
    public VerificationBean verifyAccount(UserIdentityClaimDTO[] claims, CaptchaInfoBean captcha,
			String tenantDomain) throws RemoteException {
    	VerificationBean bean = null;
    	try {
			bean = infoRecoveryStub.verifyAccount(claims, captcha, tenantDomain);
		} catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
			e.printStackTrace();
		}
    	return bean;
    }
    
    public VerificationBean registerUser(String userName, String password,
			UserIdentityClaimDTO[] claims, String profileName, String tenantDomain) throws RemoteException {
    	VerificationBean bean = null;
    	try {
			bean = infoRecoveryStub.registerUser(userName, password, claims, profileName, tenantDomain);
		} catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
			e.printStackTrace();
		}
    	return bean;
    }
    
    public VerificationBean confirmUserSelfRegistration(String username, String code,
			CaptchaInfoBean captcha, String tenantDomain) throws RemoteException {
    	VerificationBean bean = null;
    	try {
			bean = infoRecoveryStub.confirmUserSelfRegistration(username, code, captcha, tenantDomain);
		} catch (UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
			e.printStackTrace();
		}
    	return bean;
    }
}
	