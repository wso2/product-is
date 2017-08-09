package org.wso2.identity.integration.test.oauth.dcrm;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.identity.integration.test.oauth.dcrm.bean.ServiceProvider;
import org.wso2.identity.integration.test.oauth.dcrm.util.OAuthDCRMConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class OAuthDCRMDeleteTestCase {

    private HttpClient client;
    private ServiceProviderRegister serviceProviderRegister;
    private ServiceProvider serviceProvider;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {

        client = new DefaultHttpClient();
        serviceProviderRegister = new ServiceProviderRegister();

        JSONObject object = new JSONObject();
        object.put(OAuthDCRMConstants.CLIENT_NAME, "DeleteApp1");
        object.put(OAuthDCRMConstants.GRANT_TYPES, "implicit");
        object.put(OAuthDCRMConstants.REDIRECT_URIS, "http://DeleteApp1.com");

        serviceProvider = serviceProviderRegister.register(object.toJSONString());
    }

    @Test(alwaysRun = true, description = "Delete Service Provider")
    public void testDeleteServiceProvider() throws IOException {

        HttpDelete request = new HttpDelete(OAuthDCRMConstants.CLIENT_CONFIGURATION_ENDPOINT
                + serviceProvider.getClientID());
        request.addHeader(HttpHeaders.AUTHORIZATION, OAuthDCRMConstants.AUTHORIZATION);

        HttpResponse response = client.execute(request);
        Assert.assertNotNull(response, "Service Provider Delete request failed");
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 204);
    }

    @Test(alwaysRun = true, description = "Delete service provider request with invalid client id")
    public void testDeleteRequestWithInvalidClientID() throws IOException {

        HttpDelete request = new HttpDelete(OAuthDCRMConstants.CLIENT_CONFIGURATION_ENDPOINT
                + OAuthDCRMConstants.INVALID_CLIENT_ID);
        request.addHeader(HttpHeaders.AUTHORIZATION, OAuthDCRMConstants.AUTHORIZATION);

        HttpResponse response = client.execute(request);
        Assert.assertNotNull(response, "Service Provider Delete request failed");

        Assert.assertEquals(response.getStatusLine().getStatusCode(), 405);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        Object object = JSONValue.parse(rd);
        Assert.assertNotNull(object, "Returned response should have produced a valid JSON.");

        JSONObject jsonObject = (JSONObject) object;
        String error = (String) jsonObject.get(OAuthDCRMConstants.ERROR);
        String errorDescription = (String) jsonObject.get(OAuthDCRMConstants.ERROR_DESCRIPTION);
        Assert.assertEquals(error, OAuthDCRMConstants.BACKEND_FAILED);
        Assert.assertEquals(errorDescription, "Error occurred while reading the existing service provider.");
    }
}
