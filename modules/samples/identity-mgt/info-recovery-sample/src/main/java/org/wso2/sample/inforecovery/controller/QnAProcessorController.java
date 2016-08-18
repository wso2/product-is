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
import org.wso2.carbon.identity.mgt.stub.beans.VerificationBean;
import org.wso2.carbon.identity.mgt.stub.dto.UserChallengesDTO;
import org.wso2.sample.inforecovery.client.UserInformationRecoveryClient;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class QnAProcessorController extends HttpServlet {

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

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException,
            IOException {

        doPost(req, res);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException,
            IOException {

        HttpSession session = req.getSession(false);

        String confirmation = (String) session.getAttribute("confirmation");
        String username = (String) session.getAttribute("username");
        String[] qids = (String[]) session.getAttribute("qids");
        String answer = req.getParameter("answer");
        String currentquestionId = (String) session.getAttribute("currentqid");
        Boolean valid = (Boolean) session.getAttribute("valid");
        int step = (Integer) session.getAttribute("step");
        String currentqid = null;
        String viewPage = null;
        VerificationBean vBean = null;
        String[] newqids = null;

        if (qids != null && qids.length > 0) {
            newqids = new String[(qids.length - 1)];

            // get the first element and populate other ids
            for (int i = (qids.length - 1), j = 0; i >= 0; i--, j++) {
                currentqid = qids[i];
                if (i >= 1) {
                    newqids[i - 1] = currentqid;
                }
            }
        }

        if (answer != null && currentquestionId != null) {

            vBean = client.checkAnswer(username, confirmation, currentquestionId, answer);
            if (!vBean.getVerified()) {
                req.setAttribute("errors", "The answer you provided is incorrect. Cannot proceed.");
                viewPage = "error.jsp";
                session.setAttribute("valid", Boolean.FALSE);
                session.setAttribute("step", Integer.valueOf(0));

            } else {
                if (step > 1) {
                    viewPage = "process_qna.jsp";
                    session.setAttribute("valid", Boolean.TRUE);

                    // Set the new confirmation key
                    confirmation = vBean.getKey();

                    UserChallengesDTO dto = client.getChallengeQuestion(username, confirmation,
                            currentqid);

                    session.setAttribute("confirmation", dto.getKey());
                    session.setAttribute("qids", newqids);
                    session.setAttribute("question", dto.getQuestion());
                    session.setAttribute("currentqid", currentqid);
                    session.setAttribute("step", Integer.valueOf(step - 1));
                } else {

                    viewPage = "password_reset.jsp";
                    session.setAttribute("currentqid", null);
                    session.setAttribute("step", Integer.valueOf(0));
                    session.setAttribute("confirmation", vBean.getKey());
                    session.setAttribute("valid", null);
                }
            }

        } else {
            // First time invoke
            UserChallengesDTO dto = client.getChallengeQuestion(username, confirmation, currentqid);

            session.setAttribute("confirmation", dto.getKey());
            session.setAttribute("qids", newqids);
            session.setAttribute("question", dto.getQuestion());
            session.setAttribute("currentqid", currentqid);
            session.setAttribute("valid", Boolean.TRUE);
            viewPage = "process_qna.jsp";
        }

        RequestDispatcher view = req.getRequestDispatcher(viewPage);
        view.forward(req, res);

    }

}
