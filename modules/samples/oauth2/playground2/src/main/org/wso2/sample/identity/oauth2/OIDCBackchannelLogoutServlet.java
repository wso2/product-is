package org.wso2.sample.identity.oauth2;

import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.ParseException;

public class OIDCBackchannelLogoutServlet extends HttpServlet {

    private static Log log = LogFactory.getLog(OIDCBackchannelLogoutServlet.class);

    public void init(ServletConfig config) throws SecurityException {
        BasicConfigurator.configure();
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        doPost(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        log.info("Logout request received.");

        String sid = null;
        try {
            sid = (String) SignedJWT.parse(req.getParameter("logoutToken")).getJWTClaimsSet().getClaim("sid");
            log.info("Logout token: " + req.getParameter("logoutToken"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        HttpSession session = SessionIdStore.getSession(sid);

        if (session != null) {
            session.invalidate();
            log.info("Session invalidated successfully.");
        } else {
            log.info("Cannot find corresponding session.");
        }

    }
}