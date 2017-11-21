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

public class RecoverUsernameController extends HttpServlet {

    /**
     *
     */
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

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException,
            ServletException {

        HttpSession session = req.getSession();
        ClaimUIDto[] newClaims = new ClaimUIDto[3];

        ClaimUIDto claimDto1 = new ClaimUIDto();
        claimDto1.setClaimUri("http://wso2.org/claims/emailaddress");
        claimDto1.setDisplayName("Email");
        newClaims[0] = claimDto1;

        ClaimUIDto claimDto2 = new ClaimUIDto();
        claimDto2.setClaimUri("http://wso2.org/claims/givenname");
        claimDto2.setDisplayName("First Name");
        newClaims[1] = claimDto2;

        ClaimUIDto claimDto3 = new ClaimUIDto();
        claimDto3.setClaimUri("http://wso2.org/claims/lastname");
        claimDto3.setDisplayName("Last Name");
        newClaims[2] = claimDto3;

        session.setAttribute("claims", newClaims);

        CaptchaInfoBean bean = client.generateCaptcha();

        session.setAttribute("captcha", bean);
        String carbonServerUrl = this.getServletConfig().getServletContext()
                .getInitParameter("carbonServerUrl");
        session.setAttribute("captchaImageUrl", carbonServerUrl + bean.getImagePath());

        RequestDispatcher view = req.getRequestDispatcher("verify_account.jsp");
        view.forward(req, res);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException,
            ServletException {

        HttpSession session = req.getSession(false);
        ClaimUIDto[] claims = (ClaimUIDto[]) session.getAttribute("claims");
        UserIdentityClaimDTO[] claimsDto = new UserIdentityClaimDTO[claims.length];

        for (int i = 0; i < claims.length; i++) {
            UserIdentityClaimDTO dto = new UserIdentityClaimDTO();
            dto.setClaimUri(claims[i].getClaimUri());
            if (claims[i].getClaimUri().equals("http://wso2.org/claims/emailaddress")) {
                dto.setClaimValue(req.getParameter("http://wso2.org/claims/emailaddress"));
            } else if (claims[i].getClaimUri().equals("http://wso2.org/claims/givenname")) {
                dto.setClaimValue(req.getParameter("http://wso2.org/claims/givenname"));
            } else if (claims[i].getClaimUri().equals("http://wso2.org/claims/lastname")) {
                dto.setClaimValue(req.getParameter("http://wso2.org/claims/lastname"));
            }
            claimsDto[i] = dto;
        }

        CaptchaInfoBean captcha = (CaptchaInfoBean) session.getAttribute("captcha");
        captcha.setUserAnswer(req.getParameter("captchaAnswer"));

        VerificationBean bean = client.verifyAccount(claimsDto, captcha, null);

        req.setAttribute("status", bean);

        RequestDispatcher view = req.getRequestDispatcher("verify_account_status.jsp");
        view.forward(req, res);

    }

}
