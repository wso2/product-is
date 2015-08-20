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
import org.wso2.carbon.identity.user.profile.stub.types.UserFieldDTO;
import org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO;
import org.wso2.sample.inforecovery.client.UserProfileClient;
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

public class UserProfileController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(UserProfileController.class);
    UserProfileClient client;

    public void init() {
        try {
            ConfigurationContext configContext = (ConfigurationContext) this.getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            String carbonServerUrl = this.getServletConfig().getServletContext()
                    .getInitParameter("carbonServerUrl");

            client = new UserProfileClient(carbonServerUrl, configContext);
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

        UserProfileDTO userProfileDTO = null;

        try {
            userProfileDTO = client.getUserProfile(req.getParameter("username"), "default");
        } catch (Exception e) {
            String msg = "Error occurred while retrieving user profile";
            log.error(msg, e);
            throw new ServletException(msg, e);
        }


        UserFieldDTO[] fieldValues = userProfileDTO.getFieldValues();

        List<ClaimUIDto> claimsUi = new ArrayList<ClaimUIDto>();
        List<UserFieldDTO> filteredClaims = new ArrayList<UserFieldDTO>();
        String viewPage = "editUser.jsp";

        if (fieldValues != null) {
            for (UserFieldDTO fieldValue : fieldValues) {
                ClaimUIDto tempDto = new ClaimUIDto();
                if (fieldValue == null) {
                    continue;
                }
                tempDto.setClaimUri(fieldValue.getClaimUri());
                tempDto.setDisplayName(fieldValue.getDisplayName());
                tempDto.setClaimValue(fieldValue.getFieldValue());
                claimsUi.add(tempDto);
            }
            req.setAttribute("claims", claimsUi.toArray(new ClaimUIDto[claimsUi.size()]));
            session.setAttribute("claims", filteredClaims.toArray(new ClaimUIDto[claimsUi.size()]));
            session.setAttribute("userProfileDTO", userProfileDTO);
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
        UserFieldDTO[] userFields = null;

        boolean success = true;
        String username = req.getParameter("username");
        if (username != null) {
            username = session.getAttribute("username").toString();
        }

        UserProfileDTO userProfile = (UserProfileDTO) session.getAttribute("userProfileDTO");
        String profileConfiguration = userProfile.getProfileConifuration();

        try {
            userFields = client.getOrderedUserFields(userProfile.getFieldValues());
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        }


        if (userFields != null) {
            for (UserFieldDTO field : userFields) {
                String value = req.getParameter(field.getClaimUri());
                if (value == null) {
                    value = "";
                }
                field.setFieldValue(value);
            }
        }

        UserProfileDTO userprofile = new UserProfileDTO();
        userprofile.setProfileName(userProfile.getProfileName());
        userprofile.setFieldValues(userFields);
        userprofile.setProfileConifuration(profileConfiguration);
        try {
            client.setUserProfile(username, userprofile);
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        }


        String viewPage = "edit_user_status.jsp";
        req.setAttribute("updatedStatus", success);
        RequestDispatcher view = req.getRequestDispatcher(viewPage);
        view.forward(req, res);

    }

    private String getDisplayName(String uri) {

        int startIndex = uri.lastIndexOf("/");
        return uri.substring(startIndex + 1);

    }
}
