package org.wso2.sample.identity.oauth2;

import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class SessionIdStore {
    private static Log log = LogFactory.getLog(SessionIdStore.class);
    private static Map<String, HttpSession> sessionMap = new HashMap<>();

    public static void storeSession(String sid, HttpSession session) {
        log.info("Storing session: " + session.getId() + " against the sid: " + sid);
        sessionMap.put(sid, session);
    }

    public static String getSid(String idToken) throws ParseException {
        String sid = (String) SignedJWT.parse(idToken).getJWTClaimsSet().getClaim("sid");
        return idToken != null ? sid : null;
    }

    public static HttpSession getSession(String sid) {
        log.info("Retrieving session: " + sessionMap.get(sid).getId() + " for the sid: " + sid);
        return sessionMap.get(sid);
    }

    public static void store(HttpServletRequest req) {
        log.info(req);
    }
}