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

public class UserInfoValidationController extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    UserInformationRecoveryClient client;

    public void init() {
        try {
            ConfigurationContext configContext =
                    (ConfigurationContext) this.getServletContext().getAttribute(CarbonConstants.
                            CONFIGURATION_CONTEXT);
            String carbonServerUrl = this.getServletConfig().getServletContext()
                    .getInitParameter("carbonServerUrl");

            client = new UserInformationRecoveryClient(carbonServerUrl, configContext);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException,
            IOException {

        HttpSession session = req.getSession(false);
        String confirmation = (String) session.getAttribute("confirmation");
        String viewPage = null;

        String username = req.getParameter("username");

        String captchaPath = req.getParameter("captchaImagePath");
        String captchaKey = req.getParameter("captchaSecretKey");
        String captchaAnswer = req.getParameter("captchaAnswer");
        String recoveryMethod = req.getParameter("recoveryMethod");

        if (confirmation != null && session != null) {

            // verify conf code
            CaptchaInfoBean captchaInfoBean = new CaptchaInfoBean();
            captchaInfoBean.setImagePath(captchaPath);
            captchaInfoBean.setSecretKey(captchaKey);
            captchaInfoBean.setUserAnswer(captchaAnswer);
            VerificationBean verificationBean = null;

            verificationBean = client.VerifyUser(username, captchaInfoBean);

            if (verificationBean != null && verificationBean.getVerified()) {

                session.setAttribute("confirmation", verificationBean.getKey());
                session.setAttribute("username", username);

                if ("notification".equals(recoveryMethod)) {
                    viewPage = "./recoverNotification";
                } else {
                    viewPage = "./recoverQnA";
                }


            } else {
                req.setAttribute(
                        "errors",
                        "Invalid information provided. Either the user not found or captcha " +
                                "answer is incorrect. Cannot proceed.");
                viewPage = "error.jsp";
            }

        } else {

            req.setAttribute("errors",
                    "Missing confirmation code or invalid session. Cannot proceed.");
            viewPage = "error.jsp";
        }

        RequestDispatcher view = req.getRequestDispatcher(viewPage);
        view.forward(req, res);

    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException,
            IOException {

        doPost(req, res);
    }
}
