
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

import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache class for storing HttpSession against sid
 */
public class SessionIdStore {

    private static Log log = LogFactory.getLog(SessionIdStore.class);
    private static Map<String, HttpSession> sessionMap = new HashMap<>();

    public static void storeSession(String sid, HttpSession session) {

        log.info("Storing session: " + session.getId() + " against the sid: " + sid);
        sessionMap.put(sid, session);
    }

    public static String getSid(String idToken) throws ParseException {

        String sid = (String) SignedJWT.parse(idToken).getJWTClaimsSet().getClaim("sid");
        return sid;
    }

    public static HttpSession getSession(String sid) {

        if (sid != null && sessionMap.get(sid) != null) {
            log.info("Retrieving session: " + sessionMap.get(sid).getId() + " for the sid: " + sid);
            return sessionMap.get(sid);
        } else {
            log.error("No session found for the sid: " + sid);
            return null;
        }
    }

    public static void removeSession(String sid) {

        sessionMap.remove(sid);
    }
}
