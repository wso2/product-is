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

import org.wso2.sample.inforecovery.client.ClientConstants;
import org.wso2.sample.inforecovery.client.authenticator.ServiceAuthenticator;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class IndexController extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public void init(ServletConfig config) throws ServletException {

        super.init(config);

        ServletContext ctx = config.getServletContext();
        ServiceAuthenticator sAuthenticator = ServiceAuthenticator.getInstance();
        sAuthenticator.setAccessUsername(ctx.getInitParameter(ClientConstants.ACCESS_USERNAME));
        sAuthenticator.setAccessPassword(ctx.getInitParameter(ClientConstants.ACCESS_PASSWORD));

        String trustStorePath = ctx.getInitParameter(ClientConstants.TRUSTSTORE_PATH);
        System.setProperty(ClientConstants.TRUSTSTORE_PROPERTY,
                Thread.currentThread().getContextClassLoader().getResource(trustStorePath).getPath());

    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException,
            ServletException {

        this.doPost(req, res);

    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException,
            ServletException {


        String requestPage = req.getRequestURI();
        String destinationUri = requestPage.substring(requestPage.indexOf("/infoRecover") + 12);

        RequestDispatcher view = null;

        HttpSession session = req.getSession(true);

        if (session != null) {

            if (destinationUri.contains("userInfoView")) {
                destinationUri = "/userInfoView";
            } else if (destinationUri.contains("validate") && !destinationUri.contains
                    ("validateAccountSetup")) {
                destinationUri = "/validate";
            } else if (destinationUri.contains("verify")) {
                if (req.getParameter("confirmation") != null) {
                    destinationUri = "/userInfoView";
                    session.setAttribute("emailConfirmation", req.getParameter("confirmation"));
                } else {
                    destinationUri = "/verify";
                }
            } else if (destinationUri.contains("signup")) {
                destinationUri = "/signup";
            } else if (destinationUri.contains("confirmReg")) {
                destinationUri = "/confirmReg";
            } else if (destinationUri.contains("recoverAccount")) {
                destinationUri = "/recoverAccount";
            }

            view = req.getRequestDispatcher(destinationUri);
            view.forward(req, res);

        }
    }
}
