/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.identity.saml2.demo;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.xml.ConfigurationException;

/**
 * Servlet implementation class SAML2ConsumerServlet
 */
public class SAML2ConsumerServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private SamlConsumerManager consumer;
	private static Log log = LogFactory.getLog(SAML2ConsumerServlet.class);

	/**
	 * Servlet init
	 */
	public void init(ServletConfig config) throws ServletException {
		try {
	        consumer = new SamlConsumerManager(config);
        } catch (ConfigurationException e) {
        	throw new ServletException("Errow while configuring SAMLConsumerManager", e);
        }
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	                                                                              throws ServletException,
	                                                                              IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	                                                                               throws ServletException,
	                                                                               IOException {

		String responseMessage = request.getParameter("SAMLResponse");
		
		if (responseMessage != null) { /* response from the identity provider */

			log.info("SAMLResponse received from IDP ");
			
			Map<String, String> result = consumer.processResponseMessage(responseMessage);
			
			if (result == null) {
				// lets logout the user
				response.sendRedirect("index.jsp");
			} else if (result.size() == 1) {
				/*
				 * No user attributes are returned, so just goto the default
				 * home page.
				 */
				response.sendRedirect("home.jsp?subject=" + result.get("Subject"));
			} else if (result.size() > 1) {
				/*
				 * We have received attributes, so lets show them in the
				 * attribute home page.
				 */
				String params = "home-attrib.jsp?";
				Object[] keys = result.keySet().toArray();
				for (int i = 0; i < result.size(); i++) {
					String key = (String) keys[i];
					String value = (String) result.get(key);
					if (i != result.size()) {
						params = params + key + "=" + value + "&";
					} else {
						params = params + key + "=" + value;
					}
				}
				response.sendRedirect(params);
			} else {
				// something wrong, re-login
				response.sendRedirect("index.jsp");
			}

		} else { /* time to create the authentication request or logout request */

			try {
				String requestMessage = consumer.buildRequestMessage(request);

				response.sendRedirect(requestMessage);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
