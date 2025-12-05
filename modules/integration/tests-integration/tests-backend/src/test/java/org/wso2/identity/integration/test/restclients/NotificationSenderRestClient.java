/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.server.notification.sender.v1.model.SMSSender;
import org.wso2.identity.integration.test.utils.OAuth2Constant;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Rest client for Notification Sender configurations.
 */
public class NotificationSenderRestClient extends RestBaseClient {

    public static final String VERSION_2 = "v2";

    private static final String NOTIFICATION_SENDER_SMS_ENDPOINT_TEMPLATE = "api/server/%s/notification-senders/sms";
    private final String serverUrl;
    private final String tenantDomain;
    private final String username;
    private final String password;
    private final String version;

    public NotificationSenderRestClient(String serverUrl, Tenant tenantInfo, String version) {

        this.serverUrl = serverUrl;
        this.tenantDomain = tenantInfo.getContextUser().getUserDomain();
        this.username = tenantInfo.getContextUser().getUserName();
        this.password = tenantInfo.getContextUser().getPassword();
        this.version = version;
    }

    /**
     * Constructor with default version v1.
     *
     * @param serverUrl Server URL.
     * @param tenantInfo Tenant information.
     */
    public NotificationSenderRestClient(String serverUrl, Tenant tenantInfo) {

        this(serverUrl, tenantInfo, "v2");
    }

    /**
     * Create SMS Sender (v1).
     *
     * @param smsSender SMS sender details.
     * @throws Exception If an error occurred while creating the SMS sender.
     */
    public void createSMSProvider(SMSSender smsSender) throws Exception {

        String jsonRequest = toJSONString(smsSender);

        try (CloseableHttpResponse response = getResponseOfHttpPost(getSMSSenderPath(), jsonRequest, getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_CREATED,
                    "Notification sender creation failed");
        }
    }

    /**
     * Create SMS Sender (v2).
     *
     * @param jsonRequest SMS sender details.
     * @throws Exception If an error occurred while creating the SMS sender.
     */
    public void createSMSProviderV2(String jsonRequest) throws Exception {

        try (CloseableHttpResponse response = getResponseOfHttpPost(getSMSSenderPath(), jsonRequest, getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_CREATED,
                    "Notification sender creation failed");
        }
    }

    /**
     * Delete SMS Sender.
     *
     * @throws Exception If an error occurred while creating the SMS sender.
     */
    public void deleteSMSProvider() throws Exception {

        try (CloseableHttpResponse response = getResponseOfHttpDelete(getSMSSenderPath() + "/SMSPublisher",
                getHeaders())) {
            Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpServletResponse.SC_NO_CONTENT,
                    "Notification sender deletion failed");
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

    private String getSMSSenderPath() {

        if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return serverUrl + String.format(NOTIFICATION_SENDER_SMS_ENDPOINT_TEMPLATE, version);
        } else {
            return serverUrl + TENANT_PATH + tenantDomain + PATH_SEPARATOR +
                    String.format(NOTIFICATION_SENDER_SMS_ENDPOINT_TEMPLATE, version);
        }
    }

    /**
     * Close the HTTP client.
     *
     * @throws IOException If an error occurred while closing the Http Client.
     */
    public void closeHttpClient() throws IOException {

        client.close();
    }
}
