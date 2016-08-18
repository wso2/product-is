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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.mgt.stub.dto.ChallengeQuestionDTO;
import org.wso2.carbon.identity.mgt.stub.dto.UserChallengesDTO;
import org.wso2.sample.inforecovery.client.IdentityManagementAdminClient;
import org.wso2.sample.inforecovery.client.UserProfileClient;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecurityQuestionSetupController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(SecurityQuestionSetupController.class);
    UserProfileClient client;
    IdentityManagementAdminClient identityManagementAdminClient;

    public void init() {
        try {
            ConfigurationContext configContext = (ConfigurationContext) this.getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            String carbonServerUrl = this.getServletConfig().getServletContext()
                    .getInitParameter("carbonServerUrl");

            client = new UserProfileClient(carbonServerUrl, configContext);
            identityManagementAdminClient = new IdentityManagementAdminClient(null,
                    carbonServerUrl, configContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * This method handles the display of sign up page.
     */
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException,
            ServletException {

        HttpSession session = req.getSession(false);

        ChallengeQuestionDTO[] challengeQuestionDTOs = null;

        String userName = req.getParameter("username");
        try {
            challengeQuestionDTOs = identityManagementAdminClient.getChallengeQuestions();
        } catch (Exception e) {
            String msg = "Error occurred while loading security questions";
            log.error(msg, e);
            throw new ServletException(msg, e);
        }

        Map<String, List<String>> questionSets = new HashMap<String, List<String>>();
        for (ChallengeQuestionDTO challengeQuestionDTO : challengeQuestionDTOs) {
            List<String> questions = questionSets.get(challengeQuestionDTO.getQuestionSetId());
            if (questions != null) {
                questions.add(challengeQuestionDTO.getQuestion());
                questionSets.put(challengeQuestionDTO.getQuestionSetId(),
                        questions);
            } else {
                questions = new ArrayList<String>();
                questions.add(challengeQuestionDTO.getQuestion());
                questionSets.put(challengeQuestionDTO.getQuestionSetId(), questions);
            }
        }

        if (questionSets != null) {
            req.setAttribute("questionSet1", questionSets.get("http://wso2" +
                    ".org/claims/challengeQuestion1"));
            req.setAttribute("questionSet2", questionSets.get("http://wso2" +
                    ".org/claims/challengeQuestion2"));
        }

        String viewPage = "setup_security_question.jsp";
        RequestDispatcher view = req.getRequestDispatcher(viewPage);
        view.forward(req, res);
    }

    /*
     * This handles the post of sign up and validation.
     */
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException,
            ServletException {

        HttpSession session = req.getSession(false);

        boolean success = true;
        String username = req.getParameter("username");
        if (username != null) {
            username = session.getAttribute("username").toString();
        }

        String answer1 = req.getParameter("answer1");
        String answer2 = req.getParameter("answer2");

        String question1 = req.getParameter("question1");
        String question2 = req.getParameter("question2");

        UserChallengesDTO userChallengesDTO1 = new UserChallengesDTO();
        userChallengesDTO1.setQuestion(question1);
        userChallengesDTO1.setAnswer(answer1);

        UserChallengesDTO userChallengesDTO2 = new UserChallengesDTO();
        userChallengesDTO2.setQuestion(question2);
        userChallengesDTO2.setAnswer(answer2);

        UserChallengesDTO[] userChallengesDTOs = {userChallengesDTO1, userChallengesDTO2};

        try {
            identityManagementAdminClient.setChallengeQuestionsOfUser(username, userChallengesDTOs);
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        }

        String viewPage = "./editUser" + "?username=" + username;
        req.setAttribute("securityQuestionSetupStage", success);
        res.sendRedirect(viewPage);

    }

    private String getDisplayName(String uri) {

        int startIndex = uri.lastIndexOf("/");
        return uri.substring(startIndex + 1);

    }
}
