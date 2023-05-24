package org.wso2.identity.integration.test.oauth2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.user.common.model.PatchRoleOperationRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.RoleRequestObject;
import org.wso2.identity.integration.test.rest.api.user.common.model.UserObject;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SCIM2RestClient {

    public static final String SCIM2_USERS_ENDPOINT = "/scim2/Users";
    public static final String SCIM2_ROLES_ENDPOINT = "/scim2/Roles";
    public static final String BASIC_AUTHORIZATION_ATTRIBUTE = "Basic ";
    public static final String CONTENT_TYPE_ATTRIBUTE = "Content-Type";
    public static final String AUTHORIZATION_ATTRIBUTE = "Authorization";
    private static final String USER_AGENT_ATTRIBUTE = "User-Agent";
    private static final String SCIM_JSON_CONTENT_TYPE = "application/scim+json";
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

    public String createUser(UserObject userInfo) throws IOException, JSONException {

        String jsonRequest = toJSONString(userInfo);
        if (userInfo.getScimSchemaExtensionEnterprise() != null) {
            jsonRequest = jsonRequest.replace("scimSchemaExtensionEnterprise",
                    "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User");
        }

        HttpResponse response = getResponseOfHttpPost(getUsersPath(), jsonRequest);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_CREATED,
                "User creation failed");

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        Object obj = JSONValue.parse(rd);
        EntityUtils.consume(response.getEntity());

        return ((JSONObject) obj).get("id").toString();
    }

    public void deleteUser(String userId) throws IOException {

        HttpResponse response = getResponseOfHttpDelete(getUsersPath() + "/" + userId);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                "User deletion failed");
        EntityUtils.consume(response.getEntity());
    }

    public String addRole(RoleRequestObject roleInfo) throws IOException, JSONException {

        String jsonRequest = toJSONString(roleInfo);

        HttpResponse response = getResponseOfHttpPost(getRolesPath(), jsonRequest);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_CREATED,
                "Role creation failed");


        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        Object obj = JSONValue.parse(rd);
        EntityUtils.consume(response.getEntity());

        return ((JSONObject) obj).get("id").toString();
    }

    public void updateUserRole(PatchRoleOperationRequestObject patchRoleInfo, String roleId) throws IOException {

        String jsonRequest = toJSONString(patchRoleInfo);

        HttpResponse response = getResponseOfHttpPatch(getRolesPath() + "/" + roleId, jsonRequest);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_OK,
                "Role update failed");
        EntityUtils.consume(response.getEntity());
    }

    public void deleteRole(String roleId) throws IOException {

        HttpResponse response = getResponseOfHttpDelete(getRolesPath() + "/" + roleId);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                "Role deletion failed");
        EntityUtils.consume(response.getEntity());
    }

    public String toJSONString(java.lang.Object object) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(object);
    }

    public HttpResponse getResponseOfHttpPost(String endPointUrl, String jsonRequest) throws IOException {

        HttpPost request = new HttpPost(endPointUrl);
        request.setHeaders(getHeaders());
        request.setEntity(new StringEntity(jsonRequest));

        return client.execute(request);
    }

    public HttpResponse getResponseOfHttpPatch(String endPointUrl, String jsonRequest) throws IOException {

        HttpPatch request = new HttpPatch(endPointUrl);
        request.setHeaders(getHeaders());
        request.setEntity(new StringEntity(jsonRequest));

        return client.execute(request);
    }

    public HttpResponse getResponseOfHttpDelete(String endPointUrl) throws IOException {

        HttpDelete request = new HttpDelete(endPointUrl);
        request.setHeaders(getHeaders());

        return client.execute(request);
    }

    public Header[] getHeaders() {

        Header[] headerList = new Header[3];
        headerList[0] = new BasicHeader(USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT);
        headerList[1] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BASIC_AUTHORIZATION_ATTRIBUTE +
                Base64.encodeBase64String((USERNAME + ":" + PASSWORD).getBytes()).trim());
        headerList[2] = new BasicHeader(CONTENT_TYPE_ATTRIBUTE, SCIM_JSON_CONTENT_TYPE);

        return headerList;
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
