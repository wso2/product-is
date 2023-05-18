package org.wso2.identity.integration.test.oauth2;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.UserCreationModel;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class SCIM2RestClient {

    public static final String SCIM2_USERS_ENDPOINT = "/scim2/Users";
    public static final String SCIM2_ROLES_ENDPOINT = "/scim2/Roles";
    public static final String DISPLAY_NAME_ATTRIBUTE = "displayName";

    public static final String USER_NAME_ATTRIBUTE = "userName";
    public static final String FAMILY_NAME_ATTRIBUTE = "familyName";
    public static final String GIVEN_NAME_ATTRIBUTE = "givenName";
    public static final String EMAIL_TYPE_WORK_ATTRIBUTE = "work";
    public static final String EMAIL_TYPE_HOME_ATTRIBUTE = "home";
    public static final String SCHEMAS_ATTRIBUTE = "schemas";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String TYPE_PARAM = "type";
    public static final String VALUE_PARAM = "value";
    public static final String USERS_PARAM = "users";
    public static final String PASSWORD_ATTRIBUTE = "password";
    public static final String EMAILS_ATTRIBUTE = "emails";
    public static final String USER_PASSWORD = "testPassword";
    public static final String ADD_OP = "add";
    public static final String OPERATION_PARAM = "op";
    public static final String OPERATIONS_PARAM = "Operations";
    private final String SERVER_URL;
    private final CloseableHttpClient client;
    private final String tenantDomain;
    private final String USERNAME;
    private final String PASSWORD;


    public SCIM2RestClient(String serverURL, Tenant tenantInfo){
        client = HttpClients.createDefault();
        this.SERVER_URL = serverURL;
        this.tenantDomain = tenantInfo.getContextUser().getUserDomain();
        this.USERNAME = tenantInfo.getContextUser().getUserName();
        this.PASSWORD = tenantInfo.getContextUser().getPassword();
    }

    public String createUser(UserCreationModel userInfo) throws IOException, JSONException {

        HttpPost request = new HttpPost(getUsersPath());

        request.setHeader(HttpHeaders.USER_AGENT, OAuth2Constant.USER_AGENT);
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + Base64.encodeBase64String((USERNAME + ":" + PASSWORD).getBytes()).trim());
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        HashMap<String, Object> rootMap = new HashMap<>();

        JSONArray schemas = new JSONArray();
        for (String item : userInfo.getSchemas()) {
            schemas.put(item);
        }
        rootMap.put(SCHEMAS_ATTRIBUTE, schemas);

        HashMap<String, Object> namesMap = new HashMap<>();
        namesMap.put(FAMILY_NAME_ATTRIBUTE, userInfo.getFamilyName());
        namesMap.put(GIVEN_NAME_ATTRIBUTE, userInfo.getGivenName());

        rootMap.put(NAME_ATTRIBUTE, new JSONObject(namesMap));
        rootMap.put(USER_NAME_ATTRIBUTE, userInfo.getUserName());

        HashMap<String, Object> emailWorkMap = new HashMap<>();
        emailWorkMap.put(TYPE_PARAM, EMAIL_TYPE_WORK_ATTRIBUTE);
        emailWorkMap.put(VALUE_PARAM, userInfo.getWorkEmail());

        HashMap<String, Object> emailHomeMap = new HashMap<>();
        emailHomeMap.put(TYPE_PARAM, EMAIL_TYPE_HOME_ATTRIBUTE);
        emailHomeMap.put(VALUE_PARAM, userInfo.getHomeEmail());

        JSONArray emails = new JSONArray();
        emails.put(new JSONObject(emailWorkMap));
        emails.put(new JSONObject(emailHomeMap));

        rootMap.put(EMAILS_ATTRIBUTE, emails);
        rootMap.put(PASSWORD_ATTRIBUTE, userInfo.getPassword());

        JSONObject rootObject = new JSONObject(rootMap);

        StringEntity entity = new StringEntity(rootObject.toString());
        request.setEntity(entity);

        HttpResponse response = client.execute(request);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        Object obj = JSONValue.parse(rd);
        EntityUtils.consume(response.getEntity());

        return ((JSONObject) obj).get("id").toString();

    }

    public String addRole(String roleName) throws IOException, JSONException {

        HttpPost request = new HttpPost(getRolesPath());

        request.setHeader(HttpHeaders.USER_AGENT, OAuth2Constant.USER_AGENT);
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + Base64.encodeBase64String((USERNAME + ":" + PASSWORD).getBytes()).trim());
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/scim+json");

        HashMap<String, Object> rootMap = new HashMap<>();
        rootMap.put(DISPLAY_NAME_ATTRIBUTE, roleName);

        JSONObject jsonObj = new JSONObject(rootMap);
        StringEntity entity = new StringEntity(jsonObj.toString());
        request.setEntity(entity);

        HttpResponse response = client.execute(request);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        Object obj = JSONValue.parse(rd);
        EntityUtils.consume(response.getEntity());

        return ((JSONObject) obj).get("id").toString();
    }

    public void updateUserRole(String roleId, String userId) throws IOException {

        HttpPatch request = new HttpPatch(getRolesPath() + "/" + roleId);

        request.setHeader(HttpHeaders.USER_AGENT, OAuth2Constant.USER_AGENT);
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + Base64.encodeBase64String((USERNAME + ":" + PASSWORD).getBytes()).trim());
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/scim+json");

        HashMap<String, Object> rootMap = new HashMap<>();

        JSONArray operations = new JSONArray();

        HashMap<String, Object> operationMap = new HashMap<>();
        operationMap.put(OPERATION_PARAM, ADD_OP);

        JSONArray users = new JSONArray();

        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put(VALUE_PARAM, userId);

        users.put(new JSONObject(userMap));

        HashMap<String, Object> usersMap = new HashMap<>();
        usersMap.put(USERS_PARAM, users);

        operationMap.put(VALUE_PARAM, new JSONObject(usersMap));
        operations.put(new JSONObject(operationMap));

        rootMap.put(OPERATIONS_PARAM, operations);

        JSONObject jsonPatchObj = new JSONObject(rootMap);

        StringEntity entity = new StringEntity(jsonPatchObj.toString());
        request.setEntity(entity);

        client.execute(request);
    }

    public void deleteRole(String roleId) throws IOException {

        HttpDelete request = new HttpDelete(getRolesPath() + "/" + roleId);

        request.setHeader(HttpHeaders.USER_AGENT, OAuth2Constant.USER_AGENT);
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + Base64.encodeBase64String((USERNAME + ":" + PASSWORD).getBytes()).trim());
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/scim+json");

        client.execute(request);
    }

    public void deleteUser(String userId) throws IOException {

        HttpDelete request = new HttpDelete(getUsersPath() + "/" + userId);

        request.setHeader(HttpHeaders.USER_AGENT, OAuth2Constant.USER_AGENT);
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + Base64.encodeBase64String((USERNAME + ":" + PASSWORD).getBytes()).trim());
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/scim+json");

        client.execute(request);
    }

    private String getUsersPath() {
        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return SERVER_URL + SCIM2_USERS_ENDPOINT;
        } else {
            return SERVER_URL + "t/" + tenantDomain + SCIM2_USERS_ENDPOINT;
        }
    }

    private String getRolesPath() {
        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return SERVER_URL + SCIM2_ROLES_ENDPOINT;
        } else {
            return SERVER_URL + "t/" + tenantDomain + SCIM2_ROLES_ENDPOINT;
        }
    }
}
