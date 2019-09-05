/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.sample.identity.oauth2;

import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.ParseException;

/**
 * Servlet for handling Backchannel logout requests
 */
public class OIDCBackchannelLogoutServlet extends HttpServlet {

    private static Log log = LogFactory.getLog(OIDCBackchannelLogoutServlet.class);

    public void init(ServletConfig config) throws SecurityException {

    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {

        doPost(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {

        log.info("Backchannel logout request received.");

        String sid = null;
        try {
            sid = (String) SignedJWT.parse(req.getParameter("logout_token")).getJWTClaimsSet().getClaim("sid");
            log.info("Logout token: " + req.getParameter("logout_token"));
        } catch (ParseException e) {
            log.error("Error in generating Logout Token.", e);
        }
        HttpSession session = SessionIdStore.getSession(sid);

        if (session != null) {
            session.invalidate();
            SessionIdStore.removeSession(sid);
            log.info("Session invalidated successfully for sid: " + sid);
        } else {
            log.info("Cannot find corresponding session for sid: " + sid);
        }
    }
}
