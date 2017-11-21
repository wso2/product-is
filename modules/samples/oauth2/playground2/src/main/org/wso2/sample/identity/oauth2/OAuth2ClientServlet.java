/*
 *Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */

package org.wso2.sample.identity.oauth2;

import java.io.IOException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// This is the servlet which handles OAuth callbacks.
public class OAuth2ClientServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5587487420597790757L;
	
	static String userName;
	static String password;
	static String serverUrl;
	

	@Override
	public void init(ServletConfig config) throws ServletException {

		// All the code below is to overcome host name verification failure we get in certificate
		// validation due to self-signed certificate. This code should not be used in a production
		// setup.

		try {

			SSLContext sc;

			// Get SSL context
			sc = SSLContext.getInstance("SSL");

			// Create empty HostnameVerifier
			HostnameVerifier hv = new HostnameVerifier() {
				public boolean verify(String urlHostName, SSLSession session) {
					return true;
				}
			};

			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(java.security.cert.X509Certificate[] certs,
						String authType) {
				}

				public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
						String authType) {
				}
			} };

			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			//SSLSocketFactory sslSocketFactory = sc.getSocketFactory();

			//HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
            SSLContext.setDefault(sc);
			HttpsURLConnection.setDefaultHostnameVerifier(hv);
			
			// Load init parameters.			
			userName = config.getInitParameter("userName");
			password = config.getInitParameter("password");
			serverUrl = config.getInitParameter("serverUrl");

		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
			IOException {
		RequestDispatcher dispatcher = req.getRequestDispatcher("oauth2.jsp");
		dispatcher.forward(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}
}
