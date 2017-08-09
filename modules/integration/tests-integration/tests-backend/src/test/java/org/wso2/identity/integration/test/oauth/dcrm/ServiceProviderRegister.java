package org.wso2.identity.integration.test.oauth.dcrm;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.Assert;
import org.wso2.identity.integration.test.oauth.dcrm.bean.ServiceProvider;
import org.wso2.identity.integration.test.oauth.dcrm.util.OAuthDCRMConstants;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class ServiceProviderRegister {
    public ServiceProvider register (String body) throws Exception {

        ServiceProvider serviceProvider = new ServiceProvider();
        HttpClient client = new DefaultHttpClient();
        HttpPost request = new HttpPost(OAuthDCRMConstants.CLIENT_CONFIGURATION_ENDPOINT);

        request.addHeader(HttpHeaders.AUTHORIZATION, OAuthDCRMConstants.AUTHORIZATION);
        request.addHeader(HttpHeaders.CONTENT_TYPE, OAuthDCRMConstants.CONTENT_TYPE);

        StringEntity entity;
        try {
            entity = new StringEntity(body);
        } catch (UnsupportedEncodingException e) {
            throw new Exception("Generation of entity from the given request body is failed.", e);
        }
        request.setEntity(entity);

        HttpResponse response = client.execute(request);
        Assert.assertNotNull(response, "Service Provider registration request failed");

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        Object obj = JSONValue.parse(rd);
        Assert.assertNotNull(obj, "Returned response should have produced a valid JSON.");

        JSONObject jsonObject = (JSONObject) obj;

        serviceProvider.setClientName((String) jsonObject.get(OAuthDCRMConstants.CLIENT_NAME));
        serviceProvider.setClientID((String) jsonObject.get(OAuthDCRMConstants.CLIENT_ID));
        serviceProvider.setClientSecret((String) jsonObject.get(OAuthDCRMConstants.CLIENT_SECRET));

        JSONArray grantTypes = (JSONArray) jsonObject.get(OAuthDCRMConstants.GRANT_TYPES);
        for (Object grantType:grantTypes) {
            serviceProvider.addGrantType((String) grantType);
        }

        JSONArray redirectURIs = (JSONArray) jsonObject.get(OAuthDCRMConstants.REDIRECT_URIS);
        for (Object redirectURI:redirectURIs) {
            serviceProvider.addRedirectUri((String) redirectURI);
        }

        return serviceProvider;
    }
}
