/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.sample.inforecovery.controller;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean;
import org.wso2.carbon.identity.mgt.stub.beans.VerificationBean;
import org.wso2.sample.inforecovery.client.UserInformationRecoveryClient;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class SelfSignupConfirmationController extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private UserInformationRecoveryClient client;

    private static final String CONFIRMATION = "confirmation";
    private static final String USER_STORE_DOMAIN = "userstoredomain";
    private static final String USER_NAME = "username";
    private static final String TENANT_DOMAIN = "tenantdomain";
    private static final String CAPTCHA = "captcha";
    private static final String CARBON_SERVER_URL = "carbonServerUrl";
    private static final String CAPTCHA_IMAGE_URL = "captchaImageUrl";
    private static final String CAPTCHA_ANSWER = "captchaAnswer";
    private static final String STATUS = "status";
    private static final String PRIMARY_USERSTORE = "PRIMARY";
    private static final String SUPER_TENANT = "carbon.super";

    public void init() {
        try {
            ConfigurationContext configContext = (ConfigurationContext) this.getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            String carbonServerUrl = this.getServletConfig().getServletContext().getInitParameter(CARBON_SERVER_URL);
            client = new UserInformationRecoveryClient(carbonServerUrl, configContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException,
            IOException {

        HttpSession session = req.getSession();
        session.setAttribute(CONFIRMATION, req.getParameter(CONFIRMATION));
        String userstoredomain = req.getParameter(USER_STORE_DOMAIN);
        String username = req.getParameter(USER_NAME);
        if (!(PRIMARY_USERSTORE.equalsIgnoreCase(userstoredomain)) && userstoredomain != null) {
            username = userstoredomain + "/" + username;
        }
        session.setAttribute(USER_NAME, username);
        String tenantDomain = req.getParameter(TENANT_DOMAIN);
        if (tenantDomain == null) {
            tenantDomain = SUPER_TENANT;
        }
        session.setAttribute(TENANT_DOMAIN, tenantDomain);
        CaptchaInfoBean bean = client.generateCaptcha();
        session.setAttribute(CAPTCHA, bean);
        String carbonServerUrl = this.getServletConfig().getServletContext().getInitParameter(CARBON_SERVER_URL);
        session.setAttribute(CAPTCHA_IMAGE_URL, carbonServerUrl + bean.getImagePath());

        RequestDispatcher view = req.getRequestDispatcher("signup_confirm.jsp");
        view.forward(req, res);
    }


    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException,
            IOException {

        HttpSession session = req.getSession(false);
        String userName = (String) session.getAttribute(USER_NAME);
        String code = (String) session.getAttribute(CONFIRMATION);
        CaptchaInfoBean captcha = (CaptchaInfoBean) session.getAttribute(CAPTCHA);
        captcha.setUserAnswer(req.getParameter(CAPTCHA_ANSWER));
        String tenantDomain = (String) session.getAttribute(TENANT_DOMAIN);
        VerificationBean bean = client.confirmUserSelfRegistration(userName, code, captcha, tenantDomain);
        req.setAttribute(STATUS, bean);
        RequestDispatcher view = req.getRequestDispatcher("signup_confirm_status.jsp");
        view.forward(req, res);
    }
}
