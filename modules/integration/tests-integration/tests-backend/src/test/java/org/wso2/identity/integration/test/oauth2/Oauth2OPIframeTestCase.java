package org.wso2.identity.integration.test.oauth2;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.identity.integration.test.utils.OAuth2Constant;


import static org.wso2.identity.integration.test.utils.OAuth2Constant.USER_AGENT;


public class Oauth2OPIframeTestCase extends OAuth2ServiceAbstractIntegrationTest {


    public static final String CALL_BACK_URL =
            "https://localhost:9853/oidc/checksession?client_id=%s&redirect_uri=http" +
                    "://localhost:8888/playground2/oauth2client";
    private AuthenticatorClient logManger;
    private DefaultHttpClient client;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
        logManger = new AuthenticatorClient(backendURL);
        logManger.login(isServer.getSuperTenant().getTenantAdmin().getUserName(),
                isServer.getSuperTenant().getTenantAdmin().getPassword(),
                isServer.getInstance().getHosts().get("default"));

        setSystemproperties();
        client = new DefaultHttpClient();


    }

    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {

        deleteApplication();
        removeOAuthApplicationData();

        logManger = null;
        consumerKey = null;
    }

    @Test(groups = "wso2.is", description = "Check Oauth2 application registration")
    public void testOPIFrameRegex() throws Exception {

        OAuthConsumerAppDTO appConfigData = new OAuthConsumerAppDTO();
        appConfigData
                .setApplicationName(org.wso2.identity.integration.test.utils.OAuth2Constant.OAUTH_APPLICATION_NAME);
        appConfigData.setCallbackUrl(OAuth2Constant.CALLBACK_URL_REGEXP);
        appConfigData.setOAuthVersion(OAuth2Constant.OAUTH_VERSION_2);
        appConfigData.setGrantTypes("authorization_code implicit password client_credentials refresh_token "
                + "urn:ietf:params:oauth:grant-type:saml2-bearer iwa:ntlm");

        OAuthConsumerAppDTO appDto = createApplication(appConfigData);
        Assert.assertNotNull(appDto, "Application creation failed.");

        consumerKey = appDto.getOauthConsumerKey();
        Assert.assertNotNull(consumerKey, "Application creation failed.");
        consumerSecret = appDto.getOauthConsumerSecret();

        HttpResponse response;

        String url = String.format(CALL_BACK_URL, consumerKey);
        response = sendGetRequest(url);
        EntityUtils.consume(response.getEntity());

    }

    private HttpResponse sendGetRequest(String url) throws Exception {

        HttpGet request = new HttpGet(url);
        request.addHeader("User-Agent", USER_AGENT);
        return client.execute(request);
    }


}
