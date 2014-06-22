<%@page import="org.wso2.sample.identity.oauth2.OAuth2Constants"%>
<%@page import="org.apache.amber.oauth2.client.URLConnectionClient"%>
<%@page import="org.apache.amber.oauth2.client.response.OAuthClientResponse"%>
<%@page import="org.apache.amber.oauth2.client.OAuthClient"%>
<%@page import="org.apache.amber.oauth2.common.message.types.GrantType"%>
<%@ page import="org.apache.amber.oauth2.client.request.OAuthClientRequest" %>
<%@ page import="org.apache.amber.oauth2.common.message.types.ResponseType" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%

    try {

        String consumerKey = request.getParameter(OAuth2Constants.CONSUMER_KEY);
        String authzEndpoint = request.getParameter(OAuth2Constants.OAUTH2_AUTHZ_ENDPOINT);
        String accessEndpoint = request.getParameter(OAuth2Constants.OAUTH2_ACCESS_ENDPOINT);
        String consumerSecret = request.getParameter(OAuth2Constants.CONSUMER_SECRET);

        String recowner = request.getParameter("recowner");
        String recpassword = request.getParameter("recpassword");


        String authzGrantType = request.getParameter(OAuth2Constants.OAUTH2_GRANT_TYPE);
        String scope = request.getParameter(OAuth2Constants.SCOPE);
        session.setAttribute("callbackurl", request.getParameter("callbackurl"));

        // By default IS does not validate scope. To validate we need to write a callback handler.
        if (scope == null || scope.trim().length() == 0){
            scope = "default";
        }
        //String callback = request.getScheme() +"://" + request.getServerName() + ":" + request.getServerPort() + "/playground/oauth2client";

        session.setAttribute(OAuth2Constants.OAUTH2_GRANT_TYPE, authzGrantType);
        session.setAttribute(OAuth2Constants.CONSUMER_KEY, consumerKey);
        session.setAttribute(OAuth2Constants.CONSUMER_SECRET, consumerSecret);

        if (authzGrantType.equals(OAuth2Constants.OAUTH2_GRANT_TYPE_CODE) || authzGrantType.equals(OAuth2Constants.OAUTH2_GRANT_TYPE_IMPLICIT)) {
            // If the grant type is authorization code or implicit - then we need to send a request to the Authorization end point.

            if (consumerKey==null || consumerKey.trim().length()==0 ||
                    session.getAttribute("callbackurl")==null || ((String)session.getAttribute("callbackurl")).trim().length()==0 ||
                    authzEndpoint==null || authzEndpoint.trim().length()==0) {
%>

<script type="text/javascript">
    window.location = "oauth2.jsp?reset=true";
</script>

<%
        return;
    }

    OAuthClientRequest authzRequest = OAuthClientRequest
            .authorizationLocation(authzEndpoint)
            .setClientId(consumerKey)
            .setRedirectURI((String)session.getAttribute("callbackurl"))
            .setResponseType(authzGrantType)
            .setScope(scope)
            .buildQueryMessage();
    response.sendRedirect(authzRequest.getLocationUri());
    return;

} else {

    // For any other grant type we need to send the request to the Access Token end point.
    OAuthClientRequest accessRequest = null;

    if (recowner == null || recpassword == null || recowner.trim().length() == 0 ||
            recpassword.trim().length() == 0 ) {
        if (consumerKey == null || consumerKey.trim().length() == 0 ||
                consumerSecret == null || consumerSecret.trim().length() == 0 ||
                accessEndpoint == null || accessEndpoint.trim().length() == 0) {
%>

<script type="text/javascript">
    window.location = "oauth2.jsp?reset=true";
</script>

<%
    }
    accessRequest = OAuthClientRequest.tokenLocation(accessEndpoint)
            .setGrantType(GrantType.CLIENT_CREDENTIALS)
            .setClientId(consumerKey)
            .setClientSecret(consumerSecret)
            .setScope(scope)
            .buildBodyMessage();

} else {

    if (consumerKey == null || consumerKey.trim().length() == 0 ||
            consumerSecret == null || consumerSecret.trim().length() == 0 ||
            recowner == null || recowner.trim().length()==0 ||
            recpassword == null || recpassword.trim().length() == 0 ||
            accessEndpoint == null || accessEndpoint.trim().length() == 0) {
%>

<script type="text/javascript">
    window.location = "oauth2.jsp?reset=true";
</script>

<%
            }
            accessRequest = OAuthClientRequest.tokenLocation(accessEndpoint)
                    .setGrantType(GrantType.PASSWORD)
                    .setClientId(consumerKey)
                    .setClientSecret(consumerSecret)
                    .setScope(scope)
                    .setUsername(recowner)
                    .setPassword(recpassword)
                    .buildBodyMessage();
        }

        // Creates OAuth client that uses custom http client under the hood
        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        OAuthClientResponse oAuthResponse = oAuthClient.accessToken(accessRequest);
        String accessToken = oAuthResponse.getParam(OAuth2Constants.ACCESS_TOKEN);

        // For future use we store the access_token in session.
        session.setAttribute(OAuth2Constants.ACCESS_TOKEN,accessToken);
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


    