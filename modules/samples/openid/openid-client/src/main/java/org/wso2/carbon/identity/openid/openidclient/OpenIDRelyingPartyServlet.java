/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.openid.openidclient;

import java.io.IOException;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openid4java.association.AssociationException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;

/**
 * Servlet implementation class OpenIDRelyingPartyServlet
 */
public class OpenIDRelyingPartyServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ConsumerManager manager;
    private String return_to;
    private String openidUserPattern;

    public void init(ServletConfig config) throws ServletException {

        openidUserPattern = config.getInitParameter("OpenIDUserPattern");
        
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
            SSLContext.setDefault(sc);
            HttpsURLConnection.setDefaultHostnameVerifier(hv);
            
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        doPost(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
            throws ServletException, IOException {
        if (httpRequest.getParameter("is_id_res") != null
                && httpRequest.getParameter("is_id_res").equals("true")) {

            try {
                // Getting all parameters in request including AuthResponse
                ParameterList authResponseParams = new ParameterList(httpRequest.getParameterMap());

                // Previously discovered information
                DiscoveryInformation discovered = (DiscoveryInformation) httpRequest.getSession()
                        .getAttribute("openid-disc");

                // Verify return-to, discoveries, nonce & signature
                // Signature will be verified using the shared secrete
                VerificationResult verificationResult = manager.verify(return_to.toString(),
                        authResponseParams, discovered);

                Identifier verified = verificationResult.getVerifiedId();

                // Identifier will be NULL if verification failed
                if (verified != null) {
                    AuthSuccess authSuccess = (AuthSuccess) verificationResult.getAuthResponse();

                    String verifiedID = authSuccess.getIdentity();
                    String email = null, firstname = null, lastname = null, country = null, language = null;

                    // Trying to get email attribute using AX extension
                    if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
                        FetchResponse fetchResp = (FetchResponse) authSuccess
                                .getExtension(AxMessage.OPENID_NS_AX);

                        String emailAttr = fetchResp.getAttributeAlias("http://axschema.org/contact/email");
                        String firstAttr = fetchResp.getAttributeAlias("http://axschema.org/namePerson/first");
                        String lastAttrr = fetchResp.getAttributeAlias("http://axschema.org/namePerson/last");
                        String countryAttr = fetchResp.getAttributeAlias("http://axschema.org/contact/country/home");
                        
                        if(emailAttr != null) {
                            email = fetchResp.getAttributeValue(emailAttr);
                        }
                        if(firstAttr != null) {
                            firstname = fetchResp.getAttributeValue(firstAttr);
                        }
                        if(lastAttrr != null) {
                            lastname = fetchResp.getAttributeValue(lastAttrr);
                        }
                        if(countryAttr != null) {
                            country = fetchResp.getAttributeValue(countryAttr);
                        }

                        // Sending results to index.jsp
                        httpResponse.sendRedirect("out.jsp?openid=" + verifiedID + "&email= " + email
                                + "&firstname=" + firstname + "&lastname=" + lastname + "&country=" + country);

                    } else { // OP has not sent any attribute
                        httpResponse.sendRedirect("out.jsp?email=Error");
                    }

                } else { // somethig went wrong, redirecting back to home
                    httpResponse.sendRedirect("index.jsp");
                }

            } catch (MessageException e) {
                e.printStackTrace();
            } catch (DiscoveryException e) {
                e.printStackTrace();
            } catch (AssociationException e) {
                e.printStackTrace();
            }

        } else {

            String claimed_id = httpRequest.getParameter("claimed_id");

            if (claimed_id == null) { // if the user access the servlet directly

                httpResponse.sendRedirect("index.jsp");

            } else { // the index.jsp's form request, must place the authRequest

                if (openidUserPattern != null && !openidUserPattern.equals("")) {
                    claimed_id = openidUserPattern + httpRequest.getParameter("claimed_id");
                }

                try {
                    // Smart consumer manager
                    manager = new ConsumerManager();

                    // Discovery on the user supplied ID
                    List discoveries = manager.discover(claimed_id);

                    // Associate with the OP and share a secrete
                    DiscoveryInformation discovered = manager.associate(discoveries);

                    // Keeping necessary parameters to verify the AuthResponse
                    httpRequest.getSession().setAttribute("openid-disc", discovered);

                    // To identify OP's HTTP POST from other POSTs
                    return_to = httpRequest.getRequestURL().toString() + "?is_id_res=true";

                    AuthRequest authReq = manager.authenticate(discovered, return_to);

                    // Getting emaill attribute using FetchRequest
                    FetchRequest fetchRequest = FetchRequest.createFetchRequest();

                    // addAttribute(attributeName, typeURI, isRequired)
                    fetchRequest.addAttribute("email", "http://axschema.org/contact/email", true);
                    fetchRequest.addAttribute("firstname", "http://axschema.org/namePerson/first", true);
                    fetchRequest.addAttribute("lastname", "http://axschema.org/namePerson/last", true);
                    fetchRequest.addAttribute("country", "http://axschema.org/contact/country/home", true);
                    // Adding the AX extension to the AuthRequest message
                    authReq.addExtension(fetchRequest);

                    // Redirecting the browser to the OP
                    httpResponse.sendRedirect(authReq.getDestinationUrl(true));

                } catch (MessageException e) {
                    e.printStackTrace();
                } catch (ConsumerException e) {
                    e.printStackTrace();
                } catch (DiscoveryException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
