package org.wso2.identity.integration.test.oauth2;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceIdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.identity.integration.common.clients.UserManagementClient;
import org.wso2.identity.integration.common.clients.oauth.OauthAdminClient;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.rmi.RemoteException;

public class OAuthAdminServiceTestCase extends ISIntegrationTest {

    private UserManagementClient userMgtClient;
    private AuthenticatorClient logManger;
    String userName = "MyUser1";
    String password = "passWord1@";
    String applicationName = "oauthApp1";
    String consumerKey;
    OauthAdminClient adminClient;
    String updatedApplicationName = "oauthApp2";

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init();

        logManger = new AuthenticatorClient(backendURL);
        userMgtClient = new UserManagementClient(backendURL, sessionCookie);

        // create a user with capital letters in user name
        userMgtClient.addUser(userName, password, null, "default");

        String[] userList = new String[]{userName};
        FlaggedName[] userFlagList = new FlaggedName[userList.length];

        for (int i = 0; i < userFlagList.length; i++) {
            FlaggedName flaggedName = new FlaggedName();
            flaggedName.setItemName(userList[i]);
            flaggedName.setSelected(true);
            userFlagList[i] = flaggedName;
        }

        userMgtClient.updateUsersOfRole("admin", userFlagList);
        String cookie = this.logManger.login(userName, password, isServer.getInstance().getHosts().get("default"));
        adminClient = new OauthAdminClient(backendURL, cookie);
    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        removeOAuthApplicationData();
        this.logManger.logOut();
        userMgtClient.deleteUser(userName);
        logManger = null;

    }

    @Test(groups = "wso2.is", description = "Check Oauth2 application flow")
    public void testRegisterApplication() throws Exception {

        createOauthApp();
        OAuthConsumerAppDTO[] appDtos = adminClient.getAllOAuthApplicationData();
        boolean found = false;

        // check the created application is in the retrieved application list
        if (appDtos != null) {
            for (OAuthConsumerAppDTO appDTO : appDtos) {
                if (applicationName.equals(appDTO.getApplicationName())) {
                    found = true;
                    consumerKey = appDTO.getOauthConsumerKey();
                    break;
                }
            }
        }
        Assert.assertTrue(found);

    }

    @Test(groups = "wso2.is", description = "Testing retrieving Oauth2 application state",
            dependsOnMethods = "testRegisterApplication")
    public void testGetOauthAppState() throws Exception {

        try {
            getOauthApplicationState();
        } catch (Exception e) {
            Assert.assertFalse(false, "Error when retrieving oauth app state");
        }
    }

    @Test(groups = "wso2.is", description = "Testing updating Oauth2 application state",
            dependsOnMethods = {"testRegisterApplication", "testGetOauthAppState"})
    public void testUpdateOauthAppState() throws Exception {

        boolean updated = false;

        try {
            updateConsumerAppState(OAuth2Constant.OAUTH_APPLICATION_STATE_REVOKED);
            if (OAuth2Constant.OAUTH_APPLICATION_STATE_REVOKED.equalsIgnoreCase(getOauthApplicationState())) {
                updated = true;
            }
        } catch (Exception e) {
            Assert.assertFalse(false, "Error when retrieving oauth app state");
        }
        Assert.assertTrue(updated);
    }

    @Test(groups = "wso2.is", description = "Testing updating Oauth2 application state",
            dependsOnMethods = {"testRegisterApplication", "testUpdateOauthAppState"})
    public void testUpdateOauthSecretKey() throws Exception {

        boolean updated = false;

        try {
            updateOauthSecretKey();
            updated = true;
        } catch (Exception e) {
            Assert.assertFalse(false, "Error when retrieving oauth app state");
        }
        Assert.assertTrue(updated);
    }

    @Test(groups = "wso2.is",
          description = "Testing updating Oauth2 application",
          dependsOnMethods = { "testRegisterApplication", "testUpdateOauthAppState", "testUpdateOauthSecretKey" })
    public void testUpdateOauthApplication() throws Exception {
        updateApplication(getApplication(applicationName));
        Assert.assertEquals(getApplication(updatedApplicationName).getApplicationName(), updatedApplicationName,
                "Application update has failed" + ".Received incorrect application name");
    }

    private void createOauthApp() throws RemoteException, OAuthAdminServiceIdentityOAuthAdminException {
        OAuthConsumerAppDTO appDTO = new OAuthConsumerAppDTO();
        appDTO.setCallbackUrl(OAuth2Constant.CALLBACK_URL);
        appDTO.setGrantTypes("authorization_code implicit password client_credentials refresh_token " +
                "urn:ietf:params:oauth:grant-type:saml2-bearer iwa:ntlm");
        appDTO.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
        appDTO.setApplicationName(applicationName);
        adminClient.registerOAuthApplicationData(appDTO);
    }

    private void removeOAuthApplicationData() throws Exception {
        adminClient.removeOAuthApplicationData(consumerKey);
    }

    private String getOauthApplicationState() throws Exception {
        return adminClient.getOauthApplicationState(applicationName);
    }

    private void updateConsumerAppState(String newState) throws Exception {
        adminClient.updateConsumerAppState(applicationName, newState);
    }

    private void updateOauthSecretKey() throws Exception {
        adminClient.updateOauthSecretKey(applicationName);
    }

    private void updateApplication(OAuthConsumerAppDTO oAuthConsumerAppDTO) throws Exception {
        oAuthConsumerAppDTO.setApplicationName(updatedApplicationName);
        adminClient.updateConsumerApplication(oAuthConsumerAppDTO);
    }

    private OAuthConsumerAppDTO getApplication(String applicationName)
            throws OAuthAdminServiceIdentityOAuthAdminException, RemoteException {
        OAuthConsumerAppDTO[] appDTOs = adminClient.getAllOAuthApplicationData();

        for (OAuthConsumerAppDTO appDTO : appDTOs) {
            if (appDTO.getApplicationName().equals(applicationName)) {
                return appDTO;
            }
        }
        return null;
    }
}
