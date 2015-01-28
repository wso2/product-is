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

package org.wso2.identity.integration.test.identity.mgt;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.claim.mgt.stub.dto.ClaimDTO;
import org.wso2.carbon.claim.mgt.stub.dto.ClaimMappingDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.clients.UserProfileMgtServiceClient;
import org.wso2.identity.integration.common.clients.mgt.UserInformationRecoveryServiceClient;
import org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean;
import org.wso2.carbon.identity.mgt.stub.beans.VerificationBean;
import org.wso2.carbon.identity.mgt.stub.dto.ChallengeQuestionDTO;
import org.wso2.carbon.identity.mgt.stub.dto.ChallengeQuestionIdsDTO;
import org.wso2.carbon.identity.mgt.stub.dto.UserChallengesDTO;
import org.wso2.carbon.identity.mgt.stub.dto.UserIdentityClaimDTO;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.carbon.identity.user.profile.stub.types.UserFieldDTO;
import org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.identity.integration.common.clients.ClaimManagementServiceClient;

/*
 * TODO - Need to update all the methods with confirmation return check.
 */
public class UserInformationRecoveryServiceTestCase extends ISIntegrationTest{

    private static final Log log = LogFactory.getLog(UserInformationRecoveryServiceTestCase.class);
    private UserInformationRecoveryServiceClient infoRecoveryClient;
	private UserManagementClient userMgtClient;
	private UserProfileMgtServiceClient profileClient;
	private ClaimManagementServiceClient claimMgtClient;
	private AuthenticatorClient loginManger;
	private ServerConfigurationManager scm;
	private File identityMgtServerFile;
    private File axisServerFile;
	private String confKey;
	
	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL})
	@BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
		super.init();
        String carbonHome = CarbonUtils.getCarbonHome();
		identityMgtServerFile = new File(carbonHome + File.separator
				+ "repository" + File.separator + "conf" + File.separator
				+ "security" + File.separator + "identity-mgt.properties");
		File identityMgtConfigFile = new File(getISResourceLocation()
				+ File.separator + "identityMgt" + File.separator
				+ "identity-mgt-enabled.properties");
        	
		axisServerFile = new File(carbonHome + File.separator
				+ "repository" + File.separator + "conf" + File.separator
				+ "axis2" + File.separator + "axis2.xml");
		File axisConfigFile = new File(getISResourceLocation()
				+ File.separator + "identityMgt" + File.separator
				+ "axis2.xml");
        scm = new ServerConfigurationManager(isServer);
        scm.applyConfigurationWithoutRestart(identityMgtConfigFile, identityMgtServerFile, true);
        scm.applyConfigurationWithoutRestart(axisConfigFile, axisServerFile, true);
        scm.restartGracefully();

        super.init();
        
		loginManger = new AuthenticatorClient(backendURL);
		userMgtClient = new UserManagementClient(backendURL, sessionCookie);
		infoRecoveryClient = new UserInformationRecoveryServiceClient(backendURL, sessionCookie);
		profileClient = new UserProfileMgtServiceClient(backendURL, sessionCookie);
		
        loginManger.login(isServer.getSuperTenant().getTenantAdmin().getUserName(),
				isServer.getSuperTenant().getTenantAdmin().getPassword(),
				isServer.getInstance().getHosts().get("default"));
        
        claimMgtClient = new ClaimManagementServiceClient(backendURL, sessionCookie);
		ClaimDTO claim1 = new ClaimDTO();
		claim1.setDialectURI("http://wso2.org/claims");
		claim1.setClaimUri("http://wso2.org/claims/identity/passwordTimestamp");
		claim1.setDescription("Password timestamp");

		ClaimDTO claim2 = new ClaimDTO();
		claim2.setDialectURI("http://wso2.org/claims");
		claim2.setClaimUri("http://wso2.org/claims/identity/unlockTime");
		claim2.setDescription("Account Unlock time");
		
		ClaimDTO claim3 = new ClaimDTO();
		claim3.setDialectURI("http://wso2.org/claims");
		claim3.setClaimUri("http://wso2.org/claims/identity/failedLoginAttempts");
		claim3.setDescription("Failed login attempts");
		
		ClaimMappingDTO claimMapping1 = new ClaimMappingDTO();
		claimMapping1.setClaim(claim1);
		claimMapping1.setMappedAttribute("facsimileTelephoneNumber");
		claimMgtClient.addNewClaimMapping(claimMapping1);
		
		ClaimMappingDTO claimMapping2 = new ClaimMappingDTO();
		claimMapping2.setClaim(claim2);
		claimMapping2.setMappedAttribute("description");
		claimMgtClient.addNewClaimMapping(claimMapping2);
		
		ClaimMappingDTO claimMapping3 = new ClaimMappingDTO();
		claimMapping3.setClaim(claim3);
		claimMapping3.setMappedAttribute("employeeType");
		claimMgtClient.addNewClaimMapping(claimMapping3);
		
        userMgtClient.addUser("user11", "passWord1@", null, "default");        
        userMgtClient.addRole("umRole11", new String[]{"user11"}, new String[]{"/permission/admin/login"}, false);
     }
    
	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL })
    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
    	
    	loginManger.logOut();
    	if(nameExists(userMgtClient.listAllUsers("user11", 100), "user11")) {
    		userMgtClient.deleteUser("user11");
    	}       
       
        if(nameExists(userMgtClient.listRoles("umRole11", 100), "umRole11")){
        	userMgtClient.deleteRole("umRole11");
        }
    	if(nameExists(userMgtClient.listAllUsers("user2", 100), "user2")) {
    		userMgtClient.deleteUser("user2");
    	}   
		File identityMgtDefaultFile = new File(getISResourceLocation()
				+ File.separator + "identityMgt" + File.separator
				+ "identity-mgt-default.properties");
        File axisConfigDefaultFile = new File(getISResourceLocation()
                + File.separator + "identityMgt" + File.separator
                + "axis2-default.xml");
        scm.applyConfigurationWithoutRestart(identityMgtDefaultFile, identityMgtServerFile, true);
        scm.applyConfigurationWithoutRestart(axisConfigDefaultFile, axisServerFile, true);
		scm.restartGracefully();

    }
    
	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check identity mgt with listing users")
	public void testListUsers() throws Exception {    	
    	Assert.assertTrue(nameExists(userMgtClient.listAllUsers("user11", 100), "user11"), "Listing user with " +
				"identity mgt enabled has failed.");
	}
    
	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check get captcha", dependsOnMethods = "testListUsers")
	public void testGetCaptcha() throws Exception { 
    	CaptchaInfoBean bean = infoRecoveryClient.getCaptcha();
    	Assert.assertNotNull(bean, "Getting the captcha call failed with null return");
    	Assert.assertNotNull(bean.getImagePath(), "Getting image path from captcha has failed.");
    	Assert.assertNotNull(bean.getSecretKey(), "Getting secret key from captcha has failed.");
	}
    /*
     * To validate password reset without captcha validation is to follow the method calls as
     * verifyUser() -> sendRecoveryNotification() -> verifyConfirmationCode() -> updatePassword()
     * Since cannot answer the question the test need to carryout with Captcha.Verification.Internally.Managed=false
     */
	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL })
    @Test(groups = "wso2.is", description = "Check verify user", dependsOnMethods = "testGetCaptcha")
	public void testVerifyUser() throws Exception { 
    	VerificationBean bean = infoRecoveryClient.verifyUser("user11", null);
    	Assert.assertNotNull(bean, "Verify User has failed with null return");
//    	Assert.assertTrue(bean.getVerified(), "Verify User has failed for user11");
//    	Assert.assertNotNull(bean.getKey(), "Verify User has failed with null key return");
    	confKey = bean.getKey();
	}
    
	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL})
    @Test(groups = "wso2.is", description = "Check recovery notification sending", dependsOnMethods = "testVerifyUser")
	public void testSendRecoveryNotification() throws Exception { 
    	UserProfileDTO profile = profileClient.getUserProfile("user11", "default");
    	UserFieldDTO email = new UserFieldDTO();
    	email.setFieldValue("testuser@wso2.com");
    	email.setClaimUri("http://wso2.org/claims/emailaddress");
    	UserFieldDTO[] params = new UserFieldDTO[1];
    	params[0] = email;
		profile.setFieldValues(params);
		profileClient.setUserProfile("user11", profile);
    	
    	VerificationBean bean = infoRecoveryClient.sendRecoveryNotification("user11", confKey, "EMAIL");

    	Assert.assertNotNull(bean, "Notification sending has failed with null return");
//    	Assert.assertTrue(bean.getVerified(), "Notification sending has failed for user11");
//    	confKey = bean.getKey();
    	
	}
    
	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL })
    @Test(groups = "wso2.is", description = "Check verify confirmation code", dependsOnMethods = "testSendRecoveryNotification")
	public void testVerifyConfirmationCode() throws Exception { 
    	VerificationBean bean = infoRecoveryClient.verifyConfirmationCode("user11", confKey, null);
    	Assert.assertNotNull(bean, "Verify confirmation code has failed with null return");
//    	Assert.assertNotNull(bean.getKey(), "Verify User has failed with null key return");
//    	confKey = bean.getKey();
//    	NotificationDataDTO dataDto = bean.getNotificationData();
	}
    
	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL })
    @Test(groups = "wso2.is", description = "Check update password", dependsOnMethods = "testVerifyConfirmationCode")
	public void testUpdatePassword() throws Exception { 
    	VerificationBean bean = infoRecoveryClient.updatePassword("user11", confKey, "passWord2@");
    	Assert.assertNotNull(bean, "Update password has failed with null return");
//    	Assert.assertNotNull(bean.getKey(), "Update password has failed with null key return");
//    	String value = loginManger.login("user11", "passWord2@", isServer.getBackEndUrl());
	}
    
	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL })
    @Test(groups = "wso2.is", description = "Check getting all challenge questions", dependsOnMethods = "testSendRecoveryNotification")
	public void testGetAllChallengeQuestions() throws Exception { 
    	ChallengeQuestionDTO[] bean = infoRecoveryClient.getAllChallengeQuestions();
    	Assert.assertNotNull(bean, "Getting supported claims has failed with null return");
	}
    
	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL })
    @Test(groups = "wso2.is", description = "Check getting challenge question ids", dependsOnMethods = "testGetAllChallengeQuestions")
	public void testGetUserChallengeQuestionIds() throws Exception { 
    	ChallengeQuestionIdsDTO bean = infoRecoveryClient.getUserChallengeQuestionIds("user11", confKey);
    	Assert.assertNotNull(bean, "Getting challenge question ids has failed with null return");
	}
    
	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL })
    @Test(groups = "wso2.is", description = "Check get user challenge question", dependsOnMethods = "testGetUserChallengeQuestionIds")
	public void testGetUserChallengeQuestion() throws Exception { 
    	UserChallengesDTO bean = infoRecoveryClient.getUserChallengeQuestion("user11", confKey, "");
    	Assert.assertNotNull(bean, "Getting challenge question has failed with null return");
	}
    
	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL })
    @Test(groups = "wso2.is", description = "Check verify user challenge question", dependsOnMethods = "testGetUserChallengeQuestion")
	public void testVerifyUserChallengeAnswer() throws Exception { 
    	VerificationBean bean = infoRecoveryClient.verifyUserChallengeAnswer("user11", confKey, "", "");
    	Assert.assertNotNull(bean, "Check verify user answer has failed with null return");
	}
    
//	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.integration_all })
//    @Test(groups = "wso2.is", description = "Check getting supported claims")
//	public void testGetUserIdentitySupportedClaims() throws Exception { 
//		loginManger.login("admin", "admin", isServer.getBackEndUrl());
//    	UserIdentityClaimDTO[] bean = infoRecoveryClient.getUserIdentitySupportedClaims("http://wso2.org/claims");
//    	Assert.assertNotNull(bean, "Getting supported claims has failed with null return");
//	}
    
	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL })
    @Test(groups = "wso2.is", description = "Check user account verification", dependsOnMethods = "testVerifyUserChallengeAnswer")
	public void testVerifyUserAccount() throws Exception { 
    	UserIdentityClaimDTO[] claims =  new UserIdentityClaimDTO[2];
    	UserIdentityClaimDTO claimEmail = new UserIdentityClaimDTO();
    	claimEmail.setClaimUri("http://wso2.org/claims/emailaddress");
    	claimEmail.setClaimValue("user11@wso2.com");
    	
    	UserIdentityClaimDTO claimLastName = new UserIdentityClaimDTO();
    	claimLastName.setClaimUri("http://wso2.org/claims/givenname");
    	claimLastName.setClaimValue("user11");
    	
    	claims[0] = claimEmail;
    	claims[1]= claimLastName;
    	
		VerificationBean bean = infoRecoveryClient.verifyAccount(claims , null, null);
    	Assert.assertNotNull(bean, "Verifying user account has failed with null return");
	}
    
	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL })
    @Test(groups = "wso2.is", description = "Check user registration", dependsOnMethods = "testVerifyUserAccount")
	public void testRegisterUser() throws Exception { 
    	UserIdentityClaimDTO[] claims =  new UserIdentityClaimDTO[2];
    	UserIdentityClaimDTO claimEmail = new UserIdentityClaimDTO();
    	claimEmail.setClaimUri("http://wso2.org/claims/emailaddress");
    	claimEmail.setClaimValue("user2@wso2.com");
    	
    	UserIdentityClaimDTO claimLastName = new UserIdentityClaimDTO();
    	claimLastName.setClaimUri("http://wso2.org/claims/givenname");
    	claimLastName.setClaimValue("user2");
    	
    	claims[0] = claimEmail;
    	claims[1]= claimLastName;
    	
		VerificationBean bean = infoRecoveryClient.registerUser("user2", "passWord1@", claims, "default", null);
    	Assert.assertNotNull(bean, "Registering user account has failed with null return");
    	confKey = bean.getKey();
	}
    
	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL })
    @Test(groups = "wso2.is", description = "Check user registration confirmation", dependsOnMethods = "testRegisterUser")
	public void testConfirmUserSelfRegistration() throws Exception { 
    	VerificationBean bean = infoRecoveryClient.confirmUserSelfRegistration("user2", confKey, null, null);
    	Assert.assertNotNull(bean, "Confirmation of user registration has failed with null return");
	}
    
    /**
     * Checks whether the passed Name exists in the FlaggedName array.
     * 
     * @param allNames
     * @param inputName
     * @return
     */
	protected boolean nameExists(FlaggedName[] allNames, String inputName) {
		boolean exists = false;

		for (FlaggedName flaggedName : allNames) {
			String name = flaggedName.getItemName();

			if (name.equals(inputName)) {
				exists = true;
				break;
			} else {
				exists = false;
			}
		}

		return exists;
	}
}
