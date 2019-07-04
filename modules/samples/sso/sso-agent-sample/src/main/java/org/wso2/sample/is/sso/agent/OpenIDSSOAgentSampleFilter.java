/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.identity.sso.agent.OpenIdSSOAgentFilter;
import org.wso2.carbon.identity.sso.agent.bean.SSOAgentConfig;
import org.wso2.carbon.identity.sso.agent.util.SSOAgentConstants;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class OpenIDSSOAgentSampleFilter extends OpenIdSSOAgentFilter {

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {

        SSOAgentConfig config = (SSOAgentConfig)filterConfig.getServletContext()
                .getAttribute(SSOAgentConstants.CONFIG_BEAN_NAME);
        config.getOpenId().setClaimedId(servletRequest.getParameter(SSOAgentConstants.SSOAgentConfig.OpenID.CLAIMED_ID));
        config.getOpenId().setMode(servletRequest.getParameter(SSOAgentConstants.OpenID.OPENID_MODE));

        servletRequest.setAttribute(SSOAgentConstants.CONFIG_BEAN_NAME,config);
        super.doFilter(servletRequest, servletResponse, chain);
    }
}
