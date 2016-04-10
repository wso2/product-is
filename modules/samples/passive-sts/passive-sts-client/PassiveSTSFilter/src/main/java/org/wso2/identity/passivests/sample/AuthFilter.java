package org.wso2.identity.passivests.sample;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.rahas.RahasConstants;
import org.apache.rahas.TrustException;
import org.apache.rahas.TrustUtil;
import org.apache.ws.secpolicy.Constants;

/**
 * Servlet Filter implementation class AuthFilter
 */
public class AuthFilter implements Filter {
    
    private String idpUrl;
    private String loginAction; //wa
    private String logoutAction; //wa
    private String replyUrl; //wreply
    private String realm; //wtrealm
    private String displayFullResponse;
    private String additionalRequestParams;
    
	public void destroy() {
	}
	
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		
	    HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
        String samlReqEmpty = "";
        String wreq = null;
        String samlVersion = request.getParameter("samlv");
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement element = factory.createOMElement(Constants.RST_TEMPLATE);

        if (samlVersion != null) {
            if (samlVersion.equalsIgnoreCase("1-1")) {
                try {
                    element = TrustUtil.createRequestSecurityTokenElement(RahasConstants.VERSION_05_12);
                    TrustUtil.createTokenTypeElement(RahasConstants.VERSION_05_12, element).setText(
                            RahasConstants.TOK_TYPE_SAML_10);

                } catch (TrustException e) {
                    e.printStackTrace();
                }
                wreq = URLEncoder.encode(element.toString(), "UTF-8");

            } else if (samlVersion.equalsIgnoreCase("2-0")) {
                try {
                    element = TrustUtil.createRequestSecurityTokenElement(RahasConstants.VERSION_05_12);
                    TrustUtil.createTokenTypeElement(RahasConstants.VERSION_05_12, element).setText(
                            RahasConstants.TOK_TYPE_SAML_20);

                } catch (TrustException e) {
                    e.printStackTrace();
                }
                wreq = URLEncoder.encode(element.toString(), "UTF-8");
            } else {
                wreq = samlReqEmpty;
            }
        }

        if(request.getParameter("wresult") == null || request.getParameter("wresult").isEmpty()){
          String redirectUrl =  idpUrl + "?wa=" + loginAction + "&wreply=" + replyUrl + "&wtrealm=" + realm
                  + ((wreq != null) ? "&wreq=" + wreq : "");
          response.sendRedirect(redirectUrl + "&" + additionalRequestParams);
          return;
        }
        
        String newLineRemovedStr = request.getParameter("wresult").replaceAll("(\\r|\\n)", "");
        handleResponse(request, newLineRemovedStr);
        
        if("true".equals(displayFullResponse)){
            String htmlSafeStr = escapeHtml(Utils.prettyFormat(newLineRemovedStr,2));
            request.getSession().setAttribute("RSTR", htmlSafeStr);
            request.getSession().setAttribute("displayFullResponse", "true");
        } else {
            request.getSession().setAttribute("displayFullResponse", "false");
        }
        
		chain.doFilter(request, response);
	}
	
	private void handleResponse(HttpServletRequest request, String response){
	    OMElement element = null;
        String username = null;
        Map<String, String> claimMap = new HashMap<String, String>();
        
        try {
            element = AXIOMUtil.stringToOM(response);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        
        element = element.getFirstChildWithName(
                new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512", "RequestSecurityTokenResponse"));
        OMElement tokenT = element
                .getFirstChildWithName(new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512", "TokenType"));
        element = element.getFirstChildWithName(
                new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512", "RequestedSecurityToken"));

        if ("http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV1.1".equalsIgnoreCase(tokenT
                .getText())) {
            element = element.getFirstChildWithName(new QName("urn:oasis:names:tc:SAML:1.0:assertion", "Assertion"));
            element = element.getFirstChildWithName(new QName("urn:oasis:names:tc:SAML:1.0:assertion",
                    "AttributeStatement"));

            if (element != null) {
                OMElement subjectElement = element.getFirstChildWithName(new QName(
                        "urn:oasis:names:tc:SAML:1.0:assertion", "Subject"));

                username = subjectElement.getFirstElement().getText();
                Iterator itr = element.getChildrenWithName(new QName("Attribute"));

                while (itr.hasNext()) {
                    OMElement elem = (OMElement) itr.next();
                    String claimURI = ((OMAttribute) elem.getAttribute(new QName("AttributeNamespace")))
                            .getAttributeValue();
                    claimMap.put(claimURI, elem.getFirstElement().getText());
                }

                request.getSession().setAttribute("message", "Response from the Passive STS for User: " + username);
                request.getSession().setAttribute("claimMap", claimMap);
            } else {
                request.getSession().setAttribute("message",
                        "No claims received! Verify RP is registered at Passive STS");
                request.getSession().setAttribute("claimMap", null);
            }

        } else if ("http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0".equalsIgnoreCase(tokenT
                .getText())) {
            element = element.getFirstChildWithName(new QName("urn:oasis:names:tc:SAML:2.0:assertion", "Assertion"));
            OMElement subject = element
                    .getFirstChildWithName(new QName("urn:oasis:names:tc:SAML:2.0:assertion", "Subject"));
            subject = subject.getFirstChildWithName(new QName("urn:oasis:names:tc:SAML:2.0:assertion", "NameID"));
            username = subject.getText();
            element = element
                    .getFirstChildWithName(new QName("urn:oasis:names:tc:SAML:2.0:assertion", "AttributeStatement"));
            if (element != null) {
                Iterator itr = element.getChildrenWithName(new QName("urn:oasis:names:tc:SAML:2.0:assertion",
                        "Attribute"));

                while (itr.hasNext()) {
                    OMElement elem = (OMElement) itr.next();
                    String claimURI = ((OMAttribute) elem.getAttribute(new QName("Name"))).getAttributeValue();
                    claimMap.put(claimURI, elem.getFirstElement().getText());
                }
                request.getSession().setAttribute("message", "Response from the Passive STS for User: " + username);
                request.getSession().setAttribute("claimMap", claimMap);
            } else {
                request.getSession().setAttribute("message",
                        "No claims received! Verify RP is registered at Passive STS");
                request.getSession().setAttribute("claimMap", null);
            }
        }

        String logoutUrl =  idpUrl + "?wa=" + logoutAction + "&wreply=" + replyUrl + "&wtrealm=" + realm;
        request.getSession().setAttribute("logouturl", logoutUrl + "&" + additionalRequestParams);
	}

	public void init(FilterConfig fConfig) throws ServletException {
		//Initialize the configurations
	    idpUrl = fConfig.getInitParameter("idpUrl");
	    loginAction = fConfig.getInitParameter("loginaction");
        logoutAction = fConfig.getInitParameter("logoutaction");
	    replyUrl = fConfig.getInitParameter("replyUrl");
	    realm = fConfig.getInitParameter("realm");
	    displayFullResponse = fConfig.getInitParameter("displayFullResponse");
	    additionalRequestParams = fConfig.getInitParameter("requestParams");
	}
}
