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
import org.json.simple.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.ParseException;

/**
 * Servlet for extracting ID Token claims for Implicit Grant
 */
public class IDTokenExtractorServlet extends HttpServlet {

    private static Log log = LogFactory.getLog(IDTokenExtractorServlet.class);

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String idToken = req.getParameter("idToken");
        String user = null;
        String sid = null;

        ServletOutputStream out = resp.getOutputStream();

        try {
            user = (String) SignedJWT.parse(idToken).getJWTClaimsSet().getSubject();

            sid = SessionIdStore.getSid(idToken);

            HttpSession session = req.getSession();
            if (user != null) {
                log.info("Logged in user: " + user);
                session.setAttribute(OAuth2Constants.LOGGED_IN_USER, user);
            }

            if (sid != null) {
                SessionIdStore.storeSession(sid, session);
            }

            resp.setContentType("application/json");
            JSONObject respData = new JSONObject();
            respData.put("user", user);

            out.print(respData.toString());

        } catch (ParseException e) {
            log.error("Invalid Token sent to IDTokenExtractorServlet.", e);
        }
    }
}
