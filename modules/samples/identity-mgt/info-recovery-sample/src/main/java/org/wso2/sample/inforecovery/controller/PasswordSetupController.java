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
import org.wso2.carbon.identity.sso.agent.bean.SSOAgentSessionBean;
import org.wso2.carbon.identity.sso.agent.util.SSOAgentConfigs;
import org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO;
import org.wso2.sample.inforecovery.client.UserAdminClient;
import org.wso2.sample.inforecovery.client.UserInformationRecoveryClient;
import org.wso2.sample.inforecovery.client.UserProfileClient;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class PasswordSetupController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    UserInformationRecoveryClient client;
    UserAdminClient userAdminClient;
    UserProfileClient userProfileClient;

    public void init() {
        try {
            ConfigurationContext configContext =
                    (ConfigurationContext) this.getServletContext().getAttribute(CarbonConstants.
                            CONFIGURATION_CONTEXT);
            String carbonServerUrl = this.getServletConfig().getServletContext()
                    .getInitParameter("carbonServerUrl");

            client = new UserInformationRecoveryClient(carbonServerUrl, configContext);
            userAdminClient = new
                    UserAdminClient(null, "https://localhost:9443/services/",
                    null);
            userProfileClient = new UserProfileClient(carbonServerUrl, configContext);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException,
            IOException {

        HttpSession session = req.getSession(false);

        String confirmation = (String) session.getAttribute("confirmation");
        String username = (String) session.getAttribute("username");
        String newPassword = req.getParameter("password");

        String viewPage = null;

        if (confirmation != null && session != null) {

            // reset the password

            UserProfileDTO userProfileDTO = null;

            try {
                userProfileDTO = userProfileClient.getUserProfile(username, "default");
                session.setAttribute("userProfileDTO", userProfileDTO);
            } catch (Exception e) {
                String msg = "Error occurred while retrieving user profile";
                throw new ServletException(msg, e);
            }

            try {
                userAdminClient.changePassword(username, newPassword);

                viewPage = "./setupSecurityQuestions" + "?username=" + username;
                req.setAttribute("validationStatus", true);
                SSOAgentSessionBean sessionBean = new SSOAgentSessionBean();
                SSOAgentSessionBean.SAMLSSOSessionBean samlssoSessionBean =
                        new SSOAgentSessionBean().new SAMLSSOSessionBean();
                samlssoSessionBean.setSubjectId(username);
                sessionBean.setSAMLSSOSessionBean(samlssoSessionBean);
                session.setAttribute(SSOAgentConfigs.getSessionBeanName(), sessionBean);
            } catch (Exception e) {
                req.setAttribute("errors",
                        "Missing confirmation code or invalid session. Cannot proceed.");
                viewPage = "error.jsp";
            }

        } else {

            req.setAttribute("errors",
                    "Missing confirmation code or invalid session. Cannot proceed.");
            viewPage = "error.jsp";
        }

        res.sendRedirect(viewPage);

    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException,
            IOException {

        doPost(req, res);
    }
}
