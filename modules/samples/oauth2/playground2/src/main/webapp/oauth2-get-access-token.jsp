<%@page import="org.apache.oltu.oauth2.client.response.OAuthClientResponse"%>
<%@page import="org.apache.oltu.oauth2.client.response.GitHubTokenResponse"%>
<%@page import="org.apache.oltu.oauth2.client.URLConnectionClient"%>
<%@page import="org.apache.oltu.oauth2.client.OAuthClient"%>
<%@page import="org.wso2.sample.identity.oauth2.OAuth2Constants"%>
<%@page import="org.apache.oltu.oauth2.common.message.types.GrantType"%>
<%@ page import="org.apache.oltu.oauth2.client.request.OAuthClientRequest" %>
<%@ page import="org.apache.oltu.oauth2.common.message.types.ResponseType" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    try {
    	   
    String consumerKey = (String) session.getAttribute(OAuth2Constants.CONSUMER_KEY);
    String consumerSecret =  request.getParameter(OAuth2Constants.CONSUMER_SECRET);

    String tokenEndpoint = request.getParameter(OAuth2Constants.OAUTH2_ACCESS_ENDPOINT);
    //String scope = request.getParameter(OAuth2Constants.SCOPE);
    //String callback = request.getScheme() +"://" + request.getServerName() + ":" + request.getServerPort() + "/playground/oauth2client";
    String code = (String) session.getAttribute(OAuth2Constants.CODE);
        String idToken = null;
   
    OAuthClientRequest accessRequest = OAuthClientRequest.tokenLocation(tokenEndpoint)
    .setGrantType(GrantType.AUTHORIZATION_CODE)
    .setClientId(consumerKey)
    .setClientSecret(consumerSecret)
    .setRedirectURI(request.getParameter("callbackurl"))
    .setCode(code)
    .buildBodyMessage();

    //create OAuth client that uses custom http client under the hood
    OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
    
    OAuthClientResponse oAuthResponse = oAuthClient.accessToken(accessRequest);
    String accessToken = oAuthResponse.getParam(OAuth2Constants.ACCESS_TOKEN);
    session.setAttribute(OAuth2Constants.ACCESS_TOKEN, accessToken);

     idToken = oAuthResponse.getParam("id_token");
        String idTokenHint =  (String)session.getAttribute("id_token");
        session.removeAttribute("id_token");
    if(idToken != null) {
        session.setAttribute("id_token", idToken);

    }
        if (idTokenHint != null) {
            session.setAttribute("id_token_hint", idTokenHint);
        }

    } catch (Exception e) {
%>
      	 <script type="text/javascript">
      	    window.location = "oauth2.jsp?reset=true&error=<%=e.getMessage()%>";
      	 </script>
<% 
    }        
%>

 <script type="text/javascript">
    window.location = "oauth2.jsp";
 </script>