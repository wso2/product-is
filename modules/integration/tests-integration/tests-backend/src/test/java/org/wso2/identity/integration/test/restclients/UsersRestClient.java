/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.identity.integration.test.restclients;

import io.restassured.http.ContentType;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.user.common.model.InvitationRequest;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

public class UsersRestClient extends RestBaseClient {

    private static final String API_USERS_BASE_PATH = "api/users/v1";
    private static final String OFFLINE_INVITE_LINK_PATH = "/offline-invite-link";

    private final String offlineInviteLinkPath;
    private final String username;
    private final String password;

    public UsersRestClient(String serverUrl, Tenant tenantInfo) {

        String tenantDomain = tenantInfo.getContextUser().getUserDomain();
        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();
        this.offlineInviteLinkPath = getOfflineInviteLinkPath(serverUrl, tenantDomain);
    }

    /**
     * Generate offline invite link for the given user to set password.
     *
     * @param invitationRequest Invitation request.
     * @return Offline invite link.
     * @throws IOException   If an error occurred while generating the offline invite link.
     */
    public String generateOfflineInviteLink(InvitationRequest invitationRequest) throws IOException {

        String jsonRequest = toJSONString(invitationRequest);
        try (CloseableHttpResponse response = getResponseOfHttpPost(offlineInviteLinkPath, jsonRequest, getHeaders())) {

            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_CREATED,
                    "Offline link generation failed.");
            return EntityUtils.toString(response.getEntity());
        }
    }

    private String getOfflineInviteLinkPath(String serverUrl, String tenantDomain) {

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return serverUrl + API_USERS_BASE_PATH + OFFLINE_INVITE_LINK_PATH;
        } else {
            return serverUrl + TENANT_PATH + tenantDomain + PATH_SEPARATOR + API_USERS_BASE_PATH +
                    OFFLINE_INVITE_LINK_PATH;
        }
    }

    private Header[] getHeaders() {

        Header[] headerList = new Header[3];
        headerList[0] = new BasicHeader(USER_AGENT_ATTRIBUTE, OAuth2Constant.USER_AGENT);
        headerList[1] = new BasicHeader(AUTHORIZATION_ATTRIBUTE, BASIC_AUTHORIZATION_ATTRIBUTE +
                Base64.encodeBase64String((username + ":" + password).getBytes()).trim());
        headerList[2] = new BasicHeader(CONTENT_TYPE_ATTRIBUTE, String.valueOf(ContentType.JSON));

        return headerList;
    }
}
