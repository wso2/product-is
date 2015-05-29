/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

    public void init() {
        try {
            ConfigurationContext configContext = (ConfigurationContext) this.getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            String carbonServerUrl = this.getServletConfig().getServletContext()
                    .getInitParameter("carbonServerUrl");

            client = new UserInformationRecoveryClient(carbonServerUrl, configContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException,
            IOException {

        HttpSession session = req.getSession();
        session.setAttribute("confirmation", req.getParameter("confirmation"));
        String userstoredomain = req.getParameter("userstoredomain");
        String username = req.getParameter("username");
        if (!("PRIMARY".equalsIgnoreCase(userstoredomain)) && userstoredomain != null) {
            username = userstoredomain + "/" + username;
        }
        session.setAttribute("userName", username);
        CaptchaInfoBean bean = client.generateCaptcha();

        session.setAttribute("captcha", bean);
        String carbonServerUrl = this.getServletConfig().getServletContext()
                .getInitParameter("carbonServerUrl");
        session.setAttribute("captchaImageUrl", carbonServerUrl + bean.getImagePath());

        RequestDispatcher view = req.getRequestDispatcher("signup_confirm.jsp");
        view.forward(req, res);
    }


    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException,
            IOException {

        HttpSession session = req.getSession(false);
        String userName = (String) session.getAttribute("userName");
        String code = (String) session.getAttribute("confirmation");
        CaptchaInfoBean captcha = (CaptchaInfoBean) session.getAttribute("captcha");
        captcha.setUserAnswer(req.getParameter("captchaAnswer"));
        VerificationBean bean = client.confirmUserSelfRegistration(userName, code, captcha, null);

        req.setAttribute("status", bean);

        RequestDispatcher view = req.getRequestDispatcher("signup_confirm_status.jsp");
        view.forward(req, res);
    }
}
