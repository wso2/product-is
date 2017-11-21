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
import org.wso2.carbon.identity.mgt.stub.dto.UserIdentityClaimDTO;
import org.wso2.sample.inforecovery.client.UserInformationRecoveryClient;
import org.wso2.sample.inforecovery.dto.ClaimUIDto;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SelfSignupController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    UserInformationRecoveryClient client;

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

    /*
     * This method handles the display of sign up page.
     */
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException,
            ServletException {

        HttpSession session = req.getSession(false);

        UserIdentityClaimDTO[] claims = client
                .getUserIdentitySupportedClaims("http://wso2.org/claims");


        List<ClaimUIDto> claimsUi = new ArrayList<ClaimUIDto>();
        List<UserIdentityClaimDTO> filteredClaims = new ArrayList<UserIdentityClaimDTO>();
        String viewPage = "signup.jsp";

        if (claims != null) {
            for (UserIdentityClaimDTO claim : claims) {
                ClaimUIDto tempDto = new ClaimUIDto();
                if (claim == null) {
                    continue;
                }
                tempDto.setClaimUri(claim.getClaimUri());
                tempDto.setDisplayName(this.getDisplayName(claim.getClaimUri()));
                claimsUi.add(tempDto);
                filteredClaims.add(claim);
            }
            req.setAttribute("claims", claimsUi.toArray(new ClaimUIDto[claimsUi.size()]));
            session.setAttribute("claims", filteredClaims.toArray
                    (new UserIdentityClaimDTO[claimsUi.size()]));
        }
        RequestDispatcher view = req.getRequestDispatcher(viewPage);
        view.forward(req, res);
    }

    /*
     * This handles the post of sign up and validation.
     */
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException,
            ServletException {

        HttpSession session = req.getSession(false);
        String userName = req.getParameter("username");
        String password = req.getParameter("password");
        //String tenantDomain = req.getParameter("tenant");
        String tenantDomain = "carbon.super";
        String viewPage;

        UserIdentityClaimDTO[] claims = (UserIdentityClaimDTO[]) session.getAttribute("claims");
        if (claims != null) {
            for (UserIdentityClaimDTO claim : claims) {
                if (claim == null) {
                    continue;
                }
                claim.setClaimValue(req.getParameter(claim.getClaimUri()));
            }
        }

        VerificationBean bean = client.registerUser(userName, password, claims, "default",
                tenantDomain);

        viewPage = "signup_status.jsp";
        req.setAttribute("status", bean);

        RequestDispatcher view = req.getRequestDispatcher(viewPage);
        view.forward(req, res);

    }

    private String getDisplayName(String uri) {

        int startIndex = uri.lastIndexOf("/");
        return uri.substring(startIndex + 1);

    }
}
