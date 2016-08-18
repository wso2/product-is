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
import org.wso2.carbon.identity.mgt.stub.dto.ChallengeQuestionIdsDTO;
import org.wso2.sample.inforecovery.client.UserInformationRecoveryClient;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class RecoverByQnAController extends HttpServlet {

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
        String username = (String) session.getAttribute("username");

        String viewPage = null;

        if (confirmation != null && username != null) {

            ChallengeQuestionIdsDTO ids = client.getChallengeQuestionIds(username, confirmation);
            if (ids != null) {

                String[] qids = ids.getIds();
                session.setAttribute("qids", qids);
                session.setAttribute("step", Integer.valueOf(qids.length));
                viewPage = "qnaProcessor";

                session.setAttribute("currentqid", null);
                session.setAttribute("confirmation", ids.getKey());

                if (ids.getError() != null) {
                    session.setAttribute("currentqid", null);
                    session.setAttribute("confirmation", null);
                    viewPage = "error.jsp";
                    req.setAttribute("errors", ids.getError() + ": Cannot proceed.");
                }

            }

        } else {

            req.setAttribute("errors",
                    "Missing confirmation code or invalid session. Cannot proceed.");
            viewPage = "error.jsp";
        }

        RequestDispatcher view = req.getRequestDispatcher(viewPage);
        view.forward(req, res);
    }
}
