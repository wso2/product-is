package org.wso2.identity.integration.test.user.selfregistration;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import java.util.Properties;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class ProvisionUserSCIM2 {

    protected Log log = LogFactory.getLog(getClass());
    public static final String SCIM2_USERS_ENDPOINT = "/scim2/Users";
    public static final String SCHEMAS_ATTRIBUTE = "schemas";
    public static final String GIVEN_NAME_ATTRIBUTE = "givenName";
    public static final String NAME_ATTRIBUTE = "userName";
    public static final String USER_NAME_ATTRIBUTE = "userName";
    public static final String PASSWORD_ATTRIBUTE = "password";
    public static final String ID_ATTRIBUTE = "id";

    private static final String GIVEN_NAME_CLAIM_VALUE = "user";
    private static final String USERNAME = "scim2user";
    private static final String PASSWORD = "scim2pwd";
    private String userId;

    String resourceLocation = System.getProperty("framework.resource.location");

    private CloseableHttpClient client;

    @BeforeClass(alwaysRun = true)
    public void testInit() throws Exception {
        setKeyStoreProperties();
        client = HttpClients.createDefault();
    }


    @Test(description = "1.1.1.1")
    public void TestCreateUser() throws Exception {
        String scimEndpoint = getIdentityHTTPSEP() + SCIM2_USERS_ENDPOINT;
        HttpPost request = new HttpPost(scimEndpoint);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        JSONObject rootObject = new JSONObject();

        JSONArray schemas = new JSONArray();
        rootObject.put(SCHEMAS_ATTRIBUTE, schemas);

        JSONObject names = new JSONObject();
        names.put(GIVEN_NAME_ATTRIBUTE, GIVEN_NAME_CLAIM_VALUE);

        rootObject.put(NAME_ATTRIBUTE, names);
        rootObject.put(USER_NAME_ATTRIBUTE, USERNAME);

        rootObject.put(PASSWORD_ATTRIBUTE, PASSWORD);

        StringEntity entity = new StringEntity(rootObject.toString());
        request.setEntity(entity);

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 201, "User " +
                "has not been created successfully");

        Object responseObj = JSONValue.parse(EntityUtils.toString(response.getEntity()));
        EntityUtils.consume(response.getEntity());

        String usernameFromResponse = ((JSONObject) responseObj).get(USER_NAME_ATTRIBUTE).toString();
        assertEquals(usernameFromResponse, USERNAME);

        userId = ((JSONObject) responseObj).get(ID_ATTRIBUTE).toString();
        assertNotNull(userId);

        testDeleteUser();
    }

     private void testDeleteUser() throws Exception {
        String userResourcePath = getIdentityHTTPSEP() + SCIM2_USERS_ENDPOINT + "/" + userId;
        HttpDelete request = new HttpDelete(userResourcePath);
        request.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 204, "User " +
                "has not been retrieved successfully");

        EntityUtils.consume(response.getEntity());

        userResourcePath = getIdentityHTTPSEP() + SCIM2_USERS_ENDPOINT + "/" + userId;
        HttpGet getRequest = new HttpGet(userResourcePath);
        getRequest.addHeader(HttpHeaders.AUTHORIZATION, getAuthzHeader());
        getRequest.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 404, "User " +
                "has not been deleted successfully");
        EntityUtils.consume(response.getEntity());
    }

    private String adminUsername = "admin";
    private String adminPassword = "admin";
    private String getAuthzHeader() {
        return "Basic " + Base64.encodeBase64String((adminUsername + ":" + adminPassword).getBytes()).trim();
    }

    private void setKeyStoreProperties() {
        System.setProperty("javax.net.ssl.trustStore", resourceLocation + "keystores/products/wso2carbon.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");

    }

    private String getIdentityHTTPSEP() {
        String bucketLocation = System.getenv("DATA_BUCKET_LOCATION");
        String url = null;
        log.info("Data Bucket location is set : " + bucketLocation);

        Properties prop = new Properties();
        //InputStream input = null;
        try (InputStream input = new FileInputStream(bucketLocation + "/infrastructure.properties")) {
            prop.load(input);
            url = prop.getProperty("ISHttpsUrl");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (url == null){
            url = "https://localhost:9443";
        }

        return url;
    }

}
