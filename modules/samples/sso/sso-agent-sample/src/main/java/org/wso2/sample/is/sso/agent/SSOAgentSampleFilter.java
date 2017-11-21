/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.sample.is.sso.agent;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.sso.agent.SSOAgentConstants;
import org.wso2.carbon.identity.sso.agent.SSOAgentFilter;
import org.wso2.carbon.identity.sso.agent.bean.SSOAgentConfig;

import javax.servlet.*;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SSOAgentSampleFilter extends SSOAgentFilter {

    private static Logger LOGGER = Logger.getLogger("org.wso2.sample.is.sso.agent");

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String CHARACTER_ENCODING = "UTF-8";
    private static Properties properties;
    protected FilterConfig filterConfig = null;

    static{
        properties = SampleContextEventListener.getProperties();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        String httpBinding = servletRequest.getParameter(
                SSOAgentConstants.SSOAgentConfig.SAML2.HTTP_BINDING);
        if(httpBinding != null && !httpBinding.isEmpty()){
            if("HTTP-POST".equals(httpBinding)){
                httpBinding = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
            } else if ("HTTP-Redirect".equals(httpBinding)) {
                httpBinding = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect";
            } else {
                LOGGER.log(Level.INFO, "Unknown SAML2 HTTP Binding. Defaulting to HTTP-POST");
                httpBinding = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
            }
        } else {
            LOGGER.log(Level.INFO, "SAML2 HTTP Binding not found in request. Defaulting to HTTP-POST");
            httpBinding = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
        }
        SSOAgentConfig config = (SSOAgentConfig)filterConfig.getServletContext().
                getAttribute(SSOAgentConstants.CONFIG_BEAN_NAME);
        config.getSAML2().setHttpBinding(httpBinding);
        config.getOpenId().setClaimedId(servletRequest.getParameter(
                SSOAgentConstants.SSOAgentConfig.OpenID.CLAIMED_ID));
        config.getOpenId().setMode(servletRequest.getParameter(
                SSOAgentConstants.OpenID.OPENID_MODE));

        if (StringUtils.isNotEmpty(servletRequest.getParameter(USERNAME)) &&
                StringUtils.isNotEmpty(servletRequest.getParameter(PASSWORD))) {

            String authorization = servletRequest.getParameter(USERNAME) + ":" + servletRequest.getParameter(PASSWORD);
            // Base64 encoded username:password value
            authorization = new String(Base64.encode(authorization.getBytes(CHARACTER_ENCODING)));
            String htmlPayload = "<html>\n" +
                    "<body>\n" +
                    "<p>You are now redirected back to " + properties.getProperty("SAML2.IdPURL") + " \n" +
                    "If the redirection fails, please click the post button.</p>\n" +
                    "<form method='post' action='" +  properties.getProperty("SAML2.IdPURL") + "'>\n" +
                    "<input type='hidden' name='sectoken' value='" + authorization + "'/>\n" +
                    "<p>\n" +
                    "<!--$saml_params-->\n" +
                    "<button type='submit'>POST</button>\n" +
                    "</p>\n" +
                    "</form>\n" +
                    "<script type='text/javascript'>\n" +
                    "document.forms[0].submit();\n" +
                    "</script>\n" +
                    "</body>\n" +
                    "</html>";
            config.getSAML2().setPostBindingRequestHTMLPayload(htmlPayload);
        } else {
            // Reset previously sent HTML payload
            config.getSAML2().setPostBindingRequestHTMLPayload(null);
        }
        servletRequest.setAttribute(SSOAgentConstants.CONFIG_BEAN_NAME,config);
        super.doFilter(servletRequest, servletResponse, filterChain);
    }

    @Override
    public void destroy() {

    }
}
